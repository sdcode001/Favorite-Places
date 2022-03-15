package eu.deysouvik.favoriteplaces.models

import java.io.Serializable

data class HappyPlaceDetail(
    val id:Int,
    val title:String,
    val image:String,
    val description:String,
    val date:String,
    val location:String,
    val lat:Double,
    val lon:Double
):Serializable
