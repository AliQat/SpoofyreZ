package com.mobileapp.spoofyrez

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.mobileapp.spoofyrez.data.repository.SongRepository
import com.mobileapp.spoofyrez.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var songRepository: SongRepository

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MainActivity", "Client ID from BuildConfig: ${BuildConfig.SPOTIFY_CLIENT_ID}")
        Log.d("MainActivity", "Secret from BuildConfig: ${BuildConfig.SPOTIFY_CLIENT_SECRET.take(4)}...")

        lifecycleScope.launch {
            songRepository.getSimilarSongs()
        }
    }
}