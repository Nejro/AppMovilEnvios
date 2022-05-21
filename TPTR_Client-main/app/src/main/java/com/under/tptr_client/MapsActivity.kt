package com.under.tptr_client

import android.annotation.SuppressLint
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.under.tptr_client.databinding.ActivityMapsBinding
import com.under.tptr_client.model.PackageClient

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var pack: PackageClient? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var manager: LocationManager
    private var meMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener{ finish() }

        val packageClientJson = (intent.extras?.getString("currentPack")).toString()
        pack = Gson().fromJson(packageClientJson, PackageClient::class.java)

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
        setInitialPos()
    }

    //Me inicializa el marcador con la última posición conocida
    @SuppressLint("MissingPermission") //DECLARO QUE YA MANEJÉ LOS PERMISOS
    fun setInitialPos(){
        meMarker = putNewMarker(pack?.latitud!!,pack?.longitud!!)
    }

    //Crea un nuevo marcador que aparece en el googleMaps, y luego lo retorna
    private fun putNewMarker(lat:Double, lng:Double) : Marker? {
        val pos = LatLng(lat, lng)
        val marker = mMap
            .addMarker(MarkerOptions().position(pos).title("${pack?.guia}").snippet("ESTADO: ${pack?.estado}"))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(pos))//ANIMATION CAMERA
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos,17f))//ANIMATION ZOOM
        return marker
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    /*
    //cambia la posicion de un marcador
    private fun editMarkerPosition(marker: Marker?,lat: Double,lng: Double){
        val pos = LatLng(lat,lng)
        marker?.position = pos
        mMap.animateCamera(CameraUpdateFactory.newLatLng(pos))
    }
     */
}