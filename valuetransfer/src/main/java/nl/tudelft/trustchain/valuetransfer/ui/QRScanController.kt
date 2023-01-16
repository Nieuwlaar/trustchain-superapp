package nl.tudelft.trustchain.valuetransfer.ui

import android.content.Intent
import android.util.Log
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.keyvault.defaultCryptoProvider
import nl.tudelft.ipv8.util.hexToBytes
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.common.contacts.Contact
import nl.tudelft.trustchain.common.util.QRCodeUtils
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.community.PowerofAttorneyCommunity
import nl.tudelft.trustchain.valuetransfer.dialogs.*
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import org.json.JSONObject


class QRScanController : VTFragment() {
    private lateinit var kvkNumberGlob : String
    private lateinit var poaTypeGlob: String

    fun initiateScan() {
        QRCodeUtils(requireContext()).startQRScanner(
            this,
            promptText = resources.getString(R.string.text_scan_any_qr),
            vertical = true
        )
    }


    fun initiatePoaVerifyScan(kvkNumberGlob: String, poaTypeGlob: String) {
        this@QRScanController.kvkNumberGlob = kvkNumberGlob
        this@QRScanController.poaTypeGlob = poaTypeGlob
        QRCodeUtils(requireContext()).startQRScanner(
            this,
            promptText = resources.getString(R.string.text_scan_qr_verify_poa),
            vertical = true
        )
    }

    private fun checkRequiredVariables(variables: List<String>, data: JSONObject): Boolean {
        variables.forEach { variable ->
            if (!data.has(variable)) {
                parentActivity.displayToast(
                    requireContext(),
                    resources.getString(
                        R.string.snackbar_missing_variable,
                        variable
                    )
                )
                return false
            }
        }
        return true
    }

    fun addAuthority(publicKey: String) {
        IdentityAttestationAuthorityDialog(defaultCryptoProvider.keyFromPublicBin(publicKey.hexToBytes())).show(parentFragmentManager, tag)
    }

    fun issuePoa(publicKey: String){
        val community = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
        community.sendPoa(publicKey)
    }

    fun addAttestation(publicKey: String) {

        val peer = getAttestationCommunity().getPeers().find { peer -> peer.publicKey.keyToBin().toHex() == publicKey }

        if (peer != null) {
            IdentityAttestationRequestDialog(peer).show(parentFragmentManager, tag)
        } else {
            parentActivity.displayToast(
                requireContext(),
                resources.getString(R.string.snackbar_peer_unlocated)
            )
        }
    }

    private fun verifyAttestation(data: JSONObject) {

        val variables = listOf(KEY_METADATA, KEY_ATTESTATION_HASH, KEY_SIGNATURE, KEY_SIGNEE_KEY, KEY_ATTESTOR_KEY)
        checkRequiredVariables(variables, data)

        val metadataVariables = listOf(KEY_ATTRIBUTE, KEY_ID_FORMAT)
        checkRequiredVariables(metadataVariables, JSONObject(data.getString(KEY_METADATA)))

        val attesteeKey = data.getString(KEY_SIGNEE_KEY).hexToBytes()
        val attestationHash = data.getString(KEY_ATTESTATION_HASH).hexToBytes()
        val metadata = data.getString(KEY_METADATA)
        val signature = data.getString(KEY_SIGNATURE).hexToBytes()
        val authorityKey = data.getString(KEY_ATTESTOR_KEY).hexToBytes()

        IdentityAttestationVerifyDialog(attesteeKey, attestationHash, metadata, signature, authorityKey).show(
            parentFragmentManager,
            this.tag
        )
    }

    fun addContact(data: JSONObject) {
        val variables = listOf(KEY_PUBLIC_KEY)
        checkRequiredVariables(variables, data)

        try {
            val publicKey = defaultCryptoProvider.keyFromPublicBin(data.optString(KEY_PUBLIC_KEY).hexToBytes())
            val name = data.optString(KEY_NAME)

            ContactAddDialog(
                getTrustChainCommunity().myPeer.publicKey,
                publicKey,
                name
            ).show(parentFragmentManager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
            parentActivity.displayToast(
                requireContext(),
                resources.getString(R.string.snackbar_invalid_public_key)
            )
        }
    }

    fun transferMoney(data: JSONObject) {
        val variables = listOf(KEY_PUBLIC_KEY, KEY_NAME, KEY_AMOUNT)
        checkRequiredVariables(variables, data)

        try {
            val publicKey = defaultCryptoProvider.keyFromPublicBin(data.optString(KEY_PUBLIC_KEY).hexToBytes())

            if (publicKey == getTrustChainCommunity().myPeer.publicKey) {
                parentActivity.displayToast(
                    requireContext(),
                    resources.getString(R.string.snackbar_exchange_transfer_error_self)
                )
                return
            }

            val amount = data.optString(KEY_AMOUNT)

            var contact = getContactStore().getContactFromPublicKey(publicKey)
            if (contact == null) {
                contact = Contact(data.optString(KEY_NAME), publicKey)
            }

            val message = data.optString(KEY_MESSAGE)
            ExchangeTransferMoneyDialog(
                contact,
                amount,
                true,
                message
            ).show(parentFragmentManager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
            parentActivity.displayToast(
                requireContext(),
                resources.getString(R.string.snackbar_invalid_public_key)
            )
        }
    }

    fun exchangeMoney(data: JSONObject, isCreation: Boolean) {
        val variables = when {
            isCreation -> listOf(KEY_PAYMENT_ID, KEY_PUBLIC_KEY, KEY_IP, KEY_PORT, KEY_NAME)
            else -> listOf(KEY_PAYMENT_ID, KEY_PUBLIC_KEY, KEY_IP, KEY_PORT, KEY_NAME, KEY_AMOUNT)
        }

        checkRequiredVariables(variables, data)

        try {
            val publicKey = defaultCryptoProvider.keyFromPublicBin(data.optString(KEY_PUBLIC_KEY).hexToBytes())
            val amount = if (isCreation) null else data.optLong(KEY_AMOUNT)

            ExchangeGatewayDialog(
                isCreation,
                publicKey,
                data.optString(KEY_PAYMENT_ID),
                data.optString(KEY_IP),
                data.optInt(KEY_PORT),
                data.optString(KEY_NAME),
                amount
            ).show(parentFragmentManager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
            parentActivity.displayToast(
                requireContext(),
                resources.getString(R.string.snackbar_invalid_public_key)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.i("PoaCommunity", "This is the QRScanController onActivityResult")
        QRCodeUtils(requireContext()).parseActivityResult(requestCode, resultCode, data)?.let { result ->
            Log.i("PoaCommunity", "Raw QR scan results:" + result)
            if (result.startsWith("PowerOfAttorney")){
                try {
                    //Removing the class name and paranthesis from string
                    val jsonString = result.replace("PowerOfAttorney(", "").replace(")", "")

                    // Splitting the string by comma to get key value pair
                    val keyValuePairs: List<String> = jsonString.split(",")


                    // Creating JSON object to add key value pair
                    val json = JSONObject()
                    for (keyValuePair in keyValuePairs) {
                        val keyValue = keyValuePair.trim { it <= ' ' }.split("=").toTypedArray()
                        json.put(keyValue[0], keyValue[1])
                    }
                    val poa = PowerOfAttorney(
                        json.getString("id"),
                        json.getLong("kvkNumber"),
                        json.getString("companyName"),
                        json.getString("poaType"),
                        json.getString("isPermitted"),
                        json.getString("isAllowedToIssuePoa"),
                        json.getString("publicKeyPoaHolder"),
                        json.getString("givenNamesPoaHolder"),
                        json.getString("surnamePoaHolder"),
                        json.getString("dateOfBirthPoaHolder"),
                        json.getString("publicKeyPoaIssuer"),
                        json.getString("givenNamesPoaIssuer"),
                        json.getString("surnamePoaIssuer"),
                        json.getString("dateOfBirthPoaIssuer")
                    )

                    Log.i("PoaCommunity", "Check if is the same:")
                    Log.i("PoaCommunity", "Scanned KVK number: "+poa.kvkNumber.toString()+" Input verify KVK number: "+ kvkNumberGlob )
                    Log.i("PoaCommunity", "Scanned PoA Type: "+poa.poaType+" Input Poa Type: "+poaTypeGlob)

                    Log.i("PoaCommunity", "Scanned QR object Type: " + poa.javaClass.name)
                    Log.i("PoaCommunity", "QR: Object to string again: " + poa.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("PoaCommunity", e.printStackTrace().toString())
                    parentActivity.displayToast(
                        requireContext(),
                        resources.getString(R.string.snackbar_qr_code_not_poa_format)
                    )
                    initiateScan()
                }
            } else {
                try {
                    val obj = JSONObject(result)

                    when {
                        obj.has(KEY_TYPE) -> {
                            when (obj.optString(KEY_TYPE)) {
                                VALUE_TRANSFER -> transferMoney(obj)
                                VALUE_CREATION -> exchangeMoney(obj, true)
                                VALUE_DESTRUCTION -> exchangeMoney(obj, false)
                                VALUE_CONTACT -> addContact(obj)
                                else -> throw RuntimeException(
                                    resources.getString(
                                        R.string.text_qr_type_not_recognized,
                                        obj.get(KEY_TYPE)
                                    )
                                )
                            }
                        }
                        obj.has(KEY_PRESENTATION) -> {
                            when (obj.optString(KEY_PRESENTATION)) {
                                VALUE_ATTESTATION -> verifyAttestation(obj)
                                else -> throw RuntimeException(
                                    resources.getString(
                                        R.string.text_qr_type_not_recognized,
                                        obj.get(KEY_PRESENTATION)
                                    )
                                )
                            }
                        }
                        obj.has(KEY_PUBLIC_KEY) -> {
                            try {
                                val publicKey = obj.optString(KEY_PUBLIC_KEY)
                                defaultCryptoProvider.keyFromPublicBin(publicKey.hexToBytes())
                                val publicKeyString = obj.optString(KEY_PUBLIC_KEY)

                                OptionsDialog(
                                    R.menu.scan_options,
                                    "Choose Option",
                                ) { _, item ->
                                    when (item.itemId) {
                                        R.id.actionAddContactOption -> addContact(obj)
                                        R.id.actionAddAuthorityOption -> addAuthority(publicKeyString)
                                        R.id.actionAddAttestationOption -> addAttestation(publicKeyString)
                                        R.id.actionIssuePoa -> issuePoa(publicKeyString)
                                    }
                                }.show(parentFragmentManager, tag)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                parentActivity.displayToast(
                                    requireContext(),
                                    resources.getString(R.string.snackbar_invalid_public_key)
                                )
                            }
                        }
                        else -> throw RuntimeException(resources.getString(R.string.text_qr_not_recognized))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("PoaCommunity", e.printStackTrace().toString())
                    parentActivity.displayToast(
                        requireContext(),
                        resources.getString(R.string.snackbar_qr_code_not_json_format)
                    )
                    initiateScan()
                }
            }
        }

    }


    companion object {
        const val KEY_AMOUNT = "amount"
        const val KEY_ATTESTATION_HASH = "attestationHash"
        const val KEY_ATTESTOR_KEY = "attestor_key"
        const val KEY_ATTRIBUTE = "attribute"
        const val KEY_ID_FORMAT = "id_format"
        const val KEY_IP = "ip"
        const val KEY_MESSAGE = "message"
        const val KEY_METADATA = "metadata"
        const val KEY_NAME = "name"
        const val KEY_PAYMENT_ID = "payment_id"
        const val KEY_PORT = "port"
        const val KEY_PRESENTATION = "presentation"
        const val KEY_PUBLIC_KEY = "public_key"
        const val POWER_OF_ATTORNEY = "power_of_attorney"
        const val KEY_SIGNATURE = "signature"
        const val KEY_SIGNEE_KEY = "signee_key"
        const val KEY_TYPE = "type"
        const val KEY_VALUE = "value"

        const val VALUE_ATTESTATION = "attestation"
        const val VALUE_TRANSFER = "transfer"
        const val VALUE_CREATION = "creation"
        const val VALUE_DESTRUCTION = "destruction"
        const val VALUE_CONTACT = "contact"

        const val FALLBACK_UNKNOWN = "UNKNOWN"

    }
}
