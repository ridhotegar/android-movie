package com.ridhotegar.androidmovie.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridhotegar.androidmovie.data.repository.MovieRepository
import com.ridhotegar.androidmovie.domain.model.MovieDetail
import com.ridhotegar.androidmovie.domain.model.Review
import com.ridhotegar.androidmovie.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val detail: MovieDetail,
        val reviews: List<Review>,
        val trailers: List<Video>,
        val isReviewsLastPage: Boolean = false
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<DetailUiState>(DetailUiState.Loading)
    val uiState: LiveData<DetailUiState> = _uiState

    private val _isLoadingMoreReviews = MutableLiveData(false)
    val isLoadingMoreReviews: LiveData<Boolean> = _isLoadingMoreReviews

    private var movieId: Int = 0
    private var reviewsPage = 1
    private var isLastReviewPage = false
    private val allReviews = mutableListOf<Review>()

    fun initialize(movieId: Int) {
        if (this.movieId != 0) return
        this.movieId = movieId
        loadMovieDetail()
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            allReviews.clear()
            reviewsPage = 1
            isLastReviewPage = false

            val detailResult = repository.getMovieDetail(movieId)
            val reviewsResult = repository.getMovieReviews(movieId, page = reviewsPage)
            val videosResult = repository.getMovieVideos(movieId)

            detailResult.fold(
                onSuccess = { detail ->
                    val reviews = reviewsResult.getOrDefault(emptyList())
                    val trailers = videosResult.getOrDefault(emptyList())
                    allReviews.addAll(reviews)
                    if (reviews.isEmpty()) {
                        isLastReviewPage = true
                    }

                    _uiState.value = DetailUiState.Success(
                        detail = detail,
                        reviews = allReviews.toList(),
                        trailers = trailers.filter { it.type.equals("Trailer", ignoreCase = true) || it.type.equals("Teaser", ignoreCase = true) },
                        isReviewsLastPage = isLastReviewPage
                    )
                },
                onFailure = { error ->
                    _uiState.value = DetailUiState.Error(
                        error.localizedMessage ?: "Failed to load movie details"
                    )
                }
            )
        }
    }

    fun loadMoreReviews() {
        if (_isLoadingMoreReviews.value == true || isLastReviewPage) return

        viewModelScope.launch {
            _isLoadingMoreReviews.value = true
            reviewsPage++

            val result = repository.getMovieReviews(movieId, page = reviewsPage)
            result.fold(
                onSuccess = { reviews ->
                    allReviews.addAll(reviews)
                    if (reviews.isEmpty()) {
                        isLastReviewPage = true
                    }

                    val currentState = _uiState.value
                    if (currentState is DetailUiState.Success) {
                        _uiState.value = currentState.copy(
                            reviews = allReviews.toList(),
                            isReviewsLastPage = isLastReviewPage
                        )
                    }
                },
                onFailure = { }
            )
            _isLoadingMoreReviews.value = false
        }
    }

    fun isLoadingMoreReviews(): Boolean = _isLoadingMoreReviews.value ?: false
    fun isLastReviewPage(): Boolean = isLastReviewPage
}
