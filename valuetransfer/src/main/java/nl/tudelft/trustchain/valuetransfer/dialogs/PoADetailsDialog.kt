package nl.tudelft.trustchain.valuetransfer.dialogs

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.createBitmap
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

class PoADetailsDialog(
    private val your_poa: Boolean,
    private val poa: PowerOfAttorney
) : VTDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_detail_poa, null)

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
                text = "********"
            }
            view.findViewById<TextView>(R.id.tvPoaType).apply {
                isVisible = true
                text = "********"
            }
            view.findViewById<TextView>(R.id.tvPoaRelated).apply {
                text = "********"
            }


            view.findViewById<RelativeLayout>(R.id.rlDeletePoa).setOnClickListener{
                dialog?.dismiss()
                PoADeleteDialog(
                    true,
                    poa
                )
                    .show(parentFragmentManager, tag)
            }

            val ivShowDetails: ImageView = view.findViewById(R.id.ivShowDetails)
            val ivHideDetails: ImageView = view.findViewById(R.id.ivHideDetails)

            ivShowDetails.setOnClickListener {
                ivShowDetails.visibility = View.GONE
                ivHideDetails.visibility = View.VISIBLE
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
            }

            ivHideDetails.setOnClickListener {
                ivShowDetails.visibility = View.VISIBLE
                ivHideDetails.visibility = View.GONE
                view.findViewById<TextView>(R.id.tvKvkNumber).apply {
                    text = "********"
                }
                view.findViewById<TextView>(R.id.tvPoaType).apply {
                    text = "********"
                }
                view.findViewById<TextView>(R.id.tvPoaRelated).apply {
                    text = "********"
                }
            }


            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            @Suppress("DEPRECATION")
            Handler().postDelayed(
                {
                    view.findViewById<ProgressBar>(R.id.pbLoadingSpinner).isVisible = false
                    view.findViewById<ImageView>(R.id.ivQRCode).setImageBitmap(
                        createBitmap(
                            requireContext(),
                            poa.toString(),
                            R.color.black,
                            R.color.light_gray
                        )
                    )
                },
                100
            )

            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }
}
