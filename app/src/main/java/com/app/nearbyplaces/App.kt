package com.app.nearbyplaces


import androidx.multidex.MultiDexApplication

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        SharedPreferenceHelper(this).init()
    }

    companion object {
        private var instance: App? = null

        fun getInstance(): App? {
            return instance
        }

    }


}