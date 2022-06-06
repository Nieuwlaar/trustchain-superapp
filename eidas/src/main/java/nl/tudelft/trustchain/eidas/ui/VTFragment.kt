package nl.tudelft.trustchain.eidas.ui

import androidx.annotation.LayoutRes
import nl.tudelft.ipv8.attestation.wallet.AttestationCommunity
import nl.tudelft.trustchain.common.contacts.ContactStore
import nl.tudelft.trustchain.common.eurotoken.GatewayStore
import nl.tudelft.trustchain.common.eurotoken.TransactionRepository
import nl.tudelft.trustchain.common.ui.BaseFragment
import nl.tudelft.trustchain.common.util.TrustChainHelper
import nl.tudelft.trustchain.eurotoken.community.EuroTokenCommunity
import nl.tudelft.trustchain.peerchat.community.PeerChatCommunity
import nl.tudelft.trustchain.peerchat.db.PeerChatStore
import nl.tudelft.trustchain.eidas.eidasMainActivity
import nl.tudelft.trustchain.eidas.community.IdentityCommunity
import nl.tudelft.trustchain.eidas.db.IdentityStore
import nl.tudelft.trustchain.eidas.ui.settings.AppPreferences
import nl.tudelft.trustchain.eidas.ui.settings.NotificationHandler
import nl.tudelft.trustchain.eidas.passport.PassportHandler

abstract class VTFragment(@LayoutRes contentLayoutId: Int = 0) : BaseFragment(contentLayoutId) {

    val parentActivity: eidasMainActivity by lazy {
        requireActivity() as eidasMainActivity
    }

    fun getIdentityCommunity(): IdentityCommunity {
        return parentActivity.getCommunity()
            ?: throw java.lang.IllegalStateException("IdentityCommunity is not configured")
    }

    fun getPeerChatCommunity(): PeerChatCommunity {
        return parentActivity.getCommunity()
            ?: throw java.lang.IllegalStateException("PeerChatCommunity is not configured")
    }

    fun getEuroTokenCommunity(): EuroTokenCommunity {
        return parentActivity.getCommunity()
            ?: throw java.lang.IllegalStateException("EuroTokenCommunity is not configured")
    }

    fun getAttestationCommunity(): AttestationCommunity {
        return parentActivity.getCommunity()
            ?: throw java.lang.IllegalStateException("AttestationCommunity is not configured")
    }

    fun getIdentityStore(): IdentityStore {
        return parentActivity.getStore()
            ?: throw java.lang.IllegalStateException("IdentityStore is not configured")
    }

    fun getPeerChatStore(): PeerChatStore {
        return parentActivity.getStore()
            ?: throw java.lang.IllegalStateException("PeerChatStore is not configured")
    }

    fun getGatewayStore(): GatewayStore {
        return parentActivity.getStore()
            ?: throw java.lang.IllegalStateException("GatewayStore is not configured")
    }

    fun getContactStore(): ContactStore {
        return parentActivity.getStore()
            ?: throw java.lang.IllegalStateException("ContactStore is not configured")
    }

    fun getTransactionRepository(): TransactionRepository {
        return parentActivity.getStore()
            ?: throw java.lang.IllegalStateException("TransactionRepository is not configured")
    }

    fun getTrustChainHelper(): TrustChainHelper {
        return parentActivity.getStore()
            ?: throw java.lang.IllegalStateException("TrustChainHelper is not configured")
    }

    fun getQRScanController(): QRScanController {
        return parentActivity.getQRScanController()
    }

    val notificationHandler: NotificationHandler by lazy {
        parentActivity.notificationHandler()
    }

    val passportHandler: PassportHandler by lazy {
        parentActivity.passportHandler()
    }

    val appPreferences: AppPreferences by lazy {
        parentActivity.appPreferences()
    }

    open fun initView() {}
}
