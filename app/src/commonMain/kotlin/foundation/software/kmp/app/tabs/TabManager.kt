// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app.tabs

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.app.HomeRoute
import foundation.software.kmp.app.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TabState(
  val id: String,
  val backStack: List<Route>,
)

data class WindowState(
  val id: String,
  val tabs: List<TabState>,
  val activeTabId: String?,
)

interface TabManager {
  val defaultNewTabRoute: Route
  val defaultEmptyWindowRoute: Route

  val windows: StateFlow<List<WindowState>>

  fun createWindow(initialTab: TabState? = null): String
  fun closeWindow(windowId: String)

  fun createTab(windowId: String, initialRoute: Route = defaultNewTabRoute): String
  fun closeTab(windowId: String, tabId: String)
  fun setActiveTab(windowId: String, tabId: String)

  fun moveTabToWindow(tabId: String, fromWindowId: String, toWindowId: String, index: Int = -1)
  fun moveTabToNewWindow(tabId: String, fromWindowId: String): String

  fun updateTabBackStack(tabId: String, backStack: List<Route>)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultTabManager : TabManager {
  private val _windows = MutableStateFlow<List<WindowState>>(emptyList())
  override val windows: StateFlow<List<WindowState>> = _windows.asStateFlow()

  override val defaultNewTabRoute: Route = HomeRoute
  override val defaultEmptyWindowRoute: Route = HomeRoute

  private var nextWindowId = 1
  private var nextTabId = 1

  override fun createWindow(initialTab: TabState?): String {
    val windowId = "window_${nextWindowId++}"
    val tab = initialTab ?: TabState(id = "tab_${nextTabId++}", backStack = listOf(defaultNewTabRoute))

    _windows.update { currentWindows ->
      currentWindows + WindowState(
        id = windowId,
        tabs = listOf(tab),
        activeTabId = tab.id
      )
    }
    return windowId
  }

  override fun closeWindow(windowId: String) {
    _windows.update { currentWindows ->
      currentWindows.filter { it.id != windowId }
    }
  }

  override fun createTab(windowId: String, initialRoute: Route): String {
    val tabId = "tab_${nextTabId++}"
    val newTab = TabState(id = tabId, backStack = listOf(initialRoute))

    _windows.update { currentWindows ->
      currentWindows.map { window ->
        if (window.id == windowId) {
          window.copy(
            tabs = window.tabs + newTab,
            activeTabId = tabId // Set newly created tab as active
          )
        } else {
          window
        }
      }
    }
    return tabId
  }

  override fun closeTab(windowId: String, tabId: String) {
    _windows.update { currentWindows ->
      currentWindows.map { window ->
        if (window.id == windowId) {
          val newTabs = window.tabs.filter { it.id != tabId }
          val newActiveTabId = if (window.activeTabId == tabId) {
            newTabs.lastOrNull()?.id
          } else {
            window.activeTabId
          }
          window.copy(tabs = newTabs, activeTabId = newActiveTabId)
        } else {
          window
        }
      }
    }
  }

  override fun setActiveTab(windowId: String, tabId: String) {
    _windows.update { currentWindows ->
      currentWindows.map { window ->
        if (window.id == windowId && window.tabs.any { it.id == tabId }) {
          window.copy(activeTabId = tabId)
        } else {
          window
        }
      }
    }
  }

  override fun moveTabToWindow(tabId: String, fromWindowId: String, toWindowId: String, index: Int) {
    _windows.update { currentWindows ->
      val fromWindow = currentWindows.find { it.id == fromWindowId } ?: return@update currentWindows
      val tabToMove = fromWindow.tabs.find { it.id == tabId } ?: return@update currentWindows

      currentWindows.map { window ->
        when (window.id) {
          fromWindowId -> {
            val newTabs = window.tabs.filter { it.id != tabId }
            val newActiveTabId = if (window.activeTabId == tabId) newTabs.lastOrNull()?.id else window.activeTabId
            window.copy(tabs = newTabs, activeTabId = newActiveTabId)
          }
          toWindowId -> {
            val newTabs = window.tabs.toMutableList()
            if (index in 0..newTabs.size) {
              newTabs.add(index, tabToMove)
            } else {
              newTabs.add(tabToMove)
            }
            // Make the moved tab active in the new window
            window.copy(tabs = newTabs, activeTabId = tabId)
          }
          else -> window
        }
      }
    }
  }

  override fun moveTabToNewWindow(tabId: String, fromWindowId: String): String {
    var newWindowId = ""
    _windows.update { currentWindows ->
      val fromWindow = currentWindows.find { it.id == fromWindowId } ?: return@update currentWindows
      val tabToMove = fromWindow.tabs.find { it.id == tabId } ?: return@update currentWindows

      val updatedWindows = currentWindows.map { window ->
        if (window.id == fromWindowId) {
          val newTabs = window.tabs.filter { it.id != tabId }
          val newActiveTabId = if (window.activeTabId == tabId) newTabs.lastOrNull()?.id else window.activeTabId
          window.copy(tabs = newTabs, activeTabId = newActiveTabId)
        } else {
          window
        }
      }

      newWindowId = "window_${nextWindowId++}"
      updatedWindows + WindowState(
        id = newWindowId,
        tabs = listOf(tabToMove),
        activeTabId = tabId
      )
    }
    return newWindowId
  }

  override fun updateTabBackStack(tabId: String, backStack: List<Route>) {
    _windows.update { currentWindows ->
      currentWindows.map { window ->
        if (window.tabs.any { it.id == tabId }) {
          val newTabs = window.tabs.map { tab ->
            if (tab.id == tabId) {
              tab.copy(backStack = backStack)
            } else {
              tab
            }
          }
          window.copy(tabs = newTabs)
        } else {
          window
        }
      }
    }
  }
}
