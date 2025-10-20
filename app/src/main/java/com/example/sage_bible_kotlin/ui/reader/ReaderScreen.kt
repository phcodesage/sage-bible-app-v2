package com.example.sage_bible_kotlin.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sage_bible_kotlin.data.BibleData
import com.example.sage_bible_kotlin.data.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    translation: String,
    book: String,
    chapter: Int,
    padding: PaddingValues,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repo = remember { BibleRepository(context) }

    var currentTranslation by remember { mutableStateOf(translationToEnum(translation)) }
    var bibleData by remember { mutableStateOf<BibleData?>(null) }
    var currentBook by remember { mutableStateOf(book) }
    var currentChapter by remember { mutableStateOf(chapter) }
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentTranslation) {
        bibleData = try { withContext(Dispatchers.IO) { repo.loadTranslation(currentTranslation) } } catch (_: Throwable) { null }
    }

    val chapterContent = remember(bibleData, currentBook, currentChapter) {
        if (bibleData != null) repo.getChapterContent(bibleData!!, currentBook, currentChapter) else null
    }

    val listState = rememberLazyListState()
    var pendingScrollToVerse by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(pendingScrollToVerse) {
        pendingScrollToVerse?.let { v ->
            val index = (v - 1).coerceAtLeast(0)
            listState.scrollToItem(index)
            pendingScrollToVerse = null
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Go to")
                    }
                    IconButton(
                        onClick = {
                            if (bibleData != null) {
                                val chapters = repo.getChapters(bibleData!!, currentBook)
                                val idx = chapters.indexOf(currentChapter)
                                if (idx > 0) currentChapter = chapters[idx - 1]
                            }
                        }
                    ) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Chapter") }
                    IconButton(
                        onClick = {
                            if (bibleData != null) {
                                val chapters = repo.getChapters(bibleData!!, currentBook)
                                val idx = chapters.indexOf(currentChapter)
                                if (idx >= 0 && idx < chapters.lastIndex) currentChapter = chapters[idx + 1]
                            }
                        }
                    ) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next Chapter") }
                    IconButton(onClick = { /* Text size dialog */ }) {
                        Icon(Icons.Filled.FormatSize, contentDescription = "Text Size")
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = buildSectionTitle(currentBook, currentChapter),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (chapterContent == null) {
                Text(
                    text = "Unable to load content.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chapterContent.verses) { v ->
                        VerseRow(number = v.verse, text = v.text)
                    }
                }
            }
        }
    }

    if (showPicker && bibleData != null) {
        PassagePickerSheet(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassagePickerSheet(
    repo: com.example.sage_bible_kotlin.data.BibleRepository,
    data: com.example.sage_bible_kotlin.data.BibleData,
    initialTranslation: com.example.sage_bible_kotlin.data.BibleRepository.Translation,
    initialBook: String,
    initialChapter: Int,
    onDismiss: () -> Unit,
    onConfirm: (com.example.sage_bible_kotlin.data.BibleRepository.Translation, String, Int, Int?) -> Unit
) {
    var translation by remember { mutableStateOf(initialTranslation) }
    var book by remember { mutableStateOf(initialBook) }
    var chapter by remember { mutableStateOf(initialChapter) }
    var verse: Int? by remember { mutableStateOf(null) }

    val books = remember(data) { data.books.map { it.name } }
    val chapters = remember(data, book) { repo.getChapters(data, book) }
    val verses = remember(data, book, chapter) {
        data.books.firstOrNull { it.name == book }
            ?.chapters?.firstOrNull { it.chapter == chapter }
            ?.verses?.map { it.verse } ?: emptyList()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Go to", style = MaterialTheme.typography.titleMedium)
            TranslationDropdown(value = translation, onChange = { translation = it })
            BookDropdown(books = books, selected = book, onChange = { sel -> sel?.let { book = it; chapter = repo.getChapters(data, it).firstOrNull() ?: 1; verse = null } })
            ChapterDropdown(chapters = chapters, selected = chapter, onChange = { ch -> ch?.let { chapter = it; verse = null } })
            VerseDropdown(verses = verses, selected = verse, onChange = { v -> verse = v })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.Button(onClick = onDismiss) { Text("Cancel") }
                androidx.compose.material3.Button(onClick = { onConfirm(translation, book, chapter, verse) }) { Text("Go") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslationDropdown(
    value: com.example.sage_bible_kotlin.data.BibleRepository.Translation,
    onChange: (com.example.sage_bible_kotlin.data.BibleRepository.Translation) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        TextField(
            readOnly = true,
            value = value.label,
            onValueChange = {},
            label = { Text("Translation") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            com.example.sage_bible_kotlin.data.BibleRepository.Translation.entries.forEach { t ->
                DropdownMenuItem(text = { Text(t.label) }, onClick = { onChange(t); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDropdown(
    books: List<String>,
    selected: String?,
    onChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        TextField(
            readOnly = true,
            value = selected ?: "",
            onValueChange = {},
            label = { Text("Book") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            books.forEach { name ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onChange(name); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterDropdown(
    chapters: List<Int>,
    selected: Int?,
    onChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        TextField(
            readOnly = true,
            value = selected?.toString() ?: "",
            onValueChange = {},
            label = { Text("Chapter") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            chapters.forEach { ch ->
                DropdownMenuItem(text = { Text(ch.toString()) }, onClick = { onChange(ch); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseDropdown(
    verses: List<Int>,
    selected: Int?,
    onChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        TextField(
            readOnly = true,
            value = selected?.toString() ?: "",
            onValueChange = {},
            label = { Text("Verse (optional)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            verses.forEach { v ->
                DropdownMenuItem(text = { Text(v.toString()) }, onClick = { onChange(v); expanded = false })
            }
        }
    }
}
