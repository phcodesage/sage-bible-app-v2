package com.example.sage_bible_kotlin.data

import android.content.Context

object HighlightRepository {
    private const val PREFS = "highlights_prefs"

    private fun key(translation: String, book: String, chapter: Int, verse: Int): String =
        "${translation}|${book}|${chapter}|${verse}"

    fun set(
        context: Context,
        translation: String,
        book: String,
        chapter: Int,
        verse: Int,
        colorHex: String
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(key(translation, book, chapter, verse), colorHex).apply()
    }

    fun remove(
        context: Context,
        translation: String,
        book: String,
        chapter: Int,
        verse: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(key(translation, book, chapter, verse)).apply()
    }

    fun get(
        context: Context,
        translation: String,
        book: String,
        chapter: Int,
        verse: Int
    ): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(key(translation, book, chapter, verse), null)
    }
}
