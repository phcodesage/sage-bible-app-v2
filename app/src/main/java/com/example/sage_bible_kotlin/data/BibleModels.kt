package com.example.sage_bible_kotlin.data

data class BibleData(
    val translation: String,
    val books: List<Book>
)

data class Book(
    val name: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val chapter: Int,
    val verses: List<Verse>
)

data class Verse(
    val verse: Int,
    val text: String
)
