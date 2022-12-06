package nl.tudelft.trustchain.valuetransfer.ui.identity

import com.mattskala.itemadapter.Item
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney

data class PoaItem(
    val poa: PowerOfAttorney
) : Item() {
    override fun areItemsTheSame(other: Item): Boolean {
        return other is PoaItem && poa.id == other.poa.id
    }
}
