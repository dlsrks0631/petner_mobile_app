package com.example.petner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class MainPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        val likesMap = mutableMapOf<String, Int>()
        val firestore = FirebaseFirestore.getInstance()
        val popularDogImage = findViewById<ImageView>(R.id.popularDogImage)
        val popularDogName = findViewById<TextView>(R.id.popularDogName)
        val userId = intent.getStringExtra("user_id")
        val showButton = findViewById<Button>(R.id.show_btn)
        val arounduserButton = findViewById<Button>(R.id.map_btn)

        arounduserButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }



        showButton.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // 문서에 "like" 필드가 있는지 확인
                    if (document.contains("like")) {
                        val likeList = document.get("like") as? List<String>

                        // 리스트의 각 값에 대해 업데이트
                        likeList?.forEach { likeValue ->
                            if (likesMap.containsKey(likeValue)) {
                                likesMap[likeValue] = likesMap[likeValue]!! + 1
                            } else {
                                likesMap[likeValue] = 1
                            }
                        }
                    }
                }
                val maxEntry = likesMap.entries.maxByOrNull { it.value }

                // 최대값이 있는 경우 처리
                if (maxEntry != null) {
                    // maxEntry의 key를 가져와서 해당 key에 해당하는 문서의 name 값을 추출
                    val maxLike = maxEntry.key
                    val documentRef = firestore.collection("users").document(maxLike)

                    documentRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            // 문서가 존재하는 경우 name 값을 가져와서 설정
                            val name = documentSnapshot.getString("name")
                            val image = documentSnapshot.getString("profilePhotoUrl")
                            popularDogName.text = name
                            image?.let {
                                Glide.with(this)
                                    .load(it)
                                    .into(popularDogImage)
                            }

                        } else {
                            Log.d("Likes", "해당하는 문서가 없습니다.")
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("Firestore", "문서 가져오기 오류: ", exception)
                    }
                } else {
                    Log.d("Likes", "좋아요 데이터가 없습니다.")
                }
            }
            .addOnFailureListener { exception ->
                // 실패 시 처리
                Log.e("Firestore", "문서 가져오기 오류: ", exception)
            }

    }
}
