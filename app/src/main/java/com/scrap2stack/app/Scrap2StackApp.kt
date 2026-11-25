package com.scrap2stack.app

import android.app.Application
import com.scrap2stack.app.core.network.GeminiConfig

class Scrap2StackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        GeminiConfig.GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY
    }
}
