package com.example.petner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore

class PostActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    lateinit var mAuth: FirebaseAuth //인증 객체

    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        firestore = FirebaseFirestore.getInstance()
        loadPosts() // Firestore에서 게시물 데이터를 읽어오는 함수 호출

        mDbRef = Firebase.database.reference

        //mAuth = FirebaseAuth.getInstance()
        //val currentUid = mAuth.currentUser?.uid
        //val currentUserUid = mAuth.currentUser?.uid


        //val userId = intent.getStringExtra("user_id") // login에서 가져온 user_id

        // Spinner 관련 코드
        val spinner: Spinner = findViewById(R.id.SelectBar)
        val items = arrayOf("게시글", "메인 페이지", "채팅 목록", "마이 페이지")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // FloatingActionButton 관련 코드
        val postingButton: FloatingActionButton = findViewById(R.id.postingButton)
        postingButton.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)

        }

        // Spinner 아이템 선택 이벤트 처리
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    1 -> startActivity(Intent(this@PostActivity, MainPageActivity::class.java))
                    3 -> {
                        val userId = intent.getStringExtra("user_id")
                        val profileIntent = Intent(this@PostActivity, profileActivity::class.java)
                        profileIntent.putExtra("user_id", userId)
                        startActivity(profileIntent)
                    }
                    // 추가적인 position에 대한 처리...
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 처리
            }
        }
    }


    private fun findUidByName(userName: String) {
        val query: Query = mDbRef.child("users").orderByChild("name").equalTo(userName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val uid = snapshot.key // 찾은 사용자의 UID
                        intent.putExtra("uid", uid) // 유저 UID 추가
                        // 여기서 uid를 활용하여 원하는 작업 수행
                    }
                } else {
                    // 사용자를 찾지 못한 경우에 대한 처리
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("데이터베이스에서 데이터를 가져오는 도중 오류가 발생했습니다.")
            }
        })
    }



    private fun loadPosts() {
        firestore.collection("posts")
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
                        intent.putExtra("userName", document.getString("userName")).toString()
                        intent.putExtra("userProfileImage", document.getString("userProfileUrl"))
                        intent.putExtra("userPhoneNumber", document.getString("userPhoneNumber"))

                        val userName = document.getString("userName") // 유저 이름
                        findUidByName(userName ?: "") // 함수 호출

                        //val postAuthorUid = document.getString("userId")
                        //intent.putExtra("userId", document.getString("userId"))
                        /*
                        * intent.putExtra("userId", document.getString("userId"))는 Firestore에서 가져온
                        * 게시물 데이터에 있는 "userId" 값을 다음 액티비티로 넘기기 위한 코드입니다.
                        * 여기서 "userId"는 게시물을 작성한 사용자의 UID입니다.
                        * 이 코드는 PostClickActivity로 넘어갈 때 해당 게시물을 작성한 사용자의 UID를 함께 전달하고 있습니다.*/
                        // 다른 필요한 데이터 전달
                        //intent.putExtra("uid", currentUserUid?.toString() ?: "")
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
                    var btn = view.findViewById<Button>(R.id.showProfile)
                    btn.setOnClickListener{
                        val userId = intent.getStringExtra("user_id")
                        val yourProfileIntent = Intent(this@PostActivity, yourProfileActivity::class.java)
                        yourProfileIntent.putExtra("yourId", document.getString("userEmail"))
                        yourProfileIntent.putExtra("user_id", userId)
                        startActivity(yourProfileIntent)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading posts: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
