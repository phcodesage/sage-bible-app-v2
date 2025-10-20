@file:Suppress("DEPRECATION")
package com.example.sage_bible_kotlin.ui.reader

import android.content.Intent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseInteractiveRow(
    number: Int,
    text: String,
    book: String,
    chapter: Int,
    translationLabel: String
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard: ClipboardManager = LocalClipboardManager.current
    var showActions by remember { mutableStateOf(false) }
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showActions = true }
            )
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$number",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text.trim(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Visible
        )
    }

    if (showActions) {
        ModalBottomSheet(onDismissRequest = { showActions = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$book $chapter:$number (${translationLabel})",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = {
                    val payload = "$book $chapter:$number (${translationLabel})\n\n${text.trim()}"
                    @Suppress("DEPRECATION")
                    clipboard.setText(AnnotatedString(payload))
                    showActions = false
                }) { Text("Copy (with reference)") }
                Button(onClick = {
                    val ref = "$book $chapter:$number (${translationLabel})"
                    @Suppress("DEPRECATION")
                    clipboard.setText(AnnotatedString(ref))
                    showActions = false
                }) { Text("Copy reference only") }
                Button(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "$book $chapter:$number (${translationLabel})\n\n${text.trim()}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share verse"))
                    showActions = false
                }) { Text("Share") }
                Button(onClick = { showActions = false }) { Text("Close") }
            }
        }
    }
}
