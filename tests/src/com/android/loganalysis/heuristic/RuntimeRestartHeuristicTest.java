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
import com.android.loganalysis.item.ProcrankItem;
import com.android.loganalysis.parser.LogcatParser;

import junit.framework.TestCase;

/**
 * Unit test for {@link RuntimeRestartHeuristic}.
 */
public class RuntimeRestartHeuristicTest extends TestCase {

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns false if the procrank is empty.
     */
    public void testCheckHeuristic_empty_procrank() {
        RuntimeRestartHeuristic heuristic = new RuntimeRestartHeuristic();
        ProcrankItem procrank = new ProcrankItem();
        heuristic.addProcrank(null, procrank);

        assertFalse(heuristic.failed());
    }

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns true if the system server
     * is process is absent.
     */
    public void testCheckHeuristic_no_system_server() {
        RuntimeRestartHeuristic heuristic = new RuntimeRestartHeuristic();
        ProcrankItem procrank = new ProcrankItem();
        procrank.addProcrankLine(0, "foo", 0, 0, 0, 0);
        heuristic.addProcrank(null, procrank);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns true if the system server
     * is process is present with a high PID.
     */
    public void testCheckHeuristic_high_system_server() {
        RuntimeRestartHeuristic heuristic = new RuntimeRestartHeuristic();
        ProcrankItem procrank = new ProcrankItem();
        procrank.addProcrankLine(heuristic.getCutoff() + 1, heuristic.getSystemServerName(), 0, 0,
                0, 0);
        heuristic.addProcrank(null, procrank);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns true if the bootanimation
     * process is present with a high PID.
     */
    public void testCheckHeuristic_high_bootanimation() {
        RuntimeRestartHeuristic heuristic = new RuntimeRestartHeuristic();
        ProcrankItem procrank = new ProcrankItem();
        procrank.addProcrankLine(heuristic.getCutoff() - 1, heuristic.getSystemServerName(), 0, 0,
                0, 0);
        procrank.addProcrankLine(heuristic.getCutoff() + 1, heuristic.getBootAnimationName(), 0, 0,
                0, 0);
        heuristic.addProcrank(null, procrank);

        assertTrue(heuristic.failed());
    }

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns false if the system server
     * process is present with a low PID.
     */
    public void testCheckHeuristic_low_system_server() {
        RuntimeRestartHeuristic heuristic = new RuntimeRestartHeuristic();
        ProcrankItem procrank = new ProcrankItem();
        procrank.addProcrankLine(heuristic.getCutoff() - 1, heuristic.getSystemServerName(), 0, 0,
                0, 0);
        heuristic.addProcrank(null, procrank);

        assertFalse(heuristic.failed());
    }

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns false if the bootanimation
     * and system server processes are present with low PIDs.
     */
    public void testCheckHeuristic_low_bootanimation() {
        RuntimeRestartHeuristic heuristic = new RuntimeRestartHeuristic();
        ProcrankItem procrank = new ProcrankItem();
        procrank.addProcrankLine(heuristic.getCutoff() - 1, heuristic.getSystemServerName(), 0, 0,
                0, 0);
        procrank.addProcrankLine(heuristic.getCutoff() - 2, heuristic.getBootAnimationName(), 0, 0,
                0, 0);
        heuristic.addProcrank(null, procrank);

        assertFalse(heuristic.failed());
    }

    /**
     * Test that {@link RuntimeRestartHeuristic#failed()} returns true if a runtime restart
     * is found in the logcat.
     */
    public void testCheckHeuristic_logcat() {
        RuntimeRestartHeuristic heuristic = new RuntimeRestartHeuristic();
        LogcatItem logcat = new LogcatItem();
        MiscLogcatItem item = new MiscLogcatItem();
        item.setCategory(LogcatParser.RUNTIME_RESTART);
        logcat.addEvent(item);
        heuristic.addLogcat(null, logcat);

        assertTrue(heuristic.failed());
    }
}
