package com.under.tptr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        ),1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var allGranted = true
        for(result in grantResults){
            if(result == PackageManager.PERMISSION_DENIED){
                allGranted = false
                break
            }
        }
        if(allGranted){
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this,"Necesita conceder los permisos para poder continuar", Toast.LENGTH_SHORT).show()
        }
    }
}