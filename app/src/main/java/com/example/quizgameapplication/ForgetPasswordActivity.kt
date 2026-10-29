package com.example.quizgameapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizgameapplication.databinding.ActivityForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {

    lateinit var forgetPasswordBinding: ActivityForgetPasswordBinding
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forgetPasswordBinding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(forgetPasswordBinding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        forgetPasswordBinding.buttonResetPassword.setOnClickListener {
            val email = forgetPasswordBinding.editTextForgotPasswordEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(applicationContext, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword(email: String) {
        forgetPasswordBinding.progressBarForgotPassword.visibility = View.VISIBLE
        forgetPasswordBinding.buttonResetPassword.isClickable = false

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            forgetPasswordBinding.progressBarForgotPassword.visibility = View.INVISIBLE
            forgetPasswordBinding.buttonResetPassword.isClickable = true

            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Password reset email sent", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}