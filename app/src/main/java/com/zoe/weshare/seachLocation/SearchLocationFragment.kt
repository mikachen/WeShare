package com.zoe.weshare.seachLocation

import android.os.Bundle
import android.Manifest
import android.location.Address
import android.location.Geocoder
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
import com.zoe.weshare.data.Author
import com.zoe.weshare.databinding.FragmentSearchLocationBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.posting.event.PostEventViewModel
import com.zoe.weshare.posting.gift.PostGiftViewModel
import java.util.*

class SearchLocationFragment : Fragment(), OnMapReadyCallback {
    val author = Author(
        name = "Mock",
        userId = "Mock1234"
    )

    // TODO 輕量地圖
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


        val newEvent = arguments?.let { SearchLocationFragmentArgs.fromBundle(it).newEvent }
        val newGift = arguments?.let {SearchLocationFragmentArgs.fromBundle(it).newGift}

        Log.d("newEvent","$newEvent")
        Log.d("newGift","$newGift")

        binding = FragmentSearchLocationBinding.inflate(inflater, container, false)


        if(newEvent != null){
            val eventViewModel by viewModels<PostEventViewModel> { getVmFactory(author) }
            Log.d("eventViewModel","Create")
            eventViewModel._event.value = newEvent

            setUpSearchView(eventVm = eventViewModel, giftVm = null)

            binding.nextButton.setOnClickListener {
                eventViewModel.event.value?.let { event -> eventViewModel.newPost(event) }
            }

        } else{
            val giftViewModel by viewModels<PostGiftViewModel> { getVmFactory(author) }
            Log.d("giftViewModel","Create")

            giftViewModel._gift.value = newGift

            setUpSearchView(eventVm = null, giftVm = giftViewModel)

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


    private fun setUpSearchView(eventVm: PostEventViewModel? , giftVm: PostGiftViewModel?) {

        searchView = binding.searchView

        searchView.queryHint = "search a location..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return if (query != null) {

                    map.clear() //清掉舊marker

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    try {
                        val listAddress: List<Address> = geocoder.getFromLocationName(query, 1)

                        if (listAddress.isNotEmpty()) {

                            val result = LatLng(listAddress[0].latitude, listAddress[0].longitude)

                            map.addMarker(MarkerOptions().position(result).title("Search Point"))
                                ?.setIcon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_RED
                                    )
                                )

                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(result, 16F))

                            eventVm?.updateTitle(title = query, point = result )
                            giftVm?.updateTitle(title = query, point = result )

                            //TODO  位置寫入VM
                        }
                    } catch (e: Exception) {

                        Log.d("Exception", "$e")
                    }
                    false
                } else {
                    true
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
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
