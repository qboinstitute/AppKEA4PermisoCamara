package com.qbo.appkea4permisocamara

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.qbo.appkea4permisocamara.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding
    private var rutaFotoActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btntomarfoto.setOnClickListener {
            if(permisoEscrituraAlmacenamiento()){
                //Llamar a la cámara
                llamarAppCamara()
            }else{
                solicitarPermiso()
            }
        }
    }

    @Throws(IOException::class)
    fun crearArchivoTemporal() : File? {
        val fechaHora = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val nombreImagen = "JPEG_${fechaHora}_"
        val directorio: File = this?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val imagen: File = File.createTempFile(nombreImagen,".jpg", directorio)
        rutaFotoActual = imagen.absolutePath
        return imagen
    }

    @Throws(IOException::class)
    fun llamarAppCamara(){
        val fotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(fotoIntent.resolveActivity(packageManager) != null ){
            val archivoFoto = crearArchivoTemporal()
            if(archivoFoto != null){
                val urlFoto: Uri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.qbo.appkea4permisocamara.provider",
                    archivoFoto
                )
                fotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    urlFoto)
                startActivityForResult(fotoIntent, 1)
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
                llamarAppCamara()
            }else{
                enviarMensaje("Permiso denegado, la aplicación no puede tomar fotos")
            }
        }
    }
    fun enviarMensaje(mensaje : String){
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_LONG).show()
    }

}