package com.tanasi.sflix.adapters.view_holders

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.tvprovider.media.tv.WatchNextProgram
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.tanasi.sflix.R
import com.tanasi.sflix.databinding.ItemEpisodeBinding
import com.tanasi.sflix.fragments.player.PlayerFragment
import com.tanasi.sflix.fragments.season.SeasonFragmentDirections
import com.tanasi.sflix.models.Episode
import com.tanasi.sflix.utils.map

@SuppressLint("RestrictedApi")
class VhEpisode(
    private val _binding: ViewBinding
) : RecyclerView.ViewHolder(
    _binding.root
) {

    private val context = itemView.context
    private lateinit var episode: Episode

    fun bind(episode: Episode) {
        this.episode = episode

        when (_binding) {
            is ItemEpisodeBinding -> displayItem(_binding)
        }
    }


    private fun displayItem(binding: ItemEpisodeBinding) {
        binding.root.apply {
            setOnClickListener {
                findNavController().navigate(
                    SeasonFragmentDirections.actionSeasonToPlayer(
                        videoType = PlayerFragment.VideoType.Episode,
                        id = episode.id,
                        title = episode.tvShow?.title ?: "",
                        subtitle = "S${episode.season?.number ?: 0} E${episode.number} • ${episode.title}",
                    )
                )
            }
            setOnFocusChangeListener { _, hasFocus ->
                val animation = when {
                    hasFocus -> AnimationUtils.loadAnimation(context, R.anim.zoom_in)
                    else -> AnimationUtils.loadAnimation(context, R.anim.zoom_out)
                }
                binding.root.startAnimation(animation)
                animation.fillAfter = true
            }
        }

        binding.ivEpisodePoster.apply {
            clipToOutline = true
            Glide.with(context)
                .load(episode.poster)
                .centerCrop()
                .into(this)
        }

        binding.pbEpisodeProgress.apply {
            val program = context.contentResolver.query(
                TvContractCompat.WatchNextPrograms.CONTENT_URI,
                WatchNextProgram.PROJECTION,
                null,
                null,
                null,
            )?.map { WatchNextProgram.fromCursor(it) }
                ?.find { it.contentId == episode.id }

            progress = when {
                program != null -> (program.lastPlaybackPositionMillis * 100 / program.durationMillis.toDouble()).toInt()
                else -> 0
            }
            visibility = when {
                program != null -> View.VISIBLE
                else -> View.GONE
            }
        }

        binding.tvEpisodeInfo.text = "Episode ${episode.number}"

        binding.tvEpisodeTitle.text = episode.title
    }
}