// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.context

import android.app.Activity
import android.content.Context
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@JvmInline
public value class ActivityContext(public val context: Context)

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
