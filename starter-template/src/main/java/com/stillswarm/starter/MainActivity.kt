package com.stillswarm.starter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.StillswarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StillswarmTheme {
                StillswarmAppShell { padding ->
                    Column(Modifier.padding(padding).padding(24.dp)) {
                        Text("Your next app", style = MaterialTheme.typography.headlineSmall)
                        Text("Replace this screen with your first feature.", modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
        }
    }
}
