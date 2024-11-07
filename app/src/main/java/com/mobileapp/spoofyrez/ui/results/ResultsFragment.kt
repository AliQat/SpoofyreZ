package com.mobileapp.spoofyrez.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mobileapp.spoofyrez.data.repository.SongRepository
import com.mobileapp.spoofyrez.databinding.FragmentResultsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// This is Ai generated, do not yell at me if this breaks, I will fix it later, this is just for PT
@AndroidEntryPoint
class ResultsFragment : Fragment() {
    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var songRepository: SongRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            songRepository.currentTrack.collectLatest { track ->
                track?.let {
                    binding.tvSongName.text = songRepository.getSongName()
                    binding.tvArtistName.text = songRepository.getArtistName()
                    binding.tvPopularity.text = "Popularity: ${songRepository.getPopularity()}"

                    val albumArtUrl = songRepository.getAlbumArt(SongRepository.ImageSize.MEDIUM)
                    context?.let { ctx ->
                        Glide.with(ctx)
                            .load(albumArtUrl)
                            .centerCrop()
                            .into(binding.ivAlbumArt)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}