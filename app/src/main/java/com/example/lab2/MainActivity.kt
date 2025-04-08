package com.example.lab2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: Button
    private lateinit var sendEmailButton: Button
    private var currentPhotoPath: String = ""
    private var photoUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        sendEmailButton = findViewById(R.id.sendEmailButton)

        takePhotoButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        sendEmailButton.setOnClickListener {
            if (photoUri != null) {
                sendEmail()
            } else {
                Toast.makeText(this, "Спочатку зробіть селфі", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Toast.makeText(this, "Помилка створення файлу", Toast.LENGTH_SHORT).show()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.lab2.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            photoUri?.let {
                imageView.setImageURI(it)
            }
        }
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
            putExtra(Intent.EXTRA_SUBJECT, "ANDROID Жмак Микита АІ-221 Лабораторна 2")
            putExtra(Intent.EXTRA_TEXT, "Посилання на репозиторій: https://github.com/YoshkinKit/Android-Lab2")
            photoUri?.let {
                putExtra(Intent.EXTRA_STREAM, it)
            }
        }

        try {
            startActivity(Intent.createChooser(intent, "Надіслати email..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "Немає додатків для відправки email", Toast.LENGTH_SHORT).show()
        }
    }
}