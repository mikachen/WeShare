package com.zoe.weshare.notification

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.databinding.ItemNotificationsBinding
import com.zoe.weshare.ext.getTimeAgoString
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.Util.getColor
import com.zoe.weshare.util.Util.getString

class NotificationAdapter(val viewModel: NotificationViewModel) :
    ListAdapter<OperationLog, NotificationAdapter.NotificationViewHolder>(DiffCallback) {

    class NotificationViewHolder(var binding: ItemNotificationsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(log: OperationLog) {
            binding.apply {
                textMessage.text = Html.fromHtml(log.logMsg)
                textCreatedTime.text = log.createdTime.getTimeAgoString()

                if (log.read) {
                    notReadView.visibility = View.GONE
                }

                when (log.logType) {
                    LogType.REQUEST_GIFT.value -> {
                        layoutTag.visibility = View.VISIBLE
                        textRequestType.text = getString(R.string.notification_request_gift)
                        textRequestType.setBackgroundColor(getColor(R.color.notification_gift_tag))
                        textRequestType.setTextColor(getColor(R.color.notification_gift_tag_string))
                    }

                    LogType.VOLUNTEER_EVENT.value -> {
                        layoutTag.visibility = View.VISIBLE
                        textRequestType.text = getString(R.string.notification_attend_volunteer)
                        textRequestType.setTextColor(getColor(R.color.notification_volunteer_tag_string))
                        textRequestType.setBackgroundColor(getColor(R.color.notification_volunteer_tag))
                    }
                    else -> {
                        layoutTag.visibility = View.GONE
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): NotificationViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return NotificationViewHolder(
                    ItemNotificationsBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)
        holder.itemView.setOnClickListener {
            holder.binding.notReadView.visibility = View.GONE
            viewModel.userOnClickAndRead(data)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<OperationLog>() {
        override fun areItemsTheSame(oldItem: OperationLog, newItem: OperationLog): Boolean {
            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: OperationLog, newItem: OperationLog): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
