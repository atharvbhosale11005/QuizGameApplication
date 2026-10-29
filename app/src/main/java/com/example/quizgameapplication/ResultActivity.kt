package com.example.quizgameapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizgameapplication.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResultActivity : AppCompatActivity() {

    lateinit var resutBinding: ActivityResultBinding

    val database = FirebaseDatabase.getInstance("https://quizgame-1c04f-default-rtdb.asia-southeast1.firebasedatabase.app")
    val databaseReference = database.reference.child("scores")

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var userCorrect = ""
    var userWrong = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        resutBinding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(resutBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user?.let {
                    val userIUD = it.uid

                    userCorrect = snapshot.child(userIUD).child("correct").value.toString()
                    userWrong = snapshot.child(userIUD).child("wrong").value.toString()

                    resutBinding.textViewScoreCorrect.text = userCorrect
                    resutBinding.textViewScoreWrong.text = userWrong
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        resutBinding.buttonPlayAgain.setOnClickListener {
            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        resutBinding.buttonExit.setOnClickListener {
            finish()
        }


    }
}