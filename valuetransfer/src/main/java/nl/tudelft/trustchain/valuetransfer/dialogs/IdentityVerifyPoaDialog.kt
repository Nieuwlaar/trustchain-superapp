package nl.tudelft.trustchain.valuetransfer.dialogs

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.integration.android.IntentIntegrator
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor

//
class IdentityVerifyPoaDialog(var myPublicKey: String) : VTDialogFragment() {
    private var filledKvkNumber = ""
    private val TAG = "PoaCommunity"


    var kvkNumberGlob: String = ""
    var poaTypeGlob: String = ""




    @SuppressLint("RestrictedApi")

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "HELLO")
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
//                Toast.makeText(activity, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                val scannedResult = result.contents
                Log.i("PoaCommunity", "WE SCANNED: "+scannedResult)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {

        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_poa_verify, null)

            val kvkNumberView = view.findViewById<TextView>(R.id.etKvkNumber)
            val verifyPoaButton = view.findViewById<ConstraintLayout>(R.id.clVerifyPoa)
            val poaType = view.findViewById<TextView>(R.id.poaType)
//            val verifyPoaButtonText = view.findViewById<TextView>(R.id.tvVerifyPoa)
            val poaSignXAmount = view.findViewById<TextView>(R.id.poaSignXAmount)
            val poaCustomFillIn = view.findViewById<TextView>(R.id.poaCustomFillIn)

            // Avoid keyboard exposing over content of dialog
            bottomSheetDialog.behavior.apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
            poaType.doAfterTextChanged {
                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
            }

            kvkNumberView.doAfterTextChanged {
                Log.i(TAG, "5")

                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
//                when {
//
//                    !isValidKvkNumber(kvkNumberView.text.toString()) && poaType.text != null -> {
//                        verifyPoaButton.background.setTint(Color.parseColor("#EEEEEE"))
//                        verifyPoaButton.isClickable = false
//                        verifyPoaButton.isFocusable = false
//                        verifyPoaButtonText.setTextColor(Color.parseColor("#000000"))
//                        verifyPoaButtonText.setTypeface(null, Typeface.NORMAL)
//                        kvkNumberGlob = kvkNumberView.text.toString()
//                    }
//                    else -> {
//                        verifyPoaButton.background.setTint(Color.parseColor("#00A6D6"))
//                        verifyPoaButton.isClickable = true
//                        verifyPoaButton.isFocusable = true
//                        verifyPoaButtonText.setTextColor(Color.parseColor("#FFFFFF"))
//                        verifyPoaButtonText.setTypeface(null, Typeface.BOLD)
//                        filledKvkNumber = kvkNumberView.text.toString()
//                    }
//                }
            }

            verifyPoaButton.setOnClickListener {
                Log.i(TAG, "Verify PoA button clicked")
                val qrScanController = getQRScanController()
                qrScanController.initiatePoaVerifyScan(kvkNumberGlob, poaTypeGlob)
                dialog?.dismiss()
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
                            R.id.actionCreateQuotations -> {
                                poaType.text = getString(R.string.poa_type_create_quotations)
                                poaTypeGlob = poaType.text.toString()
                                Log.i(TAG, poaType.text.toString())
                                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
                            }
                            R.id.actionPurchaseWholesale -> {
                                poaType.text = getString(R.string.poa_type_purchase_wholesale)
                                poaTypeGlob = poaType.text.toString()
                                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
                            }
                            R.id.actionSignUpToX -> {
                                poaType.text = getString(R.string.poa_type_sign_up_to_x)
                                poaTypeGlob = poaType.text.toString()
                                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
                            }
                            R.id.actionCustom -> {
                                poaType.text = "Custom"
                                poaTypeGlob = poaType.text.toString()
                                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
                            }
                        }
                    }
                ).show(parentFragmentManager, tag)
            }
            poaType.doAfterTextChanged{
                poaSignXAmount.isVisible = false
                poaCustomFillIn.isVisible = false
                val params = verifyPoaButton.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = poaType.id
                when(poaType.text.toString()) {
                    "Sign" -> {
                        poaSignXAmount.isVisible = true
                        params.topToBottom = poaSignXAmount.id
                        Log.i(TAG,"Sign up to X reached")
                    }
                    "Custom" -> {
                        Log.i(TAG,"PoA Type: "+ poaType.text.toString())
                        poaCustomFillIn.isVisible = true
                        params.topToBottom = poaCustomFillIn.id
                    }
                }
                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
            }

            poaSignXAmount.doAfterTextChanged{
                poaTypeGlob = "Sign"+poaSignXAmount.text.toString()
                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
            }

            poaCustomFillIn.doAfterTextChanged{
                poaTypeGlob = poaCustomFillIn.text.toString()
                makeVerifyClickable(view, isValidVerification(isValidKvkNumber(kvkNumberView.text.toString()), isValidPoaType(poaTypeGlob)))
            }




            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }

    private fun makeVerifyClickable(view : View, isValid : Boolean){
        val kvkNumberView = view.findViewById<TextView>(R.id.etKvkNumber)
        val verifyPoaButton = view.findViewById<ConstraintLayout>(R.id.clVerifyPoa)
        val verifyPoaButtonText = view.findViewById<TextView>(R.id.tvVerifyPoa)
        Log.i(TAG, "1")
        if (isValid) {
            Log.i(TAG, "2")
            verifyPoaButton.background.setTint(Color.parseColor("#00A6D6"))
            verifyPoaButton.isClickable = true
            verifyPoaButton.isFocusable = true
            verifyPoaButtonText?.setTextColor(Color.parseColor("#FFFFFF"))
            verifyPoaButtonText?.setTypeface(null, Typeface.BOLD)
            filledKvkNumber = kvkNumberView?.text.toString()
        } else {
            Log.i(TAG, "3")
            verifyPoaButton.background?.setTint(Color.parseColor("#EEEEEE"))
            verifyPoaButton.isClickable = false
            verifyPoaButton.isFocusable = false
            verifyPoaButtonText.setTextColor(Color.parseColor("#000000"))
            verifyPoaButtonText.setTypeface(null, Typeface.NORMAL)
            kvkNumberGlob = kvkNumberView.text.toString()
        }

    }

    private fun isValidVerification(validPoaType : Boolean, validKvkNumber : Boolean): Boolean {
        if ((validPoaType == true) && (validKvkNumber == true)){
            return true
        } else {
            return false
        }
    }


    private fun isValidPoaType(poaType: String): Boolean {
        if ((poaType == "") || (poaType == "Sign") || (poaType == "Custom")){
            return false
        } else {
            return true
        }
    }

    private fun isValidKvkNumber(kvkNumber: String): Boolean {
        if (!"^[0-9]*\$".toRegex().matches(kvkNumber)) {
            return false
        }
        return !(kvkNumber.length < 8 || kvkNumber.length > 8)
    }
}
