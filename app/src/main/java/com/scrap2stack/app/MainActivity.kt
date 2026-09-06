package com.scrap2stack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.scrap2stack.app.core.navigation.NavGraph
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.ui.theme.Scrap2StackTheme

/**
 * Scrap2Stack - Android Application Entry Point
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Retrofit with context for SessionManager
        RetrofitClient.getApiService(this)
        
        enableEdgeToEdge()
        
        setContent {
            Scrap2StackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
