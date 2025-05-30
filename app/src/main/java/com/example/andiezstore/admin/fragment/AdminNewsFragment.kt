package com.example.andiezstore.admin.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentAdminNewsBinding
import com.example.andiezstore.user.model.News
import com.example.andiezstore.utils.Crud
import com.google.firebase.database.*
import com.bumptech.glide.Glide // Import Glide để tải ảnh

class AdminNewsFragment : Fragment() {
    private var _binding: FragmentAdminNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsDatabaseRef: DatabaseReference
    private var _currentDisplayedNews: News? = null // Biến để lưu tin tức đang hiển thị

    // Animation variables
    private lateinit var fromBottom: Animation
    private lateinit var toBottom: Animation
    private lateinit var rotateOpen: Animation
    private lateinit var rotateClose: Animation
    private var clicked = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Khởi tạo animation
        fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)
        rotateOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim)
        rotateClose = AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Database reference
        newsDatabaseRef= FirebaseDatabase.getInstance().getReference("Admin").child("News")

        // Set click listeners for FABs
        binding.btnMain.setOnClickListener {
            onMainButtonClicked()
        }

        binding.btnAdd.setOnClickListener {
            showCrudDialog(null) // Mở dialog ở chế độ thêm mới
        }

        binding.btnUpdate.setOnClickListener {
            // Cập nhật tin tức đang hiển thị
            _currentDisplayedNews?.let { newsToUpdate ->
                showCrudDialog(newsToUpdate)
            } ?: showError("No News to update.")
        }

        binding.btnDelete.setOnClickListener {
            // Xóa tin tức đang hiển thị
            _currentDisplayedNews?.let { newsToDelete ->
                showCrudDialog(newsToDelete)
            } ?: showError("No News to delete.")
        }

        // Khởi tạo trạng thái ban đầu của các nút FAB
        setVisibility(clicked)
        setClickable(clicked)

        // Tải và hiển thị tin tức mới nhất khi fragment được tạo
        fetchAndDisplayLatestNews()
    }

    // Hàm để xử lý click vào nút FAB chính (btnMain)
    private fun onMainButtonClicked() {
        clicked = !clicked
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
    }

    // Hàm để đặt khả năng hiển thị của các nút FAB phụ
    private fun setVisibility(clicked: Boolean) {
        binding.btnAdd.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding.btnUpdate.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding.btnDelete.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
    }

    // Hàm để áp dụng animation cho các nút FAB phụ
    private fun setAnimation(clicked: Boolean) {
        binding.btnAdd.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnUpdate.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnDelete.startAnimation(if (!clicked) fromBottom else toBottom)
        binding.btnMain.startAnimation(if (!clicked) rotateOpen else rotateClose)
    }

    // Hàm để đặt khả năng click của các nút FAB phụ
    private fun setClickable(clicked: Boolean) {
        binding.btnAdd.isClickable = !clicked
        binding.btnUpdate.isClickable = !clicked
        binding.btnDelete.isClickable = !clicked
    }

    // Hàm để hiển thị dialog CRUD
    private fun showCrudDialog(news: News? = null) {
        Crud.showCrudDialog(
            context = requireContext(),
            currentNews = news, // Crud object sẽ tự động ẩn/hiện nút dựa trên currentNews
            onAdd = { newNews ->
                onAddNews(newNews)
            },
            onUpdate = { updatedNews ->
                onUpdateNews(updatedNews)
            },
            onDelete = { newsToDelete ->
                onDeleteNews(newsToDelete)
            },
            onCancel = {
            }
        )
    }

    // Hàm để tải tin tức mới nhất từ Firebase và hiển thị lên Fragment
    private fun fetchAndDisplayLatestNews() {
        // Sử dụng addValueEventListener để lắng nghe thay đổi theo thời gian thực
        newsDatabaseRef.orderByKey().limitToLast(1) // Lấy tin tức mới nhất (giả định key tăng dần)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val newsSnapshot = snapshot.children.first()
                        val news = newsSnapshot.getValue(News::class.java)
                        news?.let {
                            it.firebaseKey = newsSnapshot.key // Gán Firebase key
                            _currentDisplayedNews = it // Lưu tin tức đang hiển thị
                            displayNewsOnFragment(it) // Hiển thị tin tức lên UI
                        } ?: run {
                            showError("Faild to fetch News.")
                            displayNewsOnFragment(null) // Xóa hiển thị nếu có lỗi
                        }
                    } else {
                        displayNewsOnFragment(null) // Xóa hiển thị nếu không có tin tức
                        _currentDisplayedNews = null // Đặt lại tin tức đang hiển thị
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AdminNewsFragment", "Database Error: ${error.message}")
                    showError("Error fetching news: ${error.message}")
                    displayNewsOnFragment(null)
                    _currentDisplayedNews = null
                }
            })
    }

    // Hàm để hiển thị thông tin tin tức lên các TextView và ImageView của Fragment
    private fun displayNewsOnFragment(news: News?) {
        if (news != null) {
            binding.tvTitle.text = news.title ?: "No title"
            binding.tvDecription.text = news.decription ?: "No description"
            binding.tvDate.text = news.date ?: "No Date"
            // Tải ảnh. Hiện tại XML có imgNews với background drawable.
            // Nếu bạn có URL ảnh trong News object, hãy dùng Glide ở đây.
            // Ví dụ: Glide.with(this).load(news.imageUrl).into(binding.imgNews)
            // Hiện tại, tôi sẽ giữ nguyên background mặc định hoặc đặt một placeholder.
            binding.imgNews.setImageResource(R.drawable.ic_laucher)
        } else {
            // Xóa nội dung hoặc hiển thị trạng thái rỗng
            binding.tvTitle.text = "No title"
            binding.tvDecription.text = "Please add more news"
            binding.tvDate.text = ""
            binding.imgNews.setImageResource(R.drawable.ic_laucher) // Đặt ảnh placeholder
        }
    }

    // Các hàm xử lý logic CRUD thực tế cho Tin tức (tương tác với Firebase)
    private fun onAddNews(news: News) {
        val newNewsData: HashMap<String, Any?> = hashMapOf(
            "title" to news.title,
            "decription" to news.decription,
            "date" to news.date
        )

        newsDatabaseRef.push().setValue(newNewsData)
            .addOnSuccessListener {
                Log.d("Firebase", "Admin: News added successfully.")
                showSuccess("News added successfully!")
                // Sau khi thêm, fetch lại để hiển thị tin tức mới nhất
                // fetchAndDisplayLatestNews() // addValueEventListener sẽ tự động cập nhật
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Admin: Error adding news: ${e.message}", e)
                showError("Failed to add News: ${e.message}")
            }
    }

    private fun onUpdateNews(news: News) {
        // Đảm bảo có firebaseKey để cập nhật đúng tin tức
        news.firebaseKey?.let { key ->
            val updatedNewsData: HashMap<String, Any?> = hashMapOf(
                "title" to news.title,
                "decription" to news.decription,
                "date" to news.date
            )

            newsDatabaseRef.child(key).updateChildren(updatedNewsData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Admin: News updated successfully.")
                    showSuccess("News updated successfully!")
                    // Sau khi cập nhật, fetch lại để hiển thị tin tức mới nhất
                    // fetchAndDisplayLatestNews() // addValueEventListener sẽ tự động cập nhật
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Admin: Error updating news: ${e.message}", e)
                    showError("Faild to update news: ${e.message}")
                }
        } ?: showError("Can't find News to update.")
    }

    private fun onDeleteNews(news: News) {
        // Đảm bảo có firebaseKey để xóa đúng tin tức
        news.firebaseKey?.let { key ->
            newsDatabaseRef.child(key).removeValue()
                .addOnSuccessListener {
                    Log.d("Firebase", "Admin: News deleted successfully.")
                    showSuccess("News deleted successfully!")
                    // Sau khi xóa, fetch lại để hiển thị tin tức mới nhất (hoặc trống nếu không còn)
                    // fetchAndDisplayLatestNews() // addValueEventListener sẽ tự động cập nhật
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Admin: Error deleting news: ${e.message}", e)
                    showError("Failed to delete news: ${e.message}")
                }
        } ?: showError("No News to delete.")
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Đảm bảo hủy listener khi Fragment bị hủy để tránh rò rỉ bộ nhớ
        // (Nếu fetchAndDisplayLatestNews() sử dụng addValueEventListener, nó sẽ tự động được gỡ khi fragment bị hủy)
    }
}