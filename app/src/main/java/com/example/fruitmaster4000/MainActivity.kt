    package com.example.fruitmaster4000

    import android.app.ProgressDialog
    import android.graphics.Rect
    import android.location.GpsStatus
    import android.location.Location
    import android.os.Bundle
    import android.util.Log

    import androidx.appcompat.app.AppCompatActivity
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.viewinterop.AndroidView
    import com.google.android.gms.common.api.GoogleApiClient
    import com.google.android.gms.location.LocationRequest
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.*
    import org.osmdroid.api.IMapController
    import org.osmdroid.config.Configuration
    import org.osmdroid.events.MapListener
    import org.osmdroid.events.ScrollEvent
    import org.osmdroid.events.ZoomEvent
    import org.osmdroid.tileprovider.tilesource.TileSourceFactory
    import org.osmdroid.views.MapView
    import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
    import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.runtime.*
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.viewinterop.AndroidView
    import org.osmdroid.util.GeoPoint


    class MainActivity : ComponentActivity(), MapListener, GpsStatus.Listener {

//        lateinit var mMap: MapView
//        lateinit var controller: IMapController;
//        lateinit var mMyLocationOverlay: MyLocationNewOverlay;
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Configuration.getInstance().load(
                applicationContext,
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
            )

            setContent {
                MapScreen()
            }


        }
        override fun onScroll(event: ScrollEvent?): Boolean {
            // event?.source?.getMapCenter()
            Log.e("TAG", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
            Log.e("TAG", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
            //  Log.e("TAG", "onScroll   x: ${event?.x}  y: ${event?.y}", )
            return true
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            //  event?.zoomLevel?.let { controller.setZoom(it) }


            Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
            return false;
        }

        override fun onGpsStatusChanged(event: Int) {


            TODO("Not yet implemented")
        }


    }

        @Composable
        fun MapScreen() {
            val context = LocalContext.current
            var mapView by remember { mutableStateOf<MapView?>(null) }
            var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(6.0)

                        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                            enableMyLocation()
                            enableFollowLocation()
                            isDrawAccuracyEnabled = true
                            runOnFirstFix {
                                context as ComponentActivity
                                context.runOnUiThread {
                                    controller.setCenter(myLocation)
                                    controller.animateTo(myLocation)
                                }
                            }
                        }

                        overlays.add(myLocationOverlay)

                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                Log.e("TAG", "onCreate:la ${event?.source?.mapCenter?.latitude}")
                                Log.e("TAG", "onCreate:lo ${event?.source?.mapCenter?.longitude}")
                                return true
                            }

                            override fun onZoom(event: ZoomEvent?): Boolean {
                                Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
                                return false
                            }
                        })

                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // You can update the map view here if needed
                }
            )

            // You can add more Compose UI elements here if needed
        }

//    import android.os.Bundle
//    import android.Manifest
//    import android.content.pm.PackageManager
//    import android.widget.Toast
//    import androidx.appcompat.app.AppCompatActivity
//    import androidx.core.app.ActivityCompat
//    import androidx.core.content.ContextCompat
//    import com.google.android.gms.location.FusedLocationProviderClient
//    import com.google.android.gms.location.LocationServices
//    import org.osmdroid.config.Configuration
//    import org.osmdroid.tileprovider.tilesource.TileSourceFactory
//    import org.osmdroid.util.GeoPoint
//    import org.osmdroid.views.MapView
//    import com.example.fruitmaster4000.databinding.ActivityMainBinding

//    class MainActivity : AppCompatActivity() {
//
//        private lateinit var binding: ActivityMainBinding
//        private lateinit var mapView: MapView
//        private lateinit var fusedLocationClient: FusedLocationProviderClient
//        private val LOCATION_PERMISSION_REQUEST_CODE = 1
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//
//            // Initialize the OpenStreetMap configuration
//            Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
//
//            binding = ActivityMainBinding.inflate(layoutInflater)
//            setContentView(binding.root)
//
//            mapView = binding.mapView
//            mapView.setTileSource(TileSourceFactory.MAPNIK)
//
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//            requestLocationPermission()
//        }
//
//        private fun requestLocationPermission() {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                    LOCATION_PERMISSION_REQUEST_CODE
//                )
//            } else {
//                getLocation()
//            }
//        }
//
//        override fun onRequestPermissionsResult(
//            requestCode: Int,
//            permissions: Array<out String>,
//            grantResults: IntArray
//        ) {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    getLocation()
//                } else {
//                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        private fun getLocation() {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                    location?.let {
//                        val currentLocation = GeoPoint(it.latitude, it.longitude)
//                        mapView.controller.setCenter(currentLocation)
//                        mapView.controller.setZoom(15.0)
//                    }
//                }
//            }
//        }
//
//        override fun onResume() {
//            super.onResume()
//            mapView.onResume()
//        }
//
//        override fun onPause() {
//            super.onPause()
//            mapView.onPause()
//        }
//    }