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

import com.android.loganalysis.item.LogcatItem;
import com.android.loganalysis.item.MemInfoItem;
import com.android.loganalysis.item.MiscLogcatItem;
import com.android.loganalysis.parser.LogcatParser;

import junit.framework.TestCase;

/**
 * Unit test for {@link MemoryUsageHeuristic}.
 */
public class MemoryUsageHeuristicTest extends TestCase {

    /**
     * Test that {@link MemoryUsageHeuristic#failed()} returns true if memory usage is high.
     */
    public void testCheckHeuristic_high_memory() {
        MemoryUsageHeuristic heuristic = new MemoryUsageHeuristic();
        MemInfoItem memInfo = new MemInfoItem();
        memInfo.put("MemTotal", 1000000);
        memInfo.put("MemFree", (int) ((1.0 - heuristic.getCutoff()) * memInfo.get("MemTotal")) - 1);
        heuristic.addMemInfo(null, memInfo);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link MemoryUsageHeuristic#failed()} returns false if memory usage is low.
     */
    public void testCheckHeuristic_low_memory() {
        MemoryUsageHeuristic heuristic = new MemoryUsageHeuristic();
        MemInfoItem memInfo = new MemInfoItem();
        memInfo.put("MemTotal", 1000000);
        memInfo.put("MemFree", (int) ((1.0 - heuristic.getCutoff()) * memInfo.get("MemTotal")) + 1);
        heuristic.addMemInfo(null, memInfo);

        assertFalse(heuristic.failed());
    }

    /**
     * Test that {@link CpuUsageHeuristic#failed()} returns true if a high memory usage
     * indicator is found in the logcat.
     */
    public void testCheckHeuristic_logcat() {
        MemoryUsageHeuristic heuristic = new MemoryUsageHeuristic();
        LogcatItem logcat = new LogcatItem();
        MiscLogcatItem item = new MiscLogcatItem();
        item.setCategory(LogcatParser.HIGH_MEMORY_USAGE);
        logcat.addEvent(item);
        heuristic.addLogcat(null, logcat);

        assertTrue(heuristic.failed());
    }
}
