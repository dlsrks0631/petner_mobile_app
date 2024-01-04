package com.example.petner
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class AddressSearchActivity : AppCompatActivity() {
    private val AUTOCOMPLETE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.address_search)
        Places.initialize(applicationContext, "AIzaSyAMoyBCq2Gm7T6UTLpjR_hy2hXXJynAwUU")

        // 버튼 클릭 시 검색창 표시
        val searchButton: Button = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            startAutocompleteActivity()
        }
    }

    private fun startAutocompleteActivity() {
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS
            )
        )

            .setCountry("KR")
            .setHint("주소를 입력하세요")
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    // 에러 처리
                }
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    // 선택한 장소 정보 처리
                    val placeName = place.name
                    val placeAddress = place.address
                    val placeId = place.id
                    val latLng = place.latLng

                    // 여기에서 선택한 주소 정보를 사용
                    val selectedAddress = placeAddress ?: "주소 정보 없음"

                    val resultIntent = Intent()
                    resultIntent.putExtra("selected_address", selectedAddress)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                Activity.RESULT_CANCELED -> {
                    // 사용자가 취소한 경우
                }
            }
        }
    }
}