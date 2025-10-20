package com.example.sage_bible_kotlin.ui.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sage_bible_kotlin.data.BibleData
import com.example.sage_bible_kotlin.data.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues,
    onOpenReader: (translation: String, book: String, chapter: Int) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { BibleRepository(context) }

    var translation by remember { mutableStateOf(BibleRepository.Translation.KJV) }
    var bibleData by remember { mutableStateOf<BibleData?>(null) }
    var selectedBook by remember { mutableStateOf<String?>(null) }
    var selectedChapter by remember { mutableStateOf<Int?>(null) }
    var testamentTab by remember { mutableStateOf(0) } // 0 = OT, 1 = NT
    var openedDefault by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(translation) {
        isLoading = true
        bibleData = try { withContext(Dispatchers.IO) { repo.loadTranslation(translation) } } catch (_: Throwable) { null }
        selectedBook = bibleData?.books?.firstOrNull()?.name
        selectedChapter = bibleData?.books?.firstOrNull()?.chapters?.firstOrNull()?.chapter
        isLoading = false
    }

    LaunchedEffect(bibleData) {
        if (!openedDefault && bibleData != null) {
            openedDefault = true
            onOpenReader(translation.name, "Genesis", 1)
        }
    }

    val allBooks = remember(bibleData) { bibleData?.books?.map { it.name }.orEmpty() }
    val otSet = remember { oldTestamentBooks.toSet() }
    val ntSet = remember { newTestamentBooks.toSet() }
    val filteredBooks = remember(allBooks, testamentTab) {
        if (testamentTab == 0) allBooks.filter { it in otSet } else allBooks.filter { it in ntSet }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bible") })
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading || bibleData == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    TranslationDropdown(
                        value = translation,
                        onChange = { translation = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                TabRow(selectedTabIndex = testamentTab) {
                    Tab(selected = testamentTab == 0, onClick = { testamentTab = 0 }, text = { Text("Old Testament") })
                    Tab(selected = testamentTab == 1, onClick = { testamentTab = 1 }, text = { Text("New Testament") })
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBooks) { name ->
                        BookCard(title = name) {
                            onOpenReader(translation.name, name, 1)
                        }
                    }
                }
            }
        }
    }
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
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start)
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            chapters.forEach { ch ->
                DropdownMenuItem(
                    text = { Text(ch.toString()) },
                    onClick = { onChange(ch); expanded = false }
                )
            }
        }
    }
}
