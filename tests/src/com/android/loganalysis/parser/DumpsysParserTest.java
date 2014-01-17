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
package com.android.loganalysis.parser;

import com.android.loganalysis.item.DumpsysBatteryInfoItem.WakeLockCategory;
import com.android.loganalysis.item.DumpsysItem;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link DumpsysParser}
 */
public class DumpsysParserTest extends TestCase {

    /**
     * Test that the dumpsys section of the bugreport is parsed.
     */
    public void testParse() {
        List<String> inputBlock = Arrays.asList(
                "-------------------------------------------------------------------------------",
                "DUMP OF SERVICE process1:",
                "-------------------------------------------------------------------------------",
                "DUMP OF SERVICE batteryinfo:",
                "Statistics since last charge:",
                "  Kernel Wake lock \"PowerManagerService.WakeLocks\": 5m 10s 61ms (2 times) realtime",
                "  Kernel Wake lock \"pm8921_eoc\": 9s 660ms (0 times) realtime",
                "",
                "  All partial wake locks:",
                "  Wake lock #0 partialWakelock: 5m 9s 260ms (1 times) realtime",
                "  Wake lock #1000 AlarmManager: 422ms (7 times) realtime",
                "",
                "-------------------------------------------------------------------------------",
                "DUMP OF SERVICE process2:",
                "-------------------------------------------------------------------------------");

        DumpsysItem item = new DumpsysParser().parse(inputBlock);

        assertNotNull(item.getBatteryInfo());
        assertEquals(2, item.getBatteryInfo().getWakeLocks(
                WakeLockCategory.LAST_CHARGE_WAKELOCK).size());
        assertEquals(2, item.getBatteryInfo().getWakeLocks(
                WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK).size());
    }

    /**
     * Test that an empty input returns {@code null}.
     */
    public void testEmptyInput() {
        DumpsysItem item = new DumpsysParser().parse(Arrays.asList(""));
        assertNull(item);
    }
}
