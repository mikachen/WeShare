package com.zoe.weshare.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.Cards
import com.zoe.weshare.databinding.FragmentMapBinding
import com.zoe.weshare.ext.generateSmallIcon
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.requestPermissions


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private lateinit var currentLocation: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardGalleryAdapter

    private var lastSelectedMarker: Marker? = null
    private val markersRef = mutableListOf<Marker>()

    val viewModel by viewModels<MapViewModel> { getVmFactory() }

    private var isPermissionGranted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)

        viewModel.gifts.observe(viewLifecycleOwner) {
            viewModel.onCardPrepare(gifts = it, events = null)
        }

        viewModel.events.observe(viewLifecycleOwner) {
            viewModel.onCardPrepare(gifts = null, events = it)
        }

        viewModel.cards.observe(viewLifecycleOwner) {
            if (viewModel.isEventCardsComplete && viewModel.isGiftCardsComplete) {

                Log.d("shuffled1","$it")
                //markers on map
                createMarker(it)

                //cards recycler view
                adapter.submitCards(it)
            }
        }

        viewModel.snapPosition.observe(viewLifecycleOwner) {

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
                findNavController().navigate(
                    NavGraphDirections.actionGlobalGiftDetailFragment(it))

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


        getLocationPermission()
        // TODO 當user同意location權限後，檢查user是否有啟用GooglePlayService

        if (!isPermissionGranted) {
            //TODO 這邊用call back判斷 重寫
            AlertDialog.Builder(requireContext())
                .setTitle("開啟位置權限")
                .setMessage("此應用程式，位置權限已被關閉，需開啟才能正常使用")
                .setPositiveButton("確定") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, 111)
                }
                .setNegativeButton("取消") { _, _ -> getLocationPermission() }
                .show()

        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            binding.mapView.onCreate(savedInstanceState)
            binding.mapView.getMapAsync(this)

            setupCardGallery()
        }

        return binding.root
    }


    private fun createMarker(cards: List<Cards>?) {
        Log.d("shuffled2","$cards")

        cards?.forEach {
            it.postLocation.let { location ->

                val point = location!!.getLocation

                val options = MarkerOptions().position(point).title(it.title)

                val newMarker = map.addMarker(options)

                if (newMarker != null) {
                    when (it.postType) {
                        GIFT_TYPE -> newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                            generateSmallIcon(requireContext(), R.drawable.ic_map_event_marker)))
                        EVENT_TYPE -> newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                            generateSmallIcon(requireContext(), R.drawable.ic_map_gift_marker)))
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
            getLocationPermission()
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

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.isMyLocationButtonEnabled = true

        map.setOnMyLocationButtonClickListener {
            getLastLocation()
            true
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

    private fun getLocationPermission() {
        //檢查權限
        if (ActivityCompat.checkSelfPermission(
                (activity as MainActivity),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //已獲取到權限
            Toast.makeText(requireContext(), "已獲取", Toast.LENGTH_SHORT).show()
            isPermissionGranted = true
            //todo checkGPSState()
        } else {
            //詢問要求獲取權限
            (activity as MainActivity).requestPermissions()
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
                val t = Math.max(
                    1 - interpolator.getInterpolation(elapsed.toFloat() / duration), 0f)

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
