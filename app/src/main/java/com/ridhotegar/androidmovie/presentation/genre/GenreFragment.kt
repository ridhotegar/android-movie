package com.ridhotegar.androidmovie.presentation.genre

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ridhotegar.androidmovie.R
import com.ridhotegar.androidmovie.databinding.FragmentGenreBinding
import com.ridhotegar.androidmovie.domain.model.Genre
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class GenreFragment : Fragment() {

    private var _binding: FragmentGenreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GenreViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvGenres.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadGenres()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is GenreUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvGenres.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                }

                is GenreUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvGenres.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE

                    val adapter = GenreAdapter(
                        genres = state.genres,
                        onGenreClick = { genre -> navigateToMovieList(genre) }
                    )
                    binding.rvGenres.adapter = adapter
                }

                is GenreUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvGenres.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE
                    binding.tvError.text = state.message

                    binding.btnRetry.setOnClickListener {
                        viewModel.loadGenres()
                    }

                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToMovieList(genre: Genre) {
        val bundle = Bundle().apply {
            putInt(ARG_GENRE_ID, genre.id)
            putString(ARG_GENRE_NAME, genre.name)
        }
        findNavController().navigate(R.id.action_genre_to_movie_list, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_GENRE_ID = "genre_id"
        const val ARG_GENRE_NAME = "genre_name"
    }
}
