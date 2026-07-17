package com.ridhotegar.androidmovie.domain.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val releaseDate: String?,
    val originalTitle: String?,
    val originalLanguage: String?,
    val genres: List<Genre>?,
    val popularity: Double?,
    val voteAverage: Double?,
    val voteCount: Int?,
    val runtime: Int?,
    val status: String?,
    val tagline: String?,
    val budget: Long?,
    val revenue: Long?,
    val homepage: String?,
    val imdbId: String?,
    val spokenLanguages: List<String>?,
    val productionCompanies: List<String>?
)
