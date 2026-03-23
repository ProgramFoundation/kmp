// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.context

import android.content.Context

@JvmInline
public value class ApplicationContext(public val context: Context)

@JvmInline
public value class ActivityContext(public val context: Context)

@JvmInline
public value class VirtualDeviceContext(public val context: Context)

@JvmInline
public value class WindowContext(public val context: Context)
