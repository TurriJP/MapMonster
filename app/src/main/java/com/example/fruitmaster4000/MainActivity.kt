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
    import android.graphics.Point
    import android.widget.Toast
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material3.Button
    import androidx.compose.material3.FloatingActionButton
    import androidx.compose.material3.Icon
    import androidx.compose.material3.Text
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.unit.dp
    import androidx.core.app.ActivityCompat
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import io.realm.Realm
    import io.realm.RealmConfiguration
    import io.realm.RealmObject
    import io.realm.annotations.PrimaryKey
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import org.bson.types.ObjectId
    import java.math.BigDecimal
    import java.util.Date

    class MainActivity : ComponentActivity(), MapListener, GpsStatus.Listener {

//        lateinit var mMap: MapView
//        lateinit var controller: IMapController;
//        lateinit var mMyLocationOverlay: MyLocationNewOverlay;
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private var mapView: MapView? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            initiateRealm()

            // Initialize FusedLocationProviderClient
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
                    mapView?.controller?.apply {
                        setCenter(geoPoint)
                        setZoom(18.0)
                    }
                }
            }

            companion object {
                private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
            }

        private fun initiateRealm(){
            Realm.init(this)
            val config = RealmConfiguration.Builder()
                .name("Fruitmaster.realm")
                .allowWritesOnUiThread(false)
                .allowQueriesOnUiThread(false)
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded() //debug
//                .migration(Migration())
                .build()
            Realm.setDefaultConfiguration(config)
        }


    }

    @Composable
    fun MapScreen(onMapReady: (MapView) -> Unit) {
        val context = LocalContext.current
        var mapView by remember { mutableStateOf<MapView?>(null) }
        var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
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

                        myLocationOverlay =
                            MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                                enableMyLocation()
                                enableFollowLocation()
                                isDrawAccuracyEnabled = true
                            }

                        overlays.add(myLocationOverlay)

                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                Log.e("TAG", "onCreate:la ${event?.source?.mapCenter?.latitude}")
                                Log.e("TAG", "onCreate:lo ${event?.source?.mapCenter?.longitude}")
                                return true
                            }

                            override fun onZoom(event: ZoomEvent?): Boolean {
                                Log.e(
                                    "TAG",
                                    "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}"
                                )
                                return false
                            }
                        })

                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Update map view
                }
            )

            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        mapView?.let { map ->
                            val currentLocation = map.mapCenter
                            val geoPoint =
                                GeoPoint(currentLocation.latitude, currentLocation.longitude)
                            saveLocation(geoPoint)
                            Toast.makeText(context, "Location saved!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Save Location")
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        val savedLocations = remember { mutableStateListOf<PointEntity>() }

        NavHost(navController = navController, startDestination = "map") {
            composable("map") {
                MapScreen(
                    onSaveLocation = { latitude, longitude ->
                        savedLocations.add(SavedLocation(savedLocations.size + 1, latitude, longitude))
                    },
                    onViewSavedLocations = {
                        navController.navigate("savedLocations")
                    }
                )
            }
            composable("savedLocations") {
                SavedLocationsScreen(
                    locations = savedLocations,
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }

    suspend fun saveLocation(location: GeoPoint) {
        // Save current location to database
        Log.d("MapScreen", "Saving location: ${location.latitude}, ${location.longitude}")
        CoroutineScope(Dispatchers.IO).launch {
            var personalDetail = PointEntity().apply {
                name = "Mockado"
                lat = location.latitude
                lon = location.longitude
            }

            var realmDb = Realm.getDefaultInstance() // get default Instance
            realmDb.beginTransaction()
            realmDb.copyToRealmOrUpdate(personalDetail) // insert or update the data
            realmDb.commitTransaction()
            realmDb.close()
        }
    }

    fun getLocations(config:RealmConfiguration) : List<PointEntity> {
        val realm = Realm.getDefaultInstance()
        val locations = realm.query<PointEntity>().find()
    }

    open class PointEntity : RealmObject(){
        @PrimaryKey
        var _id : ObjectId = ObjectId()

        var name : String? = null
        var hexcolor : String? = null
        var lat : Double? = null
        var lon : Double? = null
//        var created_at : Date? = null

    }

    @Composable
    fun SavedLocationsScreen(locations: List<PointEntity>, onBackClick: () -> Unit) {
        Column {
            Button(onClick = onBackClick) {
                Text("Back to Map")
            }
            LazyColumn {
                items(locations) { location ->
                    Text("Location ${location._id}: ${location.lat}, ${location.lon}")
                }
            }
        }
    }
