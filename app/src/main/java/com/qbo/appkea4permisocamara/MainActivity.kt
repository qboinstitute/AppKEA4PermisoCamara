package com.qbo.appkea4permisocamara

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
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
import kotlin.math.min

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
        binding.btncompartir.setOnClickListener {
            if (rutaFotoActual != ""){
                val contenidoUrl = FileProvider.getUriForFile(
                    applicationContext,
                    "com.qbo.appkea4permisocamara.provider",
                    File(rutaFotoActual)
                )
                val enviarIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contenidoUrl)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    type = "image/jpeg"
                }
                val eleccionIntent =
                    Intent.createChooser(enviarIntent, "Compartir Imagen")
                if(enviarIntent.resolveActivity(packageManager) != null){
                    startActivity(eleccionIntent)
                }
            }
        }
    }

    fun mostrarFoto(){
        val exitInterface = ExifInterface(rutaFotoActual)
        val orientacion: Int = exitInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        if(orientacion == ExifInterface.ORIENTATION_ROTATE_90){
            binding.ivfoto.rotation = 90.0F
        }else{
            binding.ivfoto.rotation = 0.0F
        }
        val anchoImageView = binding.ivfoto.width
        val altoImageView = binding.ivfoto.height
        val bmOpciones = BitmapFactory.Options()
        bmOpciones.inJustDecodeBounds = true
        BitmapFactory.decodeFile(rutaFotoActual, bmOpciones)
        val anchoFoto = bmOpciones.outWidth
        val altoFoto = bmOpciones.outHeight
        val escalaFoto = min(anchoFoto / anchoImageView, altoFoto / altoImageView)
        bmOpciones.inSampleSize = escalaFoto
        bmOpciones.inJustDecodeBounds = false
        val bitMapFoto = BitmapFactory.decodeFile(rutaFotoActual, bmOpciones)
        binding.ivfoto.setImageBitmap(bitMapFoto)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                grabarFotoGaleria()
                mostrarFoto()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    fun grabarFotoGaleria(){
        val archivoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val nuevoArchivo = File(rutaFotoActual)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val contenidoUrl = FileProvider.getUriForFile(
                applicationContext,
                "com.qbo.appkea4permisocamara.provider",
                nuevoArchivo
            )
            archivoIntent.data = contenidoUrl
        }else{
            val contenidoUrl = Uri.fromFile(nuevoArchivo)
            archivoIntent.data = contenidoUrl
        }
        this.sendBroadcast(archivoIntent)
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