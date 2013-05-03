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

import com.android.loganalysis.item.KernelLogItem;
import com.android.loganalysis.item.MiscKernelLogItem;
import com.android.loganalysis.parser.KernelLogParser;

import junit.framework.TestCase;

/**
 * Unit test for {@link KernelResetHeuristic}.
 */
public class KernelResetHeuristicTest extends TestCase {

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns true if a reset is found
     * in the kernel log.
     */
    public void testCheckHeuristic_reset() {
        KernelResetHeuristic heuristic = new KernelResetHeuristic();
        KernelLogItem kernelLog = new KernelLogItem();
        MiscKernelLogItem item = new MiscKernelLogItem();
        item.setCategory(KernelLogParser.KERNEL_RESET);
        kernelLog.addEvent(item);
        heuristic.addKernelLog(kernelLog, null, null);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns false if no resets are
     * found in the kernel log.
     */
    public void testCheckHeuristic_no_reset() {
        KernelResetHeuristic heuristic = new KernelResetHeuristic();
        KernelLogItem kernelLog = new KernelLogItem();
        heuristic.addKernelLog(kernelLog, null, null);

        assertFalse(heuristic.failed());
    }
}
