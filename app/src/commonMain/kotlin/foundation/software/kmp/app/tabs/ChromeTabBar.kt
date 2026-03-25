// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import foundation.software.kmp.app.tabs.dragdrop.tabDragSource
import foundation.software.kmp.app.tabs.dragdrop.tabDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ChromeTabBar(
  windowId: String,
  tabs: List<TabState>,
  activeTabId: String?,
  onTabSelected: (String) -> Unit,
  onTabClosed: (String) -> Unit,
  onAddTab: () -> Unit,
  onTabDropped: (tabId: String, fromWindowId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .tabDropTarget { tabId, fromWindowId ->
        onTabDropped(tabId, fromWindowId)
      }
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    tabs.forEach { tab ->
      val isActive = tab.id == activeTabId

      // Tab visuals
      Row(
        modifier = Modifier
          .width(160.dp)
          .padding(end = 4.dp)
          .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
          .background(if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent)
          .clickable { onTabSelected(tab.id) }
          .tabDragSource(tabId = tab.id, windowId = windowId)
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab icon (placeholder)
        Box(modifier = Modifier.size(16.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))

        Spacer(modifier = Modifier.width(8.dp))

        // Tab title
        Text(
          text = "Tab ${tab.id.substringAfter("_")}",
          style = MaterialTheme.typography.labelMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f)
        )

        // Close button
        IconButton(
          onClick = { onTabClosed(tab.id) },
          modifier = Modifier.size(20.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close tab",
            modifier = Modifier.size(16.dp),
            tint = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Add tab button
    IconButton(
      onClick = onAddTab,
      modifier = Modifier.size(32.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "New tab",
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
