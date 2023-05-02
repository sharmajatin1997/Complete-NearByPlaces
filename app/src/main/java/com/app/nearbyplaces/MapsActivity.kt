package com.app.nearbyplaces

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.app.nearbyplaces.databinding.ActivityMapsBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


open class MapsActivity : FragmentActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private var mMap: GoogleMap? = null
    private var binding: ActivityMapsBinding? = null
    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    private var lastLocation: Location? = null
    private var currentUserLocationMarker: Marker? = null
    private var latitide = 0.0
    private var longitude = 0.0
    private val ProximityRadius = 10000


    lateinit var  preferenceHelper: SharedPreferenceHelper


    lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    private var url: String? = null

    init {
        preferenceHelper = SharedPreferenceHelper.getInstance()!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MapsActivity)
        fetchLocation()
    }


    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            return
        }
        val task = fusedLocationProviderClient.lastLocation

        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location

                // Add a marker in Sydney and move the camera
                val sydney = LatLng(currentLocation.latitude, currentLocation.longitude)
                mMap?.addMarker(MarkerOptions().position(sydney))
                mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                mMap?.animateCamera(CameraUpdateFactory.zoomTo(12f))
                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
                        currentLocation.longitude, Toast.LENGTH_SHORT).show()
                val supportMapFragment = (supportFragmentManager.findFragmentById(R.id.map) as
                        SupportMapFragment?)!!
                supportMapFragment.getMapAsync(this@MapsActivity)

            }
        }

    }

    fun onClick(v: View) {
        val hospital = "hospital"
        val school = "school"
        val restaurant = "restaurant"
        val transferData = arrayOfNulls<Any>(2)
        val getNearbyPlaces = GetNearbyPlaces()
        when (v.id) {
            R.id.hospitals_nearby -> {
                preferenceHelper.clearAll()
                preferenceHelper.saveIsHos(true)
                mMap!!.clear()
                fetchLocation()
                val url = getUrl(currentLocation.latitude, currentLocation.longitude, hospital)
                transferData[0] = mMap
                transferData[1] = url
                getNearbyPlaces.execute(*transferData)
                Toast.makeText(this, "Searching for Nearby Hospitals...", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Showing Nearby Hospitals...", Toast.LENGTH_SHORT).show()
            }
            R.id.schools_nearby -> {
                preferenceHelper.clearAll()
                preferenceHelper.saveIsSchool(true)
                mMap!!.clear()
                fetchLocation()
                url = getUrl(currentLocation.latitude, currentLocation.longitude, school)
                transferData[0] = mMap
                transferData[1] = url
                getNearbyPlaces.execute(*transferData)
                Toast.makeText(this, "Searching for Nearby Schools...", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Showing Nearby Schools...", Toast.LENGTH_SHORT).show()
            }
            R.id.restaurants_nearby -> {
                preferenceHelper.clearAll()
                preferenceHelper.saveIsResort(true)
                mMap!!.clear()
                fetchLocation()
                url = getUrl(currentLocation.latitude, currentLocation.longitude, restaurant)
                transferData[0] = mMap
                transferData[1] = url
                getNearbyPlaces.execute(*transferData)
                Toast.makeText(this, "Searching for Nearby Restaurants...", Toast.LENGTH_SHORT)
                    .show()
                Toast.makeText(this, "Showing Nearby Restaurants...", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun getUrl(latitide: Double, longitude: Double, nearbyPlace: String): String {
        val googleURL =
            StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
        googleURL.append("location=$latitide,$longitude")
        googleURL.append("&radius=$ProximityRadius")
        googleURL.append("&type=$nearbyPlace")
        googleURL.append("&sensor=true")
        googleURL.append("&key=" + "API KEY")
        Log.d("GoogleMapsActivity", "url = $googleURL")
        return googleURL.toString()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }
    }

    fun checkUserLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Request_User_Location_Code)
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Request_User_Location_Code)
            }
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {
                if (googleApiClient == null) {
                    buildGoogleApiClient()
                }
                fetchLocation()
            }
        }

    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClient!!.connect()
    }

    override fun onLocationChanged(location: Location) {
        latitide = location.latitude
        longitude = location.longitude
        lastLocation = location
        if (currentUserLocationMarker != null) {
            currentUserLocationMarker!!.remove()
        }

        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("user Current Location")
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.new_resort))
        currentUserLocationMarker = mMap!!.addMarker(markerOptions)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomBy(12f))
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient!!, this)
        }
    }

    override fun onConnected(bundle: Bundle?) {
        locationRequest = LocationRequest()
        locationRequest!!.interval = 1100
        locationRequest!!.fastestInterval = 1100
        locationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient!!,
                locationRequest!!,
                this)
        }
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        private const val Request_User_Location_Code = 99
    }
}

