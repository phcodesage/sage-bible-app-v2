@file:Suppress("DEPRECATION")
package com.example.sage_bible_kotlin.ui.reader

import android.content.Intent
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
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
import com.example.sage_bible_kotlin.data.BookmarkRepository
import com.example.sage_bible_kotlin.data.HighlightRepository
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VerseInteractiveRow(
    number: Int,
    text: String,
    book: String,
    chapter: Int,
    translationLabel: String,
    flash: Boolean = false
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard: ClipboardManager = LocalClipboardManager.current
    var showActions by remember { mutableStateOf(false) }
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var highlightHex by remember(context, translationLabel, book, chapter, number) {
        mutableStateOf(HighlightRepository.get(context, translationLabel, book, chapter, number))
    }
    var isBookmarked by remember(context, translationLabel, book, chapter, number) {
        mutableStateOf(
            BookmarkRepository.list(context).any {
                it.translation == translationLabel && it.book == book && it.chapter == chapter && it.verse == number
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showActions = true }
            )
            .background(
                color = when {
                    highlightHex != null -> Color(AndroidColor.parseColor(highlightHex))
                    flash -> Color(AndroidColor.parseColor("#99FFF59D")) // temporary glow-like background
                    else -> Color.Transparent
                }
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
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        if (isBookmarked) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = "Bookmarked",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp)
            )
        }
    }

    if (showActions) {
        ModalBottomSheet(onDismissRequest = { showActions = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$book $chapter:$number (${translationLabel})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Highlight", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                val palette = listOf(
                    "#66FFF59D", // yellow (alpha 0x66)
                    "#66A5D6A7", // green
                    "#6690CAF9", // blue
                    "#66F48FB1", // teal
                    "#66FFAB91", // orange
                    "#66CE93D8"  // purple
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    palette.forEach { hex ->
                        Row(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(AndroidColor.parseColor(hex)), shape = MaterialTheme.shapes.small)
                                .combinedClickable(
                                    onClick = {
                                        HighlightRepository.set(context, translationLabel, book, chapter, number, hex)
                                        highlightHex = hex
                                        showActions = false
                                    },
                                    onLongClick = {}
                                )
                        ) {}
                    }
                }
                if (highlightHex != null) {
                    Button(onClick = {
                        HighlightRepository.remove(context, translationLabel, book, chapter, number)
                        highlightHex = null
                        showActions = false
                    }) { Text("Remove highlight") }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isBookmarked) {
                        Button(onClick = {
                            BookmarkRepository.add(
                                context,
                                BookmarkRepository.Bookmark(
                                    translation = translationLabel,
                                    book = book,
                                    chapter = chapter,
                                    verse = number,
                                    text = text.trim(),
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            isBookmarked = true
                            showActions = false
                        }) { Text("Bookmark") }
                    } else {
                        Button(onClick = {
                            val existing = BookmarkRepository.list(context).firstOrNull {
                                it.translation == translationLabel && it.book == book && it.chapter == chapter && it.verse == number
                            }
                            if (existing != null) {
                                BookmarkRepository.remove(context, existing)
                                isBookmarked = false
                            }
                            showActions = false
                        }) { Text("Remove bookmark") }
                    }

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
}
