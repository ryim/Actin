package com.ryim.actin.ui.ReusableComposables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    gapAbove: Boolean = true,
    padding: Int = 16) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column {
            if (gapAbove) {
                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = padding.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = padding.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

