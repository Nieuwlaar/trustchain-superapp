package nl.tudelft.trustchain.valuetransfer.community

import android.util.Log
import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.messaging.Packet
import nl.tudelft.trustchain.valuetransfer.ui.mandate.MyMessage

class MandateCommunity : Community() {
    override val serviceId = "02313685c1912a141279f8248fc8d65899c5df52"
    private val MESSAGE_ID = 1

    init {
        messageHandlers[MESSAGE_ID] = ::onMessage
    }

    private fun onMessage(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(MyMessage.Deserializer)
        Log.i("MandateCommunity", peer.mid + ": " + payload.message)
    }

    fun broadcastGreeting() {
        for (peer in getPeers()) {
            val packet = serializePacket(MESSAGE_ID, MyMessage("Hello!"))
            send(peer.address, packet)
        }
    }
}

