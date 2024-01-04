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
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class profileActivity : AppCompatActivity() {
    private lateinit var errorMessage: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var nameTextView: TextView
    private lateinit var introductionTextView: TextView
    private lateinit var profileBtnEdit: Button
    private lateinit var likeTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        var profileImage = findViewById<ImageView>(R.id.profileImage)
        nameTextView = findViewById(R.id.nameTextView)
        introductionTextView = findViewById(R.id.introduction)
        profileBtnEdit = findViewById(R.id.editbtn)
        likeTextView = findViewById(R.id.likeTextView)
        val likeCount = findViewById<TextView>(R.id.likeCount)
        val db = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

        val userId = intent.getStringExtra("user_id")

        firestore = FirebaseFirestore.getInstance()

        userId?.let {
            loadUserPosts(userId)
        } ?: run {
            // userId가 null이면 처리할 코드
            Toast.makeText(this, "사용자 아이디가 없습니다.", Toast.LENGTH_SHORT).show()
        }

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
                    nameTextView.text = "$name"
                    introductionTextView.text = "$gender/$size/$type"
                    val likeList = documentSnapshot.get("like") as? List<String>
                    // 'like' 필드가 null이 아니면 좋아요 수 표시
                    likeList?.let {
                        likeCount.text = "${it.size}"
                    }

                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "get failed with ", exception)
            }

        profileBtnEdit.setOnClickListener {
            val editIntent = Intent(this@profileActivity, profileEditActivity::class.java)
            editIntent.putExtra("user_id", userId)
            startActivity(editIntent)
            finish()
        }

        likeTextView.setOnClickListener {
            val likeIntent = Intent(this@profileActivity, likeListActivity::class.java)
            likeIntent.putExtra("user_id", userId)
            startActivity(likeIntent)
        }
    }

    private fun loadUserPosts(userId: String) {
        firestore.collection("posts")
            .whereEqualTo("userEmail", userId)  // 이 줄을 추가하여 userId로 글을 필터링합니다.
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