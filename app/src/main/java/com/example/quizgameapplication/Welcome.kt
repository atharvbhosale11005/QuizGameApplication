package com.example.quizgameapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizgameapplication.databinding.ActivityWelcomeBinding

class Welcome : AppCompatActivity() {

    lateinit var splashBinding : ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashBinding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val alphaAnimation = android.view.animation.AnimationUtils.loadAnimation(applicationContext,R.anim.splash_anim)
        splashBinding.textViewSplash.startAnimation(alphaAnimation)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(this@Welcome, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 5000)

    }
}