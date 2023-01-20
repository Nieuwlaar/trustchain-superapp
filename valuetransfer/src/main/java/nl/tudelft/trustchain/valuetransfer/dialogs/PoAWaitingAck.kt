package nl.tudelft.trustchain.valuetransfer.dialogs

import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

class PoAWaitingAck(
    private val title: String?,
    private val subtitle: String?,
    private val data: String
) : VTDialogFragment() {
    private val TAG = "PoaCommunity"

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_qrcode, null)

            // Fix keyboard exposing over content of dialog
            bottomSheetDialog.behavior.apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }

            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)

            view.findViewById<TextView>(R.id.tvTitle).apply {
                isVisible = title != null
                text = title
            }
            view.findViewById<TextView>(R.id.tvSubTitle).apply {
                isVisible = subtitle != null
                text = subtitle
            }

            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            // when closing dialog: EventBus.getDefault().unregister(this)

            @Suppress("DEPRECATION")
            Handler().postDelayed(
                {
                    view.findViewById<ProgressBar>(R.id.pbLoadingSpinner).isVisible = false
                },
                10000
            )

            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }
}
