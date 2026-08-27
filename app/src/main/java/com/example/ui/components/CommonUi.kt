package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CatAudition
import com.example.ui.theme.CatAuditionContainer
import com.example.ui.theme.CatCollab
import com.example.ui.theme.CatCollabContainer
import com.example.ui.theme.CatCoverBand
import com.example.ui.theme.CatCoverBandContainer
import com.example.ui.theme.CatMarchingBand
import com.example.ui.theme.CatMarchingBandContainer
import com.example.ui.theme.CatSession
import com.example.ui.theme.CatSessionContainer

/**
 * Interactive tap animation modifier with spring physics and tactile haptic feedback.
 */
fun Modifier.bounceClickable(
    enabled: Boolean = true,
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "BounceScale"
    )
    val view = LocalView.current

    this
        .scale(scale)
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Returns consistent Category Color & Container pairs
 */
data class CategoryStyle(
    val color: Color,
    val containerColor: Color,
    val icon: ImageVector,
    val label: String
)

fun getCategoryStyle(category: String): CategoryStyle {
    return when (category.lowercase()) {
        "marching band", "marching" -> CategoryStyle(
            color = CatMarchingBand,
            containerColor = CatMarchingBandContainer,
            icon = Icons.Default.Audiotrack,
            label = "Marching Band"
        )
        "cover band", "cover" -> CategoryStyle(
            color = CatCoverBand,
            containerColor = CatCoverBandContainer,
            icon = Icons.Default.Group,
            label = "Cover Band"
        )
        "session", "session musician" -> CategoryStyle(
            color = CatSession,
            containerColor = CatSessionContainer,
            icon = Icons.Default.Person,
            label = "Session"
        )
        "audition", "auditions" -> CategoryStyle(
            color = CatAudition,
            containerColor = CatAuditionContainer,
            icon = Icons.Default.Mic,
            label = "Audition"
        )
        "collab", "collaboration" -> CategoryStyle(
            color = CatCollab,
            containerColor = CatCollabContainer,
            icon = Icons.Default.Handshake,
            label = "Collab"
        )
        else -> CategoryStyle(
            color = CatCoverBand,
            containerColor = CatCoverBandContainer,
            icon = Icons.Default.Group,
            label = category
        )
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier,
    isFilled: Boolean = false
) {
    val style = getCategoryStyle(category)
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    val badgeBg = if (isDark) {
        style.color.copy(alpha = 0.2f)
    } else {
        style.containerColor
    }

    val textColor = if (isDark) {
        style.color
    } else {
        style.color
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeBg)
            .border(0.5.dp, style.color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Box(modifier = Modifier.size(4.dp))
        Text(
            text = if (isFilled) "${style.label} (Filled)" else style.label.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.05.sp
            )
        )
    }
}
