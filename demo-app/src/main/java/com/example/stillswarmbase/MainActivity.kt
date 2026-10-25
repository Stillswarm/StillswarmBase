package com.example.stillswarmbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.StillswarmColors
import com.stillswarm.base.ui.StillswarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ThemeDemo() }
    }
}

@Composable
private fun ThemeDemo(initialDark: Boolean = isSystemInDarkTheme()) {
    var dark by rememberSaveable { mutableStateOf(initialDark) }
    var branded by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    StillswarmTheme(
        darkTheme = dark,
        lightColorScheme = if (branded) StillswarmColors.Light.copy(
            primary = Color(0xFF245A45), onPrimary = Color.White,
        ) else StillswarmColors.Light,
        darkColorScheme = if (branded) StillswarmColors.Dark.copy(
            primary = Color(0xFFA3D3B8), onPrimary = Color(0xFF103828),
        ) else StillswarmColors.Dark,
    ) {
        StillswarmAppShell { padding ->
            Column(
                Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(Dimens.dp16),
                verticalArrangement = Arrangement.spacedBy(Dimens.dp16),
            ) {
                Text("Stillswarm Foundation", style = MaterialTheme.typography.titleLarge)
                Text("Manrope headings. Inter body text. Offline fonts and fixed palettes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Toggle("Dark appearance", dark) { dark = it }
                Toggle("App colour override", branded) { branded = it }
                Text("Your app", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { name = "Stillswarm" }) { Text("Use sample name") }
                StillswarmBaseTheme {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Column(Modifier.padding(Dimens.dp16), verticalArrangement = Arrangement.spacedBy(Dimens.dp12)) {
                            Text("Shared screen appearance", style = MaterialTheme.typography.titleMedium)
                            Text("Settings and About keep the neutral palette, even when the app above uses its own colours.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { branded = false }) { Text("Reset app colours") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = label })
    }
}

@Preview(name = "Base light", showBackground = true)
@Composable
private fun LightPreview() = ThemeDemo(initialDark = false)

@Preview(name = "Base dark", showBackground = true)
@Composable
private fun DarkPreview() = ThemeDemo(initialDark = true)
