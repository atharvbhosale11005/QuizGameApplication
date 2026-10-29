package com.example.quizgameapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizgameapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {

    lateinit var signUpBinding: ActivitySignUpBinding

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpBinding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signUpBinding.buttonSignUp.setOnClickListener {
            val email = signUpBinding.editTextSignUpEmail.text.toString().trim()
            val password = signUpBinding.editTextSignUpPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signupWithFirebase(email, password)
            } else {
                Toast.makeText(applicationContext, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        signUpBinding.loginLink.setOnClickListener {
            finish()
        }
    }

    fun signupWithFirebase(email: String, password: String) {

        signUpBinding.progressBar.visibility = View.VISIBLE
        signUpBinding.buttonSignUp.isClickable = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                signUpBinding.progressBar.visibility = View.INVISIBLE
                signUpBinding.buttonSignUp.isClickable = true

                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Your account has been created", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
