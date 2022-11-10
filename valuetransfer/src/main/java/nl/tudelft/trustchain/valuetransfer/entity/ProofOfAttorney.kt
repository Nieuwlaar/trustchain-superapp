package nl.tudelft.trustchain.valuetransfer.entity

import nl.tudelft.ipv8.keyvault.PublicKey
import java.util.*

data class ProofOfAttorney(
    /**
     * KVK number of the Proof of Attorney.
     */
    val kvkNumber: Int,

    /**
     * Is the Proof of Attorney issued from a root of trust.
     * Options:
     * No, KVK or EBSI
     */
    val issuedFromRootOfTrust: String,

    /**
     * The type of Proof of Attorney
     */
    val poaType: String,

    /**
     * Is the holder allowed to issue Proof of Attorney.
     */
    val allowedToIssuePoa: Boolean,

    /**
     * Public key of the holder of the Proof of Attorney.
     */
    val publicKeyPoaHolder: PublicKey,

    /**
     * Given Names of the Proof of Attorney holder.
     */
    var givenNamesPoaHolder: String,

    /**
     * Surname  of the Proof of Attorney holder.
     */
    var surnamePoaHolder: String,

    /**
     * Date of birth of the Proof of Attorney holder.
     */
    var dateOfBirthPoaHolder: Date,

    /**
     * Public key of the issuer of the Proof of Attorney.
     */
    val publicKeyPoaIssuer: PublicKey,

    /**
     * Given Names of the Proof of Attorney issuer.
     */
    var givenNamesPoaIssuer: String,

    /**
     * Surname  of the Proof of Attorney issuer.
     */
    var surnamePoaIssuer: String,

    /**
     * Date of birth of the Proof of Attorney issuer.
     */
    var dateOfBirthPoaIssuer: Date
) {
//    override fun toString(): String {
//        return "$givenNames $surname ($gender) born on $dateOfBirth as $nationality. Personal number $personalNumber, document number $documentNumber. Identity is verified: $verified. Expires on $dateOfExpiry"
//    }
}
