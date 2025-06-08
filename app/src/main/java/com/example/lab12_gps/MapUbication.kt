package com.example.lab12_gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapUbication() {
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var mapReady by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()
    val mapTypeOptions = listOf(
        MapType.NORMAL to "Normal",
        MapType.HYBRID to "Hybrid",
        MapType.TERRAIN to "Terrain",
        MapType.SATELLITE to "Satellite"
    )

    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var expanded by remember { mutableStateOf(false) }

    var mapProperties by remember {
        mutableStateOf(MapProperties(mapType = selectedMapType))
    }

    data class CustomMarker(val position: LatLng, val colorHue: Float)

    var markerList by remember { mutableStateOf<List<CustomMarker>>(emptyList()) }
    var currentMarkerIndex by remember { mutableStateOf(0) }

    // Obtener ubicación cuando se conceda el permiso
    LaunchedEffect(locationPermission.status) {
        if (
            locationPermission.status.isGranted &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location = getCurrentLocation(context)
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                userLocation = latLng
                markerList = listOf(CustomMarker(latLng, BitmapDescriptorFactory.HUE_RED))
            }
        }
    }

    // Mover la cámara cuando haya ubicación Y el mapa esté listo
    LaunchedEffect(userLocation, mapReady) {
        if (userLocation != null && mapReady) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f),
                1000
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tipo de mapa") },
                actions = {
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(
                                text = mapTypeOptions.first { it.first == selectedMapType }.second
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            mapTypeOptions.forEach { (type, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedMapType = type
                                        mapProperties = mapProperties.copy(mapType = type)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = {
                    if (markerList.isNotEmpty()) {
                        currentMarkerIndex = (currentMarkerIndex - 1 + markerList.size) % markerList.size
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Anterior")
                }
                IconButton(onClick = {
                    if (markerList.isNotEmpty()) {
                        currentMarkerIndex = (currentMarkerIndex + 1) % markerList.size
                    }
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    val newZoom = cameraPositionState.position.zoom + 1
                    cameraPositionState.move(CameraUpdateFactory.zoomTo(newZoom))
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                IconButton(onClick = {
                    val newZoom = cameraPositionState.position.zoom - 1
                    cameraPositionState.move(CameraUpdateFactory.zoomTo(newZoom))
                }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Zoom Out")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (!locationPermission.status.isGranted) {
                Button(
                    onClick = { locationPermission.launchPermissionRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Permitir ubicación")
                }
            }

            if (userLocation != null) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        zoomGesturesEnabled = true,
                        scrollGesturesEnabled = true
                    ),
                    onMapLoaded = {
                        mapReady = true
                    },
                    onMapClick = { latLng ->
                        val hue = when (markerList.size % 3) {
                            0 -> BitmapDescriptorFactory.HUE_ORANGE
                            1 -> BitmapDescriptorFactory.HUE_AZURE
                            else -> BitmapDescriptorFactory.HUE_GREEN
                        }
                        markerList = markerList + CustomMarker(latLng, hue)
                        currentMarkerIndex = markerList.lastIndex
                    }
                ) {
                    markerList.forEachIndexed { index, marker ->
                        Marker(
                            state = rememberMarkerState(position = marker.position),
                            icon = BitmapDescriptorFactory.defaultMarker(marker.colorHue),
                            title = "Punto ${index + 1}"
                        )
                    }
                }
            } else {
                Text("Esperando ubicación...", modifier = Modifier.padding(16.dp))
            }
        }
    }

}

suspend fun getCurrentLocation(context: Context): Location? =
    suspendCancellableCoroutine { cont ->
        val fused = LocationServices.getFusedLocationProviderClient(context)

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()

        val token = CancellationTokenSource()

        fused.getCurrentLocation(request, token.token)
            .addOnSuccessListener { location ->
                cont.resume(location)
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }