package nl.tudelft.trustchain.valuetransfer.entity

import java.time.LocalDate

data class PowerOfAttorney(
    /**
     * KVK number of the Power of Attorney.
     */
    val kvkNumber: Int,

    /**
     * Is the Power of Attorney issued from a root of trust.
     * Options:
     * No, KVK or EBSI
     */
    val issuedFromRootOfTrust: String,

    /**
     * The type of Power of Attorney
     */
    val poaType: String,

    /**
     * Is the holder allowed to issue Power of Attorney.
     */
    val allowedToIssuePoa: Boolean,

    /**
     * Public key of the holder of the Power of Attorney.
     */
    val publicKeyPoaHolder: ByteArray,

    /**
     * Given Names of the Power of Attorney holder.
     */
    var givenNamesPoaHolder: String,

    /**
     * Surname  of the Power of Attorney holder.
     */
    var surnamePoaHolder: String,

    /**
     * Date of birth of the Power of Attorney holder.
     */
    var dateOfBirthPoaHolder: LocalDate,

    /**
     * Public key of the issuer of the Power of Attorney.
     */
    val publicKeyPoaIssuer: ByteArray,

    /**
     * Given Names of the Power of Attorney issuer.
     */
    var givenNamesPoaIssuer: String,

    /**
     * Surname  of the Power of Attorney issuer.
     */
    var surnamePoaIssuer: String,

    /**
     * Date of birth of the Power of Attorney issuer.
     */
    var dateOfBirthPoaIssuer: LocalDate
) {
//    override fun toString(): String {
//        return "$givenNames $surname ($gender) born on $dateOfBirth as $nationality. Personal number $personalNumber, document number $documentNumber. Identity is verified: $verified. Expires on $dateOfExpiry"
//    }
}
