package com.under.tptr

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.under.tptr.databinding.ActivityMapsBinding
import com.under.tptr.model.DeliveryMan
import com.under.tptr.model.PackageClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback{

    private var pack:PackageClient? = null
    private var currentUser: DeliveryMan? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var manager: LocationManager

    private var meMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageClientJson = (intent.extras?.getString("currentPack")).toString()
        pack = Gson().fromJson(packageClientJson, PackageClient::class.java)
        val currentUserJson = (intent.extras?.getString("currentUser")).toString()
        currentUser = Gson().fromJson(currentUserJson, DeliveryMan::class.java)

        binding.updateButton.setOnClickListener {
            val latitude: Double = meMarker?.position?.latitude!!
            val longitude: Double = meMarker?.position?.longitude!!

            Firebase.firestore
                .collection("empresas")
                .document(currentUser?.empresaNIT!!)
                .collection("paquetes")
                .document(pack?.guia!!).update("estado",PackageClient().DELIVERED_STATE).addOnCompleteListener {
                    Log.e(">>>","Estado  cambiado")
                }

            Firebase.firestore
                .collection("repartidores")
                .document(currentUser?.id!!)
                .collection("paquetesEnDistribucion")
                .document(pack?.guia!!).delete().addOnCompleteListener {
                    Log.e(">>>","Eliminado de los deliverys")
                }

            Firebase.firestore
                .collection("repartidores")
                .document(currentUser?.id!!)
                .collection("paquetesEnDistribucion").get().addOnCompleteListener { task ->
                    val ids : ArrayList<String> = ArrayList()
                    for(document in task.result!!) ids.add("${document.get("id")}")
                    lifecycleScope.launch(Dispatchers.IO){
                        if(ids.size!=0){
                            for(id in ids){
                                var query = Firebase.firestore
                                    .collection("empresas")
                                    .document(currentUser?.empresaNIT!!)
                                    .collection("paquetes")
                                    .document(id)
                                query.update("latitud",latitude).addOnCompleteListener {
                                    Log.e(">>>","${id}: Latitud  cambiada")
                                }
                                query.update("longitud",longitude).addOnCompleteListener {
                                    Log.e(">>>","${id}: Longitud cambiada")
                                }
                            }
                        }
                    }
                }
            finish()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        manager = getSystemService(LOCATION_SERVICE) as LocationManager
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
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setInitialPos()

        //interacciones basicas
        mMap.setOnMapClickListener {
            editMarkerPosition(meMarker,it.latitude,it.longitude)
        }
    }

    //Me inicializa el marcador con la última posición conocida
    @SuppressLint("MissingPermission") //DECLARO QUE YA MANEJÉ LOS PERMISOS
    fun setInitialPos(){
        val pos = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) //Location (SENSOR)
        meMarker = if (pos != null){
            putNewMarker(pos.latitude,pos.longitude)
        }else {
            putNewMarker(3.492445, -76.522139)//BODEGA
        }
    }

    //Crea un nuevo marcador que aparece en el googleMaps, y luego lo retorna
    private fun putNewMarker(lat:Double, lng:Double) : Marker? {
        val pos = LatLng(lat, lng)
        val marker = mMap
            .addMarker(MarkerOptions().position(pos).title("${pack?.guia}").snippet("ID del cliente: ${pack?.idCliente}"))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(pos))//ANIMATION CAMERA
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos,17f))//ANIMATION ZOOM
        return marker
    }

    //cambia la posicion de un marcador
    private fun editMarkerPosition(marker: Marker?,lat: Double,lng: Double){
        val pos = LatLng(lat,lng)
        marker?.position = pos
        mMap.animateCamera(CameraUpdateFactory.newLatLng(pos))
    }
}