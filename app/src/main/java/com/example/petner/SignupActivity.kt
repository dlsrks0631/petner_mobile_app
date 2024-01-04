package com.example.petner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var duplicateCheckResultTextView: TextView
    private lateinit var db: FirebaseFirestore

    private lateinit var mDbRef: DatabaseReference

    // 중복 체크 여부를 나타내는 변수
    private var isDuplicate = false
    private lateinit var editTextAddress: EditText
    private lateinit var profilePhoto: ImageView
    private lateinit var selectedImageUri: Uri
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        mDbRef = Firebase.database.reference

        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextDogType = findViewById<EditText>(R.id.editTextDogType)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextPasswordCheck = findViewById<EditText>(R.id.editTextPasswordCheck)
        val editTextPhoneNumber = findViewById<EditText>(R.id.editTextPhoneNumber)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val checkDuplicateButton = findViewById<Button>(R.id.checkDuplicateButton)
        duplicateCheckResultTextView = findViewById(R.id.duplicateCheckResult)
        val backButton = findViewById<View>(R.id.backButton) // 이미지 버튼 추가
        val radioGroupGender = findViewById<RadioGroup>(R.id.radioGroupGender)
        val radioButtonMale = findViewById<RadioButton>(R.id.radioButtonMale)
        val radioButtonFemale = findViewById<RadioButton>(R.id.radioButtonFemale)
        val checkBoxNeutered = findViewById<CheckBox>(R.id.checkBoxNeutered)
        val spinnerSize = findViewById<Spinner>(R.id.spinnerSize)
        val sizeList = listOf("강아지 크기 선택   +","소형견", "중형견", "대형견")
        val sizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sizeList)

        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSize.adapter = sizeAdapter
        profilePhoto = findViewById(R.id.profilePhoto)

        val photoButton = findViewById<Button>(R.id.photoButton)
        photoButton.setOnClickListener {
            openImageChooser()
        }
        editTextAddress = findViewById(R.id.editTextAddress)
        editTextAddress.setOnClickListener {
            openAddressSearchActivity()
        }
        signupButton.setOnClickListener {
            // 여기에 회원가입 처리 로직을 추가하면 됩니다.
            // 입력된 정보를 가져와서 처리하도록 작성합니다.
            val id = editTextId.text.toString()
            val name = editTextName.text.toString()
            val password = editTextPassword.text.toString()
            val dogType=editTextDogType.text.toString()
            val passwordCheck = editTextPasswordCheck.text.toString()
            val phoneNumber = editTextPhoneNumber.text.toString()
            val gender = if (radioButtonMale.isChecked) "수컷" else "암컷"
            val isNeutered = checkBoxNeutered.isChecked
            val selectedSize = spinnerSize.selectedItem.toString()
            val address = editTextAddress.text.toString()

            val realtimename = editTextName.text.toString().trim()
            val realtimeemail = editTextId.text.toString()

            // 비밀번호와 비밀번호 확인이 일치하는지 확인
            // 필수 입력란이 비어있는지 확인
            if (id.isEmpty()) {
                showToast("이메일을 입력해주세요.")
                return@setOnClickListener
            }
            if (!::selectedImageUri.isInitialized) {
                showToast("프로필 사진을 선택해주세요.")
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                showToast("이름을 입력해주세요.")
                return@setOnClickListener
            }
            if (dogType.isEmpty()) {
                showToast("이름을 입력해주세요.")
                return@setOnClickListener
            }
            if (selectedSize=="강아지 크기 선택") {
                showToast("크기를 입력해주세요.")
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
            if (phoneNumber.isEmpty()) {
                showToast("전화번호를 입력해주세요.")
                return@setOnClickListener
            }
            if (!phoneNumber.matches("\\d{10,11}".toRegex())) {
                showToast("올바른 전화번호을 입력해주세요.")
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                showToast("주소 찾기를 해주세요")
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
                        saveUserProfilePhoto(id)
                        // Firestore에 사용자 정보 저장
                        val user = hashMapOf(
                            "id" to id,
                            "name" to name,
                            "password" to password,
                            "phoneNumber" to phoneNumber,
                            "gender" to gender,
                            "neutered" to isNeutered,
                            "size" to selectedSize,
                            "address" to address,
                            "dogType" to dogType
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
                        //auth에 저장
                        addUserToDatabase(name, id, auth.currentUser?.uid!!)
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

            // Firestore에서 중복된 아이디 확인
            db.collection("users")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty()) {
                        // 중복된 경우
                        duplicateCheckResultTextView.text = "이미 사용 중인 아이디입니다."
                        duplicateCheckResultTextView.setTextColor(
                            ContextCompat.getColor(this, android.R.color.holo_red_dark)
                        )
                    } else {
                        // 중복되지 않은 경우
                        duplicateCheckResultTextView.text = "사용 가능한 아이디입니다."
                        duplicateCheckResultTextView.setTextColor(
                            ContextCompat.getColor(this, android.R.color.holo_green_dark)
                        )
                        this@SignupActivity.isDuplicate = true
                    }
                }
                .addOnFailureListener { e ->
                    // 중복 확인 중 오류 발생
                    showToast("중복 확인 중 오류 발생: $e")
                }
        }
    }
    // 이메일 형태 확인 함수
    private fun isEmailFormat(email: String): Boolean {
        // 이메일 형식 확인 로직
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun openAddressSearchActivity() {
        // 주소 검색 화면 열기 (원하는 방법으로 구현)
        // 예: Intent를 사용하여 주소 검색 액티비티로 이동
        val intent = Intent(this, AddressSearchActivity::class.java)
        startActivityForResult(intent, ADDRESS_SEARCH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADDRESS_SEARCH_REQUEST_CODE && resultCode == RESULT_OK) {
            // 주소 검색 액티비티에서 돌아왔을 때 처리
            val selectedAddress = data?.getStringExtra("selected_address")
            if (!selectedAddress.isNullOrEmpty()) {
                // 선택된 주소를 EditText에 설정
                editTextAddress.setText(selectedAddress)
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            // 이미지 선택 액티비티에서 돌아왔을 때 처리
            selectedImageUri = data.data!!
            profilePhoto.setImageURI(selectedImageUri)
        }
    }
    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), PICK_IMAGE_REQUEST)
    }

    private fun saveUserProfilePhoto(userId: String) {
        if (::selectedImageUri.isInitialized) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_photos/$userId.jpg")
            storageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    showToast("프로필 사진 업로드 성공")

                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        db.collection("users")
                            .document(userId)
                            .update("profilePhotoUrl", uri.toString())
                            .addOnSuccessListener {
                                showToast("프로필 사진 URL 저장 성공")
                            }
                            .addOnFailureListener { e ->
                                showToast("프로필 사진 URL 저장 실패: $e")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    showToast("프로필 사진 업로드 실패: $e")
                }
        }
    }


    // 예시로 사용할 메시지를 표시하는 메서드
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    companion object {
        const val ADDRESS_SEARCH_REQUEST_CODE = 123
    }

    //realtime db에 저장
    private fun addUserToDatabase(name: String, email: String, uId: String){
        mDbRef.child("user").child(uId).setValue(UserRealtime(name, email, uId))
    }
}