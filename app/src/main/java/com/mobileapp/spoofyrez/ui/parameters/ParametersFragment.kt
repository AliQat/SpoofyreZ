package com.mobileapp.spoofyrez.ui.parameters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobileapp.spoofyrez.R
import com.mobileapp.spoofyrez.databinding.FragmentParametersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl
import org.json.JSONObject

class ParametersFragment : Fragment() {
    private var _binding: FragmentParametersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParametersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    // Returns list of similar songs
    fun getSimilarTracks(trackName: String, artistName: String, onResult: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val apiKey = "b72b1ce5d07e7ee5166556188824b31f"
            val baseUrl = "https://ws.audioscrobbler.com/2.0/"

            val url = HttpUrl.Builder()
                .scheme("http")
                .host("ws.audioscrobbler.com")
                .addPathSegment("2.0/")
                .addQueryParameter("method", "track.getsimilar")
                .addQueryParameter("track", trackName)
                .addQueryParameter("artist", artistName)
                .addQueryParameter("api_key", apiKey)
                .addQueryParameter("format", "json")
                .build()

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody)
                        val similarTracks = jsonObject.optJSONObject("similartracks")
                        val tracks = similarTracks?.optJSONArray("track")

                        val result = if (tracks != null && tracks.length() > 0) {
                            val stringBuilder = StringBuilder()
                            for (i in 0 until tracks.length()) {
                                val track = tracks.getJSONObject(i)
                                val name = track.optString("name")
                                val artist = track.optJSONObject("artist")?.optString("name")
                                stringBuilder.append("$name --- $artist\n")
                            }
                            stringBuilder.toString()
                        } else {
                            "No similar tracks found."
                        }

                        withContext(Dispatchers.Main) {
                            onResult(result)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult("API call failed with status: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("An error occurred: ${e.message}")
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnFindSimilar.setOnClickListener {
            // Takes in artist and song
            getSimilarTracks("One", "Metallica") { result ->
                val action = ParametersFragmentDirections.actionParametersToResults(result)
                findNavController().navigate(action)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}