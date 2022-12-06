package nl.tudelft.trustchain.valuetransfer.ui.identity

import android.util.Log
import android.view.View
import com.mattskala.itemadapter.ItemLayoutRenderer
import kotlinx.android.synthetic.main.item_identity_poa.view.*
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney

class PoaItemRenderer(
    private val onOptionsClick: (PowerOfAttorney) -> Unit,
) : ItemLayoutRenderer<PoaItem, View>(
    PoaItem::class.java
) {
    val TAG = "PoaCommunity"
    override fun bindView(item: PoaItem, view: View) = with(view) {
        tv_poa_company.text = item.poa.companyName
        tv_poa_type.text = item.poa.poaType
        iv_companyImage.setImageResource(R.drawable.id_card)

        poa_cardView.setOnClickListener {
            Log.i(TAG, "Clicked item: "+item.poa.id)
//            TODO: Implement the PoA card dialog
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.item_identity_poa
    }
}
