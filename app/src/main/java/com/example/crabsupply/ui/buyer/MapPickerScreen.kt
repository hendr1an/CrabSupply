package com.example.crabsupply.ui.buyer

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation // Ikon GPS
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapPickerScreen(
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current

    // Konfigurasi OSM
    Configuration.getInstance().load(context, context.getSharedPreferences("osm", 0))

    // Default Lokasi (Misal UMY / Monas)
    var centerPoint by remember { mutableStateOf(GeoPoint(-7.8113, 110.3235)) }

    // Controller Peta (agar kita bisa gerakkan secara koding)
    var mapController by remember { mutableStateOf<org.osmdroid.api.IMapController?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // --- LOGIKA IZIN LOKASI ---
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Jika diizinkan, aktifkan lokasi
            mapViewRef?.let { map ->
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()
                map.overlays.add(locationOverlay)
                map.invalidate() // Refresh peta

                // Pindah kamera ke lokasi user
                locationOverlay.runOnFirstFix {
                    val myLoc = locationOverlay.myLocation
                    if (myLoc != null) {
                        // Harus di run di UI Thread
                        (context as? android.app.Activity)?.runOnUiThread {
                            map.controller.animateTo(myLoc)
                            centerPoint = myLoc // Update titik tengah
                        }
                    }
                }
            }
        } else {
            Toast.makeText(context, "Izin lokasi ditolak, gunakan cara manual", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. TAMPILAN PETA
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(18.0) // Zoom lebih dekat
                    controller.setCenter(centerPoint)

                    mapController = controller
                    mapViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.setMapListener(object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                        val center = mapView.mapCenter
                        centerPoint = GeoPoint(center.latitude, center.longitude)
                        return true
                    }
                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean = true
                })
            }
        )

        // 2. PIN MERAH DI TENGAH (Penunjuk Lokasi Pilihan)
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin",
            tint = Color.Red,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .offset(y = (-24).dp)
        )

        // 3. TOMBOL "MY LOCATION" (POJOK KANAN BAWAH)
        FloatingActionButton(
            onClick = {
                // Cek apakah izin sudah diberikan?
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Sudah ada izin -> Pindah ke lokasi user
                    mapViewRef?.let { map ->
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                        locationOverlay.enableMyLocation()
                        map.overlays.add(locationOverlay)

                        val myLoc = locationOverlay.lastFix // Coba ambil lokasi terakhir
                        if (myLoc != null) {
                            map.controller.animateTo(GeoPoint(myLoc))
                        } else {
                            // Coba paksa update
                            locationOverlay.enableFollowLocation()
                            Toast.makeText(context, "Mencari sinyal GPS...", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Belum ada izin -> Minta Izin
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp), // Posisi di atas tombol "PILIH"
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Lokasi Saya", tint = MaterialTheme.colorScheme.primary)
        }

        // 4. TOMBOL "PILIH LOKASI INI" (BAWAH)
        Button(
            onClick = {
                onLocationSelected(centerPoint.latitude, centerPoint.longitude)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("PILIH LOKASI INI")
        }
    }
}