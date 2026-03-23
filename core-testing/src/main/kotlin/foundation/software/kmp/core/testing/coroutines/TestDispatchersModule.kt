// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.testing.coroutines

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.coroutines.DefaultDispatcher
import foundation.software.kmp.core.coroutines.DispatchersModule
import foundation.software.kmp.core.coroutines.IoDispatcher
import foundation.software.kmp.core.coroutines.MainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
@ContributesTo(
  scope = AppScope::class,
  replaces = [DispatchersModule::class]
)
public interface TestDispatchersModule {
  @Provides
  @SingleIn(AppScope::class)
  public fun provideTestDispatcher(): TestDispatcher = UnconfinedTestDispatcher()

  @Provides
  @SingleIn(AppScope::class)
  public fun provideMainDispatcher(testDispatcher: TestDispatcher): MainDispatcher = MainDispatcher(testDispatcher)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideIoDispatcher(testDispatcher: TestDispatcher): IoDispatcher = IoDispatcher(testDispatcher)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideDefaultDispatcher(testDispatcher: TestDispatcher): DefaultDispatcher = DefaultDispatcher(testDispatcher)
}
