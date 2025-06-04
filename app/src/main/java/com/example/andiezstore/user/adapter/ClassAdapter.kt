package com.example.andiezstore.user.adapter

import android.annotation.SuppressLint // Thêm annotation này nếu bạn muốn dùng ở cấp class, hoặc giữ ở cấp method
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.Classroom
import java.util.Locale // Import cần thiết cho lowercase

// Sửa constructor để nhận initialList và khởi tạo các danh sách nội bộ
class ClassAdapter(initialList: List<Classroom>) :
    RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {

    private var originalClassroomList = mutableListOf<Classroom>()
    private var displayedClassroomList = mutableListOf<Classroom>()
    private var currentQuery: String = ""

    init {
        // Khởi tạo danh sách gốc và danh sách hiển thị từ initialList
        originalClassroomList.addAll(initialList)
        displayedClassroomList.addAll(initialList)
    }

    class ClassViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(classroom: Classroom) {
            subjects.text = classroom.subject
            subjectDecrips.text = classroom.description
            starCounts.text = classroom.tvStar
            times.text = classroom.timeStart
            quantityS.text = classroom.quantityS.toString()
            quantityE.text = classroom.quantityE.toString()
            // Xử lý imgClasroom an toàn hơn
            classroom.imgClasroom?.let { imgClasss.setImageResource(it) }
                ?: imgClasss.setImageResource(R.drawable.view3)
        }

        // Khai báo các View
        private val subjects: TextView = view.findViewById(R.id.tvSubjects)
        private val subjectDecrips: TextView = view.findViewById(R.id.tvDecription)
        private val starCounts: TextView = view.findViewById(R.id.tvStar)
        private val times: TextView = view.findViewById(R.id.tvTime)
        private val quantityS: TextView = view.findViewById(R.id.tvQuantity1)
        private val imgClasss: ImageView = view.findViewById(R.id.imgClass)
        private val quantityE: TextView = view.findViewById(R.id.tvQuantity2)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ClassViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        // Sử dụng displayedClassroomList để bind dữ liệu
        if (position < displayedClassroomList.size) { // Kiểm tra giới hạn an toàn
            val classroom = displayedClassroomList[position]
            holder.bind(classroom) // Gọi hàm bind (hoặc onBind nếu bạn giữ tên cũ)
        }
    }

    override fun getItemCount(): Int {
        // Trả về kích thước của danh sách đang được hiển thị
        return displayedClassroomList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newClassrooms: List<Classroom>) {
        originalClassroomList.clear()
        originalClassroomList.addAll(newClassrooms)
        filter(currentQuery)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        currentQuery = query.trim()
        displayedClassroomList.clear()

        if (currentQuery.isEmpty()) {
            displayedClassroomList.addAll(originalClassroomList)
        } else {
            val searchQuery = currentQuery.lowercase(Locale.getDefault())
            for (classroom in originalClassroomList) {
                if (classroom.subject?.lowercase(Locale.getDefault())
                        ?.contains(searchQuery) == true
                ) {
                    displayedClassroomList.add(classroom)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun getOriginalListSizeForEmptyCheck(): Int {
        return originalClassroomList.size
    }

    fun getDisplayedListSize(): Int {
        return displayedClassroomList.size
    }
}
