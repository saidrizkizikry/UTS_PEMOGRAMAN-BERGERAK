package com.example.memeapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memeapp.MainActivity
import com.example.memeapp.databinding.ItemMemeBinding

class MemeAdapter: ListAdapter<MainActivity.MemeWithUser, MemeAdapter.VH>(DIFF_CALLBACK) {

    inner class VH(private val b: ItemMemeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(meme: MainActivity.MemeWithUser) {
            b.tvDescription.text = meme.meme.description
            b.tvUsername.text = meme.userName
            Glide.with(b.ivMeme.context)
                .load(meme.meme.imageUrl)
                .into(b.ivMeme)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMemeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MainActivity.MemeWithUser>() {
            override fun areItemsTheSame(old: MainActivity.MemeWithUser, new: MainActivity.MemeWithUser) =
                old.meme.id == new.meme.id

            override fun areContentsTheSame(old: MainActivity.MemeWithUser, new: MainActivity.MemeWithUser) =
                old == new
        }
    }
}