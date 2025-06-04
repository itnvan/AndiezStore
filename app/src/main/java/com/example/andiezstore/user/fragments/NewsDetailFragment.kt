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
import com.example.andiezstore.databinding.FragmentNewsDetailBinding
import com.example.andiezstore.user.viewmodel.NewsViewModel
import kotlin.getValue

class NewsDetailFragment : Fragment() {

    private var _binding: FragmentNewsDetailBinding? = null
    private val binding get() = _binding!! // Chỉ hợp lệ giữa onCreateView và onDestroyView

    private lateinit var newsViewModel: NewsViewModel
    private val args: NewsDetailFragmentArgs by navArgs() // Args từ navigation graph

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsDetailBinding.inflate(inflater, container, false)
        newsViewModel = ViewModelProvider(this).get(NewsViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsId = args.newsId // Lấy newsId từ arguments
        Log.d("NewsDetailFragment", "Received newsId: $newsId")

        observeViewModelDetailData()

        if (newsId.isNotEmpty()) {
            newsViewModel.fetchNewsDetailById(newsId)
        } else {
            Toast.makeText(context, "ID Tin tức không hợp lệ", Toast.LENGTH_LONG).show()
            Log.e("NewsDetailFragment", "News ID is empty or null.")
            // Có thể navigate back hoặc hiển thị thông báo lỗi rõ ràng hơn
        }
    }

    private fun observeViewModelDetailData() {
        newsViewModel.selectedNewsDetail.observe(viewLifecycleOwner) { news ->
            if (news != null) {
                binding.tvNewsDetailTitle.text = news.title ?: "Không có tiêu đề"
                binding.tvNewsDetailAuthor.text = if (news.author != null) "Tác giả: ${news.author}" else "Không rõ tác giả"
                binding.tvNewsDetailDate.text = news.date ?: "Không rõ ngày"
                binding.tvNewsDetailTime.text = news.time ?: ""
                binding.tvNewsDetailDescription.text = news.decription ?: "No description available."
                // Load ảnh bìa nếu có: Glide.with(this).load(news.coverImageUrl).into(binding.imgNewsDetailCover)
                Log.d("NewsDetailFragment", "Displaying news detail: ${news.title}")
            } else {
                // Trường hợp news là null sau khi fetch (có thể do lỗi hoặc không tìm thấy)
                // _error LiveData sẽ xử lý hiển thị Toast lỗi
                Log.d("NewsDetailFragment", "Selected news detail is null after fetch attempt.")
            }
        }

        newsViewModel.isLoadingDetail.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarNewsDetailLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d("NewsDetailFragment", "News detail loading state: $isLoading")
        }

        newsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Chỉ hiển thị lỗi nếu không có dữ liệu nào được hiển thị
                if (newsViewModel.selectedNewsDetail.value == null) {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    Log.e("NewsDetailFragment", "Error observed while fetching news detail: $it")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        newsViewModel.clearSelectedNewsDetail() // Xóa dữ liệu tin tức đã chọn khi view bị hủy
        _binding = null // Tránh memory leak
    }
}
