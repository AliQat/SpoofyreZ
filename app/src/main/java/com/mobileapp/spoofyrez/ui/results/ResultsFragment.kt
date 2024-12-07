package com.mobileapp.spoofyrez.ui.results

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.mobileapp.spoofyrez.data.repository.SongRepository
import com.mobileapp.spoofyrez.databinding.FragmentResultsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ResultsFragment : Fragment() {
    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

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
                    displayTrackInfo()
                }
            }
        }

        // If parameters from `feature/parameters` are needed:
        val res = ResultsFragmentArgs.fromBundle(requireArguments()).result
        fetchMbid("Cemetery Gates", "Pantera")
    }

    private suspend fun displayTrackInfo() {
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

    private fun fetchMbid(songName: String, artistName: String) {
        lifecycleScope.launch {
            val mbids = getReleaseMbids(songName, artistName)
            val coverArtUrl = getFirstAvailableCoverArt(mbids)
            if (mbids.isNotEmpty()) {
                Log.d("MBID", "Fetched MBID: $mbids")
                Log.d("CoverArt", "Cover art URL: $coverArtUrl")
            } else {
                Log.d("MBID", "No MBID found.")
            }
        }
    }

    private suspend fun getReleaseMbids(songName: String, artistName: String): List<String> {
        val baseUrl = "https://musicbrainz.org/ws/2/recording/"
        val query = "?query=recording:$songName AND artist:$artistName&fmt=json"
        val url = baseUrl + query.replace(" ", "%20")

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "YourAppName/1.0 (your-email@example.com)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("MBID", "Error: ${response.code}")
                        return@withContext emptyList()
                    }

                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val json = JSONObject(responseBody)
                        val recordings = json.optJSONArray("recordings")
                        if (recordings != null) {
                            val mbids = mutableListOf<String>()
                            for (i in 0 until recordings.length()) {
                                val releases = recordings.getJSONObject(i).optJSONArray("releases")
                                if (releases != null) {
                                    for (j in 0 until releases.length()) {
                                        mbids.add(releases.getJSONObject(j).optString("id"))
                                    }
                                }
                            }
                            return@withContext mbids
                        }
                    }
                    Log.e("MBID", "No releases found.")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("MBID", "Error fetching release MBIDs", e)
                emptyList()
            }
        }
    }

    private suspend fun getFirstAvailableCoverArt(mbids: List<String>): String {
        for (mbid in mbids) {
            val coverArtUrl = getCoverArtUrl(mbid)
            if (coverArtUrl.isNotEmpty()) {
                return coverArtUrl
            }
        }
        return ""
    }

    private suspend fun getCoverArtUrl(releaseMbid: String): String {
        val url = "https://coverartarchive.org/release/$releaseMbid/front"

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "YourAppName/1.0 (your-email@example.com)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext url
                    } else {
                        Log.e("CoverArt", "Error fetching cover art: ${response.code}")
                        return@withContext ""
                    }
                }
            } catch (e: Exception) {
                Log.e("CoverArt", "Error fetching cover art", e)
                ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
