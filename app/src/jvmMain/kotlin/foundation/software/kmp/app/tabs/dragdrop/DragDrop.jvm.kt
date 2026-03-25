// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app.tabs.dragdrop

import androidx.compose.ui.Modifier

// Drag and drop not fully supported on JVM for multi-window yet
actual fun Modifier.tabDragSource(tabId: String, windowId: String): Modifier = this

actual fun Modifier.tabDropTarget(
  onTabDropped: (tabId: String, fromWindowId: String) -> Unit
): Modifier = this
