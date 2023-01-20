package nl.tudelft.trustchain.valuetransfer.dialogs

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.community.PowerofAttorneyCommunity
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.ui.identity.IdentityFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor
import java.util.*

class PoAAddReceivedDialog(
    private val yourPoa: Boolean,
    private val issuedPoaType: String,
    private val poa: PowerOfAttorney,
    private val myPublicKey : String
) : VTDialogFragment() {
    val TAG = "PoaCommunity"
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {

        Log.i(TAG, "PoAAddReceivedDialog reached")

        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_poa_add_received, null)

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
                text = issuedPoaType
            }

            if (!yourPoa) {
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
                val identity = getIdentityCommunity().getIdentity()!!
                val surnameFromIdentity = identity.content.surname
                val givenNamesFromIdentity = identity.content.givenNames
                val finalPoa = PowerOfAttorney(
                    id = UUID.randomUUID().toString(),
                    kvkNumber = poa.kvkNumber,
                    companyName = poa.companyName,
                    poaType = issuedPoaType,
                    isPermitted = "YES",
                    isAllowedToIssuePoa = "NO",
                    publicKeyPoaHolder = myPublicKey,
                    givenNamesPoaHolder = givenNamesFromIdentity,
                    surnamePoaHolder = surnameFromIdentity,
                    dateOfBirthPoaHolder = IdentityFragment.getDateOfBirth(identity),
                    publicKeyPoaIssuer = poa.publicKeyPoaHolder,
                    givenNamesPoaIssuer = poa.givenNamesPoaHolder,
                    surnamePoaIssuer = poa.surnamePoaHolder,
                    dateOfBirthPoaIssuer = poa.dateOfBirthPoaHolder
                )
                Log.i(TAG, "My public key:" +myPublicKey)
//                TODO: Add UUID to recovation list.
                val poaCommunity = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
                poaCommunity.addPoa(finalPoa)
                poaCommunity.sendPoaAck(true, poa.publicKeyPoaHolder, poa, finalPoa)
//                closing dialogs
                val fragmentManager = requireActivity().supportFragmentManager
                val dialogs = fragmentManager.fragments
                    .filterIsInstance<DialogFragment>()
                    .filter { it != this }

                for (dialog in dialogs) {
                    dialog.dismiss()
                }
                dialog?.dismiss()
            }
            view.findViewById<TextView>(R.id.tvButtonNo).setOnClickListener{
                val poaCommunity = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
                poaCommunity.sendPoaAck(false, poa.publicKeyPoaHolder, poa, poa)
//                closing dialogs
                val fragmentManager = requireActivity().supportFragmentManager
                val dialogs = fragmentManager.fragments
                    .filterIsInstance<DialogFragment>()
                    .filter { it != this }

                for (dialog in dialogs) {
                    dialog.dismiss()
                }
                dialog?.dismiss()
            }




            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }
}
