package com.zoe.weshare.detail.event

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventDetailBinding
import com.zoe.weshare.detail.CommentsAdapter
import com.zoe.weshare.detail.CommentsViewModel
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat

class EventDetailFragment : Fragment() {

    val author = Author(
        name = "Kelyie Chen",
        uid = "kelly0808",
        image = "https://hips.hearstapps.com/hmg-prod.s3.amazonaws.com/images/hbz-grace-kelly-1950s-gettyimages-517423148-1569860702.jpg"
    )

    private lateinit var binding: FragmentEventDetailBinding
    lateinit var adapter: CommentsAdapter

    val viewModel by viewModels<CommentsViewModel> { getVmFactory() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =  FragmentEventDetailBinding.inflate(inflater, container, false)

        val selectedEvent = EventDetailFragmentArgs.fromBundle(requireArguments()).selectedEvent
        setUpDetailContent(selectedEvent)
        viewModel.getEventComments(selectedEvent.id)


        adapter = CommentsAdapter(viewModel)
        binding.commentsRecyclerView.adapter = adapter

        viewModel.comments.observe(viewLifecycleOwner) {
            viewModel.getUserList(it)
        }

        //want to make sure finish getting userInfo before start recyclerView adapter
        viewModel.onCommentsDisplay.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.submitList(viewModel.comments.value)
            }
        }

        viewModel.newComment.observe(viewLifecycleOwner) {
            viewModel.sendComment(selectedEvent.id, it)
        }



        setUpBtn()
        return binding.root
    }

    private fun setUpBtn() {
        binding.buttonSendComment.setOnClickListener {
            val newComment = binding.editLeaveComment.text.toString()

            if (newComment.isNotEmpty()){
                viewModel._newComment.value = Comment(
                    uid = author.uid,
                    content = newComment
                )
            }else{
                Toast.makeText(requireContext(),"comment is empty",Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setUpDetailContent(selectedEvent: EventPost) {
        binding.apply {
            bindImage(this.images, selectedEvent.image)
            textGiftTitle.text = selectedEvent.title
            textProfileName.text = selectedEvent.author?.name
            bindImage(this.imageProfileAvatar, selectedEvent.author?.image)
            textPostedLocation.text = selectedEvent.location?.locationName
            textCreatedTime.text = selectedEvent.createdTime.toDisplayFormat()
            textSort.text = selectedEvent.sort
            textDescription.text = selectedEvent.description
        }
    }
}