package nl.tudelft.trustchain.valuetransfer.ui.identity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mattskala.itemadapter.Item
import com.mattskala.itemadapter.ItemAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.attestation.schema.SchemaManager
import nl.tudelft.ipv8.attestation.wallet.AttestationBlob
import nl.tudelft.ipv8.keyvault.defaultCryptoProvider
import nl.tudelft.ipv8.util.hexToBytes
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.common.util.QRCodeUtils
import nl.tudelft.trustchain.common.util.viewBinding
import nl.tudelft.trustchain.common.valuetransfer.entity.IdentityAttribute
import nl.tudelft.trustchain.common.valuetransfer.extensions.decodeImage
import nl.tudelft.trustchain.common.valuetransfer.extensions.encodeImage
import nl.tudelft.trustchain.common.valuetransfer.extensions.exitEnterView
import nl.tudelft.trustchain.valuetransfer.R
import nl.tudelft.trustchain.valuetransfer.ValueTransferMainActivity
import nl.tudelft.trustchain.valuetransfer.community.PowerofAttorneyCommunity
import nl.tudelft.trustchain.valuetransfer.databinding.FragmentIdentityBinding
import nl.tudelft.trustchain.valuetransfer.dialogs.*
import nl.tudelft.trustchain.valuetransfer.entity.Identity
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.trustchain.valuetransfer.ui.QRScanController
import nl.tudelft.trustchain.valuetransfer.ui.VTFragment
import nl.tudelft.trustchain.valuetransfer.util.DividerItemDecorator
import nl.tudelft.trustchain.valuetransfer.util.copyToClipboard
import nl.tudelft.trustchain.valuetransfer.util.getInitials
import nl.tudelft.trustchain.valuetransfer.util.mapToJSON
import org.json.JSONObject
import java.util.*

class IdentityFragment : VTFragment(R.layout.fragment_identity) {
    private val binding by viewBinding(FragmentIdentityBinding::bind)

    private var titlesList = mutableListOf<String>()
    private var imagesList = mutableListOf<Int>()

    private val adapterIdentity = ItemAdapter()
    private val adapterAttributes = ItemAdapter()
    private val adapterAttestations = ItemAdapter()
    private val adapterYourPoas = ItemAdapter()
    private val adapterIssuedPoas = ItemAdapter()

    private val identityImage = MutableLiveData<String?>()
    private val TAG = "PoaCommunity"
    private var scanIntent: Int = -1

    private val itemsIdentity: LiveData<List<Item>> by lazy {
        combine(getIdentityStore().getAllIdentities(), identityImage.asFlow()) { identities, identityImage ->
            createIdentityItems(identities, identityImage)
        }.asLiveData()
    }

    private val itemsAttributes: LiveData<List<Item>> by lazy {
        getIdentityStore().getAllAttributes().map { attributes ->
            createAttributeItems(attributes)
        }.asLiveData()
    }

    private val itemsYourPoas: LiveData<List<Item>> by lazy {
        getPoaStore().getAllYourPoas().map { poas ->
            createPoaItems(poas)
        }.asLiveData()
    }

    private val itemsIssuedPoas: LiveData<List<Item>> by lazy {
        getPoaStore().getAllIssuedPoas().map { issuedPoas ->
            createPoaItems(issuedPoas)
        }.asLiveData()
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_identity, container, false)
    }


    override fun initView() {
        parentActivity.apply {
            setActionBarTitle(resources.getString(R.string.menu_navigation_identity), null)
            toggleActionBar(false)
            toggleBottomNavigation(true)
        }
    }

    init {
        setHasOptionsMenu(true)

        lifecycleScope.launchWhenCreated {
            while (isActive) {
                if (appPreferences.getIdentityFace() != identityImage.value) {
                    identityImage.postValue(appPreferences.getIdentityFace())
                    parentActivity.invalidateOptionsMenu()
                }

                delay(1000)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapterIdentity.registerRenderer(
            IdentityItemRenderer(
                1,
                { identity ->
                    val map = mapOf(
                        QRScanController.KEY_PUBLIC_KEY to identity.publicKey.keyToBin().toHex(),
                        QRScanController.KEY_NAME to identity.content.let {
                            "${it.givenNames.getInitials()} ${it.surname}"
                        },
                    )

                    QRCodeDialog(resources.getString(R.string.text_my_public_key), resources.getString(R.string.text_public_key_share_desc), mapToJSON(map).toString())
                        .show(parentFragmentManager, tag)
                },
                { identity ->
                    copyToClipboard(
                        requireContext(),
                        identity.publicKey.keyToBin().toHex(),
                        resources.getString(R.string.text_public_key)
                    )
                    parentActivity.displayToast(
                        requireContext(),
                        resources.getString(
                            R.string.snackbar_copied_clipboard,
                            resources.getString(R.string.text_public_key)
                        )
                    )
                },
                {
                    if (identityImage.value!!.isBlank()) {
                        identityImageIntent()
                    } else {
                        OptionsDialog(
                            R.menu.identity_image_options,
                            "Choose Option",
                            bigOptionsEnabled = true,
                            menuMods = { menu ->
                                menu.apply {
                                    findItem(R.id.actionDeleteIdentityImage).isVisible =
                                        identityImage.value!!.isNotBlank()
                                }
                            },
                            optionSelected = { _, item ->
                                when (item.itemId) {
                                    R.id.actionAddIdentityImage -> identityImageIntent()
                                    R.id.actionDeleteIdentityImage -> {
                                        appPreferences.deleteIdentityFace()
                                        parentActivity.invalidateOptionsMenu()
                                    }
                                }
                            }
                        ).show(parentFragmentManager, tag)
                    }
                }
            )
        )

        adapterAttributes.registerRenderer(
            IdentityAttributeItemRenderer(
                1
            ) {
                OptionsDialog(
                    R.menu.identity_attribute_options,
                    "Choose Option",
                    bigOptionsEnabled = true,
                ) { _, item ->
                    when (item.itemId) {
                        R.id.actionEditIdentityAttribute -> IdentityAttributeDialog(it).show(
                            parentFragmentManager,
                            tag
                        )
                        R.id.actionDeleteIdentityAttribute -> deleteIdentityAttribute(it)
                        R.id.actionShareIdentityAttribute -> IdentityAttributeShareDialog(
                            null,
                            it
                        ).show(parentFragmentManager, tag)
                    }
                }.show(parentFragmentManager, tag)
            }
        )

        adapterYourPoas.registerRenderer(
            PoaItemRenderer {  poa ->
                PoADetailsDialog(
                    true,
                    poa
                )
                    .show(parentFragmentManager, tag)
            }
        )

        adapterIssuedPoas.registerRenderer(
            PoaItemRenderer {
                val args = Bundle().apply {
                    putString(ValueTransferMainActivity.ARG_PARENT, ValueTransferMainActivity.walletOverviewFragmentTag)
                }

                parentActivity.detailFragment(ValueTransferMainActivity.contactChatFragmentTag, args)
            }
        )

        adapterAttestations.registerRenderer(
            AttestationItemRenderer(
                parentActivity,
                {
                    val blob = it.attestationBlob

                    if (blob.signature != null) {
                        val manager = SchemaManager()
                        manager.registerDefaultSchemas()
                        val attestation = manager.deserialize(blob.blob, blob.idFormat)
                        val parsedMetadata = JSONObject(blob.metadata!!)

                        val map = mapOf(
                            QRScanController.KEY_PRESENTATION to QRScanController.VALUE_ATTESTATION,
                            QRScanController.KEY_METADATA to blob.metadata,
                            QRScanController.KEY_ATTESTATION_HASH to attestation.getHash().toHex(),
                            QRScanController.KEY_SIGNATURE to blob.signature!!.toHex(),
                            QRScanController.KEY_SIGNEE_KEY to IPv8Android.getInstance().myPeer.publicKey.keyToBin().toHex(),
                            QRScanController.KEY_ATTESTOR_KEY to blob.attestorKey!!.keyToBin().toHex()
                        )

                        QRCodeDialog(
                            resources.getString(R.string.dialog_title_attestation),
                            StringBuilder()
                                .append(
                                    parsedMetadata.optString(
                                        QRScanController.KEY_ATTRIBUTE,
                                        QRScanController.FALLBACK_UNKNOWN
                                    )
                                )
                                .append(
                                    parsedMetadata.optString(
                                        QRScanController.KEY_VALUE,
                                        QRScanController.FALLBACK_UNKNOWN
                                    )
                                )
                                .toString(),
                            mapToJSON(map).toString()
                        )
                            .show(parentFragmentManager, tag)
                    } else {
                        deleteAttestation(it)
                    }
                }
            ) {
                deleteAttestation(it)
            }
        )
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        binding.rvIdentities.apply {
            adapter = adapterIdentity
            layoutManager = LinearLayoutManager(context)
        }

        binding.rvAttributes.apply {
            adapter = adapterAttributes
            layoutManager = LinearLayoutManager(context)
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.divider_identity_attribute, requireContext().theme)
            addItemDecoration(DividerItemDecorator(drawable!!) as RecyclerView.ItemDecoration)
        }

        binding.rvYourPoas.apply {
            adapter = adapterYourPoas
            layoutManager = LinearLayoutManager(context)
        }

        binding.rvIssuedPoas.apply {
            adapter = adapterIssuedPoas
            layoutManager = LinearLayoutManager(context)
        }



        itemsIdentity.observe(
            viewLifecycleOwner,
            Observer {
                adapterIdentity.updateItems(it)
                toggleVisibility()
            }
        )

        lifecycleScope.launchWhenStarted {
            while (isActive) {
                updateAttestations()
                updatePoas()
                toggleVisibility()
                delay(1000)
            }
        }

        itemsAttributes.observe(
            viewLifecycleOwner,
            Observer {
                adapterAttributes.updateItems(it)
                toggleVisibility()
            }
        )

        itemsYourPoas.observe(
            viewLifecycleOwner,
            Observer {
                adapterYourPoas.updateItems(it)
                Log.i(TAG, "ALL YOUR POAS: " +itemsYourPoas.value)
            }
        )

        itemsIssuedPoas.observe(
            viewLifecycleOwner,
            Observer {
                adapterIssuedPoas.updateItems(it)
                Log.i(TAG, "ALL ISSUED POAS: " +itemsIssuedPoas.value)
            }
        )

        binding.ivAddAttributeAttestation.setOnClickListener {
            OptionsDialog(
                R.menu.identity_add_options,
                resources.getString(R.string.dialog_choose_option),
                menuMods = { menu ->
                    menu.apply {
                        findItem(R.id.actionAddIdentityAttribute).isVisible = getIdentityCommunity().getUnusedAttributeNames().isNotEmpty()
                    }
                },
                optionSelected = { _, item ->
                    when (item.itemId) {
                        R.id.actionAddIdentityAttribute -> addIdentityAttribute()
                        R.id.actionAddAttestation -> addAttestation()
                        R.id.actionAddAuthority -> addAuthority()
                    }
                }
            ).show(parentFragmentManager, tag)
        }

        binding.ivAddAttestation.setOnClickListener {
            OptionsDialog(
                R.menu.identity_add_attestation_options,
                resources.getString(R.string.dialog_choose_option),
                menuMods = { menu ->
                    menu.apply {
                        findItem(R.id.actionAddKvkPoa).isVisible = true
                        findItem(R.id.actionAddEBSIAttestation).isVisible = getIdentityCommunity().getUnusedAttributeNames().isNotEmpty()
                        findItem(R.id.actionIssuePoa).isVisible = getIdentityCommunity().getUnusedAttributeNames().isNotEmpty()
                        findItem(R.id.actionIssueFakePoa).isVisible = true
                    }
                },
                optionSelected = { _, item ->
                    when (item.itemId) {
                        R.id.actionAddKvkPoa -> addKvkPoa()
                        R.id.actionAddEBSIAttestation -> addPoa()
                        R.id.actionIssuePoa -> issuePoa()
                        R.id.actionIssueFakePoa -> issueFakePoa()
                    }
                }
            ).show(parentFragmentManager, tag)
            Log.i(TAG, "Add PoA button clicked")
            postToList()

            val wifiManager: WifiManager = context?.getSystemService(WIFI_SERVICE) as WifiManager
            val ip = ipToString(wifiManager.connectionInfo.ipAddress)
            Log.i(TAG, ip)
//            6ec97d075cb62c9a12ffdd5d5c4afe029b70570e
            val community = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
            Log.i(TAG, community.toString())
            val peers = community.getPeers()
            if (peers.isNullOrEmpty()) {
                Log.i(TAG,"Peers is null or empty")
            }
            for (peer in peers) {
                Log.i(TAG, peer.mid)
            }
            toggleVisibility()
            val ipv8 = getIpv8()
            Log.i(TAG,"My Public key: "+ipv8.myPeer.publicKey.keyToBin().toHex())
//            community.sendPoa()
        }

        binding.tvShowIdentityAttributes.setOnClickListener {
            if (binding.clIdentityAttributes.isVisible) return@setOnClickListener
        }

        binding.tvShowIssuedPoas.setOnClickListener {
            if (binding.clIssuedPoas.isVisible) return@setOnClickListener

            binding.tvShowYourPoas.apply {
                setTypeface(null, Typeface.NORMAL)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.pill_rounded)
            }
            binding.tvShowIssuedPoas.apply {
                setTypeface(null, Typeface.BOLD)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.pill_rounded_selected)
            }
            binding.clYourPoa.exitEnterView(requireContext(), binding.clIssuedPoas, true)
        }

        binding.tvShowYourPoas.setOnClickListener {
            if (binding.clYourPoa.isVisible) return@setOnClickListener

            binding.tvShowYourPoas.apply {
                setTypeface(null, Typeface.BOLD)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.pill_rounded_selected)
            }
            binding.tvShowIssuedPoas.apply {
                setTypeface(null, Typeface.NORMAL)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.pill_rounded)
            }
            binding.clIssuedPoas.exitEnterView(requireContext(), binding.clYourPoa, false)
        }
    }

    private fun ipToString(i: Int): String {
        return (i and 0xFF).toString() + "." +
            (i shr 8 and 0xFF) + "." +
            (i shr 16 and 0xFF) + "." +
            (i shr 24 and 0xFF)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        menu.add(Menu.NONE, MENU_ITEM_OPTIONS, Menu.NONE, null)
            .setIcon(R.drawable.ic_baseline_more_vert_24)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val community = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!
        OptionsDialog(
            R.menu.identity_options,
            resources.getString(R.string.dialog_choose_option),
        ) { _, selectedItem ->
            when (selectedItem.itemId) {
                R.id.actionViewAuthorities -> IdentityAttestationAuthoritiesDialog(
                    trustchain.getMyPublicKey().toHex()
                ).show(parentFragmentManager, tag)
                R.id.actionDeleteAllPoas -> {
                    Log.i(TAG, "Delete all PoAs button clicked")
                    community.deleteAllPoas()
                    toggleVisibility()
                }
            }
        }.show(parentFragmentManager, tag)

        return super.onOptionsItemSelected(item)
    }

    private fun toggleVisibility() {
        binding.tvNoIssuedPoas.isVisible = adapterIssuedPoas.itemCount == 0
        binding.tvNoYourPoas.isVisible = adapterYourPoas.itemCount == 0
        binding.tvNoAttributes.isVisible = adapterAttributes.itemCount == 0
    }

    private fun addAttestation() {
        scanIntent = ADD_ATTESTATION_INTENT
        QRCodeUtils(requireContext()).startQRScanner(
            this,
            promptText = resources.getString(R.string.text_scan_public_key_to_add_attestation),
            vertical = true
        )
    }

    private fun addKvkPoa() {
        val ipv8 = getIpv8()
        Log.i(TAG, "PUBLIC KEY TO POA DIALOG: "+ipv8.myPeer.publicKey.keyToBin().toHex() )
        IdentityAddKvkPoaDialog(ipv8.myPeer.publicKey.keyToBin().toHex()).show(parentFragmentManager, tag)
    }

    private fun addPoa() {
        IdentityAddPoaDialog().show(parentFragmentManager, tag)
    }

    private fun issuePoa() {
        scanIntent = ISSUE_POA_INTENT
        QRCodeUtils(requireContext()).startQRScanner(
            this,
            promptText = resources.getString(R.string.text_scan_public_key_to_issue_poa),
            vertical = true
        )
    }

    private fun updateAttestations() {
        val oldCount = adapterAttestations.itemCount
        val itemsAttestations = getAttestationCommunity().database.getAllAttestations()

        if (oldCount != itemsAttestations.size) {
            adapterAttestations.updateItems(
                createAttestationItems(itemsAttestations)
            )

//            binding.rvRecyclerViewYourPoas.setItemViewCacheSize(itemsAttestations.size)
        }

        toggleVisibility()
    }

    private fun deleteAttestation(attestation: AttestationItem) {
        ConfirmDialog(
            resources.getString(
                R.string.text_confirm_delete,
                resources.getString(R.string.text_this_attestation)
            )
        ) { dialog ->
            try {
                getAttestationCommunity().database.deleteAttestationByHash(attestation.attestationBlob.attestationHash)
                updateAttestations()

                dialog.dismiss()
                parentActivity.displayToast(
                    requireContext(),
                    resources.getString(R.string.snackbar_attestation_remove_success)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                parentActivity.displayToast(
                    requireContext(),
                    resources.getString(R.string.snackbar_attestation_remove_error)
                )
            }
        }
            .show(parentFragmentManager, tag)
    }

    private fun addIdentityAttribute() {
        IdentityAttributeDialog(null).show(parentFragmentManager, tag)
    }

    private fun deleteIdentityAttribute(attribute: IdentityAttribute) {
        ConfirmDialog(
            resources.getString(
                R.string.text_confirm_delete,
                resources.getString(R.string.text_this_attribute)
            )
        ) { dialog ->
            try {
                getIdentityStore().deleteAttribute(attribute)

                activity?.invalidateOptionsMenu()
                dialog.dismiss()

                parentActivity.displayToast(
                    requireContext(),
                    resources.getString(R.string.snackbar_identity_attribute_remove_success)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                parentActivity.displayToast(
                    requireContext(),
                    resources.getString(R.string.snackbar_identity_attribute_remove_error)
                )
            }
        }
            .show(parentFragmentManager, tag)
    }

    private fun addAuthority() {
        scanIntent = ADD_AUTHORITY_INTENT
        QRCodeUtils(requireContext()).startQRScanner(
            this,
            promptText = resources.getString(R.string.text_scan_public_key_to_add_authority),
            vertical = true
        )
    }

    private fun identityImageIntent() {
        val mimeTypes = arrayOf("image/*")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(
            Intent.createChooser(
                intent,
                resources.getString(R.string.text_send_photo_video)
            ),
            PICK_IDENTITY_IMAGE
        )
    }

    private fun createAttributeItems(attributes: List<IdentityAttribute>): List<Item> {
        return attributes.map { attribute ->
            IdentityAttributeItem(attribute)
        }
    }

    private fun createPoaItems(poas: List<PowerOfAttorney>): List<Item> {
        return poas.map { poa ->
            PoaItem(poa)
        }
    }

    private fun createAttestationItems(attestations: List<AttestationBlob>): List<Item> {
        return attestations
            .map { blob ->
                AttestationItem(blob)
            }
            .sortedBy {
                if (it.attestationBlob.metadata != null) {
                    return@sortedBy JSONObject(it.attestationBlob.metadata!!).optString(QRScanController.KEY_ATTRIBUTE)
                } else {
                    return@sortedBy ""
                }
            }
    }


    private fun createIdentityItems(identities: List<Identity>, imageString: String?): List<Item> {
        return identities.map { identity ->
            IdentityItem(
                identity,
                imageString?.let { decodeImage(it) },
                false
            )
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IDENTITY_IMAGE) {
                if (data != null) {
                    data.data?.let { uri ->
                        val bitmap = if (Build.VERSION.SDK_INT >= 29) {
                            val source = ImageDecoder.createSource(parentActivity.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(parentActivity.contentResolver, uri)
                        }

                        appPreferences.setIdentityFace(encodeImage(bitmap))
                    }
                }
            } else {
                QRCodeUtils(requireContext()).parseActivityResult(requestCode, resultCode, data)
                    ?.let { result ->
                        try {
                            val obj = JSONObject(result)

                            if (obj.has(QRScanController.KEY_PUBLIC_KEY)) {
                                try {
                                    defaultCryptoProvider.keyFromPublicBin(
                                        obj.optString(
                                            QRScanController.KEY_PUBLIC_KEY
                                        ).hexToBytes()
                                    )
                                    val publicKey = obj.optString(QRScanController.KEY_PUBLIC_KEY)

                                    when (scanIntent) {
                                        ADD_ATTESTATION_INTENT -> getQRScanController().addAttestation(
                                            publicKey
                                        )
                                        ADD_AUTHORITY_INTENT -> getQRScanController().addAuthority(
                                            publicKey
                                        )
                                        ISSUE_POA_INTENT -> getQRScanController().issuePoa(
                                            publicKey
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    parentActivity.displayToast(
                                        requireContext(),
                                        resources.getString(R.string.snackbar_invalid_public_key)
                                    )
                                }
                            } else {
                                parentActivity.displayToast(
                                    requireContext(),
                                    resources.getString(R.string.snackbar_no_public_key_found)
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            parentActivity.displayToast(
                                requireContext(),
                                resources.getString(R.string.snackbar_qr_code_not_json_format)
                            )
                        }
                    }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

//    TODO: implement
    private fun updatePoas(){

    }

    private fun addToList(title: String, image: Int) {
        titlesList.add(title)
        imagesList.add(image)
    }

    private fun postToList(){
        for (i in 1..3) {
            addToList("Title $i", R.drawable.ic_bug_report_black_24dp)
            Log.i(TAG, "Added $i to list")
        }
    }

    @SuppressLint("NewApi")
    private fun issueFakePoa() {
        val identity = getIdentityCommunity().getIdentity()!!
        val ipv8 = getIpv8()
        val myPublicKey = ipv8.myPeer.publicKey.keyToBin().toHex()
        Log.i(TAG, "FAKE POA ISSUED INITIALIZED")
        val fakePoa = PowerOfAttorney(
            id = UUID.randomUUID().toString(),
            kvkNumber = 12345678,
            companyName = "Xaigis B.V.",
            poaType = "FAKE Root Power of Attorney",
            isPermitted = "YES",
            isAllowedToIssuePoa = "YES",
            publicKeyPoaHolder = myPublicKey,
            givenNamesPoaHolder = identity.content.givenNames,
            surnamePoaHolder = identity.content.surname,
            dateOfBirthPoaHolder = getDateOfBirth(identity),
            publicKeyPoaIssuer = "A",
            givenNamesPoaIssuer = "JAN",
            surnamePoaIssuer = "JANSEN",
            dateOfBirthPoaIssuer = "20-01-2000"
        )
        Log.i(TAG, "dateOfBirthPoaHolder in fake POA: "+identity.content.dateOfBirth.toString())
        val poaCommunity = IPv8Android.getInstance().getOverlay<PowerofAttorneyCommunity>()!!

        poaCommunity.addFakePoa(fakePoa)
        Log.i(TAG, fakePoa.toString())
    }


    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun getDateOfBirth(identity: Identity): String {
            val fullDateOfBirthSeperated = identity.content.dateOfBirth.toString().split("\\s".toRegex()).toTypedArray()
            return fullDateOfBirthSeperated[2] + "-" + convertMonthToNumber(fullDateOfBirthSeperated[1].uppercase()) + "-" + fullDateOfBirthSeperated[5]
        }
        private fun convertMonthToNumber(month: String): String {
            when (month) {
                "JANUARY" -> return "01"
                "FEBRUARY" -> return "02"
                "MARCH" -> return "03"
                "APRIL" -> return "04"
                "MAY" -> return "05"
                "JUNE" -> return "06"
                "JULY" -> return "07"
                "AUGUST" -> return "08"
                "SEPTEMBER" -> return "09"
                "OCTOBER" -> return "10"
                "NOVEMBER" -> return "11"
                "DECEMBER" -> return "12"
                else -> {
                    Log.e("PoaCommunity", "Month could not be converted to number!")
                    return "00"
                }
            }
        }

        private const val ADD_ATTESTATION_INTENT = 0
        private const val ADD_AUTHORITY_INTENT = 1
        private const val PICK_IDENTITY_IMAGE = 2
        private const val ISSUE_POA_INTENT = 3
        private const val MENU_ITEM_OPTIONS = 1234
    }
}
