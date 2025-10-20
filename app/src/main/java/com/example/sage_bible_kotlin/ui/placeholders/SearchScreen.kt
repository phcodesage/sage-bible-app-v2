package com.example.sage_bible_kotlin.ui.placeholders

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SearchScreen(padding: PaddingValues) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Search (coming soon)", style = MaterialTheme.typography.bodyLarge)
    }
}
