package com.zoe.weshare.posting

import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
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
import com.zoe.weshare.util.UserManager.weShareUser

class SearchLocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentSearchLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var progressBar: ProgressBar
    private lateinit var animation: ObjectAnimator

    private val navArg: SearchLocationFragmentArgs by navArgs()

    private val eventViewModel by viewModels<PostEventViewModel> { getVmFactory(weShareUser) }
    private val giftViewModel by viewModels<PostGiftViewModel> { getVmFactory(weShareUser) }

    private var isPermissionGranted: Boolean = false

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

        val newEvent = navArg.newEvent
        val newGift = navArg.newGift

        if (isPermissionGranted) {

            if (newEvent != null) {
                eventViewModel._event.value = newEvent

                eventViewModel.postingProgress.observe(viewLifecycleOwner) {

                    animation = ObjectAnimator.ofInt(
                        progressBar,
                        "progress",
                        progressBar.progress,
                        it * 100
                    )
                    animation.duration = 300
                    animation.interpolator = DecelerateInterpolator()
                    animation.start()
                }

                eventViewModel.roomCreateComplete.observe(viewLifecycleOwner) {
                    if (it.isNotEmpty()) {
                        eventViewModel.onNewEventPost(it)
                    }
                }

                eventViewModel.saveLogComplete.observe(viewLifecycleOwner) {
                    sendNotificationsToFollowers(it)

                    (activity as MainActivity).binding.layoutMainProgressBar.visibility =
                        View.INVISIBLE
                    (activity as MainActivity).binding.imageUploadingHint.visibility =
                        View.INVISIBLE
                    (activity as MainActivity).showNavigationBar()

                    findNavController().navigate(
                        SearchLocationFragmentDirections
                            .actionSearchLocationFragmentToEventDetailFragment(EventPost(id = it.postDocId))
                    )
                }

                eventViewModel.onPostEvent.observe(viewLifecycleOwner) {
                    eventViewModel.onNewRoomPrepare(it)
                }
            } else {
                giftViewModel._gift.value = newGift

                giftViewModel.saveLogComplete.observe(viewLifecycleOwner) {
                    sendNotificationsToFollowers(it)

                    (activity as MainActivity).binding.layoutMainProgressBar.visibility =
                        View.INVISIBLE
                    (activity as MainActivity).binding.imageUploadingHint.visibility =
                        View.INVISIBLE
                    (activity as MainActivity).showNavigationBar()

                    findNavController().navigate(
                        SearchLocationFragmentDirections
                            .actionSearchLocationFragmentToGiftDetailFragment(GiftPost(id = it.postDocId))
                    )
                }

                giftViewModel.postingProgress.observe(viewLifecycleOwner) {

                    animation = ObjectAnimator.ofInt(
                        progressBar,
                        "progress",
                        progressBar.progress,
                        it * 100
                    )
                    animation.duration = 300
                    animation.interpolator = DecelerateInterpolator()
                    animation.start()
                }

                giftViewModel.onPostGift.observe(viewLifecycleOwner) {
                    giftViewModel.newGiftPost(it)
                }
            }

            binding.mapView.onCreate(savedInstanceState)
            binding.mapView.getMapAsync(this)

            initializePlace()
            setUpAutoCompleteSearchPlace(gift = newGift, event = newEvent)
            setupViewAndBtn(gift = newGift, event = newEvent)
        }

        return binding.root
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

    private fun setUpAutoCompleteSearchPlace(
        event: EventPost?,
        gift: GiftPost?,
    ) {
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

                when (null) {
                    name -> Toast.makeText(requireContext(), "查無此地名", Toast.LENGTH_SHORT).show()
                    result -> Toast.makeText(requireContext(), "查無此地", Toast.LENGTH_SHORT).show()
                    else -> {
                        binding.textPostedLocation.text = name
                        map.addMarker(MarkerOptions().position(result).title("Search Point"))
                            ?.setIcon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED
                                )
                            )

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(result, 16F))

                        if (event != null) {
                            eventViewModel.updateLocation(locationName = name, point = result)
                        } else {
                            giftViewModel.updateLocation(locationName = name, point = result)
                        }
                    }
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("TAG", "An error occurred: $status")
            }
        })
    }

    private fun setupViewAndBtn(gift: GiftPost?, event: EventPost?) {
        progressBar = (activity as MainActivity).binding.progressBar
        progressBar.max = 100 * 100

        if (gift != null) {
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
                        (activity as MainActivity).binding.layoutMainProgressBar.visibility =
                            View.VISIBLE
                        (activity as MainActivity).binding.imageUploadingHint.visibility =
                            View.VISIBLE

                        (activity as MainActivity).hideNavigationBar()

                        giftViewModel.uploadImage()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_gift_location_isEmpty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        if (event != null) {
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
                        (activity as MainActivity).binding.layoutMainProgressBar.visibility =
                            View.VISIBLE
                        (activity as MainActivity).binding.imageUploadingHint.visibility =
                            View.VISIBLE

                        (activity as MainActivity).hideNavigationBar()

                        eventViewModel.uploadImage()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_event_location_isEmpty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
}
