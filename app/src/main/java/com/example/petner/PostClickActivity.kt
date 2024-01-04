package com.example.petner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore

class PostClickActivity : AppCompatActivity() {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var mDbRef: DatabaseReference
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_click)

        //db초기확
        mDbRef = Firebase.database.reference



        val title = intent.getStringExtra("postTitle")
        val content = intent.getStringExtra("postContent")
        val imageUrl = intent.getStringExtra("postImage")
        val userName = intent.getStringExtra("userName")
        val uid = intent.getStringExtra("uid").toString()
        val userPhoneNumber = intent.getStringExtra("userPhoneNumber").toString()

        val userNameTextView: TextView = findViewById(R.id.post_click_name)
        val titleTextView: TextView = findViewById(R.id.post_click_title)
        val contentTextView: TextView = findViewById(R.id.post_click_info)
        val imageView: ImageView = findViewById(R.id.post_click_image)

        val userProfileImageUrl = intent.getStringExtra("userProfileImage")
        val userProfileImageView: ImageView = findViewById(R.id.post_click_profile_image) // ImageView ID 수정

        //val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        //val clickedUserUid = intent.getStringExtra("clickedUserUid")

        val callButton = findViewById<Button>(R.id.CallBtn)

        userProfileImageUrl?.let {
            Glide.with(this).load(it).into(userProfileImageView)
        }

        titleTextView.text = title
        contentTextView.text = content

        userNameTextView.text = userName

        Glide.with(this).load(imageUrl).into(imageView)

        callButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java) //chatActivity로 넘어간다
            intent.putExtra("userName", userNameTextView.text)
            intent.putExtra("uid", uid)
            intent.putExtra("userPhoneNumber",userPhoneNumber)
            //intent.putExtra("myName", )
            startActivity(intent)
        }
    }
}

//여기서 chatActivity에 이름과 이메일을 전달하면 chatActivity에서는 receivername과 receiveremail로 받으면 됨
//그리고 그 정보를 가지고 realtime db에 방을 생성하고 내가(a) 상대방(b)과 채팅할때 a의 텍스트를 send에 넣어서 올리고 b가 보낸 메세지를 가져와서 receive에 넣고 올리면 끝 인데 시발 왤케 안되는겨