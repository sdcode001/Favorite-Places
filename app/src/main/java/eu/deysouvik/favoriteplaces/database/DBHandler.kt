package eu.deysouvik.favoriteplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import eu.deysouvik.favoriteplaces.models.HappyPlaceDetail

class DBHandler(context:Context):SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {
    companion object{
        val DATABASE_NAME="FAVORITEPLACES_DATABASE"
        val DATABASE_VERSION=1
        val TABLE_NAME="FAVORITE_PLACES_TABLE"
        //all the collum names
        val KEY_ID="_id"
        val KEY_TITLE="title"
        val KEY_IMAGE="image"
        val KEY_DESCRIPTION="description"
        val KEY_DATE="Date"
        val KEY_LOCATION="location"
        val KEY_LATITUDE="lat"
        val KEY_LONGITUDE="lon"

    }
    override fun onCreate(db: SQLiteDatabase?) {
       val CREATE_DATABASE_TABLE=("CREATE TABLE $TABLE_NAME ("
               + KEY_ID+" INTEGER PRIMARY KEY,"
               + KEY_TITLE+" TEXT,"
               + KEY_IMAGE+" TEXT,"
               + KEY_DESCRIPTION+" TEXT,"
               + KEY_DATE+" TEXT,"
               + KEY_LOCATION+" TEXT,"
               + KEY_LATITUDE+" TEXT,"
               + KEY_LONGITUDE+" TEXT)"
               )
        db?.execSQL(CREATE_DATABASE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
         db?.execSQL("DROP TABLE IF EXISTS ${TABLE_NAME}")
        onCreate(db)
    }


    fun addPlace(place:HappyPlaceDetail):Long{
        val db=this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(KEY_TITLE,place.title)
        contentValues.put(KEY_IMAGE,place.image)
        contentValues.put(KEY_DESCRIPTION,place.description)
        contentValues.put(KEY_DATE,place.date)
        contentValues.put(KEY_LOCATION,place.location)
        contentValues.put(KEY_LATITUDE,place.lat)
        contentValues.put(KEY_LONGITUDE,place.lon)
        val result=db.insert(TABLE_NAME,null,contentValues)
        db.close()
        return result
    }

    fun deletePlace(place:HappyPlaceDetail):Int{
        val db=this.writableDatabase
        val result=db.delete(TABLE_NAME, KEY_ID+"="+place.id,null)
        db.close()
        return result
    }


    fun getPlaces():ArrayList<HappyPlaceDetail>{
        val db=this.readableDatabase
        val placesList=ArrayList<HappyPlaceDetail>()
        val selectQuery="Select * From $TABLE_NAME"

        try{
            val cursor:Cursor= db.rawQuery(selectQuery,null)
                if(cursor.moveToFirst()) {
                    do  {
                        val place = HappyPlaceDetail(
                            cursor.getInt(cursor.getColumnIndex(KEY_ID).toInt()),
                            cursor.getString(cursor.getColumnIndex(KEY_TITLE).toInt()),
                            cursor.getString(cursor.getColumnIndex(KEY_IMAGE).toInt()),
                            cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION).toInt()),
                            cursor.getString(cursor.getColumnIndex(KEY_DATE).toInt()),
                            cursor.getString(cursor.getColumnIndex(KEY_LOCATION).toInt()),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE).toInt()),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE).toInt())
                        )
                        placesList.add(place)

                    }while(cursor.moveToNext())
                }
            cursor.close()
            db.close()
        }catch(e:SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return placesList
    }




}