package com.example.lab12_gps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTypeScreen() {
    val arequipa = LatLng(-16.4040102, -71.559611)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipa, 13f)
    }

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

    var markerList by remember {
        mutableStateOf(listOf(CustomMarker(arequipa, BitmapDescriptorFactory.HUE_RED)))
    }

    var currentMarkerIndex by remember { mutableStateOf(0) }

    // Centrar cámara en el marcador actual
    LaunchedEffect(currentMarkerIndex, markerList) {
        if (markerList.isNotEmpty()) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(markerList[currentMarkerIndex].position, 13f),
                durationMs = 1000
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tipo de mapa") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(
                                text = mapTypeOptions.first { it.first == selectedMapType }.second,
                                color = MaterialTheme.colorScheme.onPrimary
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
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = false
            ),
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
    }
}