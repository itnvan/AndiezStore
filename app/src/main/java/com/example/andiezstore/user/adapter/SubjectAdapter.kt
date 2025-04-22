package com.example.andiezstore.user.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.Subject
import com.example.andiezstore.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

open class SubjectAdapter(
    private val listSubject: MutableList<Subject>,
    private val context: Context,
) :
    RecyclerView.Adapter<SubjectAdapter.SubjectViewModel>() {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private val auth = FirebaseAuth.getInstance()
    private val addedSubjects = mutableSetOf<String>() // Lưu trữ tên các môn học đã thêm

    class SubjectViewModel(val view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvSubjects)
        val tvDecription: TextView = view.findViewById(R.id.tvDecription)
        val timeStart: TextView = view.findViewById(R.id.tvStar)
        val timeEnd: TextView = view.findViewById(R.id.tvTime)
        val imgSubject: ImageView = view.findViewById(R.id.imgSubject)
        val btnSubject: Button = view.findViewById(R.id.btnSubject)
        val quantityS: TextView = view.findViewById(R.id.tvQuantity1)
        val quantityE: TextView = view.findViewById(R.id.tvQuantity2)

        fun onBind(subject: Subject, onAddClickListener: (Subject) -> Unit, isAdded: Boolean) {
            tvSubject.text = subject.subject
            tvDecription.text = subject.description
            timeStart.text = subject.timeStart
            timeEnd.text = subject.tvStar
            quantityS.text = subject.quantityS.toString()
            quantityE.text=subject.quantityE.toString()
            subject.imgSubject?.let { imgSubject.setImageResource(it) }
            btnSubject.text = if (isAdded) "Applied" else "Apply"
            btnSubject.isEnabled = !isAdded
            btnSubject.setOnClickListener {
                onAddClickListener(subject)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SubjectViewModel {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return SubjectViewModel(view)
    }

    override fun onBindViewHolder(holder: SubjectViewModel, position: Int) {
        val subject = listSubject[position]
        val isAdded = addedSubjects.contains(subject.subject)
        holder.onBind(subject, { clickedSubject ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                if (!addedSubjects.contains(clickedSubject.subject)) {
                    Util.showDialog(context, "Wait a second...")
                    CoroutineScope(Dispatchers.IO).launch {
                        val isSuccess = addSubjectToUser(currentUser.uid, clickedSubject)
                        withContext(Dispatchers.Main) {
                            Util.hideDialog()
                            if (isSuccess) {
                                addedSubjects.add(clickedSubject.subject.toString())
                                notifyItemChanged(position) // Cập nhật giao diện của item
                                Toast.makeText(
                                    context,
                                    "Success to apply ${clickedSubject.subject}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to apply ${clickedSubject.subject}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "${clickedSubject.subject} has applied ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(context, "You need to login to apply subject", Toast.LENGTH_SHORT)
                    .show()
            }
        }, isAdded)
    }

    override fun getItemCount(): Int {
        return listSubject.size
    }

    private suspend fun addSubjectToUser(uid: String, subject: Subject): Boolean {
        return try {
            val subjectData = mapOf(
                "subject" to subject.subject,
                "description" to subject.description,
                "timeStart" to subject.timeStart,
                "starCount" to subject.tvStar,
                "quantityS" to subject.quantityS,
                "quantityE" to subject.quantityE,
                "imgSubject" to subject.imgSubject
            )
            database.child(uid).child("classrooms").child(subject.subject.toString()).setValue(subjectData).await()
            Log.d("RealtimeDB", "Subject '${subject.subject}' added to user $uid")
            true
        } catch (e: Exception) {
            Log.e("RealtimeDB", "Error adding subject to user $uid: ${e.message}", e)
            false
        }
    }
}