package com.zoe.weshare.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.detail.askgift.AskForGiftViewModel
import com.zoe.weshare.detail.commenting.CommentViewModel
import com.zoe.weshare.detail.event.EventDetailViewModel
import com.zoe.weshare.detail.gift.GiftDetailViewModel
import com.zoe.weshare.message.ChatRoomViewModel
import com.zoe.weshare.message.roomlist.RoomListViewModel
import com.zoe.weshare.posting.event.PostEventViewModel
import com.zoe.weshare.posting.gift.PostGiftViewModel
import com.zoe.weshare.profile.ProfileViewModel

/**
 * Factory for all ViewModels which need [Author].
 */
@Suppress("UNCHECKED_CAST")
class AuthorViewModelFactory(
    private val repository: WeShareRepository,
    private val author: Author?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(PostEventViewModel::class.java)) {
            return PostEventViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(PostGiftViewModel::class.java)) {
            return PostGiftViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(AskForGiftViewModel::class.java)) {
            return AskForGiftViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
            return CommentViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            return ChatRoomViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(RoomListViewModel::class.java)) {
            return RoomListViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(EventDetailViewModel::class.java)) {
            return EventDetailViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(GiftDetailViewModel::class.java)) {
            return GiftDetailViewModel(repository, author) as T
        }

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repository, author) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

}
