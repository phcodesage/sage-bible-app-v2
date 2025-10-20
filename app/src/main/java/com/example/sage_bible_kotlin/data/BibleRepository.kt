package com.example.sage_bible_kotlin.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class BibleRepository(private val context: Context) {
    private val gson = Gson()

    enum class Translation(val fileName: String, val label: String) {
        KJV("KJV.json", "KJV"),
        AKJV("AKJV.json", "AKJV"),
        CEB("CebPinadayag.json", "CEB (Pinadayag)")
    }

    private fun readAsset(fileName: String): String {
        context.assets.open(fileName).use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                return reader.readText()
            }
        }
    }

    fun loadTranslation(translation: Translation): BibleData {
        val json = readAsset(translation.fileName)
        val type = object : TypeToken<BibleData>() {}.type
        return gson.fromJson(json, type)
    }

    fun getBooks(data: BibleData): List<String> = data.books.map { it.name }

    fun getChapters(data: BibleData, bookName: String): List<Int> {
        val book = data.books.firstOrNull { it.name == bookName } ?: return emptyList()
        return book.chapters.map { it.chapter }
    }

    fun getChapterContent(data: BibleData, bookName: String, chapterNumber: Int): Chapter? {
        val book = data.books.firstOrNull { it.name == bookName } ?: return null
        return book.chapters.firstOrNull { it.chapter == chapterNumber }
    }
}
