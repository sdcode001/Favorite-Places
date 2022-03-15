package eu.deysouvik.favoriteplaces

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import eu.deysouvik.favoriteplaces.models.HappyPlaceDetail
import kotlinx.android.synthetic.main.activity_add_places.*
import kotlinx.android.synthetic.main.activity_favorite_place_detail.*

class FavoritePlaceDetail : AppCompatActivity() {

    lateinit var placeDetail:HappyPlaceDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_place_detail)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAIL)){
            placeDetail=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAIL) as HappyPlaceDetail
        }

        setSupportActionBar(toolbar_happy_place_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title=placeDetail.title
        toolbar_happy_place_detail.setNavigationOnClickListener {
            onBackPressed()
        }

        iv_Place_Image.setImageURI(Uri.parse(placeDetail.image))
        tv_Description.text=placeDetail.description
        tv_Location.text=placeDetail.location

    }


    fun btn_mapView(view: View){
        val intent = Intent(this,MapViewActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAIL,placeDetail)
        startActivity(intent)
    }


}