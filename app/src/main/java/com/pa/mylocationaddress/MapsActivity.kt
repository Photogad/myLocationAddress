package com.pa.mylocationaddress

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pa.mylocationaddress.R
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap


    //// add location manager and listener variables. Needed if we are getting user location on map. initialize later when map is ready.
    var locationManager : LocationManager? = null
    var locationListener : LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //// set map long click listener
        mMap.setOnMapLongClickListener(myListener)

/*        // Add a marker in Sydney and move the camera
        val amsterdam = LatLng(52.3791283, 4.8980833)
        mMap.addMarker(MarkerOptions().position(amsterdam).title("Amsterdam Central Station"))

        // mMap.moveCamera(CameraUpdateFactory.newLatLng(amsterdam))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, 18F))*/


        //// create the locationmanager which gets the system service from location service
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //// add the location listener as an object
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {

                if (location != null) {

                    //// when location is changed, old markers will be erased.
                    mMap.clear()

                    //// get user location
                    val userLocation = LatLng(location!!.latitude, location.longitude)

                    //// add marker and zoom into user location
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15F))



                    //// variable for geocoder (get address)
                    val geocoder = Geocoder(applicationContext, Locale.getDefault())

                    //// we are going to try and catch this, because sometimes there just is not an address.
                    try {

                        //// try to get address from user location and pass the list to a variable. We expect 1 result.
                        val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        //// checking to make sure an address was retrieved (greater than 0)
                        if (addressList != null && addressList.size > 0) {
                            println("Address Info: " + addressList[0].toString())

                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

            }

            override fun onProviderEnabled(provider: String?) {

            }

            override fun onProviderDisabled(provider: String?) {

            }





        }

        //// checks if the user has granted fine location permission. If not, then ask them for it.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //// request the permission, with the request code to be used later
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            //// if we already have permission, request location updates. How often do we want to check, and what distance?
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f, locationListener)

            //// last location variable
            val lastLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            var userLastLocation = LatLng(lastLocation.latitude, lastLocation.longitude)


            //// do the same thing like before in the onLocationChanged listener, but with userLastLocation
            mMap.addMarker(MarkerOptions().position(userLastLocation).title("Your Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15F))

        }







    }


    //// what happens when the user long clicks on the map
    val myListener = object : GoogleMap.OnMapLongClickListener {
        override fun onMapLongClick(p0: LatLng?) {

            //// get rid of previous markers
            mMap.clear()

            //// geocoder variable
            val geocoder = Geocoder(applicationContext,Locale.getDefault())

            //// address variable
            var address = ""

            try {

                val addressList = geocoder.getFromLocation(p0!!.latitude, p0!!.longitude, 1)

                if (addressList != null && addressList.size > 0) {
                    if (addressList[0].thoroughfare != null) {
                        address += addressList[0].thoroughfare

                        if (addressList[0].subThoroughfare != null) {
                            address += addressList[0].subThoroughfare
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }


            if (address.equals("")){
                address = "No Address"
            }

            mMap.addMarker(MarkerOptions().position(p0!!).title(address))

        }

    }

    //// what happens the first time the user gives permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        //// requst code from earlier permission check
        if (requestCode == 1) {
            //// do we have a grant result?
            if (grantResults.size > 0) {
                //// is the grant result the one we want?
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //// do same thing like before in permission request
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f, locationListener)
                }
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }




}
