// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app.tabs.dragdrop

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent

private const val MIME_TYPE_TAB_ID = "application/vnd.foundation.software.kmp.tab"

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.tabDragSource(tabId: String, windowId: String): Modifier = this.dragAndDropSource { _ ->
  val clipData = ClipData(
    ClipDescription("Tab", arrayOf(MIME_TYPE_TAB_ID)),
    ClipData.Item(tabId)
  )
  // Add window ID to distinguish local vs remote drops if needed
  clipData.addItem(ClipData.Item(windowId))

  DragAndDropTransferData(
    clipData = clipData,
    flags = android.view.View.DRAG_FLAG_GLOBAL
  )
}

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.tabDropTarget(
  onTabDropped: (tabId: String, fromWindowId: String) -> Unit
): Modifier = this.dragAndDropTarget(
  shouldStartDragAndDrop = { event ->
    val androidEvent = event.toAndroidDragEvent()
    androidEvent.clipDescription?.hasMimeType(MIME_TYPE_TAB_ID) == true
  },
  target = object : DragAndDropTarget {
    override fun onDrop(event: DragAndDropEvent): Boolean {
      val androidEvent = event.toAndroidDragEvent()
      val clipData = androidEvent.clipData ?: return false

      if (clipData.itemCount >= 2) {
        val tabId = clipData.getItemAt(0).text.toString()
        val fromWindowId = clipData.getItemAt(1).text.toString()
        onTabDropped(tabId, fromWindowId)
        return true
      }
      return false
    }
  }
)
