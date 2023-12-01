package com.example.petner

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindIdActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_id)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 뒤로 돌아가기 버튼 설정
        val backButton = findViewById<View>(R.id.backButton)
        backButton.setOnClickListener {
            // 로그인 액티비티로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티를 종료하고 이동
        }

        // 아이디 찾기 버튼 설정
        val findIdButton = findViewById<View>(R.id.findIdButton)
        findIdButton.setOnClickListener {
            // 입력된 이름과 전화번호 가져오기
            val editTextName = findViewById<EditText>(R.id.editTextName)
            val editTextPhoneNumber = findViewById<EditText>(R.id.editTextPhoneNumber)
            val name = editTextName.text.toString()
            val phoneNumber = editTextPhoneNumber.text.toString()
            // 입력 확인
            if (name.isEmpty() || phoneNumber.isEmpty()) {
                showResultDialog("회원정보를 입력해주세요.")
                return@setOnClickListener
            }
            // Firebase에서 사용자 데이터 검색
            db.collection("users")
                .whereEqualTo("name", name)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // 사용자를 찾지 못한 경우
                        showResultDialog("아이디를 찾을 수 없습니다.")
                    } else {
                        // 사용자를 찾은 경우
                        val userId = documents.documents[0].id
                        showResultDialog("찾은 아이디: $userId")
                    }
                }
                .addOnFailureListener { e ->
                    // Firebase 조회 중 에러 발생
                    showResultDialog("아이디 조회 중 오류 발생: $e")
                }
        }
    }

    private fun showResultDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("아이디 찾기 결과")
        alertDialogBuilder.setMessage(message)

        alertDialogBuilder.setPositiveButton("확인") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
