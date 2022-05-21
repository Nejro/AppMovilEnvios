package com.under.tptr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.under.tptr.databinding.ActivityLoginBinding
import com.under.tptr.model.DeliveryMan

class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.loginBTN.setOnClickListener {
            val email = binding.loginEmailET.text.toString()
            val password = binding.loginPasswordET.text.toString()
            login(email,password)
        }
    }

    private fun login(email:String, password:String){
        if(email!="" && password!=""){
            Firebase.firestore
                .collection("repartidores")
                .whereEqualTo("correo",email)
                .get().addOnCompleteListener{ task ->
                    if(task.result?.size() != 0){
                        lateinit var currentUser: DeliveryMan
                        for(document in task.result!!) currentUser = document.toObject(DeliveryMan::class.java)
                        if(currentUser.password == password){
                            when(currentUser.estado){
                                currentUser.DELIVERING_STATE -> {
                                    val intent = Intent(this, RouteActivity::class.java).apply { putExtra("currentUser", Gson().toJson(currentUser)) }
                                    startActivity(intent)
                                    finish()
                                }
                                currentUser.NO_DELIVERING_STATE -> {
                                    val intent = Intent(this, StartDistributionActivity::class.java).apply { putExtra("currentUser", Gson().toJson(currentUser)) }
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }else Toast.makeText(this,R.string.incorrect_password,Toast.LENGTH_SHORT).show()
                    }else Toast.makeText(this,R.string.incorrect_email,Toast.LENGTH_SHORT).show()
            }
        }else Toast.makeText(this,R.string.empty_fields,Toast.LENGTH_SHORT).show()
    }
}