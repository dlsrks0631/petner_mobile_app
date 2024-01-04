package com.example.petner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class likeListActivity : AppCompatActivity() {
    private lateinit var errorMessage: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.like_list)

        val userId = intent.getStringExtra("user_id")
        firestore = FirebaseFirestore.getInstance()
        userId?.let {
            loadLikeList(userId)
        } ?: run {
            // userId가 null이면 처리할 코드
            Toast.makeText(this, "사용자 아이디가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLikeList(userId: String) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDocuments ->
                val postsContainer: LinearLayout = findViewById(R.id.like_container)
                postsContainer.removeAllViews()
                val likedUsers = userDocuments["like"] as? List<String>
                if (likedUsers != null) {
                    for (likedUserId in likedUsers) {
                        // likedUserId를 이용하여 각 사용자의 정보를 가져옵니다.
                        firestore.collection("users")
                            .document(likedUserId)
                            .get()
                            .addOnSuccessListener { likedUserDocument ->
                                // 가져온 사용자 정보를 활용하여 작업을 수행합니다.

                                val userEmail = likedUserDocument.getString("id")
                                val userProfileUrl = likedUserDocument.getString("profilePhotoUrl")
                                // 여기에서 가져온 정보를 활용하여 화면에 표시하거나 다른 작업을 수행할 수 있습니다.
                                val view = LayoutInflater.from(this).inflate(R.layout.like_layout, postsContainer, false)
                                val userNameTextView: TextView = view.findViewById(R.id.userName)
                                val userProfileImageView: ImageView = view.findViewById(R.id.userProfileImage)
                                userNameTextView.text = likedUserDocument.getString("name") ?: "Anonymous"

                                userProfileUrl?.let {
                                    Glide.with(this)
                                        .load(it)
                                        .into(userProfileImageView) // 프로필 이미지 로드
                                }

                                postsContainer.addView(view)
                                var btn = view.findViewById<Button>(R.id.showProfile)
                                btn.setOnClickListener {
                                    // 버튼을 클릭했을 때 해당 사용자의 ID를 인텐트로 넘깁니다.
                                    val yourProfileIntent = Intent(this@likeListActivity, yourProfileActivity::class.java)
                                    yourProfileIntent.putExtra("yourId", userEmail) // 해당 레이아웃에 저장된 사용자의 ID를 넘깁니다.
                                    yourProfileIntent.putExtra("user_id", userId) // 현재 사용자의 ID를 넘깁니다.
                                    startActivity(yourProfileIntent)
                                }
                            }

                            .addOnFailureListener { exception ->
                                // 실패 시 처리
                                Toast.makeText(
                                    this,
                                    "유저 정보를 불러오는 중 오류 발생: ${exception.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    // "like" 필드가 null이거나 List<String> 타입이 아닌 경우 처리할 코드
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading posts: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}