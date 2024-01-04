package com.example.petner
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class yourProfileActivity : AppCompatActivity() {
    private lateinit var errorMessage: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var nameTextView: TextView
    private lateinit var introductionTextView: TextView
    private lateinit var profileBtnEdit: Button
    private lateinit var likeTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.your_profile)
        var likeBtn = findViewById<Button>(R.id.likebtn)
        var profileImage = findViewById<ImageView>(R.id.profileImage)
        nameTextView = findViewById(R.id.nameTextView)
        introductionTextView = findViewById(R.id.introduction)



        val db = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

        val userId = intent.getStringExtra("user_id")
        val yourId = intent.getStringExtra("yourId")
        firestore = FirebaseFirestore.getInstance()

        yourId?.let {
            loadUserPosts(yourId)
        } ?: run {
            // userId가 null이면 처리할 코드
            Toast.makeText(this, "사용자 아이디가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        val userDocRef = db.collection("users").document(yourId!!)
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
                    nameTextView.text = "$name"
                    introductionTextView.text = "$gender/$size/$type"

                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }
        likeBtn.setOnClickListener{
            // 좋아요를 누른 사용자의 문서에 현재 사용자의 ID를 추가
            userId?.let {
                val userDocRef = FirebaseFirestore.getInstance().collection("users").document(it)
                userDocRef.update("like", FieldValue.arrayUnion(yourId))
                    .addOnSuccessListener {
                        Toast.makeText(this@yourProfileActivity, "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show()
                        Log.d("Firestore", "Like updated successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating like", e)
                    }
            }
        }
    }

    private fun loadUserPosts(yourId: String) {
        firestore.collection("posts")
            .whereEqualTo("userEmail", yourId)  // 이 줄을 추가하여 userId로 글을 필터링합니다.
            .get()
            .addOnSuccessListener { documents ->
                val postsContainer: LinearLayout = findViewById(R.id.post_container)
                postsContainer.removeAllViews()

                for (document in documents) {
                    val view = LayoutInflater.from(this).inflate(R.layout.post_layout, postsContainer, false)

                    view.setOnClickListener {
                        val intent = Intent(this, PostClickActivity::class.java)
                        intent.putExtra("postTitle", document.getString("title"))
                        intent.putExtra("postContent", document.getString("content"))
                        intent.putExtra("postImage", document.getString("imageUrl"))
                        intent.putExtra("userName", document.getString("userName"))
                        intent.putExtra("userProfileImage", document.getString("userProfileUrl"))
                        // 다른 필요한 데이터 전달
                        startActivity(intent)
                    }

                    val userNameTextView: TextView = view.findViewById(R.id.userName)
                    val postTitleTextView: TextView = view.findViewById(R.id.postTitle)
                    val postSummaryTextView: TextView = view.findViewById(R.id.postSummary)
                    val userProfileImageView: ImageView = view.findViewById(R.id.userProfileImage)

                    val userProfileUrl = document.getString("userProfileUrl")
                    userProfileUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(userProfileImageView) // 프로필 이미지 로드
                    }

                    val postImageView: ImageView = view.findViewById(R.id.postTemplateImageView)

                    userNameTextView.text = document.getString("userName") ?: "Anonymous"
                    postTitleTextView.text = document.getString("title") ?: "Untitled"
                    postSummaryTextView.text = document.getString("content") ?: "No content"
                    val imageUrl = document.getString("imageUrl")

                    imageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(postImageView) // Glide를 사용하여 ImageView에 이미지 로드
                    }
                    postsContainer.addView(view)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "글을 불러오는 중 오류 발생: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}