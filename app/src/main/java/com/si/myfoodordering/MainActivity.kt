package com.si.myfoodordering

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.si.myfoodordering.push.OrderRealtimeManager
import com.si.myfoodordering.ui.navigation.SetupNavGraph
import com.si.myfoodordering.ui.theme.MyFoodOrderingTheme
import com.si.myfoodordering.ui.viewmodel.AuthViewModel
import com.si.myfoodordering.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var orderRealtimeManager: OrderRealtimeManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* refused : notifications désactivées jusqu'aux paramètres système */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            val darkTheme by themeViewModel.isDarkTheme.collectAsState()
            val userProfile by authViewModel.userProfile.collectAsState()

            LaunchedEffect(userProfile) {
                val profile = userProfile
                if (profile != null && profile.role != "admin") {
                    orderRealtimeManager.startListening(profile.id)
                } else {
                    orderRealtimeManager.stopListening()
                }
            }

            MyFoodOrderingTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SetupNavGraph()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        orderRealtimeManager.stopListening()
    }
}
