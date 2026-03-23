// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.context

import android.app.Activity
import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@JvmInline
public value class ApplicationContext(public val context: Context)

public annotation class ActivityScope

@GraphExtension(ActivityScope::class)
public interface ActivityGraph {
  public val activity: Activity

  @Provides
  public fun provideActivityContext(activity: Activity): ActivityContext = ActivityContext(activity)

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides activity: Activity): ActivityGraph
  }
}

@JvmInline
public value class ActivityContext(public val context: Context)

@JvmInline
public value class DisplayContext(public val context: Context)

public annotation class WindowScope

@GraphExtension(WindowScope::class)
public interface WindowGraph {
  public val windowContext: WindowContext

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides windowContext: WindowContext): WindowGraph
  }
}

@JvmInline
public value class WindowContext(public val context: Context)
