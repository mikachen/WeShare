package com.zoe.weshare.manage.distribution

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.ItemGiftDistrubutionListBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime
import com.zoe.weshare.util.Util

class DistributeAdapter(val viewModel: DistributeViewModel) :
    ListAdapter<Comment, DistributeAdapter.DistributionViewHolder>(DiffCall()) {

    val userInfo = Util.readInstanceProperty<UserInfo>(viewModel, "userInfo")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DistributionViewHolder {
        return DistributionViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderGift: DistributionViewHolder, position: Int) {
        val comment = getItem(position)
        holderGift.bind(comment, viewModel)
        Log.d("onBindViewHolder","onBindViewHolder")
    }

    class DistributionViewHolder(val binding: ItemGiftDistrubutionListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: DistributeViewModel) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.toDisplaySentTime()

            // displaying user's image and name
            if (viewModel.onProfileSearchComplete.value == 0) {
                if (viewModel.profileList.isNotEmpty()) {
                    val sender = viewModel.profileList.singleOrNull { it.uid == comment.uid }
                    sender?.let {
                        bindImage(binding.imageProfileAvatar, sender.image)
                        binding.textProfileName.text = sender.name
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): DistributionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGiftDistrubutionListBinding.inflate(layoutInflater, parent, false)

                return DistributionViewHolder(binding)
            }
        }
    }
}

class DiffCall : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(
        oldItem: Comment,
        newItem: Comment,
    ): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(
        oldItem: Comment,
        newItem: Comment,
    ): Boolean {
        return oldItem == newItem
    }
}