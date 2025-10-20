package com.example.sage_bible_kotlin.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sage_bible_kotlin.data.BibleData
import com.example.sage_bible_kotlin.data.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    translation: String,
    book: String,
    chapter: Int,
    padding: PaddingValues,
    initialVerse: Int? = null
) {
    val context = LocalContext.current
    val repo = remember { BibleRepository(context) }

    var currentTranslation by remember { mutableStateOf(translationToEnum(translation)) }
    var bibleData by remember { mutableStateOf<BibleData?>(null) }
    var currentBook by remember { mutableStateOf(book) }
    var currentChapter by remember { mutableStateOf(chapter) }
    var showPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentTranslation) {
        isLoading = true
        bibleData = try { withContext(Dispatchers.IO) { repo.loadTranslation(currentTranslation) } } catch (_: Throwable) { null }
        isLoading = false
    }

    val chapterContent = remember(bibleData, currentBook, currentChapter) {
        if (bibleData != null) repo.getChapterContent(bibleData!!, currentBook, currentChapter) else null
    }

    val listState = rememberLazyListState()
    var pendingScrollToVerse by remember { mutableStateOf<Int?>(null) }
    var flashVerse by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(pendingScrollToVerse) {
        pendingScrollToVerse?.let { v ->
            val index = (v - 1).coerceAtLeast(0)
            listState.scrollToItem(index)
            pendingScrollToVerse = null
        }
    }

    LaunchedEffect(initialVerse, currentBook, currentChapter) {
        if (initialVerse != null) {
            pendingScrollToVerse = initialVerse
            flashVerse = initialVerse
            delay(1200)
            flashVerse = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${currentBook} ${currentChapter} - ${currentTranslation.label}",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Go to")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 8.dp).padding(top = 0.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = buildSectionTitle(currentBook, currentChapter),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (isLoading || bibleData == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else if (chapterContent == null) {
                Text(
                    text = "Unable to load content.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(chapterContent.verses) { v ->
                        VerseInteractiveRow(
                            number = v.verse,
                            text = v.text,
                            book = currentBook,
                            chapter = currentChapter,
                            translationLabel = currentTranslation.label,
                            flash = flashVerse == v.verse
                        )
                    }
                }
                // Bottom navigation for previous/next chapters
                val chapters = repo.getChapters(bibleData!!, currentBook)
                val idx = chapters.indexOf(currentChapter)
                val prevChapter = if (idx > 0) chapters[idx - 1] else null
                val nextChapter = if (idx >= 0 && idx < chapters.lastIndex) chapters[idx + 1] else null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            prevChapter?.let {
                                currentChapter = it
                                pendingScrollToVerse = 1
                            }
                        },
                        enabled = prevChapter != null,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Chapter")
                        Text(text = prevChapter?.toString() ?: "—", modifier = Modifier.padding(start = 4.dp))
                    }
                    Button(
                        onClick = {
                            nextChapter?.let {
                                currentChapter = it
                                pendingScrollToVerse = 1
                            }
                        },
                        enabled = nextChapter != null,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(text = nextChapter?.toString() ?: "—", modifier = Modifier.padding(end = 4.dp))
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Chapter")
                    }
                }
            }
        }
    }

    if (showPicker && bibleData != null) {
        ReaderPassagePickerSheet(
            repo = repo,
            data = bibleData!!,
            initialTranslation = currentTranslation,
            initialBook = currentBook,
            initialChapter = currentChapter,
            onDismiss = { showPicker = false },
            onConfirm = { t: BibleRepository.Translation, b: String, c: Int, v: Int? ->
                if (t != currentTranslation) {
                    currentTranslation = t
                }
                currentBook = b
                currentChapter = c
                pendingScrollToVerse = v
                showPicker = false
            }
        )
    }
}

@Composable
private fun VerseRow(number: Int, text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$number",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text.trim(),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun translationToEnum(value: String): BibleRepository.Translation = when (value.uppercase()) {
    BibleRepository.Translation.KJV.name -> BibleRepository.Translation.KJV
    BibleRepository.Translation.AKJV.name -> BibleRepository.Translation.AKJV
    BibleRepository.Translation.CEB.name -> BibleRepository.Translation.CEB
    else -> BibleRepository.Translation.KJV
}

private fun buildSectionTitle(book: String, chapter: Int): String = "$book $chapter"
