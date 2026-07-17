package com.ridhotegar.androidmovie.presentation.movielist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ridhotegar.androidmovie.R
import com.ridhotegar.androidmovie.databinding.ItemMovieBinding
import com.ridhotegar.androidmovie.databinding.ItemLoadingBinding
import com.ridhotegar.androidmovie.domain.model.Movie

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MOVIE = 0
        private const val TYPE_LOADING = 1
    }

    private val movies = mutableListOf<Movie>()
    var isLoadingMore = false
    var isLastPage = false

    fun submitList(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == movies.size && !isLastPage) {
            TYPE_LOADING
        } else {
            TYPE_MOVIE
        }
    }

    override fun getItemCount(): Int {
        val footerCount = if (isLastPage || movies.isEmpty()) 0 else 1
        return movies.size + footerCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LOADING -> {
                val binding = ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }
            else -> {
                val binding = ItemMovieBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MovieViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MovieViewHolder -> {
                if (position < movies.size) {
                    holder.bind(movies[position])
                }
            }
            is LoadingViewHolder -> {
                holder.bind(isLoadingMore)
            }
        }
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieRating.text = String.format("%.1f", movie.voteAverage ?: 0.0)
            binding.tvMovieYear.text = movie.releaseDate?.take(4) ?: "N/A"

            val posterUrl = movie.posterPath?.let {
                "https://image.tmdb.org/t/p/w342$it"
            }
            Glide.with(binding.root.context)
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(binding.ivMoviePoster)

            binding.root.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    inner class LoadingViewHolder(
        private val binding: ItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(isLoading: Boolean) {
            binding.progressBarLoading.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
