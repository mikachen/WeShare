package com.zoe.weshare.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zoe.weshare.MainActivity
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentMapBinding
import com.zoe.weshare.ext.getVmFactory

const val COARSE_FINE_LOCATION_PERMISSION_ID = 42

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap

    private lateinit var currentLocation: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var isPermissionGranted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)

        // 檢查、要求user啟用ACCESS_FINE_LOCATION權限
        checkUserPermissions()

        // 當user同意location權限後，檢查user是否有啟用GooglePlayService
        if (isPermissionGranted) {
            if (checkGooglePlayService()) {

                binding.mapView.onCreate(savedInstanceState)
                binding.mapView.getMapAsync(this)

                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())

                val viewModel by viewModels<MapViewModel> { getVmFactory() }

                viewModel.gifts.observe(viewLifecycleOwner) {
                    createMarker(gifts = it, null)
                }

                viewModel.events.observe(viewLifecycleOwner) {
                    createMarker(null, events = it)
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

    private fun createMarker(gifts: List<GiftPost>?, events: List<EventPost>?) {
//        map.addMarker(MarkerOptions().position(hotel).title("Hotel"))
//            ?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gift))

        gifts?.forEach {

            val point =
                LatLng(it.location!!.latitude.toDouble(), it.location!!.longitude.toDouble())

            map.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(it.title)
            )
                ?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        }

        events?.forEach {

            val point =
                LatLng(it.location!!.latitude.toDouble(), it.location!!.longitude.toDouble())

            map.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(it.title)
            )
                ?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
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
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))
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
        map.setOnCameraMoveStartedListener {
            (activity as MainActivity).binding.bottomAppBar.performHide()
        }
        map.setOnCameraIdleListener {
            (activity as MainActivity).binding.bottomAppBar.performShow()
        }

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.isMyLocationButtonEnabled = true

        map.setOnMyLocationButtonClickListener {
            getLastLocation()
            true
        }
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
