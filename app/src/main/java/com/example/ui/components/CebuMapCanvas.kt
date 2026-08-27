package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.GigEntity
import com.example.ui.theme.CatAudition
import com.example.ui.theme.CatCollab
import com.example.ui.theme.CatCoverBand
import com.example.ui.theme.CatMarchingBand
import com.example.ui.theme.CatSession
import com.example.ui.theme.MapHighwayDark
import com.example.ui.theme.MapHighwayLight
import com.example.ui.theme.MapLandDark
import com.example.ui.theme.MapLandLight
import com.example.ui.theme.MapParkDark
import com.example.ui.theme.MapParkLight
import com.example.ui.theme.MapRoadDark
import com.example.ui.theme.MapRoadLight
import com.example.ui.theme.MapWaterDark
import com.example.ui.theme.MapWaterLight
import kotlin.math.pow
import kotlin.math.sqrt

data class CebuLandmark(
    val name: String,
    val x: Float, // Normalized 0..1
    val y: Float
)

val CEBU_LANDMARKS = listOf(
    CebuLandmark("Fuente Osmeña Circle", 0.43f, 0.44f),
    CebuLandmark("IT Park, Lahug", 0.52f, 0.28f),
    CebuLandmark("Ayala Center Cebu", 0.54f, 0.38f),
    CebuLandmark("Guadalupe", 0.38f, 0.38f),
    CebuLandmark("Colon Street / Downtown", 0.45f, 0.52f),
    CebuLandmark("SM Seaside SRP", 0.35f, 0.65f),
    CebuLandmark("Mandaue City", 0.65f, 0.30f),
    CebuLandmark("Lapu-Lapu / Mactan", 0.78f, 0.42f),
    CebuLandmark("Talisay Seaside", 0.26f, 0.72f),
    CebuLandmark("Talamban", 0.58f, 0.18f),
    CebuLandmark("Busay / Tops Cebu", 0.32f, 0.18f)
)

fun getNearestLandmark(x: Float, y: Float): String {
    return CEBU_LANDMARKS.minByOrNull {
        val dx = it.x - x
        val dy = it.y - y
        dx * dx + dy * dy
    }?.name ?: "Cebu City Area"
}

@Composable
fun CebuMapCanvas(
    modifier: Modifier = Modifier,
    gigs: List<GigEntity> = emptyList(),
    selectedGig: GigEntity? = null,
    onGigSelected: ((GigEntity) -> Unit)? = null,
    isPinDropMode: Boolean = false,
    dropPinPos: Pair<Float, Float> = Pair(0.45f, 0.42f),
    onPinDropped: ((Float, Float) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val waterColor = if (isDark) MapWaterDark else MapWaterLight
    val landColor = if (isDark) MapLandDark else MapLandLight
    val roadColor = if (isDark) MapRoadDark else MapRoadLight
    val highwayColor = if (isDark) MapHighwayDark else MapHighwayLight
    val parkColor = if (isDark) MapParkDark else MapParkLight

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.85f, 3.5f)
                    offsetX = (offsetX + pan.x).coerceIn(-400f * scale, 400f * scale)
                    offsetY = (offsetY + pan.y).coerceIn(-400f * scale, 400f * scale)
                }
            }
            .pointerInput(isPinDropMode, gigs) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()

                    // Convert screen tap to normalized map coords
                    val normX = ((tapOffset.x - offsetX) / (w * scale)).coerceIn(0.05f, 0.95f)
                    val normY = ((tapOffset.y - offsetY) / (h * scale)).coerceIn(0.05f, 0.95f)

                    if (isPinDropMode) {
                        onPinDropped?.invoke(normX, normY)
                    } else {
                        // Check if tapped near any gig marker
                        val tappedGig = gigs.minByOrNull { gig ->
                            val gx = (gig.posX * w * scale) + offsetX
                            val gy = (gig.posY * h * scale) + offsetY
                            val dist = sqrt((tapOffset.x - gx).pow(2) + (tapOffset.y - gy).pow(2))
                            dist
                        }
                        if (tappedGig != null) {
                            val gx = (tappedGig.posX * w * scale) + offsetX
                            val gy = (tappedGig.posY * h * scale) + offsetY
                            val dist = sqrt((tapOffset.x - gx).pow(2) + (tapOffset.y - gy).pow(2))
                            if (dist < 40 * density) {
                                onGigSelected?.invoke(tappedGig)
                            }
                        }
                    }
                }
            }
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background Water
            drawRect(color = waterColor, size = size)

            // Draw Cebu Main Island Landmass
            val landPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.72f, 0f)
                cubicTo(w * 0.68f, h * 0.20f, w * 0.62f, h * 0.40f, w * 0.52f, h * 0.55f)
                cubicTo(w * 0.45f, h * 0.65f, w * 0.32f, h * 0.80f, w * 0.20f, h)
                lineTo(0f, h)
                close()
            }

            // Draw Mactan Island
            val mactanPath = Path().apply {
                moveTo(w * 0.68f, h * 0.32f)
                cubicTo(w * 0.85f, h * 0.28f, w * 0.95f, h * 0.42f, w * 0.90f, h * 0.58f)
                cubicTo(w * 0.80f, h * 0.65f, w * 0.68f, h * 0.55f, w * 0.66f, h * 0.42f)
                close()
            }

            // Draw Olango Island outline
            val olangoPath = Path().apply {
                moveTo(w * 0.94f, h * 0.48f)
                lineTo(w * 0.98f, h * 0.52f)
                lineTo(w * 0.95f, h * 0.60f)
                close()
            }

            drawContext.canvas.save()
            drawContext.canvas.translate(offsetX, offsetY)
            drawContext.canvas.scale(scale, scale)

            // Draw Land
            drawPath(landPath, color = landColor)
            drawPath(mactanPath, color = landColor)
            drawPath(olangoPath, color = landColor)

            // Draw Green Parks & Mountain Reserve
            val mountainParkPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.35f, 0f)
                cubicTo(w * 0.30f, h * 0.22f, w * 0.25f, h * 0.35f, w * 0.15f, h * 0.50f)
                lineTo(0f, h * 0.50f)
                close()
            }
            drawPath(mountainParkPath, color = parkColor)

            // Draw Primary Roads & Expressways
            val roadStroke = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val highwayStroke = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val bridgeStroke = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)

            // SRP Coastal Road (South Road Properties)
            val srpRoad = Path().apply {
                moveTo(w * 0.20f, h * 0.90f)
                cubicTo(w * 0.30f, h * 0.75f, w * 0.38f, h * 0.65f, w * 0.44f, h * 0.56f)
            }
            drawPath(srpRoad, color = highwayColor, style = highwayStroke)

            // CCLEX Bridge (Cebu to Cordova)
            val cclexBridge = Path().apply {
                moveTo(w * 0.42f, h * 0.62f)
                cubicTo(w * 0.52f, h * 0.64f, w * 0.62f, h * 0.62f, w * 0.72f, h * 0.58f)
            }
            drawPath(cclexBridge, color = Color(0xFF00D1FF), style = bridgeStroke)

            // Mactan 1st & 2nd Bridges
            val mactanBridge1 = Path().apply {
                moveTo(w * 0.62f, h * 0.34f)
                lineTo(w * 0.68f, h * 0.36f)
            }
            val mactanBridge2 = Path().apply {
                moveTo(w * 0.64f, h * 0.40f)
                lineTo(w * 0.70f, h * 0.42f)
            }
            drawPath(mactanBridge1, color = highwayColor, style = bridgeStroke)
            drawPath(mactanBridge2, color = highwayColor, style = bridgeStroke)

            // Osmeña Blvd & Colon Network
            val arterialRoads = Path().apply {
                // Osmeña Blvd to Fuente
                moveTo(w * 0.45f, h * 0.52f)
                lineTo(w * 0.43f, h * 0.44f)
                // Fuente to Capitol / Guadalupe
                lineTo(w * 0.38f, h * 0.38f)
                // Fuente to Lahug / IT Park
                moveTo(w * 0.43f, h * 0.44f)
                cubicTo(w * 0.48f, h * 0.38f, w * 0.50f, h * 0.32f, w * 0.52f, h * 0.28f)
                // IT Park to Banilad / Talamban
                lineTo(w * 0.58f, h * 0.18f)
                // Ayala / Gorordo link
                moveTo(w * 0.43f, h * 0.44f)
                lineTo(w * 0.54f, h * 0.38f)
                lineTo(w * 0.62f, h * 0.34f)
                // Colon to Mandaue
                moveTo(w * 0.45f, h * 0.52f)
                cubicTo(w * 0.52f, h * 0.48f, w * 0.58f, h * 0.40f, w * 0.65f, h * 0.30f)
            }
            drawPath(arterialRoads, color = roadColor, style = roadStroke)

            // Draw Landmark Dots
            CEBU_LANDMARKS.forEach { landmark ->
                val lx = landmark.x * w
                val ly = landmark.y * h
                drawCircle(
                    color = if (isDark) Color(0xFF6C797F) else Color(0xFFBBC9CF),
                    radius = 3.5f,
                    center = Offset(lx, ly)
                )
            }

            // If selected gig has pulse, draw pulse ring
            selectedGig?.let { gig ->
                val px = gig.posX * w
                val py = gig.posY * h
                drawCircle(
                    color = CatCoverBand.copy(alpha = pulseAlpha),
                    radius = pulseRadius * density,
                    center = Offset(px, py)
                )
            }

            drawContext.canvas.restore()
        }

        // Overlay Interactive Pins
        if (!isPinDropMode) {
            gigs.forEach { gig ->
                val isSelected = selectedGig?.id == gig.id
                val style = when (gig.category.lowercase()) {
                    "marching band", "marching" -> CatMarchingBand
                    "cover band", "cover" -> CatCoverBand
                    "session", "session musician" -> CatSession
                    "audition" -> CatAudition
                    "collab" -> CatCollab
                    else -> CatCoverBand
                }

                val iconVector = when (gig.category.lowercase()) {
                    "marching band", "marching" -> Icons.Default.Audiotrack
                    "cover band", "cover" -> Icons.Default.Group
                    "session" -> Icons.Default.Person
                    "audition" -> Icons.Default.Mic
                    "collab" -> Icons.Default.Handshake
                    else -> Icons.Default.Group
                }

                MapMarkerView(
                    gig = gig,
                    isSelected = isSelected,
                    markerColor = style,
                    icon = iconVector,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    containerWidth = containerWidthPx,
                    containerHeight = containerHeightPx,
                    onClick = { onGigSelected?.invoke(gig) }
                )
            }
        } else {
            // Render Draggable / Tapped Pin for Post a Gig Screen
            DraggablePinOverlay(
                posX = dropPinPos.first,
                posY = dropPinPos.second,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                containerWidth = containerWidthPx,
                containerHeight = containerHeightPx
            )
        }
    }
}

@Composable
fun MapMarkerView(
    gig: GigEntity,
    isSelected: Boolean,
    markerColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    containerWidth: Float,
    containerHeight: Float,
    onClick: () -> Unit
) {
    val sizeDp = if (isSelected) 40.dp else 32.dp
    val elevationDp = if (isSelected) 8.dp else 3.dp

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset {
                    val targetX = (gig.posX * containerWidth * scale) + offsetX - (sizeDp.toPx() / 2f)
                    val targetY = (gig.posY * containerHeight * scale) + offsetY - (sizeDp.toPx() / 2f)
                    IntOffset(targetX.toInt(), targetY.toInt())
                }
                .size(sizeDp)
                .bounceClickable(onClick = onClick)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = markerColor,
                shadowElevation = elevationDp,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.5.dp, Color.White) else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = gig.category,
                        tint = Color.White,
                        modifier = Modifier.size(if (isSelected) 20.dp else 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DraggablePinOverlay(
    posX: Float,
    posY: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    containerWidth: Float,
    containerHeight: Float
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset {
                    val pinSize = 44.dp.toPx()
                    val targetX = (posX * containerWidth * scale) + offsetX - (pinSize / 2f)
                    val targetY = (posY * containerHeight * scale) + offsetY - pinSize
                    IntOffset(targetX.toInt(), targetY.toInt())
                }
                .size(44.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Dropped Location Pin",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
