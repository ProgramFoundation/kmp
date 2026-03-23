// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.preferences

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Creates a [ReadWriteProperty] delegate backed by [SharedPreferences].
 *
 * @param defaultValue The default value returned when the key is not present.
 * @param key An explicit SharedPreferences key. If omitted, a key is derived from the delegating
 *   property's class name and property name — but note that R8/ProGuard may mangle these names in
 *   release builds, causing the key to change across builds. Provide an explicit [key] to guarantee
 *   stability.
 */
public inline fun <reified T> SharedPreferences.delegate(
  defaultValue: T,
  key: String? = null,
): ReadWriteProperty<Any, T> =
  object : ReadWriteProperty<Any, T> {
    private fun resolveKey(thisRef: Any, property: KProperty<*>): String =
      key ?: "${thisRef::class.qualifiedName}.${property.name}"

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
      val k = resolveKey(thisRef, property)
      return when (T::class) {
        String::class -> getString(k, defaultValue as String?) as T
        Int::class -> getInt(k, defaultValue as Int) as T
        Boolean::class -> getBoolean(k, defaultValue as Boolean) as T
        Float::class -> getFloat(k, defaultValue as Float) as T
        Long::class -> getLong(k, defaultValue as Long) as T
        Set::class -> getStringSet(k, defaultValue as Set<String>?) as T
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
      }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
      val k = resolveKey(thisRef, property)
      with(edit()) {
        when (T::class) {
          String::class -> putString(k, value as String?)
          Int::class -> putInt(k, value as Int)
          Boolean::class -> putBoolean(k, value as Boolean)
          Float::class -> putFloat(k, value as Float)
          Long::class -> putLong(k, value as Long)
          Set::class -> putStringSet(k, value as Set<String>?)
          else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
        }
        apply()
      }
    }
  }
