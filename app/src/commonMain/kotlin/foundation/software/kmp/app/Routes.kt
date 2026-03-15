// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable data object HomeRoute : Route

@Serializable data class DetailsRoute(val data: String) : Route

@Serializable data class SettingsRoute(val userId: String) : Route
