package com.ben0551.visualtimeassistant.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Label + [-] value [+] row, sized for both touch and D-Pad. */
@Composable
fun StepperRow(
    label: String,
    valueText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 64.dp,
) {
    val minusInteraction = remember { MutableInteractionSource() }
    val plusInteraction = remember { MutableInteractionSource() }
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = onMinus,
                interactionSource = minusInteraction,
                modifier = Modifier.size(buttonSize).tvFocusScale(cornerRadius = 16.dp, interactionSource = minusInteraction),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease $label")
            }
            Spacer(Modifier.width(16.dp))
            Text(valueText, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(16.dp))
            FilledTonalButton(
                onClick = onPlus,
                interactionSource = plusInteraction,
                modifier = Modifier.size(buttonSize).tvFocusScale(cornerRadius = 16.dp, interactionSource = plusInteraction),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase $label")
            }
        }
    }
}
