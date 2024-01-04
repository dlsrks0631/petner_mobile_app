package com.example.petner

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    val db = FirebaseFirestore.getInstance()
    lateinit var mapbtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)

        mapbtn = findViewById(R.id.btn_move_activity)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapbtn.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val dogMarker = getDescriptorFromDrawable(R.drawable.dogmarker)
        val user = FirebaseAuth.getInstance().currentUser
        val markers = mutableMapOf<String, Marker>() // 기존의 마커를 저장하는 맵

        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style
            )
        )


        // 마커 클릭 리스너 설정
        googleMap.setOnMarkerClickListener { marker ->
            val clickedUser = marker.tag as User

            // 다이얼로그에 보여줄 사용자의 이름과 사진 로드
            val builder = AlertDialog.Builder(this)

            // name이 null인지 체크
            if (clickedUser.name != null) {
                builder.setTitle(clickedUser.name)
            }

            // 사용자의 사진을 로드하기 위한 ImageView 생성
            val imageView = ImageView(this)
            imageView.layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            // photoUrl이 null인지 체크
            if (clickedUser.photoUrl != null) {
                // Glide 라이브러리를 사용하여 이미지 로드
                Glide.with(this)
                    .load(clickedUser.photoUrl)
                    .override(600, 200)  // 이미지의 크기를 조절
                    .into(imageView)
            }

            builder.setView(imageView)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

            builder.create().show()

            false
        }


        user?.let {
            val id = it.email // email 대신 id를 사용

            db.collection("users").whereEqualTo("id", id).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        val address = document.getString("address")
                        if (address != null) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val location = getLocationFromAddress(this@MapActivity, address)
                                if (location != null) {
                                    withContext(Dispatchers.Main) {
                                        // 카메라 위치를 현재 사용자의 위치로 이동하고 확대
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f)) // 16 레벨로 확대
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }

        // address 데이터를 실시간으로 지도에 마커로 표시하기
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val docId = document.getString("id")
                    if (docId != null) {
                        // 기존의 마커를 지운다.
                        markers[docId]?.remove()

                        val address = document.getString("address")
                        if (address != null) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val location = getLocationFromAddress(this@MapActivity, address)
                                if (location != null) {
                                    withContext(Dispatchers.Main) {
                                        val marker = mMap.addMarker(MarkerOptions()
                                            .position(location)
                                            .title("Marker in " + address)
                                            .icon(dogMarker))
                                        // 새로 추가된 마커를 맵에 저장
                                        if (marker != null) {
                                            // 데이터베이스에서 name과 photoUrl을 가져옴
                                            val name = document.getString("name") ?: ""
                                            val photoUrl = document.getString("profilePhotoUrl") ?: ""

                                            // 가져온 name과 photoUrl로 User 객체를 생성
                                            val user = User(name, photoUrl)

                                            // 생성한 User 객체를 marker의 tag로 설정
                                            marker.tag = user

                                            markers[docId] = marker
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                onMapReady(mMap)
            }
        }
    }

    fun getLocationFromAddress(context: Context, strAddress: String): LatLng? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null

        try {
            // 주소를 최대 5개까지 찾아옵니다.
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null || address.isEmpty()) {
                return null
            }
            val location = address[0]
            p1 = LatLng(location.latitude, location.longitude)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return p1
    }

    fun getDescriptorFromDrawable(drawableId : Int) : BitmapDescriptor {
        // 마커 아이콘 만들기
        var bitmapDrawable: BitmapDrawable

        val drawable = ContextCompat.getDrawable(this, drawableId)

        if (drawable is BitmapDrawable) {
            bitmapDrawable = drawable
        } else {
            // Drawable이 BitmapDrawable이 아닌 경우에 대한 처리
            bitmapDrawable = BitmapDrawable(resources, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        }

        // 마커 크기 변환
        val scaledBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 100,100,false)

        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    data class User(
        val name: String,
        val photoUrl: String
    )

}