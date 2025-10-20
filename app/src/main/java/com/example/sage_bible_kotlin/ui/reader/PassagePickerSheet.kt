package com.example.sage_bible_kotlin.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sage_bible_kotlin.data.BibleData
import com.example.sage_bible_kotlin.data.BibleRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderPassagePickerSheet(
    repo: BibleRepository,
    data: BibleData,
    initialTranslation: BibleRepository.Translation,
    initialBook: String,
    initialChapter: Int,
    onDismiss: () -> Unit,
    onConfirm: (BibleRepository.Translation, String, Int, Int?) -> Unit
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var translation by remember { mutableStateOf(initialTranslation) }
    var book by remember { mutableStateOf(initialBook) }
    var chapter by remember { mutableStateOf(initialChapter) }
    var verse: Int? by remember { mutableStateOf<Int?>(null) }

    val books = remember(data) { data.books.map { it.name } }
    val chapters = remember(data, book) { repo.getChapters(data, book) }
    val verses = remember(data, book, chapter) {
        data.books.firstOrNull { it.name == book }
            ?.chapters?.firstOrNull { it.chapter == chapter }
            ?.verses?.map { it.verse } ?: emptyList()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Go to", style = MaterialTheme.typography.titleMedium)
            ReaderTranslationDropdown(value = translation, onChange = { translation = it })
            ReaderBookDropdown(
                books = books,
                selected = book,
                onChange = { sel -> sel?.let { book = it; chapter = repo.getChapters(data, it).firstOrNull() ?: 1; verse = null } }
            )
            ReaderChapterDropdown(chapters = chapters, selected = chapter, onChange = { ch -> ch?.let { chapter = it; verse = null } })
            ReaderVerseDropdown(verses = verses, selected = verse, onChange = { v -> verse = v })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onDismiss) { Text("Cancel") }
                Button(onClick = { onConfirm(translation, book, chapter, verse) }) { Text("Go") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTranslationDropdown(
    value: BibleRepository.Translation,
    onChange: (BibleRepository.Translation) -> Unit,
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BibleRepository.Translation.entries.forEach { t ->
                DropdownMenuItem(text = { Text(t.label) }, onClick = { onChange(t); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderBookDropdown(
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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
private fun ReaderChapterDropdown(
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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
private fun ReaderVerseDropdown(
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            verses.forEach { v ->
                DropdownMenuItem(text = { Text(v.toString()) }, onClick = { onChange(v); expanded = false })
            }
        }
    }
}
