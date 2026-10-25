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
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StillswarmTheme {
                StillswarmAppShell { padding ->
                    Column(
                        modifier = Modifier.padding(padding).padding(Dimens.xlarge),
                        content = {
                            Text(
                                text = "Your next app",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(
                                text = "Replace this screen with your first feature.",
                                modifier = Modifier.padding(top = Dimens.medium),
                            )
                        },
                    )
                }
            }
        }
    }
}
