package com.ridhotegar.androidmovie.presentation.genre

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridhotegar.androidmovie.data.repository.GenreRepository
import com.ridhotegar.androidmovie.domain.model.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GenreUiState {
    object Loading : GenreUiState()
    data class Success(val genres: List<Genre>) : GenreUiState()
    data class Error(val message: String) : GenreUiState()
}

@HiltViewModel
class GenreViewModel @Inject constructor(
    private val repository: GenreRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<GenreUiState>(GenreUiState.Loading)
    val uiState: LiveData<GenreUiState> = _uiState

    init {
        loadGenres()
    }

    fun loadGenres() {
        viewModelScope.launch {
            _uiState.value = GenreUiState.Loading
            val result = repository.getGenres()
            _uiState.value = result.fold(
                onSuccess = { genres ->
                    if (genres.isEmpty()) {
                        GenreUiState.Error("No genres available")
                    } else {
                        GenreUiState.Success(genres)
                    }
                },
                onFailure = { error ->
                    GenreUiState.Error(
                        error.localizedMessage ?: "Failed to load genres"
                    )
                }
            )
        }
    }
}
