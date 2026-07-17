package com.ridhotegar.androidmovie.presentation.movielist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ridhotegar.androidmovie.R
import com.ridhotegar.androidmovie.databinding.FragmentMovieListBinding
import com.ridhotegar.androidmovie.domain.model.Movie
import com.ridhotegar.androidmovie.presentation.common.PaginationScrollListener
import com.ridhotegar.androidmovie.presentation.genre.GenreFragment
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieListViewModel by viewModels()

    private lateinit var movieAdapter: MovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val genreId = arguments?.getInt(GenreFragment.ARG_GENRE_ID, 0) ?: 0
        val genreName = arguments?.getString(GenreFragment.ARG_GENRE_NAME, "") ?: ""

        viewModel.initialize(genreId, genreName)

        setupToolbar(genreName)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar(genreName: String) {
        binding.toolbar.setTitle(genreName)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMovies.layoutManager = layoutManager

        movieAdapter = MovieAdapter(
            onMovieClick = { movie -> navigateToMovieDetail(movie) }
        )
        binding.rvMovies.adapter = movieAdapter

        binding.rvMovies.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean = viewModel.isLastPage()
            override fun isLoading(): Boolean = viewModel.isLoadingMore()
            override fun loadMoreItems() {
                viewModel.loadNextPage()
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadMovies()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is MovieListUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvMovies.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                }

                is MovieListUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvMovies.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE

                    movieAdapter.isLastPage = state.isLastPage
                    movieAdapter.submitList(state.movies)
                }

                is MovieListUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvMovies.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE
                    binding.tvError.text = state.message

                    binding.btnRetry.setOnClickListener {
                        viewModel.loadMovies()
                    }

                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { isLoading ->
            movieAdapter.isLoadingMore = isLoading
            movieAdapter.notifyItemChanged(movieAdapter.itemCount - 1)
        }
    }

    private fun navigateToMovieDetail(movie: Movie) {
        val bundle = Bundle().apply {
            putInt(ARG_MOVIE_ID, movie.id)
            putString(ARG_MOVIE_TITLE, movie.title)
            putString(ARG_MOVIE_POSTER, movie.posterPath)
        }
        findNavController().navigate(R.id.action_movie_list_to_detail, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_MOVIE_ID = "movie_id"
        const val ARG_MOVIE_TITLE = "movie_title"
        const val ARG_MOVIE_POSTER = "movie_poster"
    }
}
