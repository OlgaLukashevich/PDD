package com.example.pdd0

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pdd0.ui.theme.PDD0Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Переход на MainScreenActivity сразу после старта MainActivity
        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
        finish() // Завершаем MainActivity, чтобы не возвращаться к нему

        // Приложение сразу переходит на MainScreenActivity
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PDD0Theme {
        // Пример, но этот код больше не нужен, так как MainActivity не будет отображаться
        Text(text = "This is MainActivity, but it redirects instantly.")
    }
}
