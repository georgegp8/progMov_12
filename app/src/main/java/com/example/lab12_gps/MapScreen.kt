package com.example.lab12_gps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen() {
    val context = LocalContext.current

    // Posición inicial (Arequipa)
    val initialLocation = LatLng(-16.4040102, -71.559611)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
    }

    // Animar cámara a Paucarpata al iniciar
    LaunchedEffect(Unit) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(
                LatLng(-16.4205151, -71.4945209), // Paucarpata
                12f
            ),
            durationMs = 3000
        )
    }

    // Múltiples ubicaciones (marcadores)
    val locations = listOf(
        LatLng(-16.433415, -71.5442652), // JLByR
        LatLng(-16.4205151, -71.4945209), // Paucarpata
        LatLng(-16.3524187, -71.5675994)  // Zamacola
    )

    // Polígonos
    val mallAventuraPolygon = listOf(
        LatLng(-16.432292, -71.509145),
        LatLng(-16.432757, -71.509626),
        LatLng(-16.433013, -71.509310),
        LatLng(-16.432566, -71.508853)
    )

    val parqueLambramaniPolygon = listOf(
        LatLng(-16.422704, -71.530830),
        LatLng(-16.422920, -71.531340),
        LatLng(-16.423264, -71.531110),
        LatLng(-16.423050, -71.530600)
    )

    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961),
        LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289),
        LatLng(-16.399299, -71.536721)
    )
    val rutaTuristica = listOf(
        LatLng(-16.398866, -71.536961), // Plaza de Armas
        LatLng(-16.393244, -71.538948), // Umacollo
        LatLng(-16.395830, -71.529330)  // Selva Alegre
    )

    val rutaCiclovia = listOf(
        LatLng(-16.403350, -71.536550),
        LatLng(-16.403880, -71.534890),
        LatLng(-16.405200, -71.535300),
        LatLng(-16.404600, -71.537000),
        LatLng(-16.403350, -71.536550)
    )

    // Polilínea
    val rutaPolilinea = locations

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Marcador principal en Arequipa
            Marker(
                state = rememberMarkerState(position = initialLocation),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                title = "Arequipa, Perú"
            )

            // Múltiples marcadores
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

            // Dibujar polígonos
            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color.Red,
                fillColor = Color(0x80FF9800), // Naranja semi-transparente
                strokeWidth = 5f
            )

            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color.Red,
                fillColor = Color(0x804CAF50), // Verde semi-transparente
                strokeWidth = 5f
            )

            Polygon(
                points = plazaDeArmasPolygon,
                strokeColor = Color.Red,
                fillColor = Color(0x803498DB), // Azul semi-transparente
                strokeWidth = 5f
            )

            // Dibujar polilínea
            Polyline(
                points = rutaPolilinea,
                color = Color.Blue,
                width = 6f
            )

            Polyline(
                points = rutaTuristica,
                color = Color.Green,
                width = 6f
            )

            Polyline(
                points = rutaCiclovia,
                color = Color.Magenta,
                width = 4f
            )
        }
    }
}