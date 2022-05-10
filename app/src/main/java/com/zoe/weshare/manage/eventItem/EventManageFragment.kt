package com.zoe.weshare.manage.eventItem

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.zxing.WriterException
import com.zoe.weshare.MainActivity
import com.zoe.weshare.databinding.FragmentEventManageBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager

class EventManageFragment : Fragment() {

    lateinit var binding: FragmentEventManageBinding
    lateinit var adapter: EventManageAdapter
    lateinit var manager: LinearLayoutManager

    var currentTabPosition = 0

    private val viewModel by viewModels<EventManageViewModel> { getVmFactory(UserManager.weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventManageBinding.inflate(inflater, container, false)

        viewModel.allEventsResult.observe(viewLifecycleOwner) {
            adapter.modifyList(it, currentTabPosition)
        }

        viewModel.qrcode.observe(viewLifecycleOwner){
            it?.let {
                generateQrcode(it)
                viewModel.generateQrcodeComplete()
            }
        }



        setupView()
        return binding.root
    }


    fun generateQrcode(docId: String) {

        val display = (activity as MainActivity).windowManager.defaultDisplay

        val point = Point()
        display.getSize(point)

        val width: Int = point.x
        val height: Int = point.y

        // generating dimension from width and height.
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4

        val qrgEncoder = QRGEncoder(docId, null, QRGContents.Type.TEXT, dimen)

        try {
            val bitmap = qrgEncoder.encodeAsBitmap()
            binding.qrcodeHolder.setImageBitmap(bitmap)

            binding.qrcodeHolder.visibility = View.VISIBLE
            binding.baseLayout.visibility = View.VISIBLE

            binding.baseLayout.setOnClickListener {
                it.visibility = View.GONE
                binding.qrcodeHolder.visibility = View.GONE
            }

        } catch (e: WriterException) {
            // this method is called for
            // exception handling.
            Logger.e("error: $e")
        }
    }



//    private fun onAlertAbandon(event: EventPost) {
//        val builder = AlertDialog.Builder(requireActivity())
//
//        builder.apply {
//            setTitle(getString(R.string.abandoned_title, gift.title))
//            setMessage(getString(R.string.abandoned_message))
//            setPositiveButton(getString(R.string.abandoned_yes)) { dialog, id ->
//                viewModel.abandonGift(gift)
//
//                adapter.viewBinderHelper.closeLayout(gift.id)
//                dialog.cancel()
//            }
//
//            setNegativeButton(getString(R.string.abandoned_no)) { dialog, id ->
//                dialog.cancel()
//            }
//        }
//
//        val alter: AlertDialog = builder.create()
//        alter.show()
//    }

    fun setupView() {
        adapter = EventManageAdapter(
            viewModel, EventManageAdapter.OnClickListener {

            }
        )

        manager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL, false
        )

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = manager

        binding.filterTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTabPosition = tab.position

                adapter.filter(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}