package com.jeiel.daddyheartjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.jeiel.daddyheartjournal.ui.screen.MainAppContainer
import com.jeiel.daddyheartjournal.ui.theme.DaddyHeartJournalTheme
import com.jeiel.daddyheartjournal.ui.viewmodel.MainViewModel
import com.jeiel.daddyheartjournal.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaddyHeartJournalTheme {
                MainAppContainer(viewModel = mainViewModel)
            }
        }
    }
}


