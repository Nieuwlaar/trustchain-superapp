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
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.community.PowerofAttorneyCommunity
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.VTDialogFragment
import nl.tudelft.trustchain.valuetransfer.util.setNavigationBarColor
import org.json.JSONObject
import java.util.*

//
class IdentityAddKvkPoaDialog(var myPublicKey: String) : VTDialogFragment() {
    private var filledKvkNumber = ""
    private val TAG = "PoaCommunity"
    private val URL_KVK_API = "https://ccc5-176-117-57-243.eu.ngrok.io/api/bevoegdheid/"
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        Log.i(TAG, "Dialog being created")
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
                // TODO: fill in identity values
                jsonObject.put("geboortedatum", "01-01-2000")
                jsonObject.put("voornamen", "Jan")
                jsonObject.put("geslachtsnaam", "Klaasen")
                jsonObject.put("voorvoegselGeslachtsnaam", "")
                Log.i(TAG, "JSON object: $jsonObject")
                val request = JsonObjectRequest(
                    Request.Method.POST, apiUrlWithKvkNumber, jsonObject,
                    { response ->
//                      TODO: add error messages
                        Log.i(TAG, "Response type: " + response.javaClass.name)

                        val id = UUID.randomUUID().toString()
                        Log.i(TAG, "API Respone id: $id")

                        val receivedKvkNumber = response.getJSONObject("inschrijving").getString("kvkNummer")
                        Log.i(TAG, "API Respone receivedKvkNumber: $receivedKvkNumber")

                        val companyName = response.getJSONObject("inschrijving").getString("naam")
                        Log.i(TAG, "API Respone companyName: $companyName")

                        val poaType = "Full Proof of Attorney"
                        Log.i(TAG, "API Respone poaType: $poaType")

                        val isBevoegd =  response.getJSONObject("bevoegdheidUittreksel").getJSONObject("matchedFunctionaris").getJSONObject("interpretatie").getString("isBevoegd")
                        val isPermitted: String
                        val isAllowedToIssuePoa: String
//                      Using the most strict form of "isBevoegd" (see KVK API documentation)
                        if (isBevoegd == "Ja"){
                            isPermitted = "YES"
                            isAllowedToIssuePoa = "YES"
                        } else {
                            isPermitted = "NO"
                            isAllowedToIssuePoa = "NO"
                        }
                        Log.i(TAG, "API Respone isPermitted: $isPermitted")
                        Log.i(TAG, "API Respone isAllowedToIssuePoa: $isAllowedToIssuePoa")

                        val publicKeyPoaHolder = myPublicKey
                        Log.i(TAG, "API Respone publicKeyPoaHolder: $publicKeyPoaHolder")

                        val givenNamesPoaHolder = response.getJSONObject("bevoegdheidUittreksel").getJSONObject("matchedFunctionaris").getString("geslachtsnaam")
                        Log.i(TAG, "API Respone givenNamesPoaHolder: $givenNamesPoaHolder")

//                        TODO: Pick where the holder info should come from (KVK or internal identity)

                        val surnamePoaHolder = response.getJSONObject("bevoegdheidUittreksel").getJSONObject("matchedFunctionaris").getString("geslachtsnaam")
                        Log.i(TAG, "API Respone surnamePoaHolder: $surnamePoaHolder")

                        val dateOfBirthPoaHolder = response.getJSONObject("bevoegdheidUittreksel").getJSONObject("matchedFunctionaris").getString("geslachtsnaam")
                        Log.i(TAG, "API Respone dateOfBirthPoaHolder: $dateOfBirthPoaHolder")

                        val publicKeyPoaIssuer = ""
                        Log.i(TAG, "API Respone publicKeyPoaIssuer: $publicKeyPoaIssuer")

                        val givenNamesPoaIssuer = "KVK Registry"
                        Log.i(TAG, "API Respone givenNamesPoaIssuer: $givenNamesPoaIssuer")

                        val surnamePoaIssuer = ""
                        Log.i(TAG, "API Respone surnamePoaIssuer: $surnamePoaIssuer")

                        val dateOfBirthPoaIssuer = ""
                        Log.i(TAG, "API Respone dateOfBirthPoaIssuer: $dateOfBirthPoaIssuer")

                        val responseString = response.toString()
                        Log.i(TAG, "API Respone: $responseString")

                        val receivedKvkPoa = PowerOfAttorney(
                            id = UUID.randomUUID().toString(),
                            kvkNumber = receivedKvkNumber.toLong(),
                            companyName = companyName,
                            poaType = poaType,
                            isPermitted = isPermitted,
                            isAllowedToIssuePoa = isAllowedToIssuePoa,
                            publicKeyPoaHolder = myPublicKey,
                            givenNamesPoaHolder = givenNamesPoaHolder,
                            surnamePoaHolder = surnamePoaHolder,
                            dateOfBirthPoaHolder = dateOfBirthPoaHolder,
                            publicKeyPoaIssuer = publicKeyPoaIssuer,
                            givenNamesPoaIssuer = givenNamesPoaIssuer,
                            surnamePoaIssuer = givenNamesPoaIssuer,
                            dateOfBirthPoaIssuer = dateOfBirthPoaIssuer
                        )
                        if (isValidKvkPoa(receivedKvkPoa)){
                            val poaCommunity = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
                            poaCommunity.addPoa(receivedKvkPoa)
                        } else {
                            Log.e(TAG, "isValidKvkPoa is FALSE: ")
                            parentActivity.displayToast(
                                requireContext(),
                                resources.getString(R.string.snackbar_unexpected_error_occurred)
                            )
                        }
                    },
                    { error ->
                        Log.e(TAG, "RESPONSE IS $error")
                        parentActivity.displayToast(
                            requireContext(),
                            resources.getString(R.string.snackbar_unexpected_error_occurred)
                        )
                    }
                )
                Log.i(TAG, request.toString())
                // Add the volley post request to the request queue
                queue.add(request)
            }


            setNavigationBarColor(requireContext(), parentActivity, bottomSheetDialog)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            bottomSheetDialog
        } ?: throw IllegalStateException(resources.getString(R.string.text_activity_not_null_requirement))
    }

    private fun isValidKvkPoa(receivedKvkPoa: PowerOfAttorney): Boolean {
        //TODO: check identity with receivedpoa
//      val identity = getIdentityCommunity().getIdentity()!!
//      Log.i(TAG, "ALL CRAP FROM getIdentity : "+ identity.toString())
//      val surnamePoaHolder = identity.content.surname
        if (receivedKvkPoa.poaType == "Full Power of Attorney"){
            return true
        }
        return true
    }

    internal fun isValidKvkNumber(kvkNumber: String): Boolean {
        if (!"^[0-9]*\$".toRegex().matches(kvkNumber)) {
            return false
        }
        return !(kvkNumber.length < 8 || kvkNumber.length > 8)
    }

}
