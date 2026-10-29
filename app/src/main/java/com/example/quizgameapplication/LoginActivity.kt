package com.example.quizgameapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizgameapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var loginBinding: ActivityLoginBinding

    lateinit var googleSignInClient : GoogleSignInClient

    lateinit var activityResultLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        val textOfGoogleButton = loginBinding.buttonGoogleSignIn.getChildAt(0) as TextView
        textOfGoogleButton.text = "Continue with Google"
        textOfGoogleButton.textSize = 18F
        textOfGoogleButton.setTextColor(Color.BLACK)

        //register
        registerActivityForGoogleSignIn()

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginBinding.buttonLogin.setOnClickListener {

            val userEmail = loginBinding.editTextLoginEmail.text.toString().trim()
            val userPassword = loginBinding.editTextLoginPassword.text.toString().trim()

            if(userEmail.isNotEmpty() && userPassword.isNotEmpty()){
                signInUser(userEmail,userPassword)
            }
            else{
                Toast.makeText(applicationContext,"Please enter email and password",Toast.LENGTH_SHORT).show()
            }

        }

        loginBinding.buttonGoogleSignIn.setOnClickListener {

            signInWithGoogle()

        }

        loginBinding.textViewSignUp.setOnClickListener {

            val intent = Intent(this@LoginActivity, SignUp::class.java)
            startActivity(intent)

        }

        loginBinding.textViewForgetPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

    }

    fun signInUser(userEmail: String, userPassword: String){
        auth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(applicationContext,"Welcome to Quiz Game",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(applicationContext,task.exception?.localizedMessage,Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser //ALLOWS INFO ABOUT THE USER
        if(user != null){
            Toast.makeText(applicationContext,"Welcome to Quiz Game",Toast.LENGTH_SHORT).show()
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun signInWithGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        signIn()
    }

    private fun signIn(){
        val signInIntent : Intent = googleSignInClient.signInIntent
        activityResultLauncher.launch(signInIntent)
    }

    private fun registerActivityForGoogleSignIn() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val resultCode = result.resultCode
            val data = result.data

            if(resultCode == RESULT_OK && data != null){
                val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                firebaseSignInWithGoogle(task)
            }

        }
    }

    private fun firebaseSignInWithGoogle(task: Task<GoogleSignInAccount>){

        try {
            val account : GoogleSignInAccount = task.getResult(ApiException::class.java)
            firebaseGoogleAccount(account)
        }catch (e: ApiException){
            Toast.makeText(applicationContext,e.localizedMessage,Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseGoogleAccount(account: GoogleSignInAccount){

        val authCredential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(applicationContext,"Welcome to Quiz Game",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
