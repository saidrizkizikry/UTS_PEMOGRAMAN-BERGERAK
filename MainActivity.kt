package com.example.memeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memeapp.adapter.MemeAdapter
import com.example.memeapp.databinding.ActivityMainBinding
import com.example.memeapp.models.MemeModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val memesRef = FirebaseDatabase.getInstance().getReference("memes")
    private val userMap = mutableMapOf<String, String>()
    private val adapter = MemeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupButton()
        setupAdapter()
        setupMemeWithUser()
    }

    private fun setupButton() {
        binding.addData.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddMemeActivity::class.java))
        }
    }

    private fun setupMemeWithUser() {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { userSnap ->
                    val uid = userSnap.key ?: return@forEach
                    val name = userSnap.child("username").getValue(String::class.java) ?: "Unknown"
                    userMap[uid] = name
                }

                listenForMemes()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listenForMemes() {
        memesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { snap ->
                    snap.getValue(MemeModel::class.java)
                }
                val enriched = list.map { meme ->
                    val uname = userMap[meme.userId] ?: "Unknown"
                    MemeWithUser(meme, uname)
                }
                adapter.submitList(enriched)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.recyclerView.adapter = adapter
    }

    data class MemeWithUser(val meme: MemeModel, val userName: String)
}