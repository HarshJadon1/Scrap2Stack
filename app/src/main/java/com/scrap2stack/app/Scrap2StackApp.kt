package com.scrap2stack.app

import android.app.Application
import com.scrap2stack.app.core.network.RetrofitClient

class Scrap2StackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize networking singleton globally
        RetrofitClient.getApiService(this)
    }
}
