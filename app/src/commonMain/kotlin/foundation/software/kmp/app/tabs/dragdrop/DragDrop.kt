// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app.tabs.dragdrop

import androidx.compose.ui.Modifier

// Common modifiers for drag and drop that will have platform-specific implementations
expect fun Modifier.tabDragSource(tabId: String, windowId: String): Modifier

expect fun Modifier.tabDropTarget(
  onTabDropped: (tabId: String, fromWindowId: String) -> Unit
): Modifier
