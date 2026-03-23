// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.context

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@JvmInline
public value class ApplicationContext(public val context: Context)

public annotation class ActivityScope

@DependencyGraph(ActivityScope::class)
public interface ActivityGraph {
  public val activityContext: ActivityContext

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(@Provides activityContext: ActivityContext): ActivityGraph
  }
}

@JvmInline
public value class ActivityContext(public val context: Context)

public annotation class VirtualDeviceScope

@DependencyGraph(VirtualDeviceScope::class)
public interface VirtualDeviceGraph {
  public val virtualDeviceContext: VirtualDeviceContext

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(@Provides virtualDeviceContext: VirtualDeviceContext): VirtualDeviceGraph
  }
}

@JvmInline
public value class VirtualDeviceContext(public val context: Context)

public annotation class WindowScope

@DependencyGraph(WindowScope::class)
public interface WindowGraph {
  public val windowContext: WindowContext

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(@Provides windowContext: WindowContext): WindowGraph
  }
}

@JvmInline
public value class WindowContext(public val context: Context)
