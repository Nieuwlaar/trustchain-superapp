package nl.tudelft.trustchain.valuetransfer.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mattskala.itemadapter.Item
import com.mattskala.itemadapter.ItemAdapter
import kotlinx.android.synthetic.main.dialog_send_poa.view.*
import nl.tudelft.ipv8.IPv4Address
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.community.PowerofAttorneyCommunity
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.ui.identity.PoaItem
import nl.tudelft.trustchain.valuetransfer.ui.identity.PoaItemRenderer
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

class PoASendDialog(
    private val publicKey: String,
    private val allYourPoasWithDelegation: List<PowerOfAttorney>
) : VTDialogFragment() {
    private val TAG = "PoaCommunity"

    @SuppressLint("RestrictedApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val community = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!



        val adapterYourPoasWithDelegation = ItemAdapter()

        val view = layoutInflater.inflate(R.layout.dialog_send_poa, null)
        Log.i(TAG, allYourPoasWithDelegation.toString())
        val tvNoDelegatablePoas = view.findViewById<TextView>(R.id.tvNoDelegatablePoas)
        val tvPublicKey = view.findViewById<TextView>(R.id.tvPublicKey)
        val llConnectedIcon = view.findViewById<RelativeLayout>(R.id.llConnectedIcon)
        val llNotConnectedIcon = view.findViewById<RelativeLayout>(R.id.llNotConnectedIcon)
        tvPublicKey.text = publicKey
        if (community.getPeerIp(publicKey) != IPv4Address("0.0.0.0", 0)){
            llConnectedIcon.visibility = View.VISIBLE
            llNotConnectedIcon.visibility = View.GONE
        }



        Log.i(TAG, createPoaItems(allYourPoasWithDelegation).toString())
        tvNoDelegatablePoas.visibility = View.GONE
        if (createPoaItems(allYourPoasWithDelegation).isNullOrEmpty()){
            tvNoDelegatablePoas.visibility = View.VISIBLE
        }


        view.rvYourPoasWithDelegation.apply {
            adapter = adapterYourPoasWithDelegation
            layoutManager = LinearLayoutManager(context)
        }
        adapterYourPoasWithDelegation.updateItems(createPoaItems(allYourPoasWithDelegation))

        adapterYourPoasWithDelegation.registerRenderer(
            PoaItemRenderer { poa ->
                val poa_list: MutableList<PowerOfAttorney> = mutableListOf()
                poa_list += poa
                adapterYourPoasWithDelegation.updateItems(createPoaItems(poa_list))
            }
        )



        Log.i(TAG, createPoaItems(allYourPoasWithDelegation).toString())

        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
//            val view = layoutInflater.inflate(R.layout.dialog_send_poa, null)
            val poaType = view.findViewById<TextView>(R.id.poaType)
            poaType.setOnClickListener {
                Log.i(TAG, "PoaType clicked")
                OptionsDialog(
                    R.menu.poa_verify_type,
                    resources.getString(R.string.dialog_choose_poa_type),
                    menuMods = { menu ->
                        menu.apply {
                            findItem(R.id.actionCreateQuotations).isVisible = true
                            findItem(R.id.actionPurchaseWholesale).isVisible = true
                            findItem(R.id.actionSignUpToX).isVisible = true
                            findItem(R.id.actionCustom).isVisible = true
                        }
                    },
                    optionSelected = { _, item ->
                        when (item.itemId) {
                            R.id.actionCreateQuotations -> {
                                poaType.text = getString(R.string.poa_type_create_quotations)
                                Log.i(TAG, poaType.text.toString())
                            }
                            R.id.actionPurchaseWholesale -> {
                                poaType.text = getString(R.string.poa_type_purchase_wholesale)
                            }
                            R.id.actionSignUpToX -> {
                                poaType.text = getString(R.string.poa_type_sign_up_to_x)
                            }
                            R.id.actionCustom -> {
                                poaType.text = "Custom"
                            }
                        }
                    }
                ).show(parentFragmentManager, tag)
            }



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
