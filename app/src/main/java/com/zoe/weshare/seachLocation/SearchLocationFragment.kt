package com.zoe.weshare.seachLocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zoe.weshare.MainActivity
import com.zoe.weshare.R
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentSearchLocationBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.posting.event.PostEventViewModel
import com.zoe.weshare.posting.gift.PostGiftViewModel

class SearchLocationFragment : Fragment(), OnMapReadyCallback {
    val author = Author(
        name = "Zoe Lo",
        uid = "zoe1018",
        image = "https://www.computerhope.com/jargon/a/android.png"
    )

    private lateinit var searchView: SearchView
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentSearchLocationBinding

    var isPermissionGranted: Boolean = false

    private val defaultTaiwan = LatLng(23.897879, 121.063772)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        initializePlace()

        val newEvent = arguments?.let { SearchLocationFragmentArgs.fromBundle(it).newEvent }
        val newGift = arguments?.let { SearchLocationFragmentArgs.fromBundle(it).newGift }

        binding = FragmentSearchLocationBinding.inflate(inflater, container, false)


        if (newEvent != null) {
            val eventViewModel by viewModels<PostEventViewModel> { getVmFactory(author) }
            setUpAutoCompleteSearchPlace(giftVm = null, eventVm = eventViewModel)

            eventViewModel._event.value = newEvent


            setUpUserPreview(gift = null, event = newEvent)

            binding.nextButton.setOnClickListener {
                eventViewModel.event.value?.let { event -> eventViewModel.newPost(event) }
            }

        } else {
            val giftViewModel by viewModels<PostGiftViewModel> { getVmFactory(author) }

            setUpAutoCompleteSearchPlace(giftVm = giftViewModel, eventVm = null)

            giftViewModel._gift.value = newGift

            setUpUserPreview(gift = newGift, event = null)

            giftViewModel.locationChoice.observe(viewLifecycleOwner) {
                binding.locationTitle.text
            }

            binding.nextButton.setOnClickListener {
                giftViewModel.gift.value?.let { gift -> giftViewModel.newPost(gift) }
            }
        }


        // 檢查、要求user啟用ACCESS_FINE_LOCATION權限
        checkUserPermissions()


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

        return binding.root
    }

    private fun initializePlace() {
        val info = (activity as MainActivity).applicationContext.packageManager
            .getApplicationInfo((activity as MainActivity).packageName,
                PackageManager.GET_META_DATA)

        val key = info.metaData[resources.getString(R.string.map_api_key_name)].toString()

        Places.initialize(requireContext(), key)
    }

    private fun setUpAutoCompleteSearchPlace(
        eventVm: PostEventViewModel?,
        giftVm: PostGiftViewModel?,
    ) {
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_support_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.LAT_LNG,
            ))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                val name = place.name
                val result = place.latLng

                when (null) {
                    name -> Toast.makeText(requireContext(), "查無此地名", Toast.LENGTH_SHORT).show()
                    result -> Toast.makeText(requireContext(), "查無此地", Toast.LENGTH_SHORT).show()
                    else -> {
                        binding.locationTitle.text = name
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

    private fun setUpUserPreview(gift: GiftPost?, event: EventPost?) {
        if (gift != null) {
            binding.apply {
                this.title.text = gift.title
                this.sort.text = gift.sort
                this.description.text = gift.description
            }
        }

        if (event != null) {
            binding.apply {
                this.title.text = event.title
                this.sort.text = event.sort
                this.description.text = event.description
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

    private fun checkUserPermissions() {
        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            isPermissionGranted = true
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
            }.check()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapSettings()
    }

    private fun setupMapSettings() {
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
