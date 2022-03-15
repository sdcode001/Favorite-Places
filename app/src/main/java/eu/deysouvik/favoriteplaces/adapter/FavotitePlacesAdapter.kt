package eu.deysouvik.favoriteplaces.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eu.deysouvik.favoriteplaces.FavoritePlaceDetail
import eu.deysouvik.favoriteplaces.MainActivity
import eu.deysouvik.favoriteplaces.R
import eu.deysouvik.favoriteplaces.database.DBHandler
import eu.deysouvik.favoriteplaces.models.HappyPlaceDetail
import kotlinx.android.synthetic.main.item_happy_place.view.*

open class FavotitePlacesAdapter(
    val context:Context,
    val placesList:ArrayList<HappyPlaceDetail>):
     RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener:OnClickListener?=null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_happy_place,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val place=placesList[position]
        if(holder is MyViewHolder){
            holder.itemView.iv_place_image.setImageURI(Uri.parse(place.image))
            holder.itemView.tvTitle.text=place.title
            holder.itemView.tvDescription.text=place.description

            holder.itemView.setOnClickListener {
               if(onClickListener!=null){
                   onClickListener!!.onClick(position, place)
               }

            }
        }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }

    interface OnClickListener{
        fun onClick(position:Int,place:HappyPlaceDetail)
    }


    fun removeAt(position:Int){
        val dbHandler=DBHandler(context)
        val isDeleted=dbHandler.deletePlace(placesList[position])
        if(isDeleted>0){
            placesList.removeAt(position)
            notifyItemRemoved(position)
        }

    }




    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)

}