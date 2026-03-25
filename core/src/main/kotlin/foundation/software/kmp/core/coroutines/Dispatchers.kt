// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.coroutines

import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@JvmInline
public value class MainDispatcher(public val dispatcher: CoroutineDispatcher)

@JvmInline
public value class IoDispatcher(public val dispatcher: CoroutineDispatcher)

@JvmInline
public value class DefaultDispatcher(public val dispatcher: CoroutineDispatcher)

@JvmInline
public value class ApplicationCoroutineScope(public val coroutineScope: kotlinx.coroutines.CoroutineScope)

@dev.zacsweers.metro.ContributesTo(AppScope::class)
public interface DispatchersModule {
  @Provides
  @SingleIn(AppScope::class)
  public fun provideMainDispatcher(): MainDispatcher = MainDispatcher(Dispatchers.Main)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideIoDispatcher(): IoDispatcher = IoDispatcher(Dispatchers.IO)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideDefaultDispatcher(): DefaultDispatcher = DefaultDispatcher(Dispatchers.Default)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideApplicationCoroutineScope(defaultDispatcher: DefaultDispatcher): ApplicationCoroutineScope =
    ApplicationCoroutineScope(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + defaultDispatcher.dispatcher))
}
