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
import com.under.tptr.databinding.ActivityStartDistributionBinding
import com.under.tptr.model.DeliveryMan
import com.under.tptr.model.PackageClient
import com.under.tptr.recyclerPlan.PackagePlanAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StartDistributionActivity : AppCompatActivity(), PackagePlanAdapter.Listener {

    private val binding: ActivityStartDistributionBinding by lazy{ ActivityStartDistributionBinding.inflate(layoutInflater) }
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: PackagePlanAdapter
    var currentUser:DeliveryMan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        layoutManager = LinearLayoutManager(this)
        adapter = PackagePlanAdapter()
        adapter.listener = this

        val deliveryManJson = (intent.extras?.getString("currentUser")).toString()
        currentUser = Gson().fromJson(deliveryManJson,DeliveryMan::class.java)

        binding.trackingPlanRecycler.layoutManager = layoutManager
        binding.trackingPlanRecycler.setHasFixedSize(true)
        binding.trackingPlanRecycler.adapter = adapter
        binding.continueButton.isGone = true
        recoveryActualState()

        binding.addPackToPlanButton.setOnClickListener {
            val guide = binding.editTextGuide.text.toString()
            addPackToPlanButton(guide)
        }

        binding.logoutButton.setOnClickListener{
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.continueButton.setOnClickListener {
            setDeliveryState()
            val intent = Intent(this, RouteActivity::class.java).apply { putExtra("currentUser", Gson().toJson(currentUser)) }
            startActivity(intent)
            finish()
        }
    }

    private fun setDeliveryState(){
        Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .update("estado",DeliveryMan().DELIVERING_STATE)
            .addOnSuccessListener { Log.d(">>>", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(">>>", "Error updating document", e) }

        Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .collection("paquetesEnDistribucion").get().addOnCompleteListener { task ->
                val ids : ArrayList<String> = ArrayList()
                for(document in task.result!!) ids.add("${document.get("id")}")
                lifecycleScope.launch(Dispatchers.IO){
                    if(ids.size!=0){
                        for(id in ids){
                            Firebase.firestore
                                .collection("empresas")
                                .document(currentUser?.empresaNIT!!)
                                .collection("paquetes")
                                .document(id).update("estado", PackageClient().DISTRIBUTION_STATE).addOnCompleteListener {
                                    Log.e(">>>","${id}")
                                }
                        }
                    }
                }
            }
    }

    private fun recoveryActualState(){
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
                }
            }
    }

    private fun addPackToPlanButton(guide:String){
        if(guide!=""){
            searchPackBussiness(guide).addOnCompleteListener { task->
                if(task.result?.size() != 0){
                    lateinit var pack:PackageClient
                    for(document in task.result!!) pack = document.toObject(PackageClient::class.java)
                    when(pack.estado){
                        pack.WAREHOUSE_STATE -> {
                            searchPackDeliveryMan(guide).addOnCompleteListener{ g ->
                                if(g.result?.size() != 0) Toast.makeText(this,R.string.pack_in_list,Toast.LENGTH_SHORT).show()
                                else{
                                    adapter.addPack(pack)
                                    Firebase.firestore
                                        .collection("repartidores")
                                        .document(currentUser?.id!!)
                                        .collection("paquetesEnDistribucion")
                                        .document(pack.guia).set(hashMapOf("id" to pack.guia))
                                        .addOnSuccessListener { Log.d(">>>", "ID DEL PAQUETE AGREGADO CORRECTAMENTE") }
                                        .addOnFailureListener { e -> Log.w(">>>", "ERROR AGREGANDO EL ID DEL PAQUETE", e) }
                                }
                            }
                        }
                        pack.DELIVERED_STATE -> Toast.makeText(this, R.string.pack_delivered_state, Toast.LENGTH_SHORT).show()
                        pack.DISTRIBUTION_STATE -> Toast.makeText(this, R.string.pack_distribution_state, Toast.LENGTH_SHORT).show()
                    }
                }else Toast.makeText(this, R.string.guide_not_found, Toast.LENGTH_SHORT).show()
            }
        }else Toast.makeText(this,"EL CAMPO ESTÁ VACÍO",Toast.LENGTH_SHORT).show()
    }

    private fun searchPackBussiness(guide: String): Task<QuerySnapshot> {
        return Firebase.firestore.collection("empresas")
            .document(currentUser?.empresaNIT!!)
            .collection("paquetes")
            .whereEqualTo("guia",guide)
            .get()
    }

    private fun searchPackDeliveryMan(guide: String): Task<QuerySnapshot>{
        return Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .collection("paquetesEnDistribucion")
            .whereEqualTo("id",guide)
            .get()
    }

    override fun onZeroPackagesListener() {
        binding.continueButton.isGone = true
    }

    override fun onRemovePackListener(pack:PackageClient?){
        Firebase.firestore
            .collection("repartidores")
            .document(currentUser?.id!!)
            .collection("paquetesEnDistribucion")
            .document(pack?.guia!!).delete()
            .addOnSuccessListener { Log.d(">>>", "EL PAQUETE SE HA ELIMINADO CORRECTAMENTE") }
            .addOnFailureListener { e -> Log.w(">>>", "ERROR ELIMINANDO EL PAQUETE", e)}
    }

    override fun onAddPackListener(){ binding.continueButton.isGone = false }
}