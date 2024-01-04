package com.example.petner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
class LoginActivity : AppCompatActivity() {

    private lateinit var errorMessage: TextView
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        auth = FirebaseAuth.getInstance()
        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val backButton = findViewById<View>(R.id.backButton)
        val errorMessage = findViewById<TextView>(R.id.errorMessage)
        val textViewFindId = findViewById<TextView>(R.id.findId)
        val textViewFindPassword = findViewById<TextView>(R.id.findPassword)
        loginButton.setOnClickListener {
            val id = editTextId.text.toString()
            val password = editTextPassword.text.toString()

            // 아이디 또는 비밀번호가 비어있는지 확인
            if (id.isEmpty()) {
                errorMessage.text = "아이디를 입력해주세요."
                errorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                errorMessage.text = "비밀번호를 입력해주세요."
                errorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Firebase Authentication을 사용하여 로그인 시도
            auth.signInWithEmailAndPassword(id, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공
                        val user: FirebaseUser? = auth.currentUser
                        showToast("로그인 되었습니다.")
                        // 여기에서 원하는 다음 액티비티로 이동하면 됩니다.
                        // 메인 페이지로 이동
                        val mainIntent = Intent(this, MainPageActivity::class.java)
                        mainIntent.putExtra("user_id", id)
                        startActivity(mainIntent)
                        finish() // 현재 액티비티를 종료
                    } else {
                        // 로그인 실패
                        showToast("올바르지 않은 아이디 또는 비밀번호 입니다.")
                    }
                }
            // 에러 메시지 숨기기
            errorMessage.visibility = View.GONE
        }

        // 뒤로 가기 버튼을 눌렀을 때 시작 액티비티로 이동
        backButton.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티를 종료하고 이동
        }
        // 아이디 찾기 텍스트를 클릭했을 때 아이디 찾기 창으로 이동
        textViewFindId.setOnClickListener {
            val intent = Intent(this, FindIdActivity::class.java)
            startActivity(intent)
        }

        // 비밀번호 찾기 텍스트를 클릭했을 때 비밀번호 찾기 창으로 이동
        textViewFindPassword.setOnClickListener {
            val intent = Intent(this, FindPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    // 예시로 사용할 메시지를 표시하는 메서드
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}