package nl.tudelft.trustchain.valuetransfer.dialogs

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

class PoAAcceptedDeniedDialog(
    private val poaAccepted: Boolean
) : VTDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_poa_accepted_denied, null)

            val llVerifiedIcon = view.findViewById<LinearLayout>(R.id.llVerifiedIcon)
            val llNotVerifiedIcon = view.findViewById<LinearLayout>(R.id.llNotVerifiedIcon)
            val tvPoaVerification = view.findViewById<TextView>(R.id.tvPoaVerification)
            val tvButtonOk = view.findViewById<TextView>(R.id.tvButtonOk)

            // Fix keyboard exposing over content of dialog
            bottomSheetDialog.behavior.apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }

            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)



            if (poaAccepted){
                llVerifiedIcon.visibility = View.VISIBLE
                llNotVerifiedIcon.visibility = View.INVISIBLE
                tvPoaVerification.text = "Person accepted your issued Power of Attorney!"
            }

            tvButtonOk.setOnClickListener {
                dialog?.dismiss()
            }


            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }
}
