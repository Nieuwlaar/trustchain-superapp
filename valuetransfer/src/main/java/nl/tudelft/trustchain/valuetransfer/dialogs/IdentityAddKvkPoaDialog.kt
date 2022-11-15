package nl.tudelft.trustchain.valuetransfer.dialogs

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor
import org.json.JSONObject

class IdentityAddKvkPoaDialog : VTDialogFragment() {
    private var filledKvkNumber = ""
    private val TAG = "PoaCommunity"
    private val URL_KVK_API = "http://127.0.0.1:3333/api/bevoegheden/"

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.dialog_identity_add_kvk_poa, null)

            val kvkNumberView = view.findViewById<TextView>(R.id.etKvkNumber)
            val kvkNumberButtonCl = view.findViewById<ConstraintLayout>(R.id.clAddKvkPoa)
            val kvkNumberButtonTv = view.findViewById<TextView>(R.id.tvAddKvkPoa)

            // Avoid keyboard exposing over content of dialog
            bottomSheetDialog.behavior.apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }

            kvkNumberView.doAfterTextChanged {
                when {
                    !isValidKvkNumber(kvkNumberView.text.toString()) -> {
                        kvkNumberButtonCl.background.setTint(Color.parseColor("#EEEEEE"))
                        kvkNumberButtonCl.isClickable = false
                        kvkNumberButtonCl.isFocusable = false
                        kvkNumberButtonTv.setTextColor(Color.parseColor("#000000"))
                        kvkNumberButtonTv.setTypeface(null, Typeface.NORMAL)
                    }
                    else -> {
                        kvkNumberButtonCl.background.setTint(Color.parseColor("#00A6D6"))
                        kvkNumberButtonCl.isClickable = true
                        kvkNumberButtonCl.isFocusable = true
                        kvkNumberButtonTv.setTextColor(Color.parseColor("#FFFFFF"))
                        kvkNumberButtonTv.setTypeface(null, Typeface.BOLD)
                        filledKvkNumber = kvkNumberView.text.toString()
                    }
                }
            }

            kvkNumberButtonCl.setOnClickListener {
                Log.i(TAG, "GET KVK PoA button clicked")
                val queue = Volley.newRequestQueue(requireContext())
                val apiUrlWithKvkNumber = URL_KVK_API+filledKvkNumber
                Log.i(TAG, "POST URL: $apiUrlWithKvkNumber")
                val jsonObject = JSONObject()
                jsonObject.put("geboortedatum", "01-01-2000")
                jsonObject.put("voornamen", "Jan")
                jsonObject.put("geslachtsnaam", "Klaasen")
                jsonObject.put("voorvoegselGeslachtsnaam", "")
                Log.i(TAG, "JSON object: $jsonObject")
                val request = JsonObjectRequest(
                    Request.Method.POST, apiUrlWithKvkNumber, jsonObject,
                    { response ->
                        val paymentId = response.getString("payment_id")
                        Log.i(TAG, "API Respone: $paymentId")
                    },
                    { error ->
                        Log.e(TAG, error.message ?: error.toString())
                        parentActivity.displayToast(
                            requireContext(),
                            resources.getString(R.string.snackbar_unexpected_error_occurred)
                        )
                    }
                )
                // Add the volley post request to the request queue
                queue.add(request)
            }


            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }

    internal fun isValidKvkNumber(kvkNumber: String): Boolean {
        if (!"^[0-9]*\$".toRegex().matches(kvkNumber)) {
            return false
        }
        return !(kvkNumber.length < 8 || kvkNumber.length > 8)
    }

}
