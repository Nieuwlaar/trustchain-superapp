package nl.tudelft.trustchain.valuetransfer.community

import android.util.Log
import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.IPv4Address
import nl.tudelft.ipv8.messaging.Packet
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.valuetransfer.ui.mandate.MyMessage

class MandateCommunity : Community() {
    override val serviceId = "02313685c1912a141279f8248fc8d65899c5df52"
    private val MESSAGE_ID = 1
    private val POA_MESSAGE_ID = 2
    private val CONFIRM_POA_MESSAGE_ID = 3
    private val REVOCATION_MESSAGE_ID = 4

    init {
        messageHandlers[MESSAGE_ID] = ::onMessage
    }

    private fun onMessage(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(MyMessage.Deserializer)
        Log.i("MandateCommunity", peer.mid + ": " + payload.message)
    }

    //TODO: Create QR scanning function to obtain publickey
    private fun getPublicKeyFromQR(): String {
        return "4c69624e61434c504b3a0bc295507498057504d7f96cac24cba48372a583d324aeb896485c2a59efbe1db3056345ae72185bab19755f688deabadb964dbaf487e64d15ff13ca43f1c634"
    }

    /*
    Obtain IP from the peer that is scanned from QR (peer has to be in community)
     */
    private fun getPeerIp(publicKey : String): IPv4Address {
        var address = IPv4Address("0.0.0.0", 0)
        for (peer in getPeers()) {
            Log.i("MandateCommunity", "Peer public keys: "+peer.publicKey.keyToBin().toHex())
            if (publicKey == peer.publicKey.keyToBin().toHex()){
                Log.i("MandateCommunity", "This is the IP address you need: "+peer.address.toString())
                address = peer.address
            }
        }
        if (address == IPv4Address("0.0.0.0", 0)){
            Log.e("MandateCommunity", "Peer Address = "+address.toString()+". Is the peer in the community?")
        }
        return address
    }

    /*
    Function to initialize a Proof of Attorney (PoA)
    Peers in community are scanned and matched to the scanned QR public key to receive the IP and send the PoA message.
    */
    fun sendPoa(){
        val packet = serializePacket(MESSAGE_ID, MyMessage("Hello, it works!!"))
        val publicKey = getPublicKeyFromQR()
        val address = getPeerIp(publicKey)
        send(address, packet)
    }

    fun broadcastGreeting() {
        for (peer in getPeers()) {
            val packet = serializePacket(MESSAGE_ID, MyMessage("Hello!"+peer.address.toString()))
            send(peer.address, packet)
        }
    }
}

