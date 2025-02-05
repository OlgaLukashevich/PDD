package com.example.pdd0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
//import androidx.compose.material3.icons.Icons
//import androidx.compose.material3.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.painterResource

class MainScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Поиск
        SearchBar()

        Spacer(modifier = Modifier.height(32.dp))

        // Заголовок
        Text(
            text = "ПДД РБ",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "изучаем",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки меню
        MenuButtons()

        Spacer(modifier = Modifier.height(32.dp))

        // Иконки социальных сетей
        SocialIcons()
    }
}

@Composable
fun SearchBar() {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Поиск билета") },
            modifier = Modifier.weight(1f),
            leadingIcon = {

                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { /* Handle Done action */ })
        )
    }
}

@Composable
fun MenuButtons() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MenuButton("Случайный билет")
        MenuButton("Все билеты")
        MenuButton("Избранные билеты")
        MenuButton("Экзамен")
    }
}

@Composable
fun MenuButton(text: String) {
    Button(
        onClick = { /* Handle button click */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SocialIcons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Пример иконок социальных сетей. Замените их на нужные.
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera), // Пример иконки
            contentDescription = "Icon 1"
        )
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera), // Пример иконки
            contentDescription = "Icon 2"
        )
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera), // Пример иконки
            contentDescription = "Icon 3"
        )
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera), // Пример иконки
            contentDescription = "Icon 4"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
