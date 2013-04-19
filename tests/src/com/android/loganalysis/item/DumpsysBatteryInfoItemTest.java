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

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Unit test for {@link DumpsysBatteryInfoItem}.
 */
public class DumpsysBatteryInfoItemTest extends TestCase {
    /**
     * Test that {@link DumpsysBatteryInfoItem#toJson()} returns correctly.
     */
    public void testToJson() throws JSONException {
        DumpsysBatteryInfoItem item = new DumpsysBatteryInfoItem();
        item.addLastUnpluggedKernelWakeLock("a", 0, 1);
        item.addLastUnpluggedKernelWakeLock("b", 2, 3);
        item.addLastUnpluggedWakeLock("x", 4, 5, 6);
        item.addLastUnpluggedWakeLock("y", 7, 8, 9);
        item.addLastUnpluggedWakeLock("z", 10, 11, 12);

        // Convert to JSON string and back again
        JSONObject output = new JSONObject(item.toJson().toString());

        assertTrue(output.has(DumpsysBatteryInfoItem.KERNEL_WAKELOCKS));
        assertTrue(output.get(DumpsysBatteryInfoItem.KERNEL_WAKELOCKS) instanceof JSONArray);
        assertTrue(output.has(DumpsysBatteryInfoItem.WAKELOCKS));
        assertTrue(output.get(DumpsysBatteryInfoItem.WAKELOCKS) instanceof JSONArray);

        JSONArray kernelWakeLocks = output.getJSONArray(DumpsysBatteryInfoItem.KERNEL_WAKELOCKS);

        assertEquals(2, kernelWakeLocks.length());
        assertTrue(kernelWakeLocks.get(0) instanceof JSONObject);

        JSONObject kernelWakeLock = kernelWakeLocks.getJSONObject(0);

        assertEquals("a", kernelWakeLock.get(DumpsysBatteryInfoItem.WakeLock.NAME));
        assertFalse(kernelWakeLock.has(DumpsysBatteryInfoItem.WakeLock.NUMBER));
        assertEquals(0, kernelWakeLock.get(DumpsysBatteryInfoItem.WakeLock.HELD_TIME));
        assertEquals(1, kernelWakeLock.get(DumpsysBatteryInfoItem.WakeLock.LOCKED_COUNT));

        JSONArray wakeLocks = output.getJSONArray(DumpsysBatteryInfoItem.WAKELOCKS);
        assertEquals(3, wakeLocks.length());
        assertTrue(wakeLocks.get(0) instanceof JSONObject);

        JSONObject wakeLock = wakeLocks.getJSONObject(0);

        assertEquals("x", wakeLock.get(DumpsysBatteryInfoItem.WakeLock.NAME));
        assertEquals(4, wakeLock.get(DumpsysBatteryInfoItem.WakeLock.NUMBER));
        assertEquals(5, wakeLock.get(DumpsysBatteryInfoItem.WakeLock.HELD_TIME));
        assertEquals(6, wakeLock.get(DumpsysBatteryInfoItem.WakeLock.LOCKED_COUNT));
    }
}
