package com.ridhotegar.androidmovie.presentation.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ridhotegar.androidmovie.R
import com.ridhotegar.androidmovie.databinding.FragmentMovieDetailBinding
import com.ridhotegar.androidmovie.domain.model.MovieDetail
import com.ridhotegar.androidmovie.domain.model.Review
import com.ridhotegar.androidmovie.domain.model.Video
import com.ridhotegar.androidmovie.presentation.common.PaginationScrollListener
import com.ridhotegar.androidmovie.presentation.movielist.MovieListFragment
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieDetailViewModel by viewModels()

    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var trailerAdapter: TrailerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = arguments?.getInt(MovieListFragment.ARG_MOVIE_ID, 0) ?: 0
        val movieTitle = arguments?.getString(MovieListFragment.ARG_MOVIE_TITLE, "") ?: ""

        viewModel.initialize(movieId)

        setupToolbar(movieTitle)
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupToolbar(title: String) {
        binding.toolbar.setTitle(title)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerViews() {
        reviewAdapter = ReviewAdapter(
            onReviewClick = { review ->
                review.url?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
        )
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = reviewAdapter

        val reviewLayoutManager = binding.rvReviews.layoutManager as LinearLayoutManager
        binding.rvReviews.addOnScrollListener(object : PaginationScrollListener(reviewLayoutManager) {
            override fun isLastPage(): Boolean = viewModel.isLastReviewPage()
            override fun isLoading(): Boolean = viewModel.isLoadingMoreReviews()
            override fun loadMoreItems() {
                viewModel.loadMoreReviews()
            }
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DetailUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                }

                is DetailUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE

                    bindMovieDetail(state.detail)
                    bindReviews(state.reviews)
                    bindTrailers(state.trailers)
                }

                is DetailUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.VISIBLE
                    binding.tvError.text = state.message

                    binding.btnRetry.setOnClickListener {
                        viewModel.loadMovieDetail()
                    }

                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun bindMovieDetail(detail: MovieDetail) {
        val backdropUrl = detail.backdropPath?.let {
            "https://image.tmdb.org/t/p/w780$it"
        }
        Glide.with(this)
            .load(backdropUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(binding.ivBackdrop)

        val posterUrl = detail.posterPath?.let {
            "https://image.tmdb.org/t/p/w342$it"
        }
        Glide.with(this)
            .load(posterUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(binding.ivPoster)

        binding.tvTitle.text = detail.title
        binding.tvTagline.text = detail.tagline

        binding.tvOverviewLabel.visibility = if (detail.overview.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvOverview.text = detail.overview ?: ""

        val ratingText = if (detail.voteAverage != null) {
            "${String.format("%.1f", detail.voteAverage)} / 10"
        } else {
            "N/A"
        }
        binding.tvRating.text = "Rating: $ratingText"

        val runtimeText = detail.runtime?.let {
            val hours = it / 60
            val minutes = it % 60
            "${hours}h ${minutes}m"
        } ?: "N/A"
        binding.tvRuntime.text = "Runtime: $runtimeText"

        binding.tvReleaseDate.text = "Release: ${detail.releaseDate ?: "N/A"}"

        val genresText = detail.genres?.joinToString(", ") { it.name } ?: ""
        if (genresText.isNotBlank()) {
            binding.tvGenres.visibility = View.VISIBLE
            binding.tvGenres.text = "Genres: $genresText"
        } else {
            binding.tvGenres.visibility = View.GONE
        }

        val languagesText = detail.spokenLanguages?.joinToString(", ") ?: ""
        if (languagesText.isNotBlank()) {
            binding.tvLanguages.visibility = View.VISIBLE
            binding.tvLanguages.text = "Languages: $languagesText"
        } else {
            binding.tvLanguages.visibility = View.GONE
        }

        binding.tvStatus.text = "Status: ${detail.status ?: "N/A"}"

        val budgetText = detail.budget?.let {
            "$${it.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}"
        } ?: "N/A"
        binding.tvBudget.text = "Budget: $budgetText"

        val revenueText = detail.revenue?.let {
            "$${it.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}"
        } ?: "N/A"
        binding.tvRevenue.text = "Revenue: $revenueText"

        if (detail.homepage.isNullOrBlank()) {
            binding.btnHomepage.visibility = View.GONE
        } else {
            binding.btnHomepage.visibility = View.VISIBLE
            binding.btnHomepage.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.homepage))
                startActivity(intent)
            }
        }

        binding.tvVoteCount.text = "${detail.voteCount ?: 0} votes"
    }

    private fun bindReviews(reviews: List<Review>) {
        if (reviews.isEmpty()) {
            binding.sectionReviews.visibility = View.GONE
        } else {
            binding.sectionReviews.visibility = View.VISIBLE
            reviewAdapter.submitList(reviews)
        }
    }

    private fun bindTrailers(trailers: List<Video>) {
        if (trailers.isEmpty()) {
            binding.sectionTrailers.visibility = View.GONE
        } else {
            binding.sectionTrailers.visibility = View.VISIBLE
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvTrailers.layoutManager = layoutManager
            trailerAdapter = TrailerAdapter(trailers)
            binding.rvTrailers.adapter = trailerAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
