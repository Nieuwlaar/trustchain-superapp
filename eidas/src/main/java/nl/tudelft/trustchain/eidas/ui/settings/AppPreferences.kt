package nl.tudelft.trustchain.eidas.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import nl.tudelft.trustchain.eidas.R
import nl.tudelft.trustchain.eidas.eidasMainActivity
import nl.tudelft.trustchain.eidas.util.md5

class AppPreferences(
    parentActivity: eidasMainActivity
) {

    private var sharedPreferences: SharedPreferences = parentActivity.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    fun getCurrentTheme(): String? {
        return sharedPreferences.getString(PREFS_THEME_NAME, APP_THEME_DAY)
    }

    fun setTheme(theme: String) {
        sharedPreferences.edit().putString(
            PREFS_THEME_NAME,
            theme
        ).apply()
    }

    fun switchTheme(theme: String?) {
        when (theme) {
            APP_THEME_DAY -> setThemeDay()
            APP_THEME_NIGHT -> setThemeNight()
            APP_THEME_SYSTEM -> setThemeSystem()
            else -> setThemeDay()
        }
    }

    fun setIdentityFace(encodedImage: String?) {
        sharedPreferences.edit().putString(
            PREFS_IDENTITY_FACE_NAME,
            encodedImage
        ).apply()
        sharedPreferences.edit().putString(
            PREFS_IDENTITY_FACE_HASH_NAME,
            encodedImage?.md5()
        ).apply()
    }

    fun getIdentityFace(): String? {
        return sharedPreferences.getString(PREFS_IDENTITY_FACE_NAME, "")
    }

    fun getIdentityFaceHash(): String? {
        return sharedPreferences.getString(PREFS_IDENTITY_FACE_HASH_NAME, null)
    }

    fun deleteIdentityFace() {
        sharedPreferences.edit().remove(PREFS_IDENTITY_FACE_NAME).apply()
        sharedPreferences.edit().remove(PREFS_IDENTITY_FACE_HASH_NAME).apply()
    }

    companion object {
        const val PREFS_FILE_NAME = "prefs_vt"
        const val PREFS_THEME_NAME = "theme"
        const val PREFS_IDENTITY_FACE_NAME = "identity_face"
        const val PREFS_IDENTITY_FACE_HASH_NAME = "identity_face_hash"

        val APP_THEME = R.style.Theme_eidas
        const val APP_THEME_DAY = "day"
        const val APP_THEME_NIGHT = "night"
        const val APP_THEME_SYSTEM = "system"

        private lateinit var instance: AppPreferences
        fun getInstance(parentActivity: eidasMainActivity): AppPreferences {
            if (!::instance.isInitialized) {
                instance = AppPreferences(parentActivity)
            }
            return instance
        }

        private fun setThemeDay() {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        private fun setThemeNight() {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        private fun setThemeSystem() {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
