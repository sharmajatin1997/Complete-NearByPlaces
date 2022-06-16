package com.app.nearbyplaces

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class GetNearbyPlaces : AsyncTask<Any?, String?, String?>() {
    private var googleplaceData: String? = null
    private var url: String? = null
    private var mMap: GoogleMap? = null
    lateinit var bitmap: Bitmap
    lateinit var drawable: Drawable
    lateinit var preferenceHelper: SharedPreferenceHelper

    protected override fun doInBackground(vararg params: Any?): String? {
        mMap = params[0] as GoogleMap
        url = params[1] as String
        val downloadUrl = DownloadUrl()
        try {
            googleplaceData = downloadUrl.ReadTheURL(url)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return googleplaceData
    }

    override fun onPostExecute(s: String?) {
        var nearByPlacesList: List<HashMap<String, String>?>? = null
        val dataParser = DataParser()
        nearByPlacesList = dataParser.parse(s)
        DisplayNearbyPlaces(nearByPlacesList)
    }

    private fun DisplayNearbyPlaces(nearByPlacesList: List<HashMap<String, String>?>) {

        for (i in nearByPlacesList.indices) {
            val markerOptions = MarkerOptions()
            val googleNearbyPlace = nearByPlacesList[i]
            val nameOfPlace = googleNearbyPlace!!["place_name"]
            val vicinity = googleNearbyPlace["vicinity"]
            val icon = googleNearbyPlace["icon"]
            val lat = googleNearbyPlace["lat"]!!.toDouble()
            val lng = googleNearbyPlace["lng"]!!.toDouble()
            val latLng = LatLng(lat, lng)
            markerOptions.position(latLng)
            markerOptions.title("$nameOfPlace : $vicinity")
            preferenceHelper = SharedPreferenceHelper.getInstance()!!
            if (preferenceHelper.getIsHosp()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.new_hos))
            } else if (preferenceHelper.getIsSchool()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school))
            } else if (preferenceHelper.getIsResort()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.new_resort))
            }
            mMap!!.addMarker(markerOptions)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(17f))
        }
    }
}