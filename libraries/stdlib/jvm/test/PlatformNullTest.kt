/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PlatformExtensionReceiverOfInline")

package test.platformNull

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.util.Collections


class PlatformNullTest {

    @Suppress("HasPlatformType", "UNCHECKED_CAST")
    fun <T> platformNull() = Collections.singletonList(null as T).first()

    @Test
    fun stringToBoolean() {
        assertFalse(platformNull<String>().toBoolean())
    }

    @Test
    fun stringEquals() {
        assertFalse(platformNull<String>().equals("sample", ignoreCase = false))
        assertFalse(platformNull<String>().equals("sample", ignoreCase = true))
        assertFalse("sample".equals(platformNull<String>(), ignoreCase = false))
        assertFalse("sample".equals(platformNull(), ignoreCase = true))
        assertTrue(platformNull<String>().equals(platformNull(), ignoreCase = true))
        assertTrue(platformNull<String>().equals(platformNull<String>(), ignoreCase = false))
    }
}