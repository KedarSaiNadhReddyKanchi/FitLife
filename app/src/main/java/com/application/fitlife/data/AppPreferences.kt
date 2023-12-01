import android.content.Context

class AppPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

    fun isDataLoaded(): Boolean {
        return sharedPreferences.getBoolean("data_loaded", false)
    }

    fun setDataLoaded(isLoaded: Boolean) {
        sharedPreferences.edit().putBoolean("data_loaded", isLoaded).apply()
    }
}