// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.context

import android.content.Context
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@JvmInline
public value class WindowContext(public val context: Context)

public annotation class WindowScope

@GraphExtension(WindowScope::class)
public interface WindowGraph {
  public val windowContext: WindowContext

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides windowContext: WindowContext): WindowGraph
  }
}
