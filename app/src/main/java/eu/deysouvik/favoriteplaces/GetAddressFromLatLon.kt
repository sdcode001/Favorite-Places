package eu.deysouvik.favoriteplaces

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.*

class GetAddressFromLatLon(val context: Context,val latitude:Double,val longitude:Double):AsyncTask<Void,String,String>() {

    private val geocoder: Geocoder =Geocoder(context, Locale.getDefault())
    lateinit var mAddressListener:AddressListener

    override fun doInBackground(vararg p0: Void?): String {
        try{
            val addressList:List<Address> =geocoder.getFromLocation(latitude,longitude,1)
            if(addressList.isNotEmpty()){
                val address=addressList[0]
                val sb=StringBuilder()
                for(i in 0..address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                sb.deleteCharAt(sb.length-1)
                return sb.toString()
            }

        }catch (e:Exception){}

      return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
       if(result==null){
           mAddressListener.onError()
       }
        else{
            mAddressListener.onAddressFound(result)
       }

    }

    fun setAddressListener(addressListener:AddressListener){
        mAddressListener=addressListener
    }
    fun getAddress(){
        execute()
    }

    interface AddressListener{
        fun onAddressFound(address: String?)
        fun onError()
    }


}