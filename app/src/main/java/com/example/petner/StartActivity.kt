package com.example.petner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupButton = findViewById<Button>(R.id.signupButton)

        // 로그인 버튼 클릭 시
        loginButton.setOnClickListener { // 로그인 액티비티로 이동
            val loginIntent = Intent(this@StartActivity, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        // 회원가입 버튼 클릭 시
        signupButton.setOnClickListener { // 회원가입 액티비티로 이동
            val signupIntent = Intent(this@StartActivity, SignupActivity::class.java)
            startActivity(signupIntent)
        }
    }
}