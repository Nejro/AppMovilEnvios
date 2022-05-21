package com.under.tptr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.under.tptr.databinding.ActivityRouteBinding
import com.under.tptr.model.Client
import com.under.tptr.model.DeliveryMan
import com.under.tptr.model.PackageClient
import com.under.tptr.recyclerCurrent.PackageCurrentAdapter
import com.under.tptr.recyclerPlan.PackagePlanAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RouteActivity : AppCompatActivity(), PackageCurrentAdapter.Listener {

    private val binding: ActivityRouteBinding by lazy { ActivityRouteBinding.inflate(layoutInflater) }
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: PackageCurrentAdapter
    var currentUser: DeliveryMan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        layoutManager = LinearLayoutManager(this)
        adapter = PackageCurrentAdapter()
        adapter.listener = this

        val deliveryManJson = (intent.extras?.getString("currentUser")).toString()
        currentUser = Gson().fromJson(deliveryManJson,DeliveryMan::class.java)

        binding.currentRecycler.layoutManager = layoutManager
        binding.currentRecycler.setHasFixedSize(true)
        binding.currentRecycler.adapter = adapter

        binding.logoutButton.setOnClickListener{
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.endButton.setOnClickListener {
            Firebase.firestore
                .collection("repartidores")
                .document(currentUser?.id!!)
                .update("estado",DeliveryMan().NO_DELIVERING_STATE).addOnCompleteListener {
                    Log.e(">>>","Se ha cambiado el estado del deliveryMan a NO_DELIVERING")
                }
            setAllWarehouseState()
            intent = Intent(this,StartDistributionActivity::class.java).apply {
                putExtra("currentUser", Gson().toJson(currentUser))
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        recoveryActualState()

    }

    private fun setAllWarehouseState() {
        adapter.clean()
        Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .collection("paquetesEnDistribucion").get().addOnCompleteListener { task ->
                val ids: ArrayList<String> = ArrayList()
                for (document in task.result!!) {
                    //
                    ids.add("${document.get("id")}")
                }
                if (ids.size != 0) {
                    for (id in ids) {
                        Firebase.firestore.collection("empresas")
                            .document(currentUser?.empresaNIT!!)
                            .collection("paquetes")
                            .document(id).update("estado", PackageClient().WAREHOUSE_STATE)
                    }
                }
            }
    }

    private fun recoveryActualState(){
        adapter.clean()
        Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .collection("paquetesEnDistribucion").get().addOnCompleteListener { task ->
                val ids : ArrayList<String> = ArrayList()
                for(document in task.result!!) {
                    //
                    ids.add("${document.get("id")}")
                }
                lifecycleScope.launch(Dispatchers.IO){
                    if(ids.size!=0){
                        for(id in ids){
                            searchPackBussiness(id).addOnCompleteListener { task ->
                                for(document in task.result!!)adapter.addPack(document.toObject(PackageClient::class.java))
                            }.await()
                        }
                    }
                    withContext(Dispatchers.Main){
                        if(adapter.itemCount == 0) onZeroPackagesListener()
                    }
                }
            }
    }

    private fun searchPackBussiness(guide: String): Task<QuerySnapshot> {
        return Firebase.firestore.collection("empresas")
            .document(currentUser?.empresaNIT!!)
            .collection("paquetes")
            .whereEqualTo("guia",guide)
            .get()
    }

    override fun onZeroPackagesListener() {
        Toast.makeText(this,R.string.finish_day, Toast.LENGTH_LONG).show()
        Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .update("estado",DeliveryMan().NO_DELIVERING_STATE).addOnCompleteListener {
                Log.e(">>>","Se ha cambiado el estado del deliveryMan a NO_DELIVERING")
                val intent = Intent(this, StartDistributionActivity::class.java).apply {
                    putExtra("currentUser", Gson().toJson(currentUser))
                }
                startActivity(intent)
            }
    }

    override fun onAddPackListener() {
        Log.e(">>>", "AGREGADO CORRECTAMENTE AL RECYCLER")
    }

    override fun onRemovePackListener(pack: PackageClient?) {
        Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .collection("paquetesEnDistribucion")
            .document(pack?.guia!!).delete()
            .addOnSuccessListener { Log.d(">>>", "EL PAQUETE SE HA ELIMINADO CORRECTAMENTE") }
            .addOnFailureListener { e -> Log.w(">>>", "ERROR ELIMINANDO EL PAQUETE", e)}
        Log.e(">>>", "ELIMINADO CORRECTAMENTE DEL RECYCLER")
    }

    override fun onNext(pack:PackageClient?) {
        Firebase.firestore.collection("clientes").document(pack?.idCliente!!).get().addOnCompleteListener { task ->
            val client:Client? = task.result.toObject(Client::class.java)
            val intent = Intent(this, ClientInfoActivity::class.java).apply {
                putExtra("currentClient", Gson().toJson(client))
                putExtra("currentPack", Gson().toJson(pack))
                putExtra("currentUser", Gson().toJson(currentUser))
            }
            startActivity(intent)
        }
    }
}