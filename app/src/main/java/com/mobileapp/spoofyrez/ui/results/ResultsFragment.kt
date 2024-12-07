package com.mobileapp.spoofyrez.ui.results

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mobileapp.spoofyrez.databinding.FragmentResultsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ResultsFragment : Fragment() {
    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

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

        val res = ResultsFragmentArgs.fromBundle(requireArguments()).result
        fetchMbid("Cemetery Gates", "Pantera")
    }

    private suspend fun getFirstAvailableCoverArt(mbids: List<String>): String {
        for (mbid in mbids) {
            val coverArtUrl = getCoverArtUrl(mbid)
            if (coverArtUrl.isNotEmpty()) {
                Log.d("eee", coverArtUrl)
                return coverArtUrl
            }
        }
        return ""
    }


    private fun fetchMbid(songName: String, artistName: String) {
        lifecycleScope.launch {
            val mbid = getReleaseMbids(songName, artistName)
            getFirstAvailableCoverArt(mbid)
            if (mbid.isNotEmpty()) {
                Log.d("MBID", "Fetched MBID: $mbid")
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
                        if (recordings != null && recordings.length() > 0) {
                            val recording = recordings.getJSONObject(0)
                            val releases = recording.optJSONArray("releases")
                            val mbids = mutableListOf<String>()
                            if (releases != null) {
                                for (i in 0 until releases.length()) {
                                    val release = releases.getJSONObject(i)
                                    mbids.add(release.optString("id"))
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
                        return@withContext url // The URL itself serves as the image source.
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
