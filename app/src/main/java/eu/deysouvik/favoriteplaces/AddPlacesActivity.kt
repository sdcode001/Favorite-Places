package eu.deysouvik.favoriteplaces

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import eu.deysouvik.favoriteplaces.database.DBHandler
import eu.deysouvik.favoriteplaces.models.HappyPlaceDetail
import kotlinx.android.synthetic.main.activity_add_places.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddPlacesActivity : AppCompatActivity() {

    companion object{
        val CAMERA_REQUEST_CODE=1
        val STORAGE_REQUEST_CODE=2
        val IMAGE_FOLDER="FavoritePlaceImages"
        val PLACE_AUTOCOMPLETE_REQUEST_CODE=3
    }
    var cal=Calendar.getInstance()
    var date:String=""
    var photo:Bitmap?=null
    var image_location:Uri?=null
    var mlatitude=0.0
    var mlongitude=0.0
    var mtitle=""
    var mdescription=""
    var mlocation:String?=""

    var GPSenabled:Boolean=false
    lateinit var locationProviderClint: FusedLocationProviderClient

    lateinit var dateSetListener:DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_places)
        setSupportActionBar(toolbar_addplace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_addplace.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener=DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDate()
        }

       updateDate()


        if(!Places.isInitialized()){
            Places.initialize(this@AddPlacesActivity,resources.getString(R.string.google_maps_api_key))
        }

        locationProviderClint= LocationServices.getFusedLocationProviderClient(this)

        if(!isLocationEnabled()){
            val gpsdialog= androidx.appcompat.app.AlertDialog.Builder(this)
            gpsdialog.setIcon(R.drawable.icon_gps_off)
            gpsdialog.setMessage("Your Location Provider is turned Off! Please turn it on in Settings")
            gpsdialog.setPositiveButton("Go to Settings",DialogInterface.OnClickListener { dialog, which ->
                val intent_to_settings=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent_to_settings)
            })
            gpsdialog.setNegativeButton("No",DialogInterface.OnClickListener { dialog, which ->  })
            gpsdialog.show()

        }
        else{
            GPSenabled=true

        }



    }


    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    fun tv_current_location(view:View){
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION

        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                     setMyLocationData()
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,token: PermissionToken) {
                val permissionDialog= androidx.appcompat.app.AlertDialog.Builder(this@AddPlacesActivity)
                permissionDialog.setMessage("It looks like you have turned off some permissions required for this App to work.please turned on them from Application Settings")
                permissionDialog.setPositiveButton("Go to Settings",DialogInterface.OnClickListener { dialog, which ->
                    try{
                        val intent_to_appSettings=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri= Uri.fromParts("package",packageName,null)
                        intent_to_appSettings.data=uri
                        startActivity(intent_to_appSettings)
                    }catch(e:ActivityNotFoundException){
                        e.printStackTrace()
                    }
                })
                permissionDialog.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialog, which->  })
                permissionDialog.show()
            }
        }).onSameThread().check()
    }



    @SuppressLint("MissingPermission")
    private fun setMyLocationData(){
        val locationRequest=com.google.android.gms.location.LocationRequest()
        locationRequest.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
        locationProviderClint.requestLocationUpdates(locationRequest,mlocationCallback, Looper.myLooper())

    }
    private val mlocationCallback=object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            val mlastLocation: Location =locationResult!!.lastLocation
            mlatitude=mlastLocation.latitude
            mlongitude=mlastLocation.longitude

            val addressTask=GetAddressFromLatLon(this@AddPlacesActivity,mlatitude,mlongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLon.AddressListener{
               override fun onAddressFound(address:String?){
                   mlocation=address.toString()
                   //Log.i("address",mlocation.toString())
                   et_location.setText(mlocation)
               }
                override fun onError(){
                    Log.i("getLocation","Something went wrong!")
                }

            })
            addressTask.getAddress()

            Toast.makeText(this@AddPlacesActivity, "Location Saved", Toast.LENGTH_SHORT).show()
            //Log.i("latlon","$mlatitude, $mlongitude")

        }
    }

    fun et_date(view: View){
          DatePickerDialog(this,
              dateSetListener,
              cal.get(Calendar.YEAR),
              cal.get(Calendar.MONTH),
              cal.get(Calendar.DAY_OF_MONTH)
              ).show()
    }

    private fun updateDate(){
        val format="dd MMM yyyy"
        val sdf=SimpleDateFormat(format, Locale.getDefault())
        date=sdf.format(cal.time)
        et_date.setText(date)
    }


    fun et_Location(view: View){
        try{
            val fields= listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )

            val intent= Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(this@AddPlacesActivity)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }catch(e:Exception){
           e.printStackTrace()
        }
    }


     fun tv_add_image(view: View){
        val pictureDialog=AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action:")
        val selectionMethods= arrayOf("Choose photo from gallery.","Capture from Camera")
        pictureDialog.setItems(selectionMethods){
            dialog,which->
            when(which){
                0->setFromGallery()
                1->setFromCamera()
            }
        }
         pictureDialog.show()
    }

    private fun setFromCamera(){
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.CAMERA,
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                      val intent_cam=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent_cam, CAMERA_REQUEST_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                val permissionDialog= androidx.appcompat.app.AlertDialog.Builder(this@AddPlacesActivity)
                permissionDialog.setMessage("It looks like you don't give Camera permissions required for this App to work.please turned on them from Application Settings")
                permissionDialog.setPositiveButton("Go to Settings",
                    DialogInterface.OnClickListener { dialog, which ->
                        try{
                            val intent_to_appSettings= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri= Uri.fromParts("package",packageName,null)
                            intent_to_appSettings.data=uri
                            startActivity(intent_to_appSettings)
                        }catch(e: ActivityNotFoundException){
                            e.printStackTrace()
                        }
                    })
                permissionDialog.setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, which->  })
                permissionDialog.show()
            }
        }).onSameThread().check()
    }



    private fun setFromGallery(){
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val gallery_intent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(gallery_intent, STORAGE_REQUEST_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                val permissionDialog= androidx.appcompat.app.AlertDialog.Builder(this@AddPlacesActivity)
                permissionDialog.setMessage("It looks like you don't give storage permissions required for this App to work.please turned on them from Application Settings")
                permissionDialog.setPositiveButton("Go to Settings",
                    DialogInterface.OnClickListener { dialog, which ->
                    try{
                        val intent_to_appSettings= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri= Uri.fromParts("package",packageName,null)
                        intent_to_appSettings.data=uri
                        startActivity(intent_to_appSettings)
                    }catch(e: ActivityNotFoundException){
                        e.printStackTrace()
                    }
                })
                permissionDialog.setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, which->  })
                permissionDialog.show()
            }
        }).onSameThread().check()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if(requestCode== CAMERA_REQUEST_CODE){
                val bitmap_img:Bitmap=data!!.extras!!.get("data") as Bitmap
                photo=bitmap_img
                image_location=saveImageToExternalStorage(photo!!)
                iv_place_image.setImageBitmap(photo)
            }
            else if(requestCode== STORAGE_REQUEST_CODE){
               if(data!=null){
                   val contentUri=data.data
                   try{
                       val img_bitmap= MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                       photo=img_bitmap
                       image_location=saveImageToExternalStorage(photo!!)
                       iv_place_image.setImageBitmap(photo)
                   }catch(e:IOException){
                      e.printStackTrace()
                       Toast.makeText(this, "Failed to load Image!", Toast.LENGTH_SHORT).show()
                   }

               }

            }
        else if(requestCode== PLACE_AUTOCOMPLETE_REQUEST_CODE){
            try{
                val place=Autocomplete.getPlaceFromIntent(data!!)
                et_location.setText(place.address)
                mlatitude=place.latLng!!.latitude
                mlongitude=place.latLng!!.longitude
                mlocation=place.address
            }catch (e:Exception){
                e.printStackTrace()
            }


            }

    }


    private fun saveImageToExternalStorage(bitmap:Bitmap?):Uri{
        val wrapper=ContextWrapper(applicationContext)
        var file=wrapper.getDir(IMAGE_FOLDER,Context.MODE_PRIVATE)
        file= File(file,"${UUID.randomUUID()}.jpg")
        try {
          val stream=FileOutputStream(file)
          bitmap!!.compress(Bitmap.CompressFormat.JPEG,100,stream)
          stream.flush()
          stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
       return Uri.parse(file.absolutePath)
    }


    fun btn_save(view: View){
              when{
                  et_title.text.isNullOrEmpty()->{
                      Toast.makeText(this, "Title is empty", Toast.LENGTH_SHORT).show()
                  }
                  et_description.text.isNullOrEmpty()->{
                      Toast.makeText(this, "Description is empty", Toast.LENGTH_SHORT).show()
                  }
                  image_location==null->{
                      Toast.makeText(this, "Please add an image", Toast.LENGTH_SHORT).show()
                  }
                  et_location.text.isNullOrEmpty()->{
                      Toast.makeText(this, "Put the location", Toast.LENGTH_SHORT).show()
                  }
                  else->{
                      mtitle=et_title.text.toString()
                      mdescription=et_description.text.toString()
                      mlocation=et_location.text.toString()
                      val place=HappyPlaceDetail(0,mtitle,image_location.toString(),mdescription,date,mlocation!!,mlatitude,mlongitude)
                      val dbHandler=DBHandler(this)
                      val result=dbHandler.addPlace(place)
                      if(result>0){
                          Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
                      }
                      else{
                          Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                      }
                      finish()
                  }
              }
    }



}