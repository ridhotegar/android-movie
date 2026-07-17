package com.ridhotegar.androidmovie.domain.model

data class Video(
    val id: String,
    val name: String?,
    val key: String?,
    val site: String?,
    val type: String?,
    val official: Boolean?,
    val publishedAt: String?
)
