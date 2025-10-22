package com.example.sage_bible_kotlin.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
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

private enum class PickerMode { None, Translation, Book, Chapter, Verse }

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
    var mode by remember { mutableStateOf(PickerMode.None) }

    val books = remember(data) { data.books.map { it.name } }
    val chapters = remember(data, book) { repo.getChapters(data, book) }
    val verses = remember(data, book, chapter) {
        data.books.firstOrNull { it.name == book }
            ?.chapters?.firstOrNull { it.chapter == chapter }
            ?.verses?.map { it.verse } ?: emptyList()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (mode) {
            PickerMode.None -> {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "Go to", style = MaterialTheme.typography.titleLarge)
                    SelectorRow(label = "Translation", value = translation.label) { mode = PickerMode.Translation }
                    SelectorRow(label = "Book", value = book) { mode = PickerMode.Book }
                    SelectorRow(label = "Chapter", value = chapter.toString()) { mode = PickerMode.Chapter }
                    SelectorRow(label = "Verse (optional)", value = verse?.toString() ?: "") { mode = PickerMode.Verse }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(onClick = onDismiss) { Text("Cancel") }
                        Button(onClick = { onConfirm(translation, book, chapter, verse) }) { Text("Go") }
                    }
                }
            }
            PickerMode.Translation -> FullListPanel(
                title = "Select translation",
                entries = BibleRepository.Translation.entries.map { it.label },
                selected = translation.label,
                onBack = { mode = PickerMode.None }
            ) { label ->
                val found = BibleRepository.Translation.entries.firstOrNull { it.label == label }
                if (found != null) translation = found
                mode = PickerMode.None
            }
            PickerMode.Book -> FullListPanel(
                title = "Select book",
                entries = books,
                selected = book,
                onBack = { mode = PickerMode.None }
            ) { sel ->
                book = sel
                chapter = repo.getChapters(data, sel).firstOrNull() ?: 1
                verse = null
                mode = PickerMode.None
            }
            PickerMode.Chapter -> FullListPanel(
                title = "Select chapter",
                entries = chapters.map { it.toString() },
                selected = chapter.toString(),
                onBack = { mode = PickerMode.None }
            ) { sel ->
                chapter = sel.toInt()
                verse = null
                mode = PickerMode.None
            }
            PickerMode.Verse -> FullListPanel(
                title = "Select verse",
                entries = verses.map { it.toString() },
                selected = verse?.toString() ?: "",
                onBack = { mode = PickerMode.None }
            ) { sel ->
                verse = sel.toInt()
                mode = PickerMode.None
            }
        }
    }
}

@Composable
private fun SelectorRow(label: String, value: String, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = if (value.isNotEmpty()) value else "Tap to select", style = MaterialTheme.typography.bodyLarge)
        }
        TextButton(onClick = onClick) { Text("Select") }
    }
}

@Composable
private fun FullListPanel(
    title: String,
    entries: List<String>,
    selected: String,
    onBack: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onBack) { Text("Back") }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { item ->
                val isSelected = item == selected
                val container = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                val content = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                TextButton(
                    onClick = { onSelect(item) },
                    colors = ButtonDefaults.textButtonColors(containerColor = container, contentColor = content),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
