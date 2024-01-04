package com.example.petner

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    //ViewHolder클래스가 하나만 있다면 ViewHolder를 Adapter안에 MessageAdapter.ViewHolder를 넣었겟지만
    //MessageAdapter에는 받는 쪽과 보낸 쪽 2개의 ViewHolder가 있기 때문에 어떤 ViewHolder든 받을 수 있게
    //RecyclerView.ViewHolder를 사용함

    //메시지(사용자 uid)에 따라 어떤 ViewHolder를 사용할 지 정하기 위해서 변수 두 개를 만듬
    private val receive = 1 //받는 타임
    private val send = 2 //보내는 타입

    lateinit var mAuth: FirebaseAuth

    //onCreateViewHolder를 통해 화면을 연결
    //override fun getItemViewType함수를 통해 메세지의 uid를 알 수 있음
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //getItemViewType에서 리턴된 값이 onCreateViewHolder의 ViewType에 들어 있다.
        //viewType이 1이면 받는 화면 2면 보내는 화면
        //받은 값으로 send or receiver 둘 중 하나 객체로 생성
        return if(viewType == 1) { //받는 화면
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            ReceiveViewHolder(view) //받는 쪽이면 ReceiveViewHolder(view) 생성
        }else{ //보내는 화면
            val view: View = LayoutInflater.from(context).inflate(R.layout.send, parent, false)
            SendViewHolder(view)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //현재 메시지
        val currentMessage = messageList[position]

        //보내는 데이터
        if(holder.javaClass == SendViewHolder::class.java){
            val viewHolder = holder as SendViewHolder //holder를 SendViewHolder타입으로 변경해서 viewHolder에 담음
            //viewHolder로 sendMessage(Textview)에 접근을 해서 currentMessage.message값을 넣어줌
            viewHolder.sendMessage.text = currentMessage.message
            viewHolder.sendPerson.text = currentMessage.myName

        }else{//받는 데이터
            //받는 쪽은 ReceiveViewHolder타입으로 변경을 해서 viewHolder로 receiveMessage(Textview)에 접근을 해서 currentMessage.message값을 넣어줌
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.receiveMessage.text = currentMessage.message
            viewHolder.receivePerson.text = currentMessage.myName

            Log.d("ff", "mesageadpater " + currentMessage.otherName)

        }
    }

    //getItemCount함수는 MessageList갯수(size)를 리던 할 것이다
    override fun getItemCount(): Int {
        return messageList.size
    }

    //getItemViewType함수를 사용해서 어떤 ViewHolder를 사용할 지 기능을 구현함
    override fun getItemViewType(position: Int): Int {
        //메시지 값
        val currentMessage = messageList[position] //messageList는 Message data class 로 구성된 리스트 이고 데이타 클래스 안에 있는 요소는 sendId와 String message이기 때문에
        //리스트의 인덱스를 통해 특정 메세지를 선택하고 그 메세지의 .sendId를 통해 누가 상대방인지 나인지 알 수 있음
        // 이 전달된 메시지를 currentMessage에 담아서 접속자uid와 currentMessage의 uid를 비교함
        //currentUser = 접속자uid, currentMessage.sendId = 전송자uid가 같다면 send를 리턴
        //아니면 receive를 리턴
        return if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.sendId)){
            send
        }else{
            receive
        }
    }
    //onCreateViewHolder의 리턴 값을 View로 전달 받아 객체를 생성함

    //보낸 쪽
    //SendViewHolder는 보낸 쪽 View를 전달 받아 객체를 생성
    //xml에서 TextView를 ViewHolder로 지정하고 onCreateViewHolder에서 뷰 홀더를 생성 후 바인딩하여 화면에 보여지게 함
    class SendViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sendMessage: TextView = itemView.findViewById(R.id.send_message_text)
        val sendPerson : TextView = itemView.findViewById(R.id.sender_name_text)
    }

    //받는 쪽
    //SendViewHolder는 받는 쪽 View를 받아 객체를 만듬
    class ReceiveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_message_text)
        val receivePerson : TextView = itemView.findViewById(R.id.receiver_name_text)
    }
}
