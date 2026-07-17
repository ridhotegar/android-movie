package com.ridhotegar.androidmovie.presentation.detail

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ridhotegar.androidmovie.R
import com.ridhotegar.androidmovie.databinding.ItemTrailerBinding
import com.ridhotegar.androidmovie.domain.model.Video

class TrailerAdapter(
    private val trailers: List<Video>
) : RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailerViewHolder {
        val binding = ItemTrailerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrailerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrailerViewHolder, position: Int) {
        holder.bind(trailers[position])
    }

    override fun getItemCount(): Int = trailers.size

    inner class TrailerViewHolder(
        private val binding: ItemTrailerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(video: Video) {
            binding.tvTrailerName.text = video.name ?: "Trailer"

            val thumbnailUrl = video.key?.let {
                "https://img.youtube.com/vi/$it/mqdefault.jpg"
            }
            Glide.with(binding.root.context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.ivTrailerThumbnail)

            binding.root.setOnClickListener {
                video.key?.let { key ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }
}
