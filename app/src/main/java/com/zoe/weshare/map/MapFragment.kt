package com.zoe.weshare.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.data.Cards
import com.zoe.weshare.databinding.FragmentMapBinding
import com.zoe.weshare.ext.getVmFactory

const val COARSE_FINE_LOCATION_PERMISSION_ID = 42

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap

    private lateinit var currentLocation: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var adapter: CardGalleryAdapter

    private val markersRef = mutableListOf<Marker>()

    val viewModel by viewModels<MapViewModel> { getVmFactory() }

    var isPermissionGranted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)

        checkUserPermissions() // 檢查、要求user啟用ACCESS_FINE_LOCATION權限

        // 當user同意location權限後，檢查user是否有啟用GooglePlayService
        if (isPermissionGranted) {
            if (checkGooglePlayService()) {

                binding.mapView.onCreate(savedInstanceState)
                binding.mapView.getMapAsync(this)

                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())


                setupCardGallery()

                viewModel.gifts.observe(viewLifecycleOwner) {
                    viewModel.onCardPrepare(gifts = it, events = null)
                }

                viewModel.events.observe(viewLifecycleOwner) {
                    viewModel.onCardPrepare(gifts = null, events = it)
                }

                viewModel.cards.observe(viewLifecycleOwner) {
                    //cards recycler view
                    adapter.submitCards(it)

                    //markers on map
                    if (viewModel.isEventCardsComplete && viewModel.isGiftCardsComplete) {
                        createMarker(it)
                    }
                }

                viewModel.snapPosition.observe(viewLifecycleOwner) {
                    Log.d("snapPosition CHANGE!", "$it")

                    //trigger marker showInfoWindow
                    markersRef[it].showInfoWindow()

                    //move camera
                    viewModel.cardsViewList[it].postLocation?.let { location ->
                        val point =
                            LatLng(location.latitude.toDouble(), location.longitude.toDouble())
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 13F))
                    }
                }


                viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
                    it?.let {
                        findNavController()
                            .navigate(NavGraphDirections.actionGlobalGiftDetailFragment(it))
                        viewModel.displayCardDetailsComplete()
                    }
                }

                viewModel.navigateToSelectedEvent.observe(viewLifecycleOwner) {
                    it?.let {
                        findNavController()
                            .navigate(NavGraphDirections.actionGlobalEventDetailFragment(it))
                        viewModel.displayCardDetailsComplete()
                    }
                }
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

    private fun createMarker(cards: List<Cards>?) {

        cards?.forEach {
            it.postLocation?.let { location ->

                val point =
                    LatLng(location.latitude.toDouble(), location.longitude.toDouble())

                val options = MarkerOptions().position(point).title(it.title)

                val newMarker = map.addMarker(options)

                if (newMarker != null) {
                    when (it.postType) {
                        GIFT_TYPE -> newMarker.setIcon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED))
                        EVENT_TYPE -> newMarker.setIcon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_YELLOW))
                    }
                    markersRef.add(newMarker)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setupMapSettings()
        getLastLocation()
    }

    // Get current location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (isPermissionGranted) {
            fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    map.isMyLocationEnabled = true
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13F))
                }
            }
        } else {
            requestLocationPermissions()
        }
    }

    // Get current location, if shifted
// from previous location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val mLastLocation: Location = locationResult.lastLocation
                currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()!!
        )
    }

    // Request permissions if not granted before
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            COARSE_FINE_LOCATION_PERMISSION_ID
        )
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

    private fun setupMapSettings() {

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.isMyLocationButtonEnabled = true

        map.setOnMyLocationButtonClickListener {
            getLastLocation()
            true
        }
    }

    private fun setupCardGallery() {

        val recyclerView = binding.cardsRecycleview

        adapter = CardGalleryAdapter(
            CardGalleryAdapter.CardOnClickListener { selectedCard ->
                viewModel.displayCardDetails(selectedCard)
            }
        )

        val manager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = manager

        val linearSnapHelper = LinearSnapHelper().apply {
            attachToRecyclerView(recyclerView)
        }

        val marginDecoration = GalleryDecoration()
        recyclerView.addItemDecoration(marginDecoration)


        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            viewModel.onGalleryScrollChange(
                recyclerView.layoutManager, linearSnapHelper
            )
        }
//        val currentPosition = recyclerView.adapter!!.itemCount / 2
//        manager.scrollToPosition(currentPosition)

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
