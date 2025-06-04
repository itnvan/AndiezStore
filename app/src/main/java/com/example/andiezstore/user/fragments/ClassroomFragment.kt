package com.example.andiezstore.user.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.andiezstore.R
import com.example.andiezstore.databinding.FragmentClassroomBinding // Đảm bảo bạn sử dụng ViewBinding cho fragment_classroom.xml
import com.example.andiezstore.user.adapter.ClassAdapter
import com.example.andiezstore.user.model.Classroom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale // Thêm import này

class ClassroomFragment : Fragment() {
    private var _binding: FragmentClassroomBinding? = null
    private val binding get() = _binding!!
    private lateinit var classAdapter: ClassAdapter
    private var firebaseRefUsers: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")
    private var firebaseRefSubjects: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Subjects")

    private val fullClassroomList = mutableListOf<Classroom>()
    private var subjectQuantityMap =
        mutableMapOf<String, Int>()

    private var searchPopupWindow: PopupWindow? = null
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassroomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            fetchInitialData(uid)
        } else {
            Toast.makeText(requireContext(), "User not logged in. Please login.", Toast.LENGTH_LONG).show()
            // findNavController().navigate(R.id.action_classroomFragment_to_loginFragment)
        }
    }

    private fun setupToolbar() {
        binding.toolbarClassroom.setNavigationOnClickListener {
            try {
                findNavController().navigate(R.id.action_classroomFragment_to_homeFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not navigate back", Toast.LENGTH_SHORT).show()
            }
        }

        val searchIconView = binding.toolbarClassroom.findViewById<View>(R.id.imgSearchAction)
        searchIconView?.setOnClickListener { anchorView ->
            Log.d("ClassroomFragment", "Search icon clicked")
            if (searchPopupWindow?.isShowing == true) {
                searchPopupWindow?.dismiss()
            } else {
                showSearchPopup(anchorView)
            }
        }
        binding.toolbarClassroom.title = "Classrooms"
    }

    private fun showSearchPopup(anchorView: View) {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_search_classroom, null)

        val edtPopupSearch = popupView.findViewById<EditText>(R.id.edtPopupSearchClassroom)
        edtPopupSearch.setText(currentSearchQuery)
        edtPopupSearch.requestFocus()

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        edtPopupSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                if(::classAdapter.isInitialized) { // Kiểm tra classAdapter đã được khởi tạo chưa
                    classAdapter.filter(currentSearchQuery)
                    updateEmptyStateVisibility()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        edtPopupSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                imm?.hideSoftInputFromWindow(edtPopupSearch.windowToken, 0)
                searchPopupWindow?.dismiss()
                true
            } else {
                false
            }
        }

        searchPopupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // Focusable
        )
        searchPopupWindow?.setBackgroundDrawable(BitmapDrawable())
        searchPopupWindow?.isOutsideTouchable = true

        searchPopupWindow?.setOnDismissListener {
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
            if (edtPopupSearch.text.toString().isEmpty() && currentSearchQuery.isNotEmpty()) {
                currentSearchQuery = ""
                if(::classAdapter.isInitialized) {
                    classAdapter.filter("")
                    updateEmptyStateVisibility()
                }
            }
        }

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Căn chỉnh nó ở bên phải của anchorView.
        val xOffset = anchorView.width - (popupView.measuredWidth ?: 300) // Ước lượng chiều rộng popup nếu chưa đo được
        val yOffset = 0 // Sát dưới anchorView
        searchPopupWindow?.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)


    }


    private fun setupRecyclerView() {
        binding.rcvClass.layoutManager = LinearLayoutManager(context)
        classAdapter = ClassAdapter(emptyList()) // Khởi tạo với danh sách rỗng
        binding.rcvClass.adapter = classAdapter
    }

    private fun fetchInitialData(uid: String) {
        classroomsFetched = false
        subjectQuantitiesFetched = false
        fullClassroomList.clear()
        subjectQuantityMap.clear()
        currentSearchQuery = ""

        fetchSubjectQuantity()
        fetchClassrooms(uid)
    }


    private fun fetchSubjectQuantity() {
        firebaseRefSubjects.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val subjectName = childSnapshot.key
                    val quantityS = childSnapshot.child("quantityS").getValue(Int::class.java)
                    if (subjectName != null && quantityS != null) {
                        subjectQuantityMap[subjectName] = quantityS
                    }
                }
                subjectQuantitiesFetched = true
                Log.d("ClassroomFragment", "Subject quantities fetched: ${subjectQuantityMap.size}")
                updateAdapterDataIfReady()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ClassroomFragment", "Error fetching subject quantity: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load subject info: ${error.message}", Toast.LENGTH_SHORT).show()
                subjectQuantitiesFetched = true
                updateAdapterDataIfReady()
            }
        })
    }

    private var classroomsFetched = false
    private var subjectQuantitiesFetched = false

    private fun fetchClassrooms(uid: String) {
        firebaseRefUsers.child(uid).child("classrooms")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fullClassroomList.clear() // Xóa danh sách cũ trước khi thêm mới
                    for (childSnapshot in snapshot.children) {
                        val subjectName = childSnapshot.child("subject").getValue(String::class.java)
                        val description = childSnapshot.child("description").getValue(String::class.java)
                        val timeStart = childSnapshot.child("timeStart").getValue(String::class.java)
                        val starCount = childSnapshot.child("starCount").getValue(String::class.java) ?: "0"
                        val quantityE = childSnapshot.child("quantityE").getValue(Int::class.java) ?: 0
                        if (subjectName != null) {
                            val classroom = Classroom(
                                subject = subjectName,
                                description = description,
                                timeStart = timeStart,
                                tvStar = starCount,
                                quantityE = quantityE
                            )
                            fullClassroomList.add(classroom)
                        }
                    }
                    classroomsFetched = true
                    Log.d("ClassroomFragment", "Classrooms fetched: ${fullClassroomList.size}")
                    updateAdapterDataIfReady()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClassroomFragment", "Error fetching classrooms: ${error.message}")
                    Toast.makeText(requireContext(), "No classroom available or error loading.", Toast.LENGTH_SHORT).show()
                    classroomsFetched = true
                    updateAdapterDataIfReady()
                }
            })
    }

    private fun updateAdapterDataIfReady() {
        if (classroomsFetched && subjectQuantitiesFetched) {
            Log.d("ClassroomFragment", "Both data sources fetched. Updating adapter.")
            val processedList = mutableListOf<Classroom>()
            for (classroom in fullClassroomList) {
                val quantityS = subjectQuantityMap[classroom.subject] ?: 0
                // Đảm bảo Classroom có trường quantityS là var hoặc dùng copy()
                val updatedClassroom = if (classroom.quantityS != quantityS) {
                    classroom.copy(quantityS = quantityS)
                } else {
                    classroom
                }
                processedList.add(updatedClassroom)
            }
            if(::classAdapter.isInitialized) {
                classAdapter.setData(processedList)
                classAdapter.filter(currentSearchQuery)
                updateEmptyStateVisibility()
                Log.d("ClassroomFragment", "Adapter data set. Empty view visibility: ${binding.tvEmptyClassroom.visibility}")
            }
        } else {
            Log.d("ClassroomFragment", "Waiting for data: classroomsFetched=$classroomsFetched, subjectQuantitiesFetched=$subjectQuantitiesFetched")
        }
    }

    private fun updateEmptyStateVisibility() {
        if(!::classAdapter.isInitialized) return // Tránh lỗi nếu adapter chưa sẵn sàng

        val isSearchQueryEmpty = currentSearchQuery.isEmpty()
        val isOriginalListEmpty = classAdapter.getOriginalListSizeForEmptyCheck() == 0
        val isDisplayedListEmpty = classAdapter.getDisplayedListSize() == 0

        if (isSearchQueryEmpty) {
            binding.tvEmptyClassroom.visibility = if (isOriginalListEmpty) View.VISIBLE else View.GONE
            if(isOriginalListEmpty) binding.tvEmptyClassroom.text = "No classrooms available."
        } else {
            binding.tvEmptyClassroom.visibility = if (isDisplayedListEmpty) View.VISIBLE else View.GONE
            if(isDisplayedListEmpty) binding.tvEmptyClassroom.text = "No classrooms found for '$currentSearchQuery'."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchPopupWindow?.dismiss()
        _binding = null
    }
}
