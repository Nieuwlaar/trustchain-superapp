package nl.tudelft.trustchain.valuetransfer.db

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import nl.tudelft.trustchain.valuetransfer.entity.PowerOfAttorney
import nl.tudelft.valuetransfer.sqldelight.Database


class PoaStore(context: Context) {
    private val driver = AndroidSqliteDriver(Database.Schema, context, "identities-vt.db")
    private val database = Database(driver)

    fun addPoa(poa: PowerOfAttorney) {
        database.dbProofofAttorneyQueries.addPoa(
            poa.id,
            poa.kvkNumber,
            poa.companyName,
            poa.poaType,
            poa.givenNamesPoaHolder,
            poa.givenNamesPoaIssuer
        )
    }

    fun createPoasTable() {
        return database.dbProofofAttorneyQueries.createPoasTable()
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
