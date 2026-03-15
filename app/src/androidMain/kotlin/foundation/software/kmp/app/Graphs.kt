// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class) interface AppGraph : MetroAppComponentProviders, ViewModelGraph
