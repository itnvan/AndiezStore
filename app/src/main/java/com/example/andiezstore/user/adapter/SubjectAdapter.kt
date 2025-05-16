package com.example.andiezstore.user.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.andiezstore.R
import com.example.andiezstore.user.model.Subject
import com.example.andiezstore.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

open class SubjectAdapter(
    private val listSubject: MutableList<Subject>,
    private val context: Context,
    private val onSubjectUpdate: (String, Subject) -> Unit
) :
    RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {
    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private val subjectDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Subjects")
    private val auth = FirebaseAuth.getInstance()
    private val appliedSubjectsByUser = mutableSetOf<String>()
    private val subjectQuantityMap = mutableMapOf<String, Int>()
    private val subjectKeyMap = mutableMapOf<String, String>()

    init {
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
                        notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error fetching applied subjects: ${error.message}")
                    }
                })
        }

        subjectDatabase.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                subjectQuantityMap.clear()
                subjectKeyMap.clear()
                for (childSnapshot in snapshot.children) {
                    val subjectName = childSnapshot.key
                    val quantity =
                        childSnapshot.child("quantityS").getValue(Long::class.java)?.toInt() ?: 0
                    subjectName?.let {
                        subjectQuantityMap[it] = quantity
                        subjectKeyMap[it] = childSnapshot.key!!
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching subject quantities: ${error.message}")
            }
        })
    }

    class SubjectViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvSubjects)
        val tvDescription: TextView = view.findViewById(R.id.tvDecription)
        val timeStart: TextView = view.findViewById(R.id.tvStar)
        val timeEnd: TextView = view.findViewById(R.id.tvTime)
        val btnSubject: Button = view.findViewById(R.id.btnSubject)
        val quantityS: TextView = view.findViewById(R.id.tvQuantity1)
        val quantityE: TextView = view.findViewById(R.id.tvQuantity2)
        val btnUpdate: Button? = view.findViewById(R.id.btnUpdate)

        fun onBind(
            subject: Subject,
            onAddClickListener: (Subject) -> Unit,
            onUpdateClickListener: (Subject) -> Unit,
            isApplied: Boolean,
            quantity: Int,
            isUpdateEnable: Boolean
        ) {
            tvSubject.text = subject.subject
            tvDescription.text = subject.description
            timeStart.text = subject.timeStart
            timeEnd.text = subject.tvStar
            quantityS.text = quantity.toString()
            quantityE.text = subject.quantityE.toString()
            btnSubject.text = if (isApplied) "Applied" else "Apply"
            btnSubject.isEnabled = !isApplied
            btnSubject.setOnClickListener {
                onAddClickListener(subject)
            }
            btnUpdate?.visibility = if (isUpdateEnable) View.VISIBLE else View.GONE
            btnUpdate?.setOnClickListener {
                onUpdateClickListener(subject)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = listSubject[position]
        val isApplied = appliedSubjectsByUser.contains(subject.subject)
        val currentQuantity = subjectQuantityMap[subject.subject] ?: subject.quantityS ?: 0
        holder.onBind(subject, { clickedSubject ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                if (!isApplied) {
                    Util.showDialog(context, "Applying...") // Keep dialog here
                    addSubjectToUserAndIncrementQuantity(
                        currentUser.uid,
                        clickedSubject
                    ) { isSuccess ->  // Callback for completion
                        Util.hideDialog()
                        if (isSuccess) {
                            Toast.makeText(
                                context,
                                "Success to apply ${clickedSubject.subject}",
                                Toast.LENGTH_SHORT
                            ).show()
                            appliedSubjectsByUser.add(clickedSubject.subject!!)
                            notifyItemChanged(position) // Update the specific item
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
        }, { subject ->
            val subjectKey = subjectKeyMap[subject.subject]
            if (subjectKey != null) {
                onSubjectUpdate(subjectKey, subject)
            } else {
                Toast.makeText(context, "Subject key not found", Toast.LENGTH_SHORT).show()
            }
        }, isApplied, currentQuantity, true)
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
        )

        val userClassroomRef = database.child(uid).child("classrooms").child(subject.subject.toString())

        // Use a transaction to ensure atomicity
        val globalSubjectRef = subjectDatabase.child(subjectKeyMap[subject.subject] ?: "")
        globalSubjectRef.runTransaction(object : Transaction.Handler { // Use runTransaction
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                // Get the current quantity
                val currentQuantity = mutableData.child("quantityS").getValue(Int::class.java) ?: 0
                // Check if there's space
                if (currentQuantity < (subject.quantityS ?: 0)) { // Use the subject's max quantity
                    // Increment quantity
                    mutableData.child("quantityS").value = currentQuantity + 1
                    return Transaction.success(mutableData)
                } else {
                    // No space, abort the transaction
                    return Transaction.abort()
                }
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("Firebase Transaction", "Transaction failed: ${error.message}")
                    onComplete(false)
                    userClassroomRef.removeValue() // remove user's classroom entry on failure
                } else if (committed) {
                    Log.d("Firebase Transaction", "Subject applied and quantity incremented")
                    userClassroomRef.setValue(subjectData) // Set user classroom data
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }

                } else {
                    Log.e("Firebase Transaction", "Transaction was aborted (no space)")
                    Toast.makeText(context, "Subject is full", Toast.LENGTH_SHORT).show()
                    onComplete(false) // Indicate failure
                }
            }
        })
    }
}
