package nl.tudelft.trustchain.valuetransfer.ui.identity

import android.util.Log
import android.view.View
import com.mattskala.itemadapter.ItemLayoutRenderer
import kotlinx.android.synthetic.main.item_identity_poa.view.*
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney

class PoaItemRenderer(
    private val onPoaItemClick: (PowerOfAttorney) -> Unit,
) : ItemLayoutRenderer<PoaItem, View>(
    PoaItem::class.java
) {
    val TAG = "PoaCommunity"
    override fun bindView(item: PoaItem, view: View) = with(view) {
        tv_poa_company.text = item.poa.companyName
        tv_poa_type.text = item.poa.poaType
        when (item.poa.companyName) {
            "Xaigis B.V. (FAKE)" -> iv_companyImage.setImageResource(R.drawable.xaigis)
            "Nieuwlaar Design" -> iv_companyImage.setImageResource(R.drawable.nieuwlaar_design_logo)
            else -> {
                iv_companyImage.setImageResource(R.drawable.img_404)
            }
        }


        poa_cardView.setOnClickListener {
            Log.i(TAG, "Clicked item: "+item.poa.id)
            onPoaItemClick(item.poa)
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.item_identity_poa
    }
}
