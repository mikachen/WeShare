package com.zoe.weshare.search.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.ItemEventsAllGridBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplayDateFormat
import java.util.*

class EventsBrowseAdapter(private val onClickListener: EventsAllOnClickListener) :
    ListAdapter<EventPost, EventsBrowseAdapter.AllEventsViewHolder>(DiffCallback) {

    private var unfilteredList = listOf<EventPost>()

    class AllEventsViewHolder(var binding: ItemEventsAllGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: EventPost) {
            binding.apply {
                textEventLocation.text = event.location?.locationName ?: ""

                textEventTime.text = WeShareApplication.instance.getString(
                    R.string.preview_event_time,
                    event.startTime.toDisplayDateFormat(),
                    event.endTime.toDisplayDateFormat()
                )

                textEventTitle.text = event.title
                bindImage(imageEventAll, event.image)
            }
        }

        companion object {
            fun from(parent: ViewGroup): AllEventsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return AllEventsViewHolder(
                    ItemEventsAllGridBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: AllEventsViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(data)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllEventsViewHolder {
        return AllEventsViewHolder.from(parent)
    }

    class EventsAllOnClickListener(val doNothing: (event: EventPost) -> Unit) {
        fun onClick(event: EventPost) = doNothing(event)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<EventPost>() {
        override fun areItemsTheSame(oldItem: EventPost, newItem: EventPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EventPost, newItem: EventPost): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun modifyList(list: List<EventPost>) {
        unfilteredList = list
        submitList(list)
    }

    fun filter(query: CharSequence?, viewModel: EventsBrowseViewModel) {
        val list = mutableListOf<EventPost>()

        // perform the data filtering
        if (!query.isNullOrEmpty()) {
            list.addAll(
                unfilteredList.filter {
                    it.title.toLowerCase(Locale.getDefault()).contains(
                        query.toString().toLowerCase(
                            Locale.getDefault()
                        )
                    ) ||
                        it.description.toLowerCase(Locale.getDefault())
                            .contains(
                                query.toString().toLowerCase(
                                    Locale.getDefault()
                                )
                            )
                }
            )

            viewModel.onSearchEmpty.value = list.isEmpty()
        } else {
            list.addAll(unfilteredList)
            viewModel.onSearchEmpty.value = false
        }

        submitList(list)
    }
}
