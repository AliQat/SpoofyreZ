package com.mobileapp.spoofyrez.ui.results

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.mobileapp.spoofyrez.R
import com.mobileapp.spoofyrez.databinding.FragmentResultsBinding
import okhttp3.OkHttpClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.bumptech.glide.Glide

class ResultsFragment : Fragment() {
    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()

    private val args: ResultsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Deserializes the string
        val jsonString = arguments?.getString("result")
        val gson = Gson()
        val type = object : TypeToken<MutableList<MutableList<String>>>() {}.type
        val songs: MutableList<MutableList<String>> = gson.fromJson(jsonString, type)

        // Gets the scroll layout
        val parentScrollView = binding.root
        val context = requireContext()
        val parentLinearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        parentScrollView.addView(parentLinearLayout)

        for (song in songs) {
            val title = song[0]
            val artist = song[1]
            val cover = song[2]
            val id = song[3]

            val childLinearLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply{
                    setMargins(0,0,0,10)
                }
                setPadding(10,10,10,16)
                setBackgroundColor(Color.DKGRAY)
            }
            val albumCoverImageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                    setMargins(20, 20, 20, 20)
                }
            }
            Glide.with(context)
                .load(cover)
                .placeholder(R.drawable.ic_launcher_background) // Optional placeholder
                .error(R.drawable.ic_launcher_background) // Optional error image
                .into(albumCoverImageView)
            val childLinearLayoutForTexts = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
            }
            val titleTextView = TextView(context).apply {
                text = title // Set the text
                textSize = 25f // Set text size in sp (use float)
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD) // Set text style to bold
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 4)
                }
            }
            val artistTextView = TextView(context).apply {
                text = artist
                textSize = 20f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            parentLinearLayout.addView(childLinearLayout)
            childLinearLayout.addView(albumCoverImageView)
            childLinearLayout.addView(childLinearLayoutForTexts)
            childLinearLayoutForTexts.addView(titleTextView)
            childLinearLayoutForTexts.addView(artistTextView)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
