package com.under.tptr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.under.tptr.databinding.ActivityClientInfoBinding
import com.under.tptr.model.Client
import com.under.tptr.model.DeliveryMan
import com.under.tptr.model.PackageClient

class ClientInfoActivity : AppCompatActivity() {

    val binding: ActivityClientInfoBinding by lazy { ActivityClientInfoBinding.inflate(layoutInflater) }
    var client: Client? = null
    var pack: PackageClient? = null
    var currentUser: DeliveryMan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val clientJson = (intent.extras?.getString("currentClient")).toString()
        client = Gson().fromJson(clientJson, Client::class.java)
        val packageClientJson = (intent.extras?.getString("currentPack")).toString()
        pack = Gson().fromJson(packageClientJson, PackageClient::class.java)
        val currentUserJson = (intent.extras?.getString("currentUser")).toString()
        currentUser = Gson().fromJson(currentUserJson, DeliveryMan::class.java)

        binding.nameInfoTXT.text = client?.name
        binding.idInfoTXT.text = client?.id
        binding.emailInfoTXT.text = client?.email
        binding.telInfoTXT.text = client?.tel
        binding.addInfoTXT.text = pack?.direccion
        binding.numberGuideInfoTXT.text = pack?.guia

        binding.backButton.setOnClickListener { finish() }
        binding.deliverButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java).apply {
                putExtra("currentPack", Gson().toJson(pack))
                putExtra("currentUser", Gson().toJson(currentUser))
            }
            startActivity(intent)
            finish()
        }
    }
}