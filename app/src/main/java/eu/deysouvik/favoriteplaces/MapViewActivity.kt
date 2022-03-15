package eu.deysouvik.favoriteplaces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import eu.deysouvik.favoriteplaces.models.HappyPlaceDetail
import kotlinx.android.synthetic.main.activity_map_view.*

class MapViewActivity : AppCompatActivity(),OnMapReadyCallback {

    var mapData:HappyPlaceDetail?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAIL)){
            mapData=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAIL) as HappyPlaceDetail
        }

        if(mapData!=null){
            setSupportActionBar(toolbar_mapView)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=mapData!!.title
            toolbar_mapView.setNavigationOnClickListener {
                onBackPressed()
            }
        }


        val supportMapFragment:SupportMapFragment=supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)

    }

    override fun onMapReady(googlemap: GoogleMap) {
        val position=LatLng(mapData!!.lat,mapData!!.lon)
         googlemap.addMarker(MarkerOptions().position(position).title(mapData!!.location))
         val zoom=CameraUpdateFactory.newLatLngZoom(position,11f)
         googlemap.animateCamera(zoom)
    }


}