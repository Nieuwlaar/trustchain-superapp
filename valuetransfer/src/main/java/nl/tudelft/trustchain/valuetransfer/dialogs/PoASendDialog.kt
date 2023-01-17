package nl.tudelft.trustchain.valuetransfer.dialogs

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mattskala.itemadapter.Item
import com.mattskala.itemadapter.ItemAdapter
import kotlinx.android.synthetic.main.dialog_send_poa.view.*
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.ui.identity.PoaItem
import nl.tudelft.trustchain.valuetransfer.ui.identity.PoaItemRenderer
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

class PoASendDialog(
    private val publicKey: String,
    private val allYourPoas: List<PowerOfAttorney>
) : VTDialogFragment() {
    private val TAG = "PoaCommunity"

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val adapterYourPoasWithDelegation = ItemAdapter()
        val view = layoutInflater.inflate(R.layout.dialog_send_poa, null)
        Log.i(TAG, allYourPoas.toString())
//        val recyclerView = view.findViewById<RecyclerView>(R.id.rvYourPoasWithDelegation)
//        val community = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
//        community.sendPoa(publicKey)

        view.rvYourPoasWithDelegation.apply {
            adapter = adapterYourPoasWithDelegation
            layoutManager = LinearLayoutManager(context)
        }

        adapterYourPoasWithDelegation.registerRenderer(
            PoaItemRenderer {  poa ->
                PoADetailsDialog(
                    true,
                    poa
                )
                    .show(parentFragmentManager, tag)
            }
        )
        adapterYourPoasWithDelegation.updateItems(createPoaItems(allYourPoas))

        Log.i(TAG, createPoaItems(allYourPoas).toString())

        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
//            val view = layoutInflater.inflate(R.layout.dialog_send_poa, null)

            // Fix keyboard exposing over content of dialog
            bottomSheetDialog.behavior.apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }

            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)



            view.findViewById<TextView>(R.id.tvButtonCancel).setOnClickListener{
                dialog?.dismiss()
            }


            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }
    fun createPoaItems(poas: List<PowerOfAttorney>): List<Item> {
        return poas.map { poa ->
            PoaItem(poa)
        }
    }
}
