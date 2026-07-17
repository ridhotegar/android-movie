package com.ridhotegar.androidmovie.data.repository

import com.ridhotegar.androidmovie.data.api.TmdbApiService
import com.ridhotegar.androidmovie.domain.model.Genre
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreRepository @Inject constructor(
    private val apiService: TmdbApiService
) {

    suspend fun getGenres(): Result<List<Genre>> {
        return try {
            val response = apiService.getGenres()
            val genres = response.genres.map { dto ->
                Genre(
                    id = dto.id,
                    name = dto.name
                )
            }
            Result.success(genres)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
