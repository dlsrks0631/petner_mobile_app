package com.example.petner

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class FindPasswordActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_password)

        db = FirebaseFirestore.getInstance()

        // 뒤로 돌아가기 버튼 설정
        val backButton = findViewById<View>(R.id.backButton)
        backButton.setOnClickListener {
            // 로그인 액티비티로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티를 종료하고 이동
        }

        // 비밀번호 찾기 버튼 설정
        val findPasswordButton = findViewById<View>(R.id.findPasswordButton)
        findPasswordButton.setOnClickListener {
            // 입력된 이름, 아이디, 전화번호 가져오기
            val editTextName = findViewById<EditText>(R.id.editTextName)
            val editTextId = findViewById<EditText>(R.id.editTextId)
            val editTextPhoneNumber = findViewById<EditText>(R.id.editTextPhoneNumber)
            val name = editTextName.text.toString()
            val id = editTextId.text.toString()
            val phoneNumber = editTextPhoneNumber.text.toString()
            // 입력 확인
            if (name.isEmpty() || phoneNumber.isEmpty() || id.isEmpty()) {
                showResultDialog("회원정보를 입력해주세요.")
                return@setOnClickListener
            }
            // Firebase에서 사용자 데이터 검색
            db.collection("users")
                .whereEqualTo("name", name)
                .whereEqualTo("id", id)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // 사용자를 찾지 못한 경우
                        showResultDialog("비밀번호를 찾을 수 없습니다.")
                    } else {
                        // 사용자를 찾은 경우
                        val password = documents.documents[0].getString("password")
                        showResultDialog("찾은 비밀번호: $password")
                    }
                }
                .addOnFailureListener { e ->
                    // Firebase 조회 중 에러 발생
                    showResultDialog("비밀번호 조회 중 오류 발생: $e")
                }
        }
    }

    private fun showResultDialog(message: String) {
        val spannableMessage = SpannableString(message)
        spannableMessage.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, message.length, 0)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(" 비밀번호 찾기 결과")
        alertDialogBuilder.setMessage(spannableMessage)
        alertDialogBuilder.setIcon(R.drawable.pawprint)
        alertDialogBuilder.setPositiveButton("확인") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
