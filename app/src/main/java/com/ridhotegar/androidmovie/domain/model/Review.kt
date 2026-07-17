package com.ridhotegar.androidmovie.domain.model

data class Review(
    val id: String,
    val author: String?,
    val authorName: String?,
    val avatarPath: String?,
    val rating: Double?,
    val content: String?,
    val createdAt: String?,
    val url: String?
)
