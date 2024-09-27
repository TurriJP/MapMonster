    package com.example.fruitmaster4000

    import android.app.ProgressDialog
    import android.content.Context.MODE_PRIVATE
    import android.content.pm.PackageManager
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
    import androidx.core.content.ContextCompat
    import org.osmdroid.util.GeoPoint
    import com.google.android.gms.location.FusedLocationProviderClient
    import com.google.android.gms.location.LocationServices
    import android.Manifest
    import android.annotation.SuppressLint
    import androidx.core.app.ActivityCompat


    class MainActivity : ComponentActivity(), MapListener, GpsStatus.Listener {

//        lateinit var mMap: MapView
//        lateinit var controller: IMapController;
//        lateinit var mMyLocationOverlay: MyLocationNewOverlay;
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private var mapView: MapView? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Initialize FusedLocationProviderClient early in onCreate
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            Configuration.getInstance().load(
                applicationContext,
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
            )

            setContent {
                MapScreen { mapView ->
                    this.mapView = mapView  // Store the mapView instance
                }
            }

    // Check for location permissions and retrieve the GPS location
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        getDeviceLocation()
    } else {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
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

        private fun getLocation() {
            val Somewhere = GeoPoint(33.989820, -81.029123)
        }

        @SuppressLint("MissingPermission")
        private fun getDeviceLocation() {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // We have the location, pass it to the map
                    val deviceGeoPoint = GeoPoint(location.latitude, location.longitude)
                    setMapLocation(deviceGeoPoint)
                } else {
                    Log.e("MainActivity", "Unable to retrieve location.")
                }
            }
        }

            private fun setMapLocation(geoPoint: GeoPoint) {
                runOnUiThread {
                    // Assuming you have a map controller from the MapView in your Composable
                    mapView?.controller?.apply {
                        setCenter(geoPoint)
                        setZoom(15.0) // Set a suitable zoom level for the user's location
                    }
                }
            }

            companion object {
                private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
            }


    }

    @Composable
    fun MapScreen(onMapReady: (MapView) -> Unit) {
        val context = LocalContext.current
        var mapView by remember { mutableStateOf<MapView?>(null) }
        var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    // Initialize osmdroid configuration
                    Configuration.getInstance().load(
                        ctx,
                        ctx.getSharedPreferences(ctx.getString(R.string.app_name), MODE_PRIVATE)
                    )

                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(18.0)

                    val somewhere = GeoPoint(-23.5402, -46.6524)

                    controller.setCenter(somewhere)

                    myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                        enableMyLocation()
                        enableFollowLocation()
                        isDrawAccuracyEnabled = true
//                        runOnFirstFix {
//                            context as ComponentActivity
//                            context.runOnUiThread {
//                                controller.setCenter(somewhere)
//                                controller.animateTo(somewhere)
//                            }
//                        }
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
