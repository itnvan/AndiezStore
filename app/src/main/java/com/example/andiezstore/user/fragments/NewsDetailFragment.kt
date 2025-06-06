package com.example.andiezstore.user.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController // Import for navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.andiezstore.R // Make sure R points to your project's R file for default images
import com.example.andiezstore.databinding.FragmentNewsDetailBinding
import com.example.andiezstore.user.viewmodel.NewsViewModel

class NewsDetailFragment : Fragment() {

    private var _binding: FragmentNewsDetailBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var newsViewModel: NewsViewModel
    // Arguments from navigation graph
    private val args: NewsDetailFragmentArgs by navArgs()

    // Declare the adapter for suggestions
    private lateinit var suggestionAdapter: SuggestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsDetailBinding.inflate(inflater, container, false)
        newsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]

        // Initialize the SuggestionAdapter with a click listener
        suggestionAdapter = SuggestionAdapter { clickedNews ->
            // Handle click on a suggested news item
            Toast.makeText(context, "Clicked: ${clickedNews.title}", Toast.LENGTH_SHORT).show()

            // OPTIONAL: Navigate to the detail of the clicked suggested news item
            // This assumes your navigation graph has an action from NewsDetailFragment
            // to itself that accepts a newsId argument.
            clickedNews.id?.let {
                val action = NewsDetailFragmentDirections.actionNewsDetailFragmentSelf(it)
                findNavController().navigate(action)
            } ?: run {
                Log.e("NewsDetailFragment", "Clicked suggested news has no ID.")
            }
        }

        // Setup RecyclerView for suggestions
        binding.rvSuggestions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = suggestionAdapter
            // Optional: Add some spacing between suggestion items if desired
            // addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsId = args.newsId // Get newsId from arguments
        Log.d("NewsDetailFragment", "Received newsId: $newsId")

        // Observe LiveData for main news detail
        observeViewModelDetailData()
        // NEW: Observe LiveData for suggested news list
        observeViewModelSuggestionData()

        if (newsId.isNotEmpty()) {
            newsViewModel.fetchNewsDetailById(newsId)
        } else {
            Toast.makeText(context, "Invalid News ID", Toast.LENGTH_LONG).show()
            Log.e("NewsDetailFragment", "News ID is empty or null.")
            // Consider navigating back or showing a more explicit error message
        }
    }

    private fun observeViewModelDetailData() {
        newsViewModel.selectedNewsDetail.observe(viewLifecycleOwner) { news ->
            if (news != null) {
                binding.tvNewsDetailTitle.text = news.title ?: "No Title"
                binding.tvNewsDetailAuthor.text = if (news.author != null) "Author: ${news.author}" else "Unknown Author"
                binding.tvNewsDetailDate.text = news.date ?: "Unknown Date"
                binding.tvNewsDetailTime.text = news.time ?: ""
                binding.tvNewsDetailDescription.text = news.decription ?: "No description available."

                // Load image using Glide
                news.image?.let { imageUrl ->
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.view3)
                        .error(R.drawable.img_user)
                        .into(binding.imgNewsDetailCover)
                } ?: run {
                    binding.imgNewsDetailCover.setImageResource(R.drawable.view3)
                }
                Log.d("NewsDetailFragment", "Displaying news detail: ${news.title}")

                // Show suggestions header only if main news is loaded successfully
                binding.tvSuggestionsHeader.visibility = View.VISIBLE
            } else {
                Log.d("NewsDetailFragment", "Selected news detail is null after fetch attempt.")
                // Hide suggestions header and list if main news is not loaded
                binding.tvSuggestionsHeader.visibility = View.GONE
                binding.rvSuggestions.visibility = View.GONE
            }
        }

        newsViewModel.isLoadingDetail.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarNewsDetailLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            // While main content is loading, hide suggestions section
            if (isLoading) {
                binding.tvSuggestionsHeader.visibility = View.GONE
                binding.rvSuggestions.visibility = View.GONE
            }
            Log.d("NewsDetailFragment", "News detail loading state: $isLoading")
        }

        newsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Only show error if no data is currently displayed for the main news
                if (newsViewModel.selectedNewsDetail.value == null) {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    Log.e("NewsDetailFragment", "Error observed while fetching news detail: $it")
                }
            }
        }
    }

    /**
     * NEW: Observes the LiveData for suggested news and updates the RecyclerView.
     */
    private fun observeViewModelSuggestionData() {
        newsViewModel.suggestedNews.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isNotEmpty()) {
                suggestionAdapter.submitList(suggestions)
                binding.rvSuggestions.visibility = View.VISIBLE
                binding.tvSuggestionsHeader.visibility = View.VISIBLE // Ensure header is visible if data exists
                Log.d("NewsDetailFragment", "Displaying ${suggestions.size} suggested news items.")
            } else {
                suggestionAdapter.submitList(emptyList()) // Clear adapter if no suggestions
                binding.rvSuggestions.visibility = View.GONE
                binding.tvSuggestionsHeader.visibility = View.GONE // Hide header if no suggestions
                Log.d("NewsDetailFragment", "No suggested news available.")
            }
        }

        newsViewModel.isLoadingSuggestions.observe(viewLifecycleOwner) { isLoading ->
            // You can add a separate progress bar for suggestions here if needed,
            // or manage visibility of the list based on this.
            Log.d("NewsDetailFragment", "Suggested news loading state: $isLoading")
            // If suggestions are loading, you might want to hide the list temporarily
            if (isLoading) {
                binding.rvSuggestions.visibility = View.GONE
                // Optionally show a progress bar for suggestions here
            } else {
                // Once loading is complete, let `suggestedNews` observer handle visibility
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear selected news and suggestions when the view is destroyed to prevent memory leaks
        newsViewModel.clearSelectedNewsDetail()
        _binding = null
    }
}