package nl.tudelft.trustchain.valuetransfer.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

//
class IdentityVerifyPoaDialog(var myPublicKey: String) : VTDialogFragment() {
    private var filledKvkNumber = ""
    private val TAG = "PoaCommunity"
    @SuppressLint("RestrictedApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_poa_verify, null)

            val kvkNumberView = view.findViewById<TextView>(R.id.etKvkNumber)
            val verifyPoaButton = view.findViewById<ConstraintLayout>(R.id.clVerifyPoa)
            val poaType = view.findViewById<EditText>(R.id.poaType)
            val verifyPoaButtonText = view.findViewById<TextView>(R.id.tvVerifyPoa)

            // Avoid keyboard exposing over content of dialog
            bottomSheetDialog.behavior.apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }

            kvkNumberView.doAfterTextChanged {
                when {
                    !isValidKvkNumber(kvkNumberView.text.toString()) -> {
                        verifyPoaButton.background.setTint(Color.parseColor("#EEEEEE"))
                        verifyPoaButton.isClickable = false
                        verifyPoaButton.isFocusable = false
                        verifyPoaButtonText.setTextColor(Color.parseColor("#000000"))
                        verifyPoaButtonText.setTypeface(null, Typeface.NORMAL)
                    }
                    else -> {
                        verifyPoaButton.background.setTint(Color.parseColor("#00A6D6"))
                        verifyPoaButton.isClickable = true
                        verifyPoaButton.isFocusable = true
                        verifyPoaButtonText.setTextColor(Color.parseColor("#FFFFFF"))
                        verifyPoaButtonText.setTypeface(null, Typeface.BOLD)
                        filledKvkNumber = kvkNumberView.text.toString()
                    }
                }
            }

            verifyPoaButton.setOnClickListener {
                Log.i(TAG, "Verify PoA button clicked")

            }

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
                            R.id.actionCreateQuotations -> Log.i(TAG, "1")
                            R.id.actionPurchaseWholesale -> Log.i(TAG, "2")
                            R.id.actionSignUpToX -> Log.i(TAG, "3")
                            R.id.actionCustom -> Log.i(TAG, "4")
                        }
                    }
                ).show(parentFragmentManager, tag)
            }


            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }


    private fun isValidKvkNumber(kvkNumber: String): Boolean {
        if (!"^[0-9]*\$".toRegex().matches(kvkNumber)) {
            return false
        }
        return !(kvkNumber.length < 8 || kvkNumber.length > 8)
    }

}
