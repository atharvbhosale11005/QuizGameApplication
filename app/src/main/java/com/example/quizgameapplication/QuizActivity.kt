package com.example.quizgameapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizgameapplication.databinding.ActivityQuizBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

data class QuestionModel(
    val q: String = "",
    val a: String = "",
    val b: String = "",
    val c: String = "",
    val d: String = "",
    val answer: String = ""
)

class QuizActivity : AppCompatActivity() {

    private lateinit var quizBinding: ActivityQuizBinding
    val database = FirebaseDatabase.getInstance("https://quizgame-1c04f-default-rtdb.asia-southeast1.firebasedatabase.app")
    val databaseReference = database.reference.child("questions")
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scoreRef = database.getReference("scores")
    private val questionList = mutableListOf<QuestionModel>()
    private var questionIndex = 0
    private var correctAnswer = ""
    private var answerSelected = false
    private var userCorrect = 0
    private var userWrong = 0
    private var countDownTimer: CountDownTimer? = null
    private val questionTime = 60_000L

    val questions = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        quizBinding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(quizBinding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(quizBinding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        do {
            val num = Random.nextInt(1,11)
            questions.add(num)
        }while (questions.size < 5)

        quizBinding.textViewCorrect.text = "0"
        quizBinding.textViewWrong.text = "0"

        setupClickListeners()
        showLoading()
        fetchQuestions()
    }

    private fun setupClickListeners() {

        quizBinding.buttonNext.setOnClickListener {
            if (questionList.isEmpty()) return@setOnClickListener

            if (!answerSelected) {
                Toast.makeText(this, "Please select an answer first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            countDownTimer?.cancel()

            if (questionIndex == questionList.size - 1) {
                showCongratulationsDialog()
            } else {
                questionIndex++
                displayQuestion()
            }
        }

        quizBinding.buttonFinish.setOnClickListener {
            countDownTimer?.cancel()
            sendScore()
        }

        quizBinding.cardOptionA.setOnClickListener { checkAnswer("a") }
        quizBinding.textViewA.setOnClickListener { checkAnswer("a") }

        quizBinding.cardOptionB.setOnClickListener { checkAnswer("b") }
        quizBinding.textViewB.setOnClickListener { checkAnswer("b") }

        quizBinding.cardOptionC.setOnClickListener { checkAnswer("c") }
        quizBinding.textViewC.setOnClickListener { checkAnswer("c") }

        quizBinding.cardOptionD.setOnClickListener { checkAnswer("d") }
        quizBinding.textViewD.setOnClickListener { checkAnswer("d") }
    }

    private fun fetchQuestions() {
        showLoading()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val allQuestions = mutableListOf<QuestionModel>()

                    for (child in snapshot.children) {
                        // Extract fields safely using .value?.toString() to handle both String and Number types in Firebase
                        val q = child.child("q").value?.toString()?.trim() ?: ""
                        val a = child.child("a").value?.toString()?.trim() ?: ""
                        val b = child.child("b").value?.toString()?.trim() ?: ""
                        val c = child.child("c").value?.toString() ?: ""
                        val d = child.child("d").value?.toString() ?: ""
                        val answer = child.child("answer").value?.toString()?.trim() ?: ""

                        if (q.isNotBlank()) {
                            val model = QuestionModel(q, a, b, c, d, answer)
                            allQuestions.add(model)
                        }
                    }

                    questionList.clear()
                    for (num in questions) {
                        val idx = num - 1
                        if (idx >= 0 && idx < allQuestions.size) {
                            questionList.add(allQuestions[idx])
                        }
                    }

                    quizBinding.progressBarQuiz.visibility = View.GONE

                    if (questionList.isEmpty()) {
                        showViews(false)
                        Toast.makeText(
                            this@QuizActivity,
                            "No valid questions found in Firebase.",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    questionIndex = 0
                    userCorrect = 0
                    userWrong = 0

                    quizBinding.textViewCorrect.text = "0"
                    quizBinding.textViewWrong.text = "0"

                    displayQuestion()
                }

                override fun onCancelled(error: DatabaseError) {
                    quizBinding.progressBarQuiz.visibility = View.GONE
                    showViews(false)

                    Toast.makeText(
                        this@QuizActivity,
                        "Failed to load questions: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun displayQuestion() {
        countDownTimer?.cancel()

        if (questionIndex >= questionList.size) {
            showCongratulationsDialog()
            return
        }

        val currentQuestion = questionList[questionIndex]

        correctAnswer = currentQuestion.answer.trim()
            .lowercase()
            .removeSurrounding("\"")

        quizBinding.textViewQuestion.text = "${questionIndex + 1}. ${currentQuestion.q}"

        quizBinding.textViewA.text = currentQuestion.a
        quizBinding.textViewB.text = currentQuestion.b
        quizBinding.textViewC.text = currentQuestion.c
        quizBinding.textViewD.text = currentQuestion.d

        answerSelected = false

        restoreOptions()
        showViews(true)

        quizBinding.progressBarQuiz.visibility = View.GONE
        quizBinding.textViewTime.text = "60"

        startTimer()
    }

    private fun showCongratulationsDialog() {
        val dialog = AlertDialog.Builder(this@QuizActivity)
        dialog.setTitle("Congratulations")
        dialog.setMessage("You have answered all the questions. Would you like to save your score?")
        dialog.setPositiveButton("Yes") { dialogInterface, _ ->
            sendScore()
            dialogInterface.dismiss()
        }
        dialog.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showLoading() {
        quizBinding.progressBarQuiz.visibility = View.VISIBLE
        showViews(false)
    }

    private fun showViews(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.INVISIBLE

        quizBinding.LinearLayoutInfo.visibility = visibility
        quizBinding.cardQuestion.visibility = visibility
        quizBinding.layoutOptions.visibility = visibility
        quizBinding.layoutButtons.visibility = visibility
    }

    private fun checkAnswer(selectedOption: String) {
        if (answerSelected || questionList.isEmpty()) return

        answerSelected = true
        countDownTimer?.cancel()

        val selected = selectedOption.lowercase()

        if (correctAnswer == selected) {
            userCorrect++

            quizBinding.textViewCorrect.text = userCorrect.toString()

            setOptionColor(selected, Color.GREEN)
        } else {
            userWrong++

            quizBinding.textViewWrong.text = userWrong.toString()

            setOptionColor(selected, Color.RED)
            setOptionColor(correctAnswer, Color.GREEN)
        }

        disableOptions()
    }

    private fun setOptionColor(option: String, color: Int) {
        when (option.lowercase()) {
            "a" -> quizBinding.cardOptionA.setCardBackgroundColor(color)
            "b" -> quizBinding.cardOptionB.setCardBackgroundColor(color)
            "c" -> quizBinding.cardOptionC.setCardBackgroundColor(color)
            "d" -> quizBinding.cardOptionD.setCardBackgroundColor(color)
        }
    }

    private fun disableOptions() {
        quizBinding.cardOptionA.isClickable = false
        quizBinding.cardOptionB.isClickable = false
        quizBinding.cardOptionC.isClickable = false
        quizBinding.cardOptionD.isClickable = false

        quizBinding.textViewA.isClickable = false
        quizBinding.textViewB.isClickable = false
        quizBinding.textViewC.isClickable = false
        quizBinding.textViewD.isClickable = false
    }

    private fun restoreOptions() {
        quizBinding.cardOptionA.setCardBackgroundColor(Color.WHITE)
        quizBinding.cardOptionB.setCardBackgroundColor(Color.WHITE)
        quizBinding.cardOptionC.setCardBackgroundColor(Color.WHITE)
        quizBinding.cardOptionD.setCardBackgroundColor(Color.WHITE)

        quizBinding.cardOptionA.isClickable = true
        quizBinding.cardOptionB.isClickable = true
        quizBinding.cardOptionC.isClickable = true
        quizBinding.cardOptionD.isClickable = true

        quizBinding.textViewA.isClickable = true
        quizBinding.textViewB.isClickable = true
        quizBinding.textViewC.isClickable = true
        quizBinding.textViewD.isClickable = true
    }

    private fun startTimer() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(questionTime, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                quizBinding.textViewTime.text =
                    ((millisUntilFinished + 999L) / 1000L).toString()
            }

            override fun onFinish() {
                quizBinding.textViewTime.text = "0"

                if (!answerSelected) {
                    answerSelected = true
                    userWrong++

                    quizBinding.textViewWrong.text = userWrong.toString()

                    setOptionColor(correctAnswer, Color.GREEN)
                    disableOptions()

                    Toast.makeText(this@QuizActivity, "Time is up!", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun openMainActivity() {
        countDownTimer?.cancel()
        val intent = Intent(this@QuizActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    fun sendScore() {
        user?.let {
            val userUID = it.uid
            scoreRef.child(userUID)
                .child("correct")
                .setValue(userCorrect)

            scoreRef.child(userUID)
                .child("wrong")
                .setValue(userWrong).addOnSuccessListener {
                    Toast.makeText(this, "Scores saved Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                    startActivity(intent)
                    finish()
                }
        } ?: run {
            val intent = Intent(this@QuizActivity, ResultActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
