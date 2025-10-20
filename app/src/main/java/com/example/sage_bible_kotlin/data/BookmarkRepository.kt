package com.example.sage_bible_kotlin.data

import android.content.Context

/** Simple SharedPreferences-backed bookmark storage without extra deps. */
object BookmarkRepository {
    private const val PREFS = "bookmarks_prefs"
    private const val KEY = "bookmarks_set"

    data class Bookmark(
        val translation: String,
        val book: String,
        val chapter: Int,
        val verse: Int,
        val text: String,
        val timestamp: Long
    )

    private fun encode(b: Bookmark): String = listOf(
        b.translation,
        b.book,
        b.chapter.toString(),
        b.verse.toString(),
        b.text.replace("\n", " ").replace("|", " "),
        b.timestamp.toString()
    ).joinToString("|")

    private fun decode(s: String): Bookmark? {
        return try {
            val parts = s.split('|')
            if (parts.size < 6) {
                null
            } else {
                Bookmark(
                    translation = parts[0],
                    book = parts[1],
                    chapter = parts[2].toInt(),
                    verse = parts[3].toInt(),
                    text = parts[4],
                    timestamp = parts[5].toLong()
                )
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun add(context: Context, bookmark: Bookmark) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(encode(bookmark))
        prefs.edit().putStringSet(KEY, set).apply()
    }

    fun remove(context: Context, bookmark: Bookmark) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.remove(encode(bookmark))
        prefs.edit().putStringSet(KEY, set).apply()
    }

    fun list(context: Context): List<Bookmark> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY, emptySet()) ?: emptySet()
        return set.mapNotNull { decode(it) }.sortedByDescending { it.timestamp }
    }
}
