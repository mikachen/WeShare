package com.zoe.weshare.posting

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.zoe.weshare.MainActivity
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentSearchLocationBinding
import com.zoe.weshare.ext.*
import com.zoe.weshare.posting.event.PostEventViewModel
import com.zoe.weshare.posting.gift.PostGiftViewModel
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager.weShareUser

class SearchLocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentSearchLocationBinding
    private lateinit var map: GoogleMap

    private val navArg: SearchLocationFragmentArgs by navArgs()
    private var giftArg: GiftPost? = null
    private var eventArg: EventPost? = null

    private var isPermissionGranted: Boolean = false

    private val eventViewModel by viewModels<PostEventViewModel> { getVmFactory(weShareUser) }
    private val giftViewModel by viewModels<PostGiftViewModel> { getVmFactory(weShareUser) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPermissionGranted = checkLocationPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentSearchLocationBinding.inflate(inflater, container, false)

        giftArg = navArg.newGift
        eventArg = navArg.newEvent

        if (isPermissionGranted) {
            when (true) {
                userIsPostingEvent() -> {

                    eventViewModel.fetchArgument(eventArg!!)

                    eventViewModel.postingProgress.observe(viewLifecycleOwner) {
                        it?.let {
                            (activity as MainActivity).progressBarLoading(it)
                        }
                    }

                    eventViewModel.onPostEvent.observe(viewLifecycleOwner) {
                        it?.let {
                            eventViewModel.onNewRoomPrepare(it)
                        }
                    }

                    eventViewModel.saveLogComplete.observe(viewLifecycleOwner) {
                        it?.let {
                            (activity as MainActivity).hideProgressBar()

                            sendNotificationsToFollowers(it)

                            findNavController().navigate(SearchLocationFragmentDirections
                                .actionPostEventCompleteToDetail(EventPost(id = it.postDocId))
                            )
                            eventViewModel.postEventComplete()
                        }
                    }
                    setupEventPreview(eventArg!!)
                }

                userIsPostingGift() -> {

                    giftViewModel.fetchArgument(giftArg!!)

                    giftViewModel.postingProgress.observe(viewLifecycleOwner) {
                        it?.let {
                            (activity as MainActivity).progressBarLoading(it)
                        }
                    }

                    giftViewModel.onPostGift.observe(viewLifecycleOwner) {
                        it?.let {
                            giftViewModel.newGiftPost(it)
                        }
                    }

                    giftViewModel.saveLogComplete.observe(viewLifecycleOwner) {
                        it?.let {
                            (activity as MainActivity).hideProgressBar()

                            sendNotificationsToFollowers(it)

                            findNavController().navigate(SearchLocationFragmentDirections
                                .actionPostGiftCompleteToDetail(GiftPost(id = it.postDocId))
                            )

                            giftViewModel.postGiftComplete()
                        }
                    }
                    setupGiftPreview(giftArg!!)
                }
                else -> {
                    findNavController().navigateUp()
                }
            }


            createMapView(savedInstanceState)
            initializePlace()

            setUpAutoCompleteSearchPlace()
        }else{
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun createMapView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    private fun initializePlace() {
        val info = (activity as MainActivity).applicationContext.packageManager
            .getApplicationInfo(
                (activity as MainActivity).packageName,
                PackageManager.GET_META_DATA
            )

        val key = info.metaData[resources.getString(R.string.map_api_key_name)].toString()

        Places.initialize(requireContext(), key)
    }

    private fun setUpAutoCompleteSearchPlace() {
        val autocompleteFragment = childFragmentManager
            .findFragmentById(R.id.autocomplete_support_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.LAT_LNG,
            )
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                val name = place.name
                val result = place.latLng

                if (name == null || result == null) {
                    activity.showToast(getString(R.string.toast_cant_find_this_place))
                } else {
                    binding.textPostedLocation.text = name

                    map.addMarker(MarkerOptions().position(result).title(name))
                        ?.setIcon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                            )
                        )

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(result, 16F))

                    if (userIsPostingEvent()) {
                        eventViewModel.updateLocation(locationName = name, point = result)
                    }

                    if (userIsPostingGift()) {
                        giftViewModel.updateLocation(locationName = name, point = result)
                    }
                }
            }


            override fun onError(status: Status) {
                Logger.i("autocompleteFragment: $status")
            }
        })
    }

    private fun setupGiftPreview(gift: GiftPost) {
        binding.apply {
            textTitle.text = gift.title
            textSort.text = gift.sort
            textCondition.text = gift.condition
            textDescription.text = gift.description
            imagePreview.setImageURI(Uri.parse(gift.image))
            titleEventTime.visibility = View.GONE
            textEventTime.visibility = View.GONE

            buttonSubmit.setOnClickListener {
                if (giftViewModel.locationChoice != null) {

                    (activity as MainActivity).showProgressBar()

                    giftViewModel.uploadImage()
                } else {
                    activity.showToast(getString(R.string.error_gift_location_isEmpty))
                }
            }
        }
    }

    private fun setupEventPreview(event: EventPost) {
        binding.apply {
            textTitle.text = event.title
            textSort.text = event.sort
            textEventTime.text =
                WeShareApplication.instance.getString(
                    R.string.preview_event_time,
                    event.startTime.toDisplayFormat(),
                    event.endTime.toDisplayFormat()
                )
            textDescription.text = event.description
            imagePreview.setImageURI(Uri.parse(event.image))
            titleTitle.text = getString(R.string.preview_event_title_title)
            titleSort.text = getString(R.string.preview_event_sort_title)
            titleCondition.text = getString(R.string.preview_event_volunteer_title)
            textCondition.text = event.volunteerNeeds.toString()
            titleDescription.text = getString(R.string.preview_event_description_title)

            buttonSubmit.setOnClickListener {
                if (eventViewModel.locationChoice != null) {
                    (activity as MainActivity).showProgressBar()

                    eventViewModel.uploadImage()
                } else {
                    activity.showToast(getString(R.string.error_event_location_isEmpty))
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapSettings()
    }

    private fun setupMapSettings() {

        val defaultTaiwan = LatLng(23.897879, 121.063772)

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultTaiwan, 8F))
    }

    override fun onStart() {
        super.onStart()
        if (isPermissionGranted) {
            binding.mapView.onStart()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPermissionGranted) {
            binding.mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPermissionGranted) {
            binding.mapView.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isPermissionGranted) {
            binding.mapView.onStop()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (isPermissionGranted) {
            binding.mapView.onSaveInstanceState(outState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isPermissionGranted) {
            binding.mapView.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()

        if (isPermissionGranted) {
            binding.mapView.onLowMemory()
        }
    }


    private fun userIsPostingEvent(): Boolean {
        return eventArg != null
    }

    private fun userIsPostingGift(): Boolean {
        return giftArg != null
    }

}
