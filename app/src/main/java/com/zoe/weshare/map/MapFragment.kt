package com.zoe.weshare.map

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var binding: FragmentMapBinding
    lateinit var map: GoogleMap
    val hotel = LatLng(25.04686368618674, 121.51322297916225)
    val school = LatLng(25.040253731228947, 121.53279480180795)  //TODO 期望是user當前位置

    var isPermissionGranted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentMapBinding.inflate(inflater, container, false)
        checkUserPermission()


        return binding.root
    }

    private fun checkUserPermission() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    isPermissionGranted = true
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_LONG).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", requireActivity().packageName, "")
                    intent.data = uri
                    startActivity(intent)
                }
            }
            ).check()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()

        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        addpolyLines()


//        val hotel = LatLng(25.04686368618674, 121.51322297916225)
        map.addMarker(MarkerOptions().position(hotel).title("Marker in hotel"))
            ?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gift))

//        val school = LatLng(25.040253731228947, 121.53279480180795)  //TODO 期望是user當前位置
        map.addMarker(MarkerOptions().position(school).title("Marker in AppWorkSchool")) //添加marker
            ?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        map.moveCamera(CameraUpdateFactory.newLatLng(school)) //相機位置移動
    }


    // 座標點間畫線
    private fun addpolyLines() {
        var pointList = mutableListOf<LatLng>()

        pointList.add(hotel)
        pointList.add(school)

        val polylineOptions = PolylineOptions()
        polylineOptions.addAll(pointList)
        polylineOptions.width(2f).color(Color.RED)

        map.addPolyline(polylineOptions)
    }
}