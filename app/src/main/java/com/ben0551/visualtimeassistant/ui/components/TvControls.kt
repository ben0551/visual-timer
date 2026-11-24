package com.ben0551.visualtimeassistant.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Chip tuned for remotes and touch:
 * - no permanent outline (that read as "everything is selected")
 * - tick + filled container when chosen
 * - one unmistakable amber ring while focused
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else {
            null
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Transparent,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 0.dp,
            selectedBorderWidth = 2.dp,
        ),
        modifier = modifier.tvFocusScale(scale = 1.06f, cornerRadius = 12.dp),
    )
}

/**
 * Secondary action without a permanent frame; emphasis comes from the focus ring and
 * Material's pressed/ripple feedback instead of an ever-present outline.
 */
@Composable
fun TvSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.tvFocusScale(cornerRadius = 16.dp),
        enabled = enabled,
        content = content,
    )
}
