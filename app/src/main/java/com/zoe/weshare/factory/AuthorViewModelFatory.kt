package com.zoe.weshare.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.posting.event.PostEventViewModel
import com.zoe.weshare.posting.gift.PostGiftViewModel

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

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
