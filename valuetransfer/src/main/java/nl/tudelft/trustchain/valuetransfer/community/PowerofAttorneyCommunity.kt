package nl.tudelft.trustchain.valuetransfer.community

import android.util.Log
import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.IPv4Address
import nl.tudelft.ipv8.Overlay
import nl.tudelft.ipv8.messaging.Packet
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.valuetransfer.db.PoaStore
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.QRScanController
import nl.tudelft.trustchain.valuetransfer.ui.powerofattorney.MyMessage
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class PowerofAttorneyCommunity(private val store: PoaStore) : Community() {
    class ShowPoAAddReceivedDialogEvent(val issuedPoaType: String, val poa: PowerOfAttorney) : EventBus()

    override val serviceId = "02313685c1912a141279f8248fc8d65899c5df52"
    private val TAG = "PoaCommunity"
    private val MESSAGE_ID = 1
    private val ISSUE_POA_MESSAGE_ID = 2
    private val CONFIRM_POA_MESSAGE_ID = 3
    private val REVOCATION_MESSAGE_ID = 4


    init {
        messageHandlers[MESSAGE_ID] = ::onMessage
        messageHandlers[ISSUE_POA_MESSAGE_ID] = ::onIssuePoaMessage
    }

    private fun onMessage(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(MyMessage.Deserializer)
        Log.i(TAG, peer.mid + ": " + payload.message)
    }

    private fun onIssuePoaMessage(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(MyMessage.Deserializer)
        Log.i(TAG, peer.mid + ": " + payload.message)
        val result = splitMessageString(payload.message)
        Log.i(TAG, "PoA type :" + result.first)
        Log.i(TAG, "PoA in string :" +result.second)
        EventBus.getDefault().post(ShowPoAAddReceivedDialogEvent(result.first, convertStringPoa2Poa(result.second)))
    }

    private val qrScanController = QRScanController()

    //TODO: Create QR scanning function to obtain publickey
    private fun getPublicKeyFromQR(): String {

//        qrScanController.initiateScan()
        return "4c69624e61434c504b3a0bc295507498057504d7f96cac24cba48372a583d324aeb896485c2a59efbe1db3056345ae72185bab19755f688deabadb964dbaf487e64d15ff13ca43f1c634"
    }


    /*
    Obtain IP from the peer that is scanned from QR (peer has to be in community)
     */
    fun getPeerIp(publicKey : String): IPv4Address {
        var address = IPv4Address("0.0.0.0", 0)
        for (peer in getPeers()) {
            Log.i("PoaCommunity", "Peer public keys: "+peer.publicKey.keyToBin().toHex())
            if (publicKey == peer.publicKey.keyToBin().toHex()){
                Log.i("PoaCommunity", "This is the IP address you need: "+peer.address.toString())
                address = peer.address
            }
        }
        if (address == IPv4Address("0.0.0.0", 0)){
            Log.e("PoaCommunity", "Peer Address = "+address.toString()+". Is the peer in the community?")
        }
        return address
    }

    /**
    Function to initialize a Power of Attorney (PoA)
    Peers in community are scanned and matched to the scanned QR public key to receive the IP and send the PoA message.
    **/
    fun sendPoa(publicKey: String, poa: PowerOfAttorney, poaType: String){
        val packet = serializePacket(ISSUE_POA_MESSAGE_ID, MyMessage("|||$poaType|||"+poa.toString()))
        val address = getPeerIp(publicKey)
        send(address, packet)
    }

    fun broadcastGreeting() {
        for (peer in getPeers()) {
            val packet = serializePacket(MESSAGE_ID, MyMessage("Hello!"+peer.address.toString()))
            send(peer.address, packet)
        }
    }

    fun deletePoa(poa: PowerOfAttorney) {
        store.deletePoa(poa.id)
    }

    fun deleteAllPoas() {
        store.deleteAllPoas()
    }

    fun createPoasTable() {
        store.createPoasTable()
    }

    fun deletePoasTable() {
        store.deletePoasTable()
    }



    //TODO: implement
//    fun isValidPoaValues(identity: Identity, poa: PowerOfAttorney): Boolean{
//        return true
//    }

    private fun convertStringPoa2Poa(input_poa_string: String) : PowerOfAttorney{
        //Removing the class name and paranthesis from string
        val jsonString = input_poa_string.replace("PowerOfAttorney(", "").replace(")", "")

        // Splitting the string by comma to get key value pair
        val keyValuePairs: List<String> = jsonString.split(",")
        // Creating JSON object to add key value pair
        val json = JSONObject()
        for (keyValuePair in keyValuePairs) {
            val keyValue = keyValuePair.trim { it <= ' ' }.split("=").toTypedArray()
            json.put(keyValue[0], keyValue[1])
        }
        val poa = PowerOfAttorney(
            json.getString("id"),
            json.getLong("kvkNumber"),
            json.getString("companyName"),
            json.getString("poaType"),
            json.getString("isPermitted"),
            json.getString("isAllowedToIssuePoa"),
            json.getString("publicKeyPoaHolder"),
            json.getString("givenNamesPoaHolder"),
            json.getString("surnamePoaHolder"),
            json.getString("dateOfBirthPoaHolder"),
            json.getString("publicKeyPoaIssuer"),
            json.getString("givenNamesPoaIssuer"),
            json.getString("surnamePoaIssuer"),
            json.getString("dateOfBirthPoaIssuer")
        )
        return poa
    }

    fun addPoa(poa: PowerOfAttorney) {
        store.addPoa(poa)
    }

    fun addFakePoa(poa: PowerOfAttorney) {
        store.addPoa(poa)
    }

    fun splitMessageString(str: String): Pair<String, String> {
        val parts = str.split("|||")
        return Pair(parts[1], parts[2])
    }

    class Factory(
        private val store: PoaStore
    ) : Overlay.Factory<PowerofAttorneyCommunity>(PowerofAttorneyCommunity::class.java) {
        override fun create(): PowerofAttorneyCommunity {
            return PowerofAttorneyCommunity(store)
        }
    }

    interface OnPoaIssuedCallback {
        fun onPoaIssued(poa: PowerOfAttorney)
    }
}

