package com.example.petner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddPostActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri: Uri? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_post)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)


        val arrowLeftButton = findViewById<Button>(R.id.button_arrow_left)
        arrowLeftButton.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }

        var saveBtn : Button = findViewById(R.id.saveBtn)
        // Initiated Storage
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type="image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // add image upload event
        saveBtn.setOnClickListener {
            Toast.makeText(this, "버튼 클릭됨", Toast.LENGTH_SHORT).show()
            contentUpload()
        }
    }

    private fun contentUpload() {
        if (photoUri != null) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "IMAGE_$timestamp.png"
            val storageRef = storage?.reference?.child("images")?.child(imageFileName)

            storageRef?.putFile(photoUri!!)
                ?.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageRef.downloadUrl // 다운로드 URL 가져오기
                }
                ?.addOnSuccessListener { uri ->
                    Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()

                    val downloadUrl = uri.toString() // 다운로드 URL
                    val title = findViewById<EditText>(R.id.edit_title).text.toString()
                    val content = findViewById<EditText>(R.id.edit_explain).text.toString()

                    // 현재 로그인한 사용자의 이메일 주소를 가져옵니다.
                    val userEmail = Firebase.auth.currentUser?.email

                    // Firestore에서 'users' 컬렉션을 참조하여 사용자의 정보를 가져옵니다.
                    // 이메일 주소를 문서 ID로 사용합니다.
                    userEmail?.let { email ->
                        firestore?.collection("users")?.document(email)?.get()
                            ?.addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userName = document.getString("name") ?: ""
                                    val userProfileUrl = document.getString("profilePhotoUrl") ?: "" // 프로필 이미지 URL 필드가 있다고 가정합니다.
                                    val userPhoneNumber = document.getString("phoneNumber") ?: "" // 사용자 전화번호

                                    val post = hashMapOf(
                                        "title" to title,
                                        "content" to content,
                                        "imageUrl" to downloadUrl,
                                        "userName" to userName,
                                        "userProfileUrl" to userProfileUrl,
                                        "userEmail" to userEmail,
                                        "userPhoneNumber" to userPhoneNumber
                                    )

                                    firestore?.collection("posts")?.add(post)
                                        ?.addOnSuccessListener {
                                            Toast.makeText(this, "게시물 저장 성공", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, PostActivity::class.java)
                                            startActivity(intent)
                                            finish() // AddPostActivity 종료
                                        }
                                        ?.addOnFailureListener { e ->
                                            Toast.makeText(this, "게시물 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(this, "사용자 정보 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } ?: run {
                        Toast.makeText(this, "사용자 인증 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var addphoto_image : ImageView = findViewById(R.id.addphoto_image)

        if(requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == RESULT_OK) {
                // 이미지 경로가 넘어옴
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            }else {
                // 취소를 눌렀을 때 작동하는 부분
                finish()
            }
        }
    }

}