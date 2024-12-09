package com.mobileapp.spoofyrez.ui.parameters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mobileapp.spoofyrez.databinding.FragmentParametersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl
import org.json.JSONObject
import com.google.gson.Gson

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
                            for (i in 0 until 30) {
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

    private fun getCoverURL(
        song: String,
        artist: String,
        callback: (String, String) -> Unit
    ) {
        val authUrl = "https://accounts.spotify.com/api/token"
        val searchUrl = "https://api.spotify.com/v1/search"
        val client = OkHttpClient()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: Get an access token
                val authRequestBody = FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .add("client_id", "f069f7aa309d43528ff346be13b7071a")
                    .add("client_secret", "f989c65f64e04c0ebe4b00eafb71f91e")
                    .build()

                val authRequest = Request.Builder()
                    .url(authUrl)
                    .post(authRequestBody)
                    .build()

                val authResponse = client.newCall(authRequest).execute()
                val authResponseData = JSONObject(authResponse.body?.string() ?: "{}")
                val accessToken = authResponseData.getString("access_token")

                // Step 2: Search for the track
                val query = "track:${song} artist:${artist}"
                val searchRequest = Request.Builder()
                    .url("$searchUrl?q=${query.replace(" ", "%20")}&type=track&limit=1")
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val searchResponse = client.newCall(searchRequest).execute()
                val searchResults = JSONObject(searchResponse.body?.string() ?: "{}")
                val tracks = searchResults.getJSONObject("tracks").getJSONArray("items")

                if (tracks.length() > 0) {
                    val track = tracks.getJSONObject(0)
                    val trackId = track.getString("id") // Extract the track ID
                    val album = track.getJSONObject("album")
                    val images = album.getJSONArray("images")
                    val coverUrl = images.getJSONObject(0).getString("url") // Extract the cover URL

                    // Return both values on the main thread
                    withContext(Dispatchers.Main) {
                        callback(coverUrl, trackId) // Pass both the cover URL and track ID to the callback
                    }
                    return@launch
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback("No Cover Found", "No Track ID Found") // Return fallback values in case of an error
                }
            }
        }
    }



    private fun setupClickListeners() {
        binding.createPlaylist.setOnClickListener {
            Toast.makeText(context, "Generating Playlist", Toast.LENGTH_LONG).show()

            val songTitle = binding.songTitleTextView.text.toString()
            val artistName = binding.artistTextView.text.toString()

            // Check if inputs are not empty
            if (songTitle.isBlank() || artistName.isBlank()) {
                Toast.makeText(requireContext(), "Please enter both a song title and an artist name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pass the extracted song title and artist name into getSimilarTracks
            getSimilarTracks(songTitle, artistName) { result ->
                val songs = result.split("\n").filter { it.isNotEmpty() && it.contains(" --- ") }
                val product = mutableListOf<MutableList<String>>()
                var completedCount = 0

                for (ele in songs) {
                    val parts = ele.split(" --- ")
                    if (parts.size == 2) {
                        val song = parts[0]
                        val artist = parts[1]

                        // Fetch cover URL for each song
                        getCoverURL(song, artist) { coverUrl, trackId ->
                            if (coverUrl != "No Cover Found") {
                                product.add(mutableListOf(song, artist, coverUrl, trackId))
                                Log.d("trackid", trackId)
                            }
                            completedCount++

                            // Check if all tasks are completed
                            if (completedCount == songs.size - 1) {
                                val gson = Gson()
                                val serList = gson.toJson(product)
                                val action = ParametersFragmentDirections.actionParametersToResults(serList)
                                findNavController().navigate(action)
//                                Log.d("FinalProduct", product.joinToString("\n") { it.joinToString(" | ") })
                            }
                        }
                    } else {
                        completedCount++
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