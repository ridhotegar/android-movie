package com.ridhotegar.androidmovie.data.repository

import com.ridhotegar.androidmovie.data.api.TmdbApiService
import com.ridhotegar.androidmovie.domain.model.Genre
import com.ridhotegar.androidmovie.domain.model.Movie
import com.ridhotegar.androidmovie.domain.model.MovieDetail
import com.ridhotegar.androidmovie.domain.model.Review
import com.ridhotegar.androidmovie.domain.model.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: TmdbApiService
) {

    suspend fun discoverMoviesByGenre(genreId: Int, page: Int = 1): Result<List<Movie>> {
        return try {
            val response = apiService.discoverMoviesByGenre(genreId = genreId, page = page)
            val movies = response.results.map { dto ->
                Movie(
                    id = dto.id,
                    title = dto.title ?: dto.originalTitle ?: "Unknown",
                    posterPath = dto.posterPath,
                    backdropPath = dto.backdropPath,
                    overview = dto.overview,
                    releaseDate = dto.releaseDate,
                    voteAverage = dto.voteAverage,
                    voteCount = dto.voteCount,
                    genreIds = dto.genreIds,
                    popularity = dto.popularity
                )
            }
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail> {
        return try {
            val response = apiService.getMovieDetail(movieId)
            val detail = MovieDetail(
                id = response.id,
                title = response.title ?: response.originalTitle ?: "Unknown",
                posterPath = response.posterPath,
                backdropPath = response.backdropPath,
                overview = response.overview,
                releaseDate = response.releaseDate,
                originalTitle = response.originalTitle,
                originalLanguage = response.originalLanguage,
                genres = response.genres?.map { Genre(id = it.id, name = it.name) },
                popularity = response.popularity,
                voteAverage = response.voteAverage,
                voteCount = response.voteCount,
                runtime = response.runtime,
                status = response.status,
                tagline = response.tagline,
                budget = response.budget,
                revenue = response.revenue,
                homepage = response.homepage,
                imdbId = response.imdbId,
                spokenLanguages = response.spokenLanguages?.mapNotNull { it.englishName ?: it.name },
                productionCompanies = response.productionCompanies?.mapNotNull { it.name }
            )
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieReviews(movieId: Int, page: Int = 1): Result<List<Review>> {
        return try {
            val response = apiService.getMovieReviews(movieId = movieId, page = page)
            val reviews = response.results.map { dto ->
                Review(
                    id = dto.id ?: "",
                    author = dto.author,
                    authorName = dto.authorDetails?.name,
                    avatarPath = dto.authorDetails?.avatarPath,
                    rating = dto.authorDetails?.rating,
                    content = dto.content,
                    createdAt = dto.createdAt,
                    url = dto.url
                )
            }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieVideos(movieId: Int): Result<List<Video>> {
        return try {
            val response = apiService.getMovieVideos(movieId = movieId)
            val videos = response.results.filter {
                it.site.equals("YouTube", ignoreCase = true)
            }.map { dto ->
                Video(
                    id = dto.id ?: "",
                    name = dto.name,
                    key = dto.key,
                    site = dto.site,
                    type = dto.type,
                    official = dto.official,
                    publishedAt = dto.publishedAt
                )
            }
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
