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

import com.android.loganalysis.item.DumpsysBatteryInfoItem;
import com.android.loganalysis.item.DumpsysBatteryInfoItem.WakeLock;
import com.android.loganalysis.item.DumpsysBatteryInfoItem.WakeLockCategory;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link DumpsysBatteryInfoParser}
 */
public class DumpsysBatteryInfoParserTest extends TestCase {

    /**
     * Test that complete battery info dumpsys is parsed.
     */
    public void testParse() {
        List<String> inputBlock = Arrays.asList(
                "Battery History:",
                "         -15m07s754ms  START",
                "         -15m05s119ms 088 20080000 status=charging health=good plug=usb temp=269 volt=4358 +plugged +sensor",
                "",
                "Per-PID Stats:",
                "  PID 4242 wake time: +5m10s24ms",
                "  PID 543 wake time: +3s585ms",
                "",
                "Statistics since last charge:",
                "  System starts: 2, currently on battery: false",
                "  Time on battery: 8m 20s 142ms (55.1%) realtime, 5m 17s 5ms (34.9%) uptime",
                "  Kernel Wake lock \"PowerManagerService.WakeLocks\": 5m 10s 61ms (2 times) realtime",
                "  Kernel Wake lock \"pm8921_eoc\": 9s 660ms (0 times) realtime",
                "  ",
                "  All partial wake locks:",
                "  Wake lock #0 partialWakelock: 5m 9s 260ms (1 times) realtime",
                "  Wake lock #1000 AlarmManager: 422ms (7 times) realtime",
                "",
                "Statistics since last unplugged:",
                "  Time on battery: 8m 20s 142ms (92.6%) realtime, 5m 17s 5ms (58.7%) uptime",
                "  Total run time: 8m 59s 968ms realtime, 5m 56s 831ms uptime, ",
                "  Screen on: 0ms (0.0%), Input events: 0, Active phone call: 0ms (0.0%)",
                "  Screen brightnesses: No activity",
                "  Kernel Wake lock \"PowerManagerService.WakeLocks\": 5m 10s 61ms (2 times) realtime",
                "  Kernel Wake lock \"pm8921_eoc\": 9s 660ms (0 times) realtime",
                "  Kernel Wake lock \"main\": 7s 323ms (0 times) realtime",
                "  Total received: 0B, Total sent: 0B",
                "  Total full wakelock time: 0ms , Total partial wakelock time: 5m 10s 60ms ",
                "  Signal levels: No activity",
                "  Signal scanning time: 0ms ",
                "  Radio types: none 8m 20s 142ms (100.0%) 0x",
                "  Radio data uptime when unplugged: 0 ms",
                "  Wifi on: 0ms (0.0%), Wifi running: 0ms (0.0%), Bluetooth on: 0ms (0.0%)",
                " ",
                "  Device is currently plugged into power",
                "    Last discharge cycle start level: 87",
                "    Last discharge cycle end level: 87",
                "    Amount discharged while screen on: 0",
                "    Amount discharged while screen off: 0",
                " ",
                "  All partial wake locks:",
                "  Wake lock #0 partialWakelock: 5m 9s 260ms (1 times) realtime",
                "  Wake lock #1000 AlarmManager: 422ms (7 times) realtime",
                "  Wake lock #1000 show keyguard: 277ms (1 times) realtime",
                "  Wake lock #1000 ActivityManager-Sleep: 72ms (1 times) realtime",
                "  Wake lock #10015 AlarmManager: 16ms (1 times) realtime",
                "",
                "  #0:",
                "    Wake lock partialWakelock: 5m 9s 260ms partial (1 times) realtime",
                "    Proc /init:",
                "      CPU: 10ms usr + 0ms krn",
                "    Proc flush-179:0:",
                "      CPU: 0ms usr + 10ms krn",
                "    Proc vold:",
                "      CPU: 20ms usr + 10ms krn",
                "  #1000:",
                "    User activity: 3 other, 1 button",
                "    Wake lock show keyguard: 277ms partial (1 times) realtime",
                "    Wake lock AlarmManager: 422ms partial (7 times) realtime");

        DumpsysBatteryInfoParser parser = new DumpsysBatteryInfoParser();
        DumpsysBatteryInfoItem item = parser.parse(inputBlock);

        assertEquals(2, item.getWakeLocks(WakeLockCategory.LAST_CHARGE_WAKELOCK).size());
        assertEquals(2, item.getWakeLocks(WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK).size());
        assertEquals(5, item.getWakeLocks(WakeLockCategory.LAST_UNPLUGGED_WAKELOCK).size());
        assertEquals(3, item.getWakeLocks(WakeLockCategory.LAST_UNPLUGGED_KERNEL_WAKELOCK).size());

        assertEquals("partialWakelock",
                item.getWakeLocks(WakeLockCategory.LAST_CHARGE_WAKELOCK).get(0).getName());
        assertEquals("PowerManagerService.WakeLocks",
                item.getWakeLocks(WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK).get(0).getName());
        assertEquals("partialWakelock",
                item.getWakeLocks(WakeLockCategory.LAST_UNPLUGGED_WAKELOCK).get(0).getName());
        assertEquals("PowerManagerService.WakeLocks",
                item.getWakeLocks(WakeLockCategory.LAST_UNPLUGGED_KERNEL_WAKELOCK).get(0).getName());
    }

    /**
     * Test that kernel wakelocks are parsed.
     */
    public void testParseKernelWakeLock() {
        String inputLine = "  Kernel Wake lock \"Process\": 1d 2h 3m 4s 5ms (6 times) realtime";

        DumpsysBatteryInfoParser parser = new DumpsysBatteryInfoParser();
        parser.parseKernelWakeLock(inputLine, WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK);
        DumpsysBatteryInfoItem item = parser.getItem();

        assertEquals(1, item.getWakeLocks(WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK).size());
        WakeLock wakeLock = item.getWakeLocks(WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK).get(0);
        assertEquals("Process", wakeLock.getName());
        assertNull(wakeLock.getNumber());
        assertEquals(DumpsysBatteryInfoParser.getMs(1, 2, 3, 4, 5), wakeLock.getHeldTime());
        assertEquals(6, wakeLock.getLockedCount());

        inputLine = "  Kernel Wake lock \"Process\": 5m 7ms (2 times) realtime";

        parser = new DumpsysBatteryInfoParser();
        parser.parseKernelWakeLock(inputLine, WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK);
        item = parser.getItem();

        assertEquals(1, item.getWakeLocks(WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK).size());
        wakeLock = item.getWakeLocks(WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK).get(0);
        assertEquals("Process", wakeLock.getName());
        assertNull(wakeLock.getNumber());
        assertEquals(5 * 60 * 1000 + 7, wakeLock.getHeldTime());
        assertEquals(2, wakeLock.getLockedCount());
    }

    /**
     * Test that wake locks are parsed.
     */
    public void testParseWakeLock() {
        String inputLine = "  Wake lock #1234 Process: 1d 2h 3m 4s 5ms (6 times) realtime";

        DumpsysBatteryInfoParser parser = new DumpsysBatteryInfoParser();
        parser.parseWakeLock(inputLine, WakeLockCategory.LAST_CHARGE_WAKELOCK);
        DumpsysBatteryInfoItem item = parser.getItem();

        assertEquals(1, item.getWakeLocks(WakeLockCategory.LAST_CHARGE_WAKELOCK).size());
        WakeLock wakeLock = item.getWakeLocks(WakeLockCategory.LAST_CHARGE_WAKELOCK).get(0);
        assertEquals("Process", wakeLock.getName());
        assertEquals((Integer) 1234, wakeLock.getNumber());
        assertEquals(DumpsysBatteryInfoParser.getMs(1, 2, 3, 4, 5), wakeLock.getHeldTime());
        assertEquals(6, wakeLock.getLockedCount());

        inputLine = "  Wake lock #1234 Process:with:colons: 5m 7ms (2 times) realtime";

        parser = new DumpsysBatteryInfoParser();
        parser.parseWakeLock(inputLine, WakeLockCategory.LAST_CHARGE_WAKELOCK);
        item = parser.getItem();

        assertEquals(1, item.getWakeLocks(WakeLockCategory.LAST_CHARGE_WAKELOCK).size());
        wakeLock = item.getWakeLocks(WakeLockCategory.LAST_CHARGE_WAKELOCK).get(0);
        assertEquals("Process:with:colons", wakeLock.getName());
        assertEquals((Integer) 1234, wakeLock.getNumber());
        assertEquals(5 * 60 * 1000 + 7, wakeLock.getHeldTime());
        assertEquals(2, wakeLock.getLockedCount());
    }

    /**
     * Test the helper function to covert time to ms.
     */
    public void testGetMs() {
        assertEquals(1, DumpsysBatteryInfoParser.getMs(0, 0, 0, 0, 1));
        assertEquals(1000, DumpsysBatteryInfoParser.getMs(0, 0, 0, 1, 0));
        assertEquals(60 * 1000, DumpsysBatteryInfoParser.getMs(0, 0, 1, 0, 0));
        assertEquals(60 * 60 * 1000, DumpsysBatteryInfoParser.getMs(0, 1, 0, 0, 0));
        assertEquals(24 * 60 * 60 * 1000, DumpsysBatteryInfoParser.getMs(1, 0, 0, 0, 0));
    }
}

