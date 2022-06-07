package nl.tudelft.trustchain.idelft2.ui.identity

import com.mattskala.itemadapter.Item
import nl.tudelft.trustchain.common.idelft2.entity.IdentityAttribute

data class IdentityAttributeItem(
    val attribute: IdentityAttribute
) : Item() {
    override fun areItemsTheSame(other: Item): Boolean {
        return other is IdentityAttributeItem && attribute.id == other.attribute.id
    }
}
