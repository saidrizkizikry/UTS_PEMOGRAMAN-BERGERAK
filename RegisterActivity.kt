package com.example.memeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memeapp.databinding.ActivityRegisterBinding
import com.example.memeapp.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersRef by lazy {
        FirebaseDatabase.getInstance().getReference("users")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupButton()
    }

    private fun setupButton() {
        binding.signUpBtn.setOnClickListener {
            registerUser()
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun registerUser() {
        val username       = binding.usernameSignUp.text.toString().trim()
        val email          = binding.emailPengguna.text.toString().trim()
        val password       = binding.passwordSingUp.text.toString()

        when {
            username.isEmpty() -> {
                binding.usernameSignUp.error = "Enter username"
                binding.usernameSignUp.requestFocus()
                return
            }

            email.isEmpty() -> {
                binding.emailPengguna.error = "Enter email"
                binding.emailPengguna.requestFocus()
                return
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailPengguna.error = "Enter a valid email"
                binding.emailPengguna.requestFocus()
                return
            }

            password.isEmpty() -> {
                binding.passwordSingUp.error = "Enter password"
                binding.passwordSingUp.requestFocus()
                return
            }

            password.length < 6 -> {
                binding.passwordSingUp.error = "Password must be at least 6 characters"
                binding.passwordSingUp.requestFocus()
                return
            }

            else -> {
                registerFirebase(email, password, username)
            }
        }
    }

    private fun registerFirebase(email: String, password: String, username: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val firebaseUser = mAuth.currentUser
                    val uid = firebaseUser?.uid ?: ""
                    val user = UserModel(
                        userId       = uid,
                        username     = username,
                        userEmail    = email,
                        userPassword = password
                    )
                    usersRef.child(uid)
                        .setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@RegisterActivity, OnBoardingActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { dbEx ->
                            Toast.makeText(
                                this@RegisterActivity,
                                "Failed to save user data: ${dbEx.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed: ${authTask.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

}