/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.loganalysis.item;

import com.android.loganalysis.item.DumpsysBatteryInfoItem.WakeLockCategory;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Unit test for {@link DumpsysBatteryInfoItem}.
 */
public class DumpsysBatteryInfoItemTest extends TestCase {
    private static final String WAKELOCKS = "WAKELOCKS";
    private static final String LAST_CHARGE_KERNEL_WAKELOCKS = "LAST_CHARGE_KERNEL_WAKELOCKS";
    private static final String LAST_UNPLUGGED_WAKELOCKS = "LAST_UNPLUGGED_WAKELOCKS";
    private static final String LAST_UNPLUGGED_KERNEL_WAKELOCKS = "LAST_UNPLUGGED_KERNEL_WAKELOCKS";
    /**
     * Test that {@link DumpsysBatteryInfoItem#toJson()} returns correctly.
     */
    public void testToJson() throws JSONException {
        DumpsysBatteryInfoItem item = new DumpsysBatteryInfoItem();
        item.addWakeLock("a", 0, 1, WakeLockCategory.LAST_UNPLUGGED_KERNEL_WAKELOCK);
        item.addWakeLock("b", 2, 3, WakeLockCategory.LAST_UNPLUGGED_KERNEL_WAKELOCK);
        item.addWakeLock("c", 4, 5, 6, WakeLockCategory.LAST_UNPLUGGED_WAKELOCK);
        item.addWakeLock("d", 7, 8, 9, WakeLockCategory.LAST_UNPLUGGED_WAKELOCK);
        item.addWakeLock("e", 10, 11, 12, WakeLockCategory.LAST_UNPLUGGED_WAKELOCK);
        item.addWakeLock("w", 0, 1, WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK);
        item.addWakeLock("v", 2, 3, WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK);
        item.addWakeLock("x", 4, 5, 6, WakeLockCategory.LAST_CHARGE_WAKELOCK);
        item.addWakeLock("y", 7, 8, 9, WakeLockCategory.LAST_CHARGE_WAKELOCK);
        item.addWakeLock("z", 10, 11, 12, WakeLockCategory.LAST_CHARGE_WAKELOCK);

        // Convert to JSON string and back again
        JSONObject output = new JSONObject(item.toJson().toString());

        assertTrue(output.has(WAKELOCKS));
        assertTrue(output.get(WAKELOCKS) instanceof JSONArray);

        JSONArray wakeLocks = output.getJSONArray(WAKELOCKS);
        assertEquals(10, wakeLocks.length());
        assertTrue(wakeLocks.get(0) instanceof JSONObject);

        JSONObject wakeLock = wakeLocks.getJSONObject(0);
        assertEquals("a", wakeLock.get(DumpsysBatteryInfoItem.WakeLock.NAME));
        assertFalse(wakeLock.has(DumpsysBatteryInfoItem.WakeLock.NUMBER));
        assertEquals(0, wakeLock.get(DumpsysBatteryInfoItem.WakeLock.HELD_TIME));
        assertEquals(1, wakeLock.get(DumpsysBatteryInfoItem.WakeLock.LOCKED_COUNT));
        assertEquals(WakeLockCategory.LAST_UNPLUGGED_KERNEL_WAKELOCK.toString(),
                wakeLock.get(DumpsysBatteryInfoItem.WakeLock.CATEGORY));
    }
}
