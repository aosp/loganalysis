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

import com.android.loganalysis.heuristic.ProcessLifecycleHeuristic.Interval;
import com.android.loganalysis.item.ConflictingItemException;
import com.android.loganalysis.item.ProcrankItem;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Unit test for {@link ProcessLifecycleHeuristic}.
 */
public class ProcessLifecycleHeuristicTest extends TestCase {

    /**
     * Test that a {@link ConflictingItemException} is thrown if there are conflicting procranks.
     */
    public void testConflictingInfo() throws ConflictingItemException {
        Date now = new Date();

        ProcrankItem p1 = new ProcrankItem();
        p1.addProcrankLine(0, "name", 0, 0, 0, 0);
        ProcrankItem p2 = new ProcrankItem();
        p2.addProcrankLine(0, "wrongName", 0, 0, 0, 0);

        ProcessLifecycleHeuristic heuristic = new ProcessLifecycleHeuristic();
        heuristic.addProcrank(p1, now, null);
        try {
            heuristic.addProcrank(p2, offsetDate(now, Calendar.MINUTE, 1), null);
            fail("ConflictingItemException not thrown");
        } catch (ConflictingItemException e) {
            // Expected
        }
    }

    /**
     * Test that multiple procranks are combined properly and that the stat functions for the
     * combined procranks all return properly.
     */
    public void testCombineProcesses() throws ConflictingItemException {
        Date now = new Date();
        ProcessLifecycleHeuristic heuristic = new ProcessLifecycleHeuristic();

        /*
         * Processes look like:
         * Proc  PID  0  1  2  3
         *   p0    0  x
         *   p0    9        x--x
         *   p1    1  x-----x--x
         *   p2    2  x--x
         *   p3    3  x--x--x
         *   p4    4  x
         *   p4    7     x
         *   p4   10           x
         *   p5    5  x--x--x
         *   p5    8     x--x--x
         *   p6    6     x--x--x
         */
        ProcrankItem p0 = new ProcrankItem();
        p0.addProcrankLine(0, "p0", 0, 0, 0, 0);
        p0.addProcrankLine(1, "p1", 0, 0, 0, 0);
        p0.addProcrankLine(2, "p2", 0, 0, 0, 0);
        p0.addProcrankLine(3, "p3", 0, 0, 0, 0);
        p0.addProcrankLine(4, "p4", 0, 0, 0, 0);
        p0.addProcrankLine(5, "p5", 0, 0, 0, 0);

        ProcrankItem p1 = new ProcrankItem();
        p1.addProcrankLine(2, "p2", 0, 0, 0, 0);
        p1.addProcrankLine(3, "p3", 0, 0, 0, 0);
        p1.addProcrankLine(5, "p5", 0, 0, 0, 0);
        p1.addProcrankLine(6, "p6", 0, 0, 0, 0);
        p1.addProcrankLine(7, "p4", 0, 0, 0, 0);
        p1.addProcrankLine(8, "p5", 0, 0, 0, 0);

        ProcrankItem p2 = new ProcrankItem();
        p2.addProcrankLine(1, "p1", 0, 0, 0, 0);
        p2.addProcrankLine(3, "p3", 0, 0, 0, 0);
        p2.addProcrankLine(5, "p5", 0, 0, 0, 0);
        p2.addProcrankLine(6, "p6", 0, 0, 0, 0);
        p2.addProcrankLine(8, "p5", 0, 0, 0, 0);
        p2.addProcrankLine(9, "p0", 0, 0, 0, 0);

        ProcrankItem p3 = new ProcrankItem();
        p3.addProcrankLine(1, "p1", 0, 0, 0, 0);
        p3.addProcrankLine(6, "p6", 0, 0, 0, 0);
        p3.addProcrankLine(8, "p5", 0, 0, 0, 0);
        p3.addProcrankLine(9, "p0", 0, 0, 0, 0);
        p3.addProcrankLine(10, "p4", 0, 0, 0, 0);

        // Add out of order to ensure that procranks are placed in chronological order.
        heuristic.addProcrank(p1, offsetDate(now, Calendar.MINUTE, 1), null);
        heuristic.addProcrank(p0, now, null);
        heuristic.addProcrank(p3, offsetDate(now, Calendar.MINUTE, 3), null);
        heuristic.addProcrank(p2, offsetDate(now, Calendar.MINUTE, 2), null);

        assertEquals(0, heuristic.getAverageLifespan("invalid"));
        assertEquals(30 * 1000, heuristic.getAverageLifespan("p0")); // 0, 2-3
        assertEquals(3 * 60 * 1000, heuristic.getAverageLifespan("p1")); // 0-3
        assertEquals(1 * 60 * 1000, heuristic.getAverageLifespan("p2")); // 0-1
        assertEquals(2 * 60 * 1000, heuristic.getAverageLifespan("p3")); // 0-2
        assertEquals(0, heuristic.getAverageLifespan("p4")); // 0, 1, 3
        assertEquals(2 * 60 * 1000, heuristic.getAverageLifespan("p5")); // 0-2, 1-3
        assertEquals(2 * 60 * 1000, heuristic.getAverageLifespan("p6")); // 1-3

        assertEquals(0, heuristic.getAverageRestartLatency("invalid"));
        assertEquals(2 * 60 * 1000, heuristic.getAverageRestartLatency("p0")); // 0, 2-3
        assertEquals(0, heuristic.getAverageRestartLatency("p1")); // 0-3
        assertEquals(0, heuristic.getAverageRestartLatency("p2")); // 0-1
        assertEquals(0, heuristic.getAverageRestartLatency("p3")); // 0-2
        assertEquals(3 * 60 * 1000 / 2, heuristic.getAverageRestartLatency("p4")); // 0, 1, 3
        assertEquals(0, heuristic.getAverageRestartLatency("p5")); // 0-2, 1-3
        assertEquals(0, heuristic.getAverageRestartLatency("p6")); // 1-3

        assertEquals(0, heuristic.getProcessInstanceCount("invalid"));
        assertEquals(2, heuristic.getProcessInstanceCount("p0")); // 0, 2-3
        assertEquals(1, heuristic.getProcessInstanceCount("p1")); // 0-3
        assertEquals(1, heuristic.getProcessInstanceCount("p2")); // 0-1
        assertEquals(1, heuristic.getProcessInstanceCount("p3")); // 0-2
        assertEquals(3, heuristic.getProcessInstanceCount("p4")); // 0, 1, 3
        assertEquals(2, heuristic.getProcessInstanceCount("p5")); // 0-2, 1-3
        assertEquals(1, heuristic.getProcessInstanceCount("p6")); // 1-3

        assertEquals(0, heuristic.getProcessOverlapCount("invalid"));
        assertEquals(0, heuristic.getProcessOverlapCount("p0")); // 0, 2-3
        assertEquals(0, heuristic.getProcessOverlapCount("p1")); // 0-3
        assertEquals(0, heuristic.getProcessOverlapCount("p2")); // 0-1
        assertEquals(0, heuristic.getProcessOverlapCount("p3")); // 0-2
        assertEquals(0, heuristic.getProcessOverlapCount("p4")); // 0, 1, 3
        assertEquals(2, heuristic.getProcessOverlapCount("p5")); // 0-2, 1-3
        assertEquals(0, heuristic.getProcessOverlapCount("p6")); // 1-3

        final List<Date> timestamps = heuristic.getProcrankTimestamps();
        assertEquals(4, timestamps.size());
        assertEquals(now, timestamps.get(0));
        assertEquals(offsetDate(now, Calendar.MINUTE, 1), timestamps.get(1));
        assertEquals(offsetDate(now, Calendar.MINUTE, 2), timestamps.get(2));
        assertEquals(offsetDate(now, Calendar.MINUTE, 3), timestamps.get(3));

        assertEquals(6, heuristic.getProcessesCreated(now));
        assertEquals(3, heuristic.getProcessesCreated(offsetDate(now, Calendar.MINUTE, 1)));
        assertEquals(1, heuristic.getProcessesCreated(offsetDate(now, Calendar.MINUTE, 2)));
        assertEquals(1, heuristic.getProcessesCreated(offsetDate(now, Calendar.MINUTE, 3)));

        assertEquals(2, heuristic.getProcessesDestroyed(now));
        assertEquals(2, heuristic.getProcessesDestroyed(offsetDate(now, Calendar.MINUTE, 1)));
        assertEquals(2, heuristic.getProcessesDestroyed(offsetDate(now, Calendar.MINUTE, 2)));
        assertEquals(5, heuristic.getProcessesDestroyed(offsetDate(now, Calendar.MINUTE, 3)));

        final int procrankSize = heuristic.getProcrankSize();
        assertEquals(4, procrankSize);

        assertEquals(now, heuristic.getProcrankTimestamp(0));
        assertEquals(offsetDate(now, Calendar.MINUTE, 1), heuristic.getProcrankTimestamp(1));
        assertEquals(offsetDate(now, Calendar.MINUTE, 2), heuristic.getProcrankTimestamp(2));
        assertEquals(offsetDate(now, Calendar.MINUTE, 3), heuristic.getProcrankTimestamp(3));

        assertEquals(p0, heuristic.getProcrankItem(0));
        assertEquals(p1, heuristic.getProcrankItem(1));
        assertEquals(p2, heuristic.getProcrankItem(2));
        assertEquals(p3, heuristic.getProcrankItem(3));
    }

    /**
     * Test that procranks are added in chronological order.
     */
    public void testAddProcranks() throws ConflictingItemException {
        Date now = new Date();

        ProcrankItem p1 = new ProcrankItem();

        ProcessLifecycleHeuristic heuristic = new ProcessLifecycleHeuristic();
        heuristic.addProcrank(p1, null, null);
        heuristic.addProcrank(p1, now, null);
        heuristic.addProcrank(p1, offsetDate(now, Calendar.MINUTE, -1), null);
        heuristic.addProcrank(p1, offsetDate(now, Calendar.MINUTE, 2), null);
        heuristic.addProcrank(p1, offsetDate(now, Calendar.MINUTE, 1), null);

        assertEquals(offsetDate(now, Calendar.MINUTE, -1), heuristic.getProcrankTimestamp(0));
        assertEquals(now, heuristic.getProcrankTimestamp(1));
        assertEquals(offsetDate(now, Calendar.MINUTE, 1), heuristic.getProcrankTimestamp(2));
        assertEquals(offsetDate(now, Calendar.MINUTE, 2), heuristic.getProcrankTimestamp(3));
    }

    /**
     * Test that the utility function for merging overlapping intervals works properly.
     */
    public void testMergeIntervals() {
        Date now = new Date();

        Collection<Interval> intervals = new HashSet<Interval>();
        intervals.add(new Interval(now, offsetDate(now, Calendar.MINUTE, 2)));
        intervals.add(new Interval(
                offsetDate(now, Calendar.MINUTE, 4), offsetDate(now, Calendar.MINUTE, 6)));
        intervals.add(new Interval(
                offsetDate(now, Calendar.MINUTE, 5), offsetDate(now, Calendar.MINUTE, 7)));

        // Expect 0-2, 4-7
        List<Interval> mergedIntervals = ProcessLifecycleHeuristic.getMergedIntervals(intervals);

        assertEquals(2, mergedIntervals.size());
        assertEquals(now, mergedIntervals.get(0).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 2), mergedIntervals.get(0).mStop);
        assertEquals(offsetDate(now, Calendar.MINUTE, 4), mergedIntervals.get(1).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 7), mergedIntervals.get(1).mStop);

        // Expect 0-2, 2-4, 4-7
        ProcessLifecycleHeuristic.mergeInterval(mergedIntervals, new Interval(
                offsetDate(now, Calendar.MINUTE, 2), offsetDate(now, Calendar.MINUTE, 4)));

        assertEquals(3, mergedIntervals.size());
        assertEquals(offsetDate(now, Calendar.MINUTE, 2), mergedIntervals.get(1).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 4), mergedIntervals.get(1).mStop);

        // Expect -1-4, 4-7
        ProcessLifecycleHeuristic.mergeInterval(mergedIntervals, new Interval(
                offsetDate(now, Calendar.MINUTE, -1), offsetDate(now, Calendar.MINUTE, 3)));

        assertEquals(2, mergedIntervals.size());
        assertEquals(offsetDate(now, Calendar.MINUTE, -1), mergedIntervals.get(0).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 4), mergedIntervals.get(0).mStop);

        // Expect -2-7
        ProcessLifecycleHeuristic.mergeInterval(mergedIntervals, new Interval(
                offsetDate(now, Calendar.MINUTE, -2), offsetDate(now, Calendar.MINUTE, 6)));

        assertEquals(1, mergedIntervals.size());
        assertEquals(offsetDate(now, Calendar.MINUTE, -2), mergedIntervals.get(0).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 7), mergedIntervals.get(0).mStop);

        // Expect -2-8
        ProcessLifecycleHeuristic.mergeInterval(mergedIntervals, new Interval(
                offsetDate(now, Calendar.MINUTE, 1), offsetDate(now, Calendar.MINUTE, 8)));

        assertEquals(1, mergedIntervals.size());
        assertEquals(offsetDate(now, Calendar.MINUTE, -2), mergedIntervals.get(0).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 8), mergedIntervals.get(0).mStop);

        // Expect -2-8
        ProcessLifecycleHeuristic.mergeInterval(mergedIntervals, new Interval(
                offsetDate(now, Calendar.MINUTE, -2), offsetDate(now, Calendar.MINUTE, 8)));

        assertEquals(1, mergedIntervals.size());
        assertEquals(offsetDate(now, Calendar.MINUTE, -2), mergedIntervals.get(0).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 8), mergedIntervals.get(0).mStop);

        // Expect -2-8, 10-10
        ProcessLifecycleHeuristic.mergeInterval(mergedIntervals, new Interval(
                offsetDate(now, Calendar.MINUTE, 10)));

        assertEquals(2, mergedIntervals.size());
        assertEquals(offsetDate(now, Calendar.MINUTE, 10), mergedIntervals.get(1).mStart);
        assertEquals(offsetDate(now, Calendar.MINUTE, 10), mergedIntervals.get(1).mStop);
    }

    private Date offsetDate(Date timestamp, int field, int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.add(field, offset);
        return cal.getTime();
    }
}
