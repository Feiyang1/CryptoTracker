package com.sc.fyc.cryptoalert

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
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

        // initialize networking lib
        AndroidNetworking.initialize(applicationContext)

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
            listenToAlerts(currentUser.uid)
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
                            listenToAlerts(user.uid)
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

    private fun listenToAlerts(userId: String) {
        firestore.collection("users/$userId/alerts").addSnapshotListener{snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Error listening to snapshot ", e)
            }

            alerts.clear()
            for (document in snapshot!!) {
                val alert = document.toObject<Alert>(Alert::class.java)
                alerts.add(alert)
            }

            for(al in alerts) {
                Log.w("TAG", "datatatatatatata: ${al.symbol} - ${al.price}")
            }
            alertsAdapter.notifyDataSetChanged()
        }
    }

    private fun addAlert(userId: String, alert: Alert) {
        firestore.collection("users/$userId/alerts").add(alert)
    }

    override fun onDialogPositiveClick(alert: Alert) {
        // User touched the dialog's positive button
        println("Add new alert, ${alert.price}, ${alert.symbol}, ${alert.type}")

        val currentUser = auth.currentUser
        if(currentUser != null) {
            addAlert(currentUser.uid, alert)

        }
    }

    override fun onDialogNegativeClick() {
        // User touched the dialog's negative button
    }
}
