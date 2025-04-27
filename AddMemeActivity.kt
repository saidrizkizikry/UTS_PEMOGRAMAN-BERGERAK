package com.example.memeapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.memeapp.databinding.ActivityAddMemeBinding
import com.example.memeapp.models.MemeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.okhttp.OkHttp

import kotlinx.coroutines.launch

class AddMemeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMemeBinding
    private var imageUri: Uri? = null

    private val supabase by lazy {
        createSupabaseClient(
            supabaseUrl    = API_URL,
            supabaseKey    = API_KEY,
        ) {
            install(Storage)
            httpEngine = OkHttp.create{}
        }
    }

    private val dbRef by lazy {
        FirebaseDatabase.getInstance().getReference("memes")
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        binding.ivPreview.setImageURI(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddMemeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupButton()
    }

    private fun setupButton() {
        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnUpload.setOnClickListener { uploadMeme() }
    }
 
    private fun uploadMeme() {
        val desc = binding.etDescription.text.toString().trim()
        val uri  = imageUri ?: run {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show()
            return
        }

        val bytes = contentResolver.openInputStream(uri)!!.use { it.readBytes() }
        val memeId = dbRef.push().key!!

        lifecycleScope.launch {
            try {
                supabase.storage
                    .from("memes")
                    .upload("public/$memeId.jpg", bytes)

                val url = supabase.storage
                    .from("memes")
                    .publicUrl("public/$memeId.jpg")

                val meme = MemeModel(
                    id          = memeId,
                    userId      = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    description = desc,
                    imageUrl    = url,
                    timestamp   = System.currentTimeMillis()
                )
                dbRef.child(memeId).setValue(meme)
                    .addOnSuccessListener {
                        Toast.makeText(this@AddMemeActivity, "Uploaded!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@AddMemeActivity, "DB error: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            } catch (e: Exception) {
                Log.e("Upload Error", "Upload Error : ${e.message}")
                Toast.makeText(this@AddMemeActivity, "Upload error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val API_URL = "https://yumnhhqhevlexdoxuaqo.supabase.co"
        const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl1bW5oaHFoZXZsZXhkb3h1YXFvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU1NjM1NTMsImV4cCI6MjA2MTEzOTU1M30.rTefDzhWUkaQBEhK2V8gT8RnVnd-sNmpz0tjpb7tkpw"
    }
}