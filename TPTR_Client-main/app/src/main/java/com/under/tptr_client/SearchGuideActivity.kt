package com.under.tptr_client

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.under.tptr_client.databinding.ActivitySearchGuideBinding
import com.under.tptr_client.model.Business
import com.under.tptr_client.model.PackageClient

class SearchGuideActivity : AppCompatActivity() {
    private val binding: ActivitySearchGuideBinding by lazy {ActivitySearchGuideBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.searchBTN.setOnClickListener {
            val guide = binding.searchET.text.toString()
            val nameBusiness = binding.searchBussNameET.text.toString()
            if(guide!="" && nameBusiness!=""){
                Firebase.firestore.collection("empresas").whereEqualTo("nombre",nameBusiness).get().addOnCompleteListener { task ->
                    if(task.result?.size() != 0){
                        var busi:Business? = null
                        for(document in task.result!!)busi = document.toObject(Business::class.java)
                        Firebase.firestore
                            .collection("empresas")
                            .document(busi?.NIT!!).collection("paquetes")
                            .whereEqualTo("guia",guide)
                            .get().addOnCompleteListener {
                                if(it.result?.size() != 0){
                                    var pack: PackageClient? = null
                                    for(document in it.result!!) {
                                        pack = document.toObject(PackageClient::class.java)
                                    }
                                    val intent = Intent(this, MapsActivity::class.java).apply {
                                        putExtra("currentPack", Gson().toJson(pack))
                                    }
                                    startActivity(intent)
                                }else Toast.makeText(this, R.string.invalid_guide, Toast.LENGTH_SHORT).show()
                            }
                    }else Toast.makeText(this, R.string.invalid_business_name, Toast.LENGTH_SHORT).show()
                }
            }else Toast.makeText(this,R.string.empty_fields,Toast.LENGTH_SHORT).show()
        }
    }
}