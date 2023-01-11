package nl.tudelft.trustchain.valuetransfer.dialogs

import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.community.PowerofAttorneyCommunity
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.createBitmap
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

class PoADetailsDialog(
    private val your_poa: Boolean,
//    private val company_name: String?,
//    private val kvk_number: String?,
//    private val poa_type: String?,
//    private val poa_related_name: String?,
//    private val data: String,
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


            view.findViewById<RelativeLayout>(R.id.rlDeletePoa).setOnClickListener{
                val community = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
                community.deletePoa(poa)
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
