// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.user

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.RequiresApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@RequiresApi(Build.VERSION_CODES.R)
@SingleIn(AppScope::class)
public class UserObserver @dev.zacsweers.metro.Inject constructor(
  private val userManager: UserManager,
  private val applicationContext: ApplicationContext,
  private val userGraphFactory: UserGraph.Factory,
) {
  private val _usersFlow: MutableStateFlow<Set<UserGraph>>

  init {
    val graphs = userManager.getUserHandles(/* excludeDying= */ true)
      .map { userGraphFactory.create(it) }
      .toSet()
    _usersFlow = MutableStateFlow(graphs)
  }

  public val usersFlow: StateFlow<Set<UserGraph>> = _usersFlow.asStateFlow()

  public fun startObserving() {
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val userHandle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableExtra(Intent.EXTRA_USER, UserHandle::class.java)
        } else {
          @Suppress("DEPRECATION")
          intent.getParcelableExtra(Intent.EXTRA_USER)
        } ?: return
        when (intent.action) {
          Intent.ACTION_USER_ADDED ->
            _usersFlow.value = _usersFlow.value + userGraphFactory.create(userHandle)
          Intent.ACTION_USER_REMOVED -> {
            val removed = _usersFlow.value.find { it.userHandle == userHandle } ?: return
            removed.coroutineScope.cancel("User removed", Exception("Cancelled by UserObserver"))
            _usersFlow.value = _usersFlow.value - removed
          }
        }
      }
    }

    applicationContext.context.registerReceiver(receiver, IntentFilter().apply {
      addAction(Intent.ACTION_USER_ADDED)
      addAction(Intent.ACTION_USER_REMOVED)
    })
  }
}
