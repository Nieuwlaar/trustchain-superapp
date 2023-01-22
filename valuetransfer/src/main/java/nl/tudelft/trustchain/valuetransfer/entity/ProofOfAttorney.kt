package nl.tudelft.trustchain.valuetransfer.entity

data class PowerOfAttorney(
    /**
     * ID of the Power of Attorney
     */
    val id: String,

    /**
     * ID of the Power of Attorney that issued this Power of Attorney
     */
    val id_issued_with: String,

    /**
     * KVK number of the Power of Attorney.
     */
    val kvkNumber: Long,

    /**
     * Company name
     */

    val companyName: String,

    /**
     * The type of Power of Attorney
     */
    val poaType: String,

    /**
     * Is the permitted to act in the company's name? (isBevoegd)
     */
    val isPermitted: String,

    /**
     * Is the holder allowed to issue Powers of Attorney.
     */
    val isAllowedToIssuePoa: String,

    /**
     * Public key of the holder of the Power of Attorney.
     */
    val publicKeyPoaHolder: String,

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
    var dateOfBirthPoaHolder: String,

    /**
     * Public key of the issuer of the Power of Attorney.
     */
    val publicKeyPoaIssuer: String,

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
    var dateOfBirthPoaIssuer: String
) {
//    override fun toString(): String {
//        return "$givenNames $surname ($gender) born on $dateOfBirth as $nationality. Personal number $personalNumber, document number $documentNumber. Identity is verified: $verified. Expires on $dateOfExpiry"
//    }
}
