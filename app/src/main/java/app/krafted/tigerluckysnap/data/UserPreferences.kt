package app.krafted.tigerluckysnap.data

import android.content.Context

object UserPreferences {
    private const val PREFS_NAME = "tiger_prefs"
    private const val KEY_ONBOARDING_SEEN = "hasSeenOnboarding"

    fun hasSeenOnboarding(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_SEEN, false)

    fun setOnboardingSeen(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDING_SEEN, true).apply()
}
