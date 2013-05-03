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
package com.android.loganalysis.heuristic;

import com.android.loganalysis.item.DumpsysBatteryInfoItem;
import com.android.loganalysis.item.DumpsysItem;

import junit.framework.TestCase;

/**
 * Unit test for {@link PowerUsageHeuristic}.
 */
public class PowerUsageHeuristicTest extends TestCase {

    /**
     * Test that {@link PowerUsageHeuristic#failed()} returns true if a wake lock is held
     * longer than the threshold.
     */
    public void testCheckHeuristic_wake_lock() {
        PowerUsageHeuristic heuristic = new PowerUsageHeuristic();
        DumpsysItem dumpsys = new DumpsysItem();
        DumpsysBatteryInfoItem batteryInfo = new DumpsysBatteryInfoItem();
        batteryInfo.addLastUnpluggedWakeLock("wakelock", 0, heuristic.getCutoff() + 1, 0);
        batteryInfo.addLastUnpluggedKernelWakeLock("kernelwakelock", heuristic.getCutoff() - 1, 0);
        dumpsys.setBatteryInfo(batteryInfo);
        heuristic.addDumpsys(dumpsys, null, null);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link PowerUsageHeuristic#failed()} returns true if a kernel wake lock is
     * held longer than the threshold.
     */
    public void testCheckHeuristic_kernel_wake_lock() {
        PowerUsageHeuristic heuristic = new PowerUsageHeuristic();
        DumpsysItem dumpsys = new DumpsysItem();
        DumpsysBatteryInfoItem batteryInfo = new DumpsysBatteryInfoItem();
        batteryInfo.addLastUnpluggedWakeLock("wakelock", 0, heuristic.getCutoff() - 1, 0);
        batteryInfo.addLastUnpluggedKernelWakeLock("kernelwakelock", heuristic.getCutoff() + 1, 0);
        dumpsys.setBatteryInfo(batteryInfo);
        heuristic.addDumpsys(dumpsys, null, null);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link PowerUsageHeuristic#failed()} returns false if no wake locks are
     * held longer than the threshold.
     */
    public void testCheckHeuristic_no_wake_lock() {
        PowerUsageHeuristic heuristic = new PowerUsageHeuristic();
        DumpsysItem dumpsys = new DumpsysItem();
        DumpsysBatteryInfoItem batteryInfo = new DumpsysBatteryInfoItem();
        batteryInfo.addLastUnpluggedWakeLock("wakelock", 0, heuristic.getCutoff() - 1, 0);
        batteryInfo.addLastUnpluggedKernelWakeLock("kernelwakelock", heuristic.getCutoff() - 1, 0);
        dumpsys.setBatteryInfo(batteryInfo);
        heuristic.addDumpsys(dumpsys, null, null);

        assertFalse(heuristic.failed());
    }
}
