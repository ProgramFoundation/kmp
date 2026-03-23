// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.preferences

import android.content.SharedPreferences
import androidx.annotation.Keep
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Keep
public inline fun <reified T> SharedPreferences.delegate(
  defaultValue: T,
): ReadWriteProperty<Any, T> =
  object : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
      val key = "${thisRef::class.qualifiedName}.${property.name}"
      return when (T::class) {
        String::class -> getString(key, defaultValue as String?) as T
        Int::class -> getInt(key, defaultValue as Int) as T
        Boolean::class -> getBoolean(key, defaultValue as Boolean) as T
        Float::class -> getFloat(key, defaultValue as Float) as T
        Long::class -> getLong(key, defaultValue as Long) as T
        Set::class -> getStringSet(key, defaultValue as Set<String>?) as T
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
      }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
      val key = "${thisRef::class.qualifiedName}.${property.name}"
      with(edit()) {
        when (T::class) {
          String::class -> putString(key, value as String?)
          Int::class -> putInt(key, value as Int)
          Boolean::class -> putBoolean(key, value as Boolean)
          Float::class -> putFloat(key, value as Float)
          Long::class -> putLong(key, value as Long)
          Set::class -> putStringSet(key, value as Set<String>?)
          else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
        }
        apply()
      }
    }
  }
