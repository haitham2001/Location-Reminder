package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback  {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var map: GoogleMap
    private var currentMarker: Marker? = null
    private val TAG = SelectLocationFragment::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveLocationBtn.setOnClickListener{
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        currentMarker?.let { marker ->
            _viewModel.reminderSelectedLocationStr.value = marker.title
            _viewModel.longitude.value = marker.position.longitude
            _viewModel.latitude.value = marker.position.latitude
        }
        findNavController().popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setPoiClick(map:GoogleMap){
        map.setOnPoiClickListener { poi ->
            currentMarker?.remove()
            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            currentMarker?.showInfoWindow()
        }
    }

    private fun isPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun setMapLongClick(map: GoogleMap){
        map.setOnMapLongClickListener { latLong ->
            createMarker(latLong)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val locLatLong = LatLng(-34.0,151.0)
        val zoomLevel = 18.0f

        currentMarker?.let {
            createMarker(locLatLong)
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLong, zoomLevel))

        //Get users location
        enableMyLocation()
        //Check if we long clicked on the map
        setMapLongClick(map)
        //Check if a poi was clicked
        setPoiClick(map)
        //Apply our custom map style
        setMapStyle(map)
    }

    private fun enableMyLocation() {
        if(isPermissionGranted()) {
            map.isMyLocationEnabled = true
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            val lastLocationTask = fusedLocationProviderClient.lastLocation

            //Go to the user location and add marker if completed
            lastLocationTask.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    task.result?.run {
                        val latLng = LatLng(latitude, longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        createMarker(latLng)
                    }
                }
            }
        }
        else {
            //Request that the user enables their location
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun createMarker(locationCoordinates: LatLng){
        val snippet = String.format(
            Locale.getDefault(),
            getString(R.string.lat_long_snippet),
            locationCoordinates.latitude,
            locationCoordinates.longitude
        )
        currentMarker?.remove()
        currentMarker = map.addMarker(
            MarkerOptions()
                .position(locationCoordinates)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                enableMyLocation()
            else
                enableLocationSettingsSnackBar()
        }
    }

    private fun setMapStyle(map: GoogleMap){
        try{
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.maps_style)
            )
            if(!success)
                Log.i(TAG,"Style Failed")
        }
        catch(e: Resources.NotFoundException){
            Log.i(TAG,"Couldn't find the file",e)
        }
    }

    private fun enableLocationSettingsSnackBar() {
        Snackbar.make(
            requireView(),
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE
        ).setAction("settings") {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
    }

    override fun onResume() {
        super.onResume()

        if(this::map.isInitialized) {
            if (isPermissionGranted())
                enableMyLocation()
            else
                enableLocationSettingsSnackBar()
        }
    }

}
