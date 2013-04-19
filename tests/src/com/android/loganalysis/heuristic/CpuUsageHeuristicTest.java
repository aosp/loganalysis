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
import com.android.loganalysis.item.MiscLogcatItem;
import com.android.loganalysis.item.TopItem;
import com.android.loganalysis.parser.LogcatParser;

import junit.framework.TestCase;

/**
 * Unit test for {@link CpuUsageHeuristic}.
 */
public class CpuUsageHeuristicTest extends TestCase {

    /**
     * Test that {@link CpuUsageHeuristic#failed()} returns true if CPU usage is high.
     */
    public void testCheckHeuristic_high_cpu() {
        CpuUsageHeuristic heuristic = new CpuUsageHeuristic();
        TopItem top = new TopItem();
        top.setTotal(1000);
        top.setUser((int) (heuristic.getCutoff() * top.getTotal()) + 1);
        top.setIdle(top.getTotal() - top.getUser());
        heuristic.addTop(null, top);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link CpuUsageHeuristic#failed()} returns false if CPU usage is low.
     */
    public void testCheckHeuristic_low_cpu() {
        CpuUsageHeuristic heuristic = new CpuUsageHeuristic();
        TopItem top = new TopItem();
        top.setTotal(1000);
        top.setUser((int) (heuristic.getCutoff() * top.getTotal()) - 1);
        top.setIdle(top.getTotal() - top.getUser());
        heuristic.addTop(null, top);

        assertFalse(heuristic.failed());
    }

    /**
     * Test that {@link CpuUsageHeuristic#failed()} returns true if a high cpu usage
     * indicator is found in the logcat.
     */
    public void testCheckHeuristic_logcat() {
        CpuUsageHeuristic heuristic = new CpuUsageHeuristic();
        LogcatItem logcat = new LogcatItem();
        MiscLogcatItem item = new MiscLogcatItem();
        item.setCategory(LogcatParser.HIGH_CPU_USAGE);
        logcat.addEvent(item);
        heuristic.addLogcat(null, logcat);

        assertTrue(heuristic.failed());
    }
}
