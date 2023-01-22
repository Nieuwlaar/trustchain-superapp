package nl.tudelft.trustchain.valuetransfer.dialogs

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.community.PowerofAttorneyCommunity
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

class PoADeleteDialog(
    private val your_poa: Boolean,
    private val poa: PowerOfAttorney
) : VTDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_delete_poa, null)

            // Fix keyboard exposing over content of dialog
            bottomSheetDialog.behavior.apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }

            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)


            view.findViewById<TextView>(R.id.tvCompanyName).apply {
                isVisible = true
                text = poa.companyName
            }
            view.findViewById<TextView>(R.id.tvKvkNumber).apply {
                isVisible = true
                text = poa.kvkNumber.toString()
            }
            view.findViewById<TextView>(R.id.tvPoaType).apply {
                isVisible = true
                text = poa.poaType
            }

            if (!your_poa) {
                view.findViewById<TextView>(R.id.tvPoaRelatedName).apply {
                    text = "Issued to:"
                }
                view.findViewById<TextView>(R.id.tvPoaRelated).apply {
                    isVisible = true
                    text = poa.givenNamesPoaHolder+" "+poa.surnamePoaHolder
                }
            } else {
                view.findViewById<TextView>(R.id.tvPoaRelated).apply {
                    isVisible = true
                    text = poa.givenNamesPoaIssuer+" "+poa.surnamePoaIssuer
                }
            }

            view.findViewById<TextView>(R.id.tvButtonYes).setOnClickListener{
                val community = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
                community.addRevokedPoa(poa.id)
                community.deletePoa(poa)
                dialog?.dismiss()
            }
            view.findViewById<TextView>(R.id.tvButtonNo).setOnClickListener{
                dialog?.dismiss()
            }


            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }
}
