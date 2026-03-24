// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.user

import android.accounts.AccountManager
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.context.ApplicationContext
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@JvmInline
public value class UserContext(public val context: Context)

public annotation class UserScope

@RequiresApi(Build.VERSION_CODES.R)
@GraphExtension(UserScope::class)
public interface UserGraph {
  public val userHandle: UserHandle
  public val userContext: UserContext
  public val packageManager: PackageManager
  public val accountManager: AccountManager
  public val launcherApps: LauncherApps
  public val coroutineScope: CoroutineScope

  @Provides
  public fun provideUserContext(applicationContext: ApplicationContext, userHandle: UserHandle): UserContext =
    UserContext(applicationContext.context.createContextAsUser(userHandle, 0))

  @Provides
  public fun providePackageManager(userContext: UserContext): PackageManager =
    userContext.context.packageManager

  @Provides
  public fun provideAccountManager(userContext: UserContext): AccountManager =
    userContext.context.getSystemService(AccountManager::class.java)

  @Provides
  public fun provideLauncherApps(userContext: UserContext): LauncherApps =
    userContext.context.getSystemService(LauncherApps::class.java)

  @Provides
  public fun provideCoroutineScope(userHandle: UserHandle, ioDispatcher: IoDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName("user-$userHandle"))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides userHandle: UserHandle): UserGraph
  }
}
