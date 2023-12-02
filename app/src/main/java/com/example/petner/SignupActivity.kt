package com.example.petner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var duplicateCheckResultTextView: TextView
    private lateinit var db: FirebaseFirestore
    // 중복 체크 여부를 나타내는 변수
    private var isDuplicate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextPasswordCheck = findViewById<EditText>(R.id.editTextPasswordCheck)
        val editTextPhoneNumber = findViewById<EditText>(R.id.editTextPhoneNumber)
        val editTextYear = findViewById<EditText>(R.id.editTextYear)
        val spinnerMonth = findViewById<Spinner>(R.id.spinnerMonth)
        val editTextDay = findViewById<EditText>(R.id.editTextDay)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val checkDuplicateButton = findViewById<Button>(R.id.checkDuplicateButton)
        duplicateCheckResultTextView = findViewById(R.id.duplicateCheckResult)
        val backButton = findViewById<View>(R.id.backButton) // 이미지 버튼 추가

        // 월 스피너에 사용할 월 목록
        val months =
            arrayOf("월", "1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")

        // 어댑터 생성 및 설정
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapter

        // 스피너의 선택 이벤트 리스너 설정
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 것도 선택하지 않은 경우 처리
            }
        }

        signupButton.setOnClickListener {
            // 여기에 회원가입 처리 로직을 추가하면 됩니다.
            // 입력된 정보를 가져와서 처리하도록 작성합니다.
            val id = editTextId.text.toString()
            val name = editTextName.text.toString()
            val password = editTextPassword.text.toString()
            val passwordCheck = editTextPasswordCheck.text.toString()
            val phoneNumber = editTextPhoneNumber.text.toString()
            val year = editTextYear.text.toString()
            val month = spinnerMonth.selectedItem.toString()
            val day = editTextDay.text.toString()
            // 비밀번호와 비밀번호 확인이 일치하는지 확인
            // 필수 입력란이 비어있는지 확인
            if (id.isEmpty()) {
                showToast("이메일을 입력해주세요.")
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                showToast("이름을 입력해주세요.")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showToast("비밀번호를 입력해주세요.")
                return@setOnClickListener
            }
            if (password.length < 6) { // 비밀번호가 6자 이상이어야 함
                showToast("비밀번호는 6자 이상이어야 합니다.")
                return@setOnClickListener
            }
            if (passwordCheck.isEmpty()) {
                showToast("비밀번호 확인을 해주세요.")
                return@setOnClickListener
            }
            if (password != passwordCheck) {
                showToast("비밀번호를 다시 확인해주세요.")
                return@setOnClickListener
            }
            if (year.isEmpty()) {
                showToast("태어난 연도를 입력해주세요.")
                return@setOnClickListener
            }
            if (!year.matches("\\d{4}".toRegex())) {
                showToast("올바른 연도를 입력해주세요.")
                return@setOnClickListener
            }
            if (month == "월") {
                showToast("월을 선택해주세요.")
                return@setOnClickListener
            }
            if (day.isEmpty()) {
                showToast("일을 입력해주세요.")
                return@setOnClickListener
            }
            if (day.toInt() < 1 || day.toInt() > 31) {
                showToast("올바른 일을 입력해주세요.")
                return@setOnClickListener
            }
            if (phoneNumber.isEmpty()) {
                showToast("전화번호를 입력해주세요.")
                return@setOnClickListener
            }
            if (!phoneNumber.matches("\\d{10,11}".toRegex())) {
                showToast("올바른 전화번호을 입력해주세요.")
                return@setOnClickListener
            }
            // 중복 체크 여부 확인
            if (!isDuplicate) {
                showToast("아이디 중복을 확인해주세요.")
                return@setOnClickListener
            }
            // Firebase 회원가입
            auth.createUserWithEmailAndPassword(id, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 회원가입 성공
                        showToast("회원가입 성공")

                        // Firestore에 사용자 정보 저장
                        val user = hashMapOf(
                            "id" to id,
                            "name" to name,
                            "password" to password,
                            "phoneNumber" to phoneNumber,
                            "year" to year,
                            "month" to month,
                            "day" to day
                        )

                        db.collection("users")
                            .document(id)
                            .set(user)
                            .addOnSuccessListener {
                                showToast("사용자 정보 저장 성공")
                            }
                            .addOnFailureListener { e ->
                                showToast("사용자 정보 저장 실패: $e")
                            }

                        // 여기에서 다음 화면으로 이동
                        val intent = Intent(this, StartActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 액티비티를 종료하고 이동
                    } else {
                        // 회원가입 실패
                        showToast("회원가입 실패: ${task.exception?.message}")
                    }
                }
        }
        // 이미지 버튼을 눌렀을 때 시작 액티비티로 이동
        backButton.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티를 종료하고 이동
        }
        // 중복 체크 버튼 클릭 리스너 설정
        checkDuplicateButton.setOnClickListener {
            val id = editTextId.text.toString().trim()

            // 아이디가 비어있는지 확인
            if (id.isEmpty()) {
                duplicateCheckResultTextView.text = "이메일을 입력해주세요."
                duplicateCheckResultTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            // 이메일 형태로 입력되었는지 확인
            if (!isEmailFormat(id)) {
                duplicateCheckResultTextView.text = "이메일 형태로 입력해주세요."
                duplicateCheckResultTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(id)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isDuplicate = task.result?.signInMethods?.isNotEmpty() == true
                        if (isDuplicate) {
                            // 중복된 경우
                            duplicateCheckResultTextView.text = "이미 사용 중인 아이디입니다."
                            duplicateCheckResultTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        } else {
                            // 중복되지 않은 경우
                            duplicateCheckResultTextView.text = "사용 가능한 아이디입니다."
                            duplicateCheckResultTextView.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                            this@SignupActivity.isDuplicate =
                                true // 중복 체크 성공 시 isDuplicate 값을 true로 설정
                        }
                    } else {
                        // 중복 확인 중 오류 발생
                        showToast("중복 확인 중 오류 발생: ${task.exception?.message}")
                    }
                }
        }
    }
    // 이메일 형태 확인 함수
    private fun isEmailFormat(email: String): Boolean {
        // 간단한 이메일 형식 확인 로직
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    // 예시로 사용할 메시지를 표시하는 메서드
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}