package com.example.andiezstore.user.adapter

import android.annotation.SuppressLint
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

open class SubjectAdapter(
    private val listSubject: MutableList<Subject>,
    private val context: Context,
) :
    RecyclerView.Adapter<SubjectAdapter.SubjectViewModel>() {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private val subjectDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Subjects")
    private val auth = FirebaseAuth.getInstance()
    private val appliedSubjectsByUser =
        mutableSetOf<String>() // Track subjects applied by the current user
    private val subjectQuantityMap = mutableMapOf<String, Int>() // Cache for subject quantities

    init {
        // Fetch the list of subjects applied by the current user
        auth.currentUser?.uid?.let { uid ->
            database.child(uid).child("classrooms")
                .addValueEventListener(object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        appliedSubjectsByUser.clear()
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.child("subject").getValue(String::class.java)?.let {
                                appliedSubjectsByUser.add(it)
                            }
                        }
                        notifyDataSetChanged() // Rebind the view holders to update the "Applied" state
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error fetching applied subjects: ${error.message}")
                    }
                })
        }

        // Fetch and listen for changes in subject quantities
        subjectDatabase.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                subjectQuantityMap.clear()
                for (childSnapshot in snapshot.children) {
                    val subjectName = childSnapshot.key
                    val quantity =
                        childSnapshot.child("quantityS").getValue(Long::class.java)?.toInt() ?: 0
                    subjectName?.let { subjectQuantityMap[it] = quantity }
                }
                notifyDataSetChanged() // Rebind the view holders to update the quantities
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching subject quantities: ${error.message}")
            }
        })
    }

    class SubjectViewModel(val view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvSubjects)
        val tvDecription: TextView = view.findViewById(R.id.tvDecription)
        val timeStart: TextView = view.findViewById(R.id.tvStar)
        val timeEnd: TextView = view.findViewById(R.id.tvTime)
        val imgSubject: ImageView = view.findViewById(R.id.imgSubject)
        val btnSubject: Button = view.findViewById(R.id.btnSubject)
        val quantityS: TextView = view.findViewById(R.id.tvQuantity1)
        val quantityE: TextView = view.findViewById(R.id.tvQuantity2)

        fun onBind(
            subject: Subject,
            onAddClickListener: (Subject) -> Unit,
            isApplied: Boolean,
            quantity: Int
        ) {
            tvSubject.text = subject.subject
            tvDecription.text = subject.description
            timeStart.text = subject.timeStart
            timeEnd.text = subject.tvStar
            quantityS.text = quantity.toString()
            quantityE.text = subject.quantityE.toString()
            subject.imgSubject?.let { imgSubject.setImageResource(it) }
            btnSubject.text = if (isApplied) "Applied" else "Apply"
            btnSubject.isEnabled = !isApplied
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
        val isApplied = appliedSubjectsByUser.contains(subject.subject)
        val currentQuantity = subjectQuantityMap[subject.subject] ?: subject.quantityS ?: 0

        holder.onBind(subject, { clickedSubject ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                if (!isApplied) {
                    Util.showDialog(context, "Wait a second...")
                    addSubjectToUserAndIncrementQuantity(
                        currentUser.uid,
                        clickedSubject
                    ) { isSuccess ->
                        Util.hideDialog()
                        if (isSuccess) {
                            Toast.makeText(
                                context,
                                "Success to apply ${clickedSubject.subject}",
                                Toast.LENGTH_SHORT
                            ).show()
                            appliedSubjectsByUser.add(clickedSubject.subject!!)
                            notifyItemChanged(position)
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to apply ${clickedSubject.subject}",
                                Toast.LENGTH_SHORT
                            ).show()
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
        }, isApplied, currentQuantity)
    }

    override fun getItemCount(): Int {
        return listSubject.size
    }

    private fun addSubjectToUserAndIncrementQuantity(
        uid: String,
        subject: Subject,
        onComplete: (Boolean) -> Unit
    ) {
        val subjectData = mapOf(
            "subject" to subject.subject,
            "description" to subject.description,
            "timeStart" to subject.timeStart,
            "starCount" to subject.tvStar,
            "quantityE" to subject.quantityE,
            "imgSubject" to subject.imgSubject
        )

        val userClassroomRef =
            database.child(uid).child("classrooms").child(subject.subject.toString())
        userClassroomRef.setValue(subjectData)
            .addOnSuccessListener {
                Log.d("RealtimeDB", "Subject '${subject.subject}' added to user $uid")
                incrementQuantityS(subject.subject.toString()) { isSuccess ->
                    onComplete(isSuccess)
                }
            }
            .addOnFailureListener { e ->
                Log.e("RealtimeDB", "Error adding subject to user $uid: ${e.message}", e)
                onComplete(false)
            }
    }

    private fun incrementQuantityS(subjectKey: String, onComplete: (Boolean) -> Unit) {
        val subjectRef =
            subjectDatabase.child(subjectKey).child("quantityS") // Reference to quantityS
        subjectRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentQuantity = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentQuantity + 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("Firebase Transaction", "Transaction failed: ${error.message}")
                    onComplete(false)
                } else if (committed) {
                    Log.d("Firebase Transaction", "QuantityS incremented for $subjectKey")
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
        })
    }

    private fun decrementQuantityS(subjectKey: String, onComplete: (Boolean) -> Unit) {
        val subjectRef =
            subjectDatabase.child(subjectKey).child("quantityS") // Reference to quantityS
        subjectRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentQuantity = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = if (currentQuantity > 0) currentQuantity - 1 else 0
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("Firebase Transaction", "Error decrementing quantityS: ${error.message}")
                    onComplete(false)
                } else if (committed) {
                    Log.d("Firebase Transaction", "QuantityS decremented for $subjectKey")
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
        })
    }

    fun withdrawFromSubject(subject: Subject, position: Int) {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { uid ->
            Util.showDialog(context, "Withdrawing...")
            takeQuantity(uid, subject) { isSuccess ->
                Util.hideDialog()
                if (isSuccess) {
                    appliedSubjectsByUser.remove(subject.subject)
                    Toast.makeText(context, "Withdrawn from ${subject.subject}", Toast.LENGTH_SHORT)
                        .show()
                    notifyItemChanged(position)
                } else {
                    Toast.makeText(context, "Failed to withdraw", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun takeQuantity(uid: String, subject: Subject, onComplete: (Boolean) -> Unit) {
        val userClassroomRef =
            database.child(uid).child("classrooms").child(subject.subject.toString())

        userClassroomRef.removeValue()
            .addOnSuccessListener {
                Log.d("RealtimeDB", "Subject '${subject.subject}' removed from user $uid")
                decrementQuantityS(subject.subject.toString()) { isSuccess ->
                    onComplete(isSuccess)
                }
            }
            .addOnFailureListener { e ->
                Log.e("RealtimeDB", "Error removing subject from user $uid: ${e.message}", e)
                onComplete(false)
            }
    }
}
