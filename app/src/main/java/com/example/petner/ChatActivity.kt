package com.example.petner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import androidx.recyclerview.widget.LinearLayoutManager

import com.example.petner.databinding.ActivityChatBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {


    //상대방 이름과 uid를 받는다
    private lateinit var receiverName: String
    private lateinit var receiverUid: String
    private lateinit var receiverPhone: String
    private lateinit var receiverMyName : String

    //바인딩 객체
    private lateinit var binding: ActivityChatBinding

    lateinit var mAuth: FirebaseAuth //인증 객체
    lateinit var mDbRef: DatabaseReference//DB 객체

    //반는쪽 방과 보내는 쪽 대화방 만들기, 대화방은 senduid + receiveruid로 이루어 지기에 같은 방을 공유한다.
    private lateinit var receiverRoom: String //받는 대화방
    private lateinit var senderRoom: String//보내는 대화방
    //private var senderRoom: String = "" //보내는 대화방

    private lateinit var messageList: ArrayList<Message>

    private var currentUserPhoneNumber: String? = null

    private lateinit var currentName: String

//---------------------------------------변수 설정----------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //초기화
        messageList = ArrayList()
        var currentName: String = "" // 상대방 닉네임

        val messageAdapter: MessageAdapter = MessageAdapter(this, messageList)

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("user")



        currentUserUid?.let { uid ->
            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("currnet", "currentName!!!")
                    if (dataSnapshot.exists()) {
                        Log.d("currnet", "currentName!!!")
                        val userPhoneNumber = dataSnapshot.child("phoneNumber").getValue(String::class.java)
                        // userPhoneNumber에 사용자의 전화번호가 있음
                        currentUserPhoneNumber = userPhoneNumber
                        val userName = dataSnapshot.child("name").getValue(String::class.java)
                        if (userName != null) {
                            currentName = userName
                            Log.d("currnet", "currentName : " + currentName)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 처리할 내용 작성
                }
            })
        }

        //RecyclerView
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        //UserAdapter로 부터 넘어온 데이터 변수에 담기
        //activity간 변수 교환 getStringExtra는 전달되 ㄴ값을 저장
        receiverName = intent.getStringExtra("userName").toString()
        receiverUid = intent.getStringExtra("uid").toString() //userAdapter의 intent를 통해 name과 uid를 넘겨 받는다
        receiverPhone = intent.getStringExtra("userPhoneNumber").toString()
        //receiverEmail = intent.getStringExtra("email").toString()

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        //접속자 Uid
        val senderUid = mAuth.currentUser?.uid

        //보낸이방   String + uid
        //senderRoom = receiverUid + senderUid //보낸이방 값은 반는사람 uid + 보낸 사람 uid
        senderRoom = receiverPhone + currentUserPhoneNumber

        //받는이방  String + uid
        //receiverRoom = senderUid + receiverUid //받는이방 값은 보낸이 uid + 받는이 uid로 이루어 진다
        receiverRoom = currentUserPhoneNumber + receiverPhone

        //액션바에 상대방 이름 보여주기
        supportActionBar?.title = receiverName

        //메시지 전송 버튼 이벤트 만들기
        //버튼을 클릭하면 입력한 메시지는 DB에 저장이 되고 DB에 저장된 메시지를 화면에 보여준다
        binding.sendBtn.setOnClickListener{
            //DB에 저장
            val message = binding.messageEdit.text.toString() //입력한 메시지를 변수에 담고
            val messageObject = Message(message, senderUid, receiverName, currentName) //messageObject라는 변수 안에 메시지 클래스 형식의 값을 넣은다

            Log.d("hel","message : " + message + " senderUid " + senderUid + " receiverName " + receiverName + " currentName " + currentName)

            //데이터 저장
            //chats이라는 공간을 만들고 그 아래에 senderRoom이라는 공간을 생성 그리고 그 아래에 또 messages로 공간 생성 그리고 마지막으로 messages라는 공간안에
            // 전송된 메시지 내용이 저장이 된다. 이 부분이 성공하면
            mDbRef.child("chats").child(senderRoom).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    //이 부분이 성공하면 받는 이의 receiverRoom하위의 messages에도 전송된 메시지가 저장됨 한 마디로 보낸 쪽 받는 쪽 두 공간에 동시에 메시지를 저장
                    //그러면 상대방이 로그인 했을때 받은 메시지를 확인할 수 있다.

                    mDbRef.child("chats").child(receiverRoom).child("messages")
                        .push().setValue(messageObject)
                }
            //메시지를 전송하고 입력부분을 초기화 하는 기능을 구현
            binding.messageEdit.setText("")
        }

        mDbRef.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for(postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged() //이 함수를 통해 메세지가 화면에 출력 됨
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}