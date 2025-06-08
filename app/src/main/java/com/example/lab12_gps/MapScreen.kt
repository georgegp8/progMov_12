package com.example.lab12_gps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val initialLocation = LatLng(-16.4040102, -71.559611) // Arequipa
    val yuraLocation = LatLng(-16.2520984, -71.6836503)   // Yura
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
    }

    // Animar cámara a Yura cuando se carga el mapa
    LaunchedEffect(Unit) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(yuraLocation, 12f),
            durationMs = 3000
        )
    }

    val locations = listOf(
        LatLng(-16.433415, -71.5442652), // JLByR
        LatLng(-16.4205151, -71.4945209), // Paucarpata
        LatLng(-16.3524187, -71.5675994)  // Zamacola
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Marcadores múltiples
            locations.forEachIndexed { index, location ->
                Marker(
                    state = rememberMarkerState(position = location),
                    title = when (index) {
                        0 -> "JLByR"
                        1 -> "Paucarpata"
                        2 -> "Zamacola"
                        else -> "Ubicación"
                    },
                    snippet = "Punto de interés"
                )
            }
        }
    }
}