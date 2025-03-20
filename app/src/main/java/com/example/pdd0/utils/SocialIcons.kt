package com.example.pdd0.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pdd0.R


fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Composable
fun SocialIcons() {
    val context = LocalContext.current

    // Оборачиваем в Box для выравнивания
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp), // Можно настроить отступы по вашему желанию
        contentAlignment = Alignment.Center // Центрируем все элементы внутри Box
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Twitter
            IconButton(onClick = { openUrl(context, "https://twitter.com") }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_twitter), // Замените на свой ресурс
                    contentDescription = "Twitter",
                    modifier = Modifier.size(40.dp) // ✅ Фиксированный размер
                )
            }

            // ✅ YouTube
            IconButton(onClick = { openUrl(context, "https://youtube.com") }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_youtube),
                    contentDescription = "YouTube",
                    modifier = Modifier.size(40.dp)
                )
            }

            // ✅ Instagram
            IconButton(onClick = { openUrl(context, "https://instagram.com") }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_instagram),
                    contentDescription = "Instagram",
                    modifier = Modifier.size(40.dp)
                )
            }

            // ✅ Telegram
            IconButton(onClick = { openUrl(context, "https://t.me") }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_telegram),
                    contentDescription = "Telegram",
                    modifier = Modifier.size(40.dp)
                )
            }

            // ✅ book
            IconButton(onClick = { openUrl(context, "https://dosaaf.net/txt_pdd.html") }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pdd),
                    contentDescription = "book",
                    modifier = Modifier.size(40.dp)
                )
            }


        }
    }
}