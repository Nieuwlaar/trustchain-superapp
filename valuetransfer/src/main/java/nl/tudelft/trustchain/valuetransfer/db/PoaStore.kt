package nl.tudelft.trustchain.valuetransfer.db

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import nl.tudelft.valuetransfer.sqldelight.Database


class PoaStore(context: Context) {
    private val driver = AndroidSqliteDriver(Database.Schema, context, "poa-vt.db")
    private val database = Database(driver)

//    fun addPoa(poa: PowerOfAttorney) {
//
//    }

//    fun deleteAttribute(identityAttribute: IdentityAttribute) {
//        database.dbAttributeQueries.deleteAttribute(identityAttribute.id)
//    }


    companion object {
    }
}
