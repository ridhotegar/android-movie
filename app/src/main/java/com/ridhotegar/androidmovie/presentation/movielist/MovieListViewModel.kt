package com.ridhotegar.androidmovie.presentation.movielist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridhotegar.androidmovie.data.repository.MovieRepository
import com.ridhotegar.androidmovie.domain.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MovieListUiState {
    object Loading : MovieListUiState()
    data class Success(
        val movies: List<Movie>,
        val isLastPage: Boolean = false
    ) : MovieListUiState()
    data class Error(val message: String) : MovieListUiState()
}

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<MovieListUiState>(MovieListUiState.Loading)
    val uiState: LiveData<MovieListUiState> = _uiState

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private var currentPage = 1
    private var isLastPage = false
    private var genreId: Int = 0
    private var genreName: String = ""
    private val currentMovies = mutableListOf<Movie>()

    fun initialize(genreId: Int, genreName: String) {
        if (this.genreId != 0) return
        this.genreId = genreId
        this.genreName = genreName
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = MovieListUiState.Loading
            currentPage = 1
            isLastPage = false
            currentMovies.clear()
            fetchMovies()
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value == true || isLastPage) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            fetchMovies()
        }
    }

    private suspend fun fetchMovies() {
        val result = repository.discoverMoviesByGenre(genreId = genreId, page = currentPage)
        result.fold(
            onSuccess = { movies ->
                currentMovies.addAll(movies)
                if (movies.isEmpty()) {
                    isLastPage = true
                }
                _uiState.value = MovieListUiState.Success(
                    movies = currentMovies.toList(),
                    isLastPage = isLastPage
                )
            },
            onFailure = { error ->
                if (currentMovies.isEmpty()) {
                    _uiState.value = MovieListUiState.Error(
                        error.localizedMessage ?: "Failed to load movies"
                    )
                }
            }
        )
        _isLoadingMore.value = false
    }

    fun getGenreName(): String = genreName

    fun isLoadingMore(): Boolean = _isLoadingMore.value ?: false
    fun isLastPage(): Boolean = isLastPage
}
