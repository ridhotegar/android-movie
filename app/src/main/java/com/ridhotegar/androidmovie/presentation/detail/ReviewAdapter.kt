package com.ridhotegar.androidmovie.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ridhotegar.androidmovie.R
import com.ridhotegar.androidmovie.databinding.ItemReviewBinding
import com.ridhotegar.androidmovie.domain.model.Review

class ReviewAdapter(
    private val onReviewClick: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val reviews = mutableListOf<Review>()

    fun submitList(newReviews: List<Review>) {
        reviews.clear()
        reviews.addAll(newReviews)
        notifyDataSetChanged()
    }

    fun addReviews(newReviews: List<Review>) {
        val startIndex = reviews.size
        reviews.addAll(newReviews)
        notifyItemRangeInserted(startIndex, newReviews.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(
        private val binding: ItemReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvReviewAuthor.text = review.authorName ?: review.author ?: "Anonymous"
            binding.tvReviewContent.text = review.content
            binding.tvReviewRating.text = if (review.rating != null) {
                "${review.rating}/10"
            } else {
                "N/A"
            }

            var avatarUrl = review.avatarPath
            if (avatarUrl != null) {
                if (avatarUrl.startsWith("/")) {
                    avatarUrl = "https://image.tmdb.org/t/p/w45$avatarUrl"
                }
            }

            Glide.with(binding.root.context)
                .load(avatarUrl)
                .placeholder(R.drawable.placeholder_circle)
                .error(R.drawable.placeholder_circle)
                .circleCrop()
                .into(binding.ivReviewAvatar)

            binding.root.setOnClickListener {
                onReviewClick(review)
            }
        }
    }
}
