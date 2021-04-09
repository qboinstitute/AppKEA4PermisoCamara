package com.qbo.appkea4permisocamara

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.qbo.appkea4permisocamara.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btntomarfoto.setOnClickListener {
            if(permisoEscrituraAlmacenamiento()){
                //Llamar a la cámara
            }else{
                solicitarPermiso()
            }
        }
    }
    fun permisoEscrituraAlmacenamiento(): Boolean{
        val resultado = ContextCompat.checkSelfPermission(
            applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        var exito = false
        if(resultado == PackageManager.PERMISSION_GRANTED) exito = true
        return exito
    }
    fun solicitarPermiso(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }
    //Para recibir la respuesta del permiso solicitado por el usuario
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 0){
            if(grantResults.isNotEmpty() && grantResults[0]
                == PackageManager.PERMISSION_GRANTED){
                //Llamar a la cámara
            }else{
                enviarMensaje("Permiso denegado, la aplicación no puede tomar fotos")
            }
        }
    }
    fun enviarMensaje(mensaje : String){
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_LONG).show()
    }

}