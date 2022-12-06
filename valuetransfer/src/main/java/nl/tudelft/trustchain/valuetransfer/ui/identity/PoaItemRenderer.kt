package nl.tudelft.trustchain.valuetransfer.ui.identity

import android.view.View
import com.mattskala.itemadapter.ItemLayoutRenderer
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney

class PoaItemRenderer(
    private val onOptionsClick: (PowerOfAttorney) -> Unit,
) : ItemLayoutRenderer<PoaItem, View>(
    PoaItem::class.java
) {
    override fun bindView(item: PoaItem, view: View) = with(view) {
//        tvAttributeName.text = "TEXT"
//        tv_poa_type.text = "KVK NUMBER"
//        iv_companyImage.setOnClickListener {
//            onOptionsClick(item.poa)
//        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.item_identity_poa
    }
}
