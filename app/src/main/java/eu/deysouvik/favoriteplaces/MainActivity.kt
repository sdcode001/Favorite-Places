package eu.deysouvik.favoriteplaces
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.happyplaces.utils.SwipeToDeleteCallback
import eu.deysouvik.favoriteplaces.adapter.FavotitePlacesAdapter
import eu.deysouvik.favoriteplaces.database.DBHandler
import eu.deysouvik.favoriteplaces.models.HappyPlaceDetail
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        add.setOnClickListener {
            val intent=Intent(this,AddPlacesActivity::class.java)
            startActivity(intent)
        }

       getPlacesFromDATABASE()


    }

    override fun onResume() {
        super.onResume()
        getPlacesFromDATABASE()
    }

    private fun setupfavoriteplaces_Recyclerview(placeList:ArrayList<HappyPlaceDetail>){
        rvPlaces.layoutManager=LinearLayoutManager(this)
        rvPlaces.setHasFixedSize(true)
        val adapter=FavotitePlacesAdapter(this,placeList)
        rvPlaces.adapter=adapter

        adapter.setOnClickListener(object :FavotitePlacesAdapter.OnClickListener{
            override fun onClick(position: Int, place: HappyPlaceDetail) {
                val intent=Intent(this@MainActivity,FavoritePlaceDetail::class.java)
                intent.putExtra(EXTRA_PLACE_DETAIL,place)
                startActivity(intent)
            }
        })

            val deleteSwipeHandler=object: SwipeToDeleteCallback(this){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val Adapter=rvPlaces.adapter as FavotitePlacesAdapter
                    Adapter.removeAt(viewHolder.adapterPosition)

                    getPlacesFromDATABASE()
                }

            }

            val deleteItemTouchHelper=ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(rvPlaces)


    }


    private fun getPlacesFromDATABASE(){
        val DB=DBHandler(this)
        val placesList:ArrayList<HappyPlaceDetail> = DB.getPlaces()

        //set all the data to recyclerview----->
        if(placesList.size>0){
            tv_overRV.visibility= View.GONE
            rvPlaces.visibility=View.VISIBLE
            setupfavoriteplaces_Recyclerview(placesList)
        }
        else{
            tv_overRV.visibility= View.VISIBLE
            rvPlaces.visibility=View.GONE
        }

    }



    companion object{
        val EXTRA_PLACE_DETAIL="place_detail"
    }







}