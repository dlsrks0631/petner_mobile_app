package com.example.petner

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.widget.*

class profileEditActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileImage : ImageView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_edit)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        profileImage = findViewById(R.id.editImage)
        var editName = findViewById<EditText>(R.id.editname)
        val editButton = findViewById<Button>(R.id.editButton) // 사진편집
        var saveButton = findViewById<Button>(R.id.saveBtn) // 저장
        var maleRadioButton = findViewById<RadioButton>(R.id.radioMale)
        var femaleRadioButton = findViewById<RadioButton>(R.id.radioFemale)
        var bigRadioButton = findViewById<RadioButton>(R.id.radioBig)
        var middleRadioButton = findViewById<RadioButton>(R.id.radioMiddle)
        var smallRadioButton = findViewById<RadioButton>(R.id.radioSmall)
        var editType = findViewById<EditText>(R.id.edittype)

        var goBack = findViewById<Button>(R.id.goback)

        val userId = intent.getStringExtra("user_id")


        val userDocRef = db.collection("users").document(userId!!)
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                if (documentSnapshot != null) {
                    // 사용자 정보가 있는 경우 UI에 표시
                    val name = documentSnapshot.getString("name")
                    val gender = documentSnapshot.getString("gender")
                    val size = documentSnapshot.getString("size")
                    val image = documentSnapshot.getString("profilePhotoUrl")
                    val type = documentSnapshot.getString("dogType")
                    image?.let {
                        Glide.with(this)
                            .load(it)
                            .into(profileImage)
                    }
                    editName.setText(name ?:"")
                    editType.setText(type ?:"")
                    if (gender == "수컷") {
                        maleRadioButton.isChecked = true
                    } else if (gender == "암컷") {
                        femaleRadioButton.isChecked = true
                    }

                    if (size == "대형견") {
                        bigRadioButton.isChecked = true
                    } else if (size == "중형견") {
                        middleRadioButton.isChecked = true
                    } else if (size == "소형견"){
                        smallRadioButton.isChecked = true
                    }


                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }

        editButton.setOnClickListener {
            openImageChooser()
        }
        goBack.setOnClickListener {
            val profileIntent = Intent(this@profileEditActivity, profileActivity::class.java)
            profileIntent.putExtra("user_id", userId)
            startActivity(profileIntent)
            finish()
        }

        saveButton.setOnClickListener {
            val updatedName = editName.text.toString()
            val updatedType = editType.text.toString()
            saveUserProfilePhoto(userId)
            // gender 선택 확인
            val updatedGender = when {
                maleRadioButton.isChecked -> "수컷"
                femaleRadioButton.isChecked -> "암컷"
                else -> ""
            }

            // size 선택 확인
            val updatedSize = when {
                bigRadioButton.isChecked -> "대형견"
                middleRadioButton.isChecked -> "중형견"
                smallRadioButton.isChecked -> "소형견"
                else -> ""
            }

            // 수정된 정보를 Map으로 생성
            val updatedData = mapOf(
                "name" to updatedName,
                "dogType" to updatedType,
                "gender" to updatedGender,
                "size" to updatedSize
            )

            // 현재 사용자의 문서에 대한 DocumentReference
            val userDocRef = db.collection("users").document(userId!!)

            // 업데이트 실행
            userDocRef.update(updatedData)
                .addOnSuccessListener {
                    Log.d("Firestore", "DocumentSnapshot successfully updated!")
                    val profileIntent = Intent(this@profileEditActivity, profileActivity::class.java)
                    profileIntent.putExtra("user_id", userId)
                    startActivity(profileIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error updating document", e)
                    // 업데이트에 실패하면 사용자에게 알림을 표시하거나 다른 조치를 취할 수 있습니다.
                }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            // 이미지 선택 액티비티에서 돌아왔을 때 처리
            selectedImageUri = data.data!!
            profileImage.setImageURI(selectedImageUri)
        }
    }
    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), PICK_IMAGE_REQUEST)
    }

    private fun saveUserProfilePhoto(userIdd: String) {
        if (::selectedImageUri.isInitialized) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_photos/$userIdd.jpg")
            storageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    showToast("프로필 사진 업로드 성공")

                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        db.collection("users")
                            .document(userIdd)
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
}