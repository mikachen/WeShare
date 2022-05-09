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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
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
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentSearchLocationBinding
import com.zoe.weshare.ext.checkLocationPermission
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.sendNotificationsToFollowers
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.posting.event.PostEventViewModel
import com.zoe.weshare.posting.gift.PostGiftViewModel
import com.zoe.weshare.util.UserManager.weShareUser

class SearchLocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentSearchLocationBinding

    lateinit var progressBar: ProgressBar
    lateinit var animation: ObjectAnimator

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

        if (isPermissionGranted) {

            binding = FragmentSearchLocationBinding.inflate(inflater, container, false)
            progressBar = binding.progressBar
            progressBar.max = 100 * 100

            initializePlace()

            val newEvent = arguments?.let { SearchLocationFragmentArgs.fromBundle(it).newEvent }
            val newGift = arguments?.let { SearchLocationFragmentArgs.fromBundle(it).newGift }

            if (newEvent != null) {
                val eventViewModel by viewModels<PostEventViewModel> { getVmFactory(weShareUser) }
                eventViewModel._event.value = newEvent

                setUpAutoCompleteSearchPlace(giftVm = null, eventVm = eventViewModel)
                setupInputPreview(gift = null, event = newEvent)

                binding.buttonSubmit.setOnClickListener {
                    if (eventViewModel.locationChoice != null) {
                        binding.layoutProgressLoading.visibility = View.VISIBLE
                        eventViewModel.uploadImage()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_event_location_isEmpty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                eventViewModel.postingProgress.observe(viewLifecycleOwner) {

                    animation = ObjectAnimator.ofInt(
                        progressBar,
                        "progress",
                        progressBar.progress,
                        it * 100
                    )
                    animation.duration = 500
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
                    findNavController().navigate(
                        SearchLocationFragmentDirections.actionSearchLocationFragmentToHomeFragment())
                }

                eventViewModel.onPostEvent.observe(viewLifecycleOwner) {
                    eventViewModel.onNewRoomPrepare(it)
                }
            } else {
                val giftViewModel by viewModels<PostGiftViewModel> { getVmFactory(weShareUser) }
                giftViewModel._gift.value = newGift

                setUpAutoCompleteSearchPlace(giftVm = giftViewModel, eventVm = null)
                setupInputPreview(gift = newGift, event = null)

                giftViewModel.saveLogComplete.observe(viewLifecycleOwner) {
                    sendNotificationsToFollowers(it)
                    findNavController().navigate(
                        SearchLocationFragmentDirections.actionSearchLocationFragmentToHomeFragment())
                }

                binding.buttonSubmit.setOnClickListener {
                    if (giftViewModel.locationChoice != null) {
                        binding.layoutProgressLoading.visibility = View.VISIBLE
                        giftViewModel.uploadImage()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_gift_location_isEmpty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                giftViewModel.postingProgress.observe(viewLifecycleOwner) {

                    animation = ObjectAnimator.ofInt(
                        progressBar,
                        "progress",
                        progressBar.progress,
                        it * 100
                    )
                    animation.duration = 500
                    animation.interpolator = DecelerateInterpolator()
                    animation.start()
                }

                giftViewModel.onPostGift.observe(viewLifecycleOwner) {
                    giftViewModel.newGiftPost(it)
                }
            }

            // 當user同意location權限後，檢查user是否有啟用GooglePlayService
            if (isPermissionGranted) {
                if (checkGooglePlayService()) {
                    binding.mapView.onCreate(savedInstanceState)
                    binding.mapView.getMapAsync(this)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "GooglePlayService Not Available",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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
        eventVm: PostEventViewModel?,
        giftVm: PostGiftViewModel?,
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
                        eventVm?.updateLocation(locationName = name, point = result)
                        giftVm?.updateLocation(locationName = name, point = result)
                    }
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("TAG", "An error occurred: $status")
            }
        })
    }

    private fun setupInputPreview(gift: GiftPost?, event: EventPost?) {
        if (gift != null) {
            binding.apply {
                textTitle.text = gift.title
                textSort.text = gift.sort
                textCondition.text = gift.condition
                textDescription.text = gift.description
                imagePreview.setImageURI(Uri.parse(gift.image))
                titleEventTime.visibility = View.GONE
                textEventTime.visibility = View.GONE
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
            }
        }
    }

    private fun checkGooglePlayService(): Boolean {

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())

        if (result == ConnectionResult.SUCCESS) {
            return true
        } else if (googleApiAvailability.isUserResolvableError(result)) {

            val dialog = googleApiAvailability.getErrorDialog(
                requireActivity(),
                result,
                201
            ) {
                Toast.makeText(requireContext(), "User Cancel Dialog", Toast.LENGTH_LONG)
                    .show()
            }
            dialog?.show()
        }
        return false
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
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
