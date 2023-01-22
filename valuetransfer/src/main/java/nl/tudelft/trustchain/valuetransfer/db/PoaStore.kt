package nl.tudelft.trustchain.valuetransfer.db

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import nl.tudelft.ipv8.IPv8
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.valuetransfer.sqldelight.Database


class PoaStore(context: Context) {
    private val driver = AndroidSqliteDriver(Database.Schema, context, "identities-vt.db")
    private val database = Database(driver)

    private val poaMapper = {
            id: String,
            id_issued_with: String,
            kvkNumber: Long,
            companyName: String,
            poaType: String,
            isPermitted: String,
            isAllowedToIssuePoa: String,
            publicKeyPoaHolder: String,
            givenNamesPoaHolder: String,
            surnamePoaHolder: String,
            dateOfBirthPoaHolder: String,
            publicKeyPoaIssuer: String,
            givenNamesPoaIssuer: String,
            surnamePoaIssuer: String,
            dateOfBirthPoaIssuer: String
        ->
        PowerOfAttorney(
            id,
            id_issued_with,
            kvkNumber,
            companyName,
            poaType,
            isPermitted,
            isAllowedToIssuePoa,
            publicKeyPoaHolder,
            givenNamesPoaHolder,
            surnamePoaHolder,
            dateOfBirthPoaHolder,
            publicKeyPoaIssuer,
            givenNamesPoaIssuer,
            surnamePoaIssuer,
            dateOfBirthPoaIssuer
        )
    }

    fun addPoa(poa: PowerOfAttorney) {
        database.dbPowerofAttorneyQueries.addPoa(
            poa.id,
            poa.id_issued_with,
            poa.kvkNumber,
            poa.companyName,
            poa.poaType,
            poa.isPermitted,
            poa.isAllowedToIssuePoa,
            poa.publicKeyPoaHolder,
            poa.givenNamesPoaHolder,
            poa.surnamePoaHolder,
            poa.dateOfBirthPoaHolder,
            poa.publicKeyPoaIssuer,
            poa.givenNamesPoaIssuer,
            poa.surnamePoaIssuer,
            poa.dateOfBirthPoaIssuer,
        )
    }

    fun getAllPoas(): Flow<List<PowerOfAttorney>> {
        return database.dbPowerofAttorneyQueries.getAllPoas(poaMapper)
            .asFlow().mapToList()
    }

    fun getAllYourPoas(): Flow<List<PowerOfAttorney>> {
        val ipv8 = getIpv8()
        val myPublicKey = ipv8.myPeer.publicKey.keyToBin().toHex()
        return database.dbPowerofAttorneyQueries.getAllYourPoas(myPublicKey,poaMapper)
            .asFlow().mapToList()
    }

    fun getAllYourPoasWithDelegation(): List<PowerOfAttorney> {
        val ipv8 = getIpv8()
        val myPublicKey = ipv8.myPeer.publicKey.keyToBin().toHex()
//        TODO: test this
        val PoAlist = database.dbPowerofAttorneyQueries.getAllYourPoasWithDelegation(myPublicKey, "Root",poaMapper).executeAsList()
            .toMutableList()
        PoAlist += database.dbPowerofAttorneyQueries.getAllYourPoasWithDelegation(myPublicKey, "Manager",poaMapper).executeAsList()
        return PoAlist
    }

    fun getAllIssuedPoas(): Flow<List<PowerOfAttorney>> {
        val ipv8 = getIpv8()
        val myPublicKey = ipv8.myPeer.publicKey.keyToBin().toHex()
        return database.dbPowerofAttorneyQueries.getAllIssuedPoas(myPublicKey, poaMapper)
            .asFlow().mapToList()
    }

    fun deletePoa(poa_id: String) {
        database.dbPowerofAttorneyQueries.deletePoa(poa_id)
    }

    fun deleteAllPoas() {
        return database.dbPowerofAttorneyQueries.deleteAllPoas()
    }

    fun getIpv8(): IPv8 {
        return IPv8Android.getInstance()
    }

    fun createPoasTable() {
        return database.dbPowerofAttorneyQueries.createPoasTable()
    }

    fun createRevokedPoasTable() {
        return database.dbRevokedPoasQueries.createRevokedPoasTable()
    }

    fun addRevokedPoa(revokedPoaID: String) {
        database.dbRevokedPoasQueries.addRevokedPoa(revokedPoaID)
    }

    fun getAllRevokedPoas(): List<String> {
        val RevokedPoAlist = database.dbRevokedPoasQueries.getAllRevokedPoas().executeAsList()
            .toMutableList()
        return RevokedPoAlist
    }


    fun deletePoasTable() {
        return database.dbPowerofAttorneyQueries.deletePoasTable()
    }


//    fun deleteAttribute(identityAttribute: IdentityAttribute) {
//        database.dbAttributeQueries.deleteAttribute(identityAttribute.id)
//    }




    companion object {
        private lateinit var instance: PoaStore
        fun getInstance(context: Context): PoaStore {
            if (!::instance.isInitialized) {
                instance = PoaStore(context)
            }
            return instance
        }
    }
}
