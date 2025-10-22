package com.example.sage_bible_kotlin.data

import android.content.Context

/** Simple SharedPreferences-backed reading position storage. */
object ReadingPositionRepository {
    private const val PREFS = "reading_position_prefs"
    private const val KEY_TRANSLATION = "last_translation"
    private const val KEY_BOOK = "last_book"
    private const val KEY_CHAPTER = "last_chapter"
    private const val KEY_VERSE = "last_verse"

    data class ReadingPosition(
        val translation: String,
        val book: String,
        val chapter: Int,
        val verse: Int? = null
    )

    fun save(context: Context, position: ReadingPosition) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TRANSLATION, position.translation)
            .putString(KEY_BOOK, position.book)
            .putInt(KEY_CHAPTER, position.chapter)
            .putInt(KEY_VERSE, position.verse ?: -1)
            .apply()
    }

    fun load(context: Context): ReadingPosition? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val translation = prefs.getString(KEY_TRANSLATION, null)
        val book = prefs.getString(KEY_BOOK, null)
        val chapter = prefs.getInt(KEY_CHAPTER, -1)
        val verse = prefs.getInt(KEY_VERSE, -1).takeIf { it > 0 }

        return if (translation != null && book != null && chapter > 0) {
            ReadingPosition(translation, book, chapter, verse)
        } else {
            null
        }
    }

    fun getDefaultPosition(): ReadingPosition {
        return ReadingPosition("KJV", "Genesis", 1)
    }
}
