package com.example.sage_bible_kotlin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sage_bible_kotlin.data.BibleData
import com.example.sage_bible_kotlin.data.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreen() {
    val context = LocalContext.current
    val repo = remember { BibleRepository(context) }

    var translation by remember { mutableStateOf(BibleRepository.Translation.KJV) }
    var bibleData by remember { mutableStateOf<BibleData?>(null) }
    var selectedBook by remember { mutableStateOf<String?>(null) }
    var selectedChapter by remember { mutableStateOf<Int?>(null) }

    // Load translation when selection changes
    LaunchedEffect(translation) {
        bibleData = try {
            withContext(Dispatchers.IO) { repo.loadTranslation(translation) }
        } catch (t: Throwable) {
            null
        }
        selectedBook = bibleData?.books?.firstOrNull()?.name
        selectedChapter = bibleData?.books?.firstOrNull()?.chapters?.firstOrNull()?.chapter
    }

    val books = remember(bibleData) { bibleData?.books?.map { it.name }.orEmpty() }
    val chapters = remember(bibleData, selectedBook) {
        if (bibleData != null && selectedBook != null) repo.getChapters(bibleData!!, selectedBook!!) else emptyList()
    }
    val chapterContent = remember(bibleData, selectedBook, selectedChapter) {
        if (bibleData != null && selectedBook != null && selectedChapter != null) {
            repo.getChapterContent(bibleData!!, selectedBook!!, selectedChapter!!)
        } else null
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Bible Reader") })
    }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                TranslationDropdown(
                    value = translation,
                    onChange = { translation = it },
                    modifier = Modifier.weight(1f)
                )
                BookDropdown(
                    books = books,
                    selected = selectedBook,
                    onChange = {
                        selectedBook = it
                        selectedChapter = chapters.firstOrNull()
                    },
                    modifier = Modifier.weight(1f)
                )
                ChapterDropdown(
                    chapters = chapters,
                    selected = selectedChapter,
                    onChange = { selectedChapter = it },
                    modifier = Modifier.weight(1f)
                )
            }

            if (chapterContent == null) {
                Text(
                    text = "Unable to load content. Ensure JSON files are in app/src/main/assets.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chapterContent.verses) { v ->
                        VerseRow(number = v.verse, text = v.text)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseRow(number: Int, text: String) {
    Text(
        text = "$number. ${text.trim()}",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslationDropdown(
    value: BibleRepository.Translation,
    onChange: (BibleRepository.Translation) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        TextField(
            readOnly = true,
            value = value.label,
            onValueChange = {},
            label = { Text("Translation") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BibleRepository.Translation.entries.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.label) },
                    onClick = { onChange(t); expanded = false }
                )
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
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        TextField(
            readOnly = true,
            value = selected ?: "",
            onValueChange = {},
            label = { Text("Book") },
            placeholder = { Text("Select book") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            books.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onChange(name); expanded = false }
                )
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
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        TextField(
            readOnly = true,
            value = selected?.toString() ?: "",
            onValueChange = {},
            label = { Text("Chapter") },
            placeholder = { Text("Select chapter") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            chapters.forEach { ch ->
                DropdownMenuItem(
                    text = { Text(ch.toString()) },
                    onClick = { onChange(ch); expanded = false }
                )
            }
        }
    }
}
