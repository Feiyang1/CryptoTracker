package com.sc.fyc.cryptoalert

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sc.fyc.cryptoalert.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), AddDialogFragment.AddDialogListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding
    private val alerts: MutableList<Alert> = mutableListOf()
    private lateinit var alertsAdapter: AlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signinButton.setOnClickListener {
            println("signin clicked!")

            var email = binding.email.text.toString()
            var pwd = binding.pwd.text.toString()

            signIn(email, pwd)
        }

        binding.signoutButton.setOnClickListener {
            println("signout clicked!")
            signOut()
        }

        binding.addButton.setOnClickListener {
            showAddDialog()
        }

        alertsAdapter = AlertAdapter(this, alerts)
        binding.alerts.adapter = alertsAdapter
        binding.alerts.setHasFixedSize(true)

        auth = Firebase.auth;
        firestore = Firebase.firestore
    }


    override fun onStart() {
        println("noStart called awelrjelkwarjlkewjrlewalkjrewalk")
        super.onStart()
        val currentUser = auth.currentUser

        updateUI(currentUser)
        if (currentUser != null) {
            getAlerts(currentUser.uid)
        }
    }

    private fun showAddDialog() {
        val dialog = AddDialogFragment()
        dialog.show(supportFragmentManager, "AddDialogFragment")
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) { // user signed in
            binding.username.text =  if (user.displayName != null && user.displayName != "") user.displayName else user.email
            println("username set to ${binding.username.text}")
            binding.signinView.visibility = View.GONE
            binding.signoutView.visibility = View.VISIBLE
        } else {
            binding.signinView.visibility = View.VISIBLE
            binding.signoutView.visibility = View.GONE
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        println("signInWithEmail:success")
                        val user = auth.currentUser

                        if (user != null) {
                            getAlerts(user.uid)
                        }

                        updateUI(user)
                    } else {
                        Log.w("signInWithEmail:failure", task.exception)
                        updateUI(null)
                    }

                }
    }

    private fun signOut() {
        auth.signOut()
        updateUI(null)
        clearState()
    }

    private fun clearState() {
        alerts.clear()
        alertsAdapter.notifyDataSetChanged()
    }

    private fun getAlerts(userId: String) {
        firestore.collection("alerts").whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->

                // there should be only 1 document per user
                documents.map { document ->
                    Log.w("TAG", "got data: $document.data")

                    val data = document.toObject<RemoteAlertDoc>(RemoteAlertDoc::class.java)
                    alerts.clear()
                    alerts.addAll(data.alerts)
                    alertsAdapter.notifyDataSetChanged()
                    for(al in alerts) {
                        Log.w("TAG", "datatatatatatata: ${al.symbol} - ${al.price}")
                    }
                }

            }
            .addOnFailureListener{ exception ->
                Log.w("TAG", "Error getting documents: ", exception)
            }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
    }
}
