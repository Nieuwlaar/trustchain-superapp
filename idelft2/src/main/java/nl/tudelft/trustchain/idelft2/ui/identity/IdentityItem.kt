package nl.tudelft.trustchain.idelft2.ui.identity

import android.graphics.Bitmap
import com.mattskala.itemadapter.Item
import nl.tudelft.trustchain.idelft2.entity.Identity

data class IdentityItem(
    val identity: Identity,
    val image: Bitmap?,
    val connected: Boolean
) : Item() {
    override fun areItemsTheSame(other: Item): Boolean {
        return other is IdentityItem && identity.id == other.identity.id
    }
}
