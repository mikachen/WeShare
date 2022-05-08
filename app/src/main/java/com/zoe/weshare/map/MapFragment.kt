package com.zoe.weshare.map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
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
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.Cards
import com.zoe.weshare.databinding.FragmentMapBinding
import com.zoe.weshare.ext.checkLocationPermission
import com.zoe.weshare.ext.generateSmallIcon
import com.zoe.weshare.ext.getVmFactory
import kotlin.math.max

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private lateinit var currentLocation: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: LinearLayoutManager
    private lateinit var adapter: CardGalleryAdapter
    private val marginDecoration = GalleryDecoration()
    private val markersRef = mutableListOf<Marker>()
    private val viewModel by viewModels<MapViewModel> { getVmFactory() }


    var needRefreshMap = false

    private var isPermissionGranted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isPermissionGranted = this.checkLocationPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)

        if (needRefreshMap) {
            map = viewModel.googleMap!!
            needRefreshMap = false
        }

        if (isPermissionGranted) {

            viewModel.gifts.observe(viewLifecycleOwner) {
                viewModel.onCardPrepare(gifts = it, events = null)
            }

            viewModel.events.observe(viewLifecycleOwner) {
                viewModel.onCardPrepare(gifts = null, events = it)
            }

            viewModel.cards.observe(viewLifecycleOwner) {
                if (viewModel.isEventCardsComplete && viewModel.isGiftCardsComplete) {
                    // markers on map
                    createMarker(it)


                    // cards recycler view
                    adapter.submitCards(it)
                    Log.d("cards", "$it")
                    Log.d("markers", "$markersRef")

                }
            }

            viewModel.snapPosition.observe(viewLifecycleOwner) {
                // when rv scroll, trigger marker showInfoWindow and move camera
                markersRef[it].showInfoWindow()
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(markersRef[it].position, 13F))
            }

            viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
                it?.let {
                    findNavController().navigate(
                        NavGraphDirections.actionGlobalGiftDetailFragment(it)
                    )

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

            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

            binding.mapView.onCreate(savedInstanceState)
            binding.mapView.getMapAsync(this)

            setupCardGallery()
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setupMapSettings()
        getLastLocation()
    }

    private fun createMarker(cards: List<Cards>?) {
        markersRef.clear()
        cards?.forEach {
            it.postLocation.let { location ->

                val point = location!!.getLocation

                val options = MarkerOptions().position(point).title(it.title)

                val newMarker = map.addMarker(options)

                if (newMarker != null) {
                    when (it.postType) {
                        GIFT_CARD -> newMarker.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                generateSmallIcon(requireContext(), R.drawable.ic_map_gift_marker)
                            )
                        )
                        EVENT_CARD -> newMarker.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                generateSmallIcon(requireContext(), R.drawable.ic_map_event_marker)
                            )
                        )
                    }
                    markersRef.add(newMarker)
                }
            }
        }
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

    private fun setupMapSettings() {


        map.uiSettings.setAllGesturesEnabled(true)

        map.uiSettings.isMyLocationButtonEnabled = true

        binding.btnGetUserLocation.setOnClickListener {
            getLastLocation()
        }

        map.setOnMarkerClickListener(this)
    }

    private fun setupCardGallery() {

        recyclerView = binding.cardsRecycleview

        adapter = CardGalleryAdapter(
            CardGalleryAdapter.CardOnClickListener { selectedCard ->
                viewModel.displayCardDetails(selectedCard)
            }
        )

        manager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = manager

        val linearSnapHelper = LinearSnapHelper().apply {
            attachToRecyclerView(recyclerView)
        }

        recyclerView.addItemDecoration(marginDecoration)

        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            viewModel.onGalleryScrollChange(
                recyclerView.layoutManager, linearSnapHelper
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (isPermissionGranted) {
            binding.mapView.onStart()
        }
        recyclerView.removeItemDecoration(marginDecoration)
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

        viewModel.saveMapInfo(map)
        needRefreshMap = true
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

    override fun onMarkerClick(marker: Marker): Boolean {

        val position = markersRef.indexOf(marker)
        recyclerView.smoothScrollToPosition(position)

        val handler = Handler(Looper.getMainLooper())
        val start = SystemClock.uptimeMillis()
        val duration = 1800

        val interpolator = BounceInterpolator()

        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = max(
                    1 - interpolator.getInterpolation(elapsed.toFloat() / duration), 0f
                )

                marker.setAnchor(0.5f, 1f + 2 * t)

                // Post again 16ms later.
                if (t > 0.0) {
                    handler.postDelayed(this, 16)
                }
            }
        })

        return false
    }
}
