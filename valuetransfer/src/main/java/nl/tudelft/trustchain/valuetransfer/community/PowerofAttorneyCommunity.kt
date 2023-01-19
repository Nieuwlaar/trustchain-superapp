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

class PowerofAttorneyCommunity(private val store: PoaStore) : Community() {
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
        val packet = serializePacket(MESSAGE_ID, MyMessage("|||$poaType|||"+poa.toString()))
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

    fun addPoa(poa: PowerOfAttorney) {
        store.addPoa(poa)
    }

    fun addFakePoa(poa: PowerOfAttorney) {
        store.addPoa(poa)
    }

    class Factory(
        private val store: PoaStore
    ) : Overlay.Factory<PowerofAttorneyCommunity>(PowerofAttorneyCommunity::class.java) {
        override fun create(): PowerofAttorneyCommunity {
            return PowerofAttorneyCommunity(store)
        }
    }
}

