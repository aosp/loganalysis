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

import com.android.loganalysis.item.ConflictingItemException;
import com.android.loganalysis.item.ProcrankItem;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link IHeuristic} to detect if there are any abnormalities in the process lifecyle.
 */
public class ProcessLifecycleHeuristic extends AbstractHeuristic {

    private static final String HEURISTIC_NAME = "Process lifecycle";
    private static final String HEURISTIC_TYPE = "PROCESS_LIFECYCLE";

    // TODO: Tune thresholds
    private static final int PROCESSES_CREATED_THRESHOLD = Integer.MAX_VALUE;
    private static final int PROCESSES_DESTROYED_THRESHOLD = Integer.MAX_VALUE;
    private static final int PROCESS_INSTANCE_THRESHOLD = Integer.MAX_VALUE;
    private static final int PROCESS_LIFESPAN_THRESHOLD = Integer.MAX_VALUE;
    private static final int PROCESS_RESTART_LATENCY_THRESHOLD = Integer.MAX_VALUE;


    /**
     * Class which holds the basic information for added procranks.
     */
    private static class ProcrankItemInfo {
        public ProcrankItem mProcrank;
        public Date mTimestamp;
        public String mUri;

        public ProcrankItemInfo(ProcrankItem procrank, Date timestamp, String uri) {
            mProcrank = procrank;
            mTimestamp = timestamp;
            mUri = uri;
        }
    }

    /**
     * Class which holds basic information for a time interval.  Exposed for unit testing.
     */
    static class Interval {
        public Date mStart;
        public Date mStop;

        public Interval(Date start, Date stop) {
            mStart = start;
            mStop = stop;
        }

        public Interval(Date timestamp) {
            mStart = timestamp;
            mStop = timestamp;
        }

        public void updateBounds(Date timestamp) {
            if (timestamp.before(mStart)) {
                mStart = timestamp;
            }
            if (timestamp.after(mStop)) {
                mStop = timestamp;
            }
        }

        public String toString() {
            return String.format("(%s,  %s)", mStart, mStop);
        }

        public boolean contains(Interval i) {
            return (mStart.before(i.mStart) || mStart.equals(i.mStart)) &&
                    (mStop.after(i.mStop) || mStop.equals(i.mStop));
        }
    }

    /**
     * Blacklist of processes known to exist for short periods.
     */
    private static final List<String> PROCESS_BLACKLIST = Arrays.asList(
            "dumpsys", "logcat", "procrank", "meminfo", "uiautomator");

    /** List of {@link ProcrankItemInfo} sorted by timestamp */
    private List<ProcrankItemInfo> mProcranks = new LinkedList<ProcrankItemInfo>();
    /** Map of process name to map of PID to {@link Interval} */
    private Map<String, Map<Integer, Interval>> mProcesses =
            new HashMap<String, Map<Integer, Interval>>();
    /** Map of PID to process names */
    private Map<Integer, String> mAllProcesses = new HashMap<Integer, String>();

    /**
     * {@inheritDoc}
     *
     * @param procrank The {@link ProcrankItem}.
     * @param timestamp The {@link Date} of the procrank, may not be {@code null}.
     * @param uri The URI of the procrank, may be {@code null}.
     */
    @Override
    public void addProcrank(ProcrankItem procrank, Date timestamp, String uri)
            throws ConflictingItemException {
        if (timestamp == null) {
            return;
        }

        for (int pid : procrank.getPids()) {
            final String expectedName = mAllProcesses.get(pid);
            final String name = procrank.getProcessName(pid);
            if (expectedName == null) {
                mAllProcesses.put(pid, name);
            } else if (!expectedName.equals(name)) {
                throw new ConflictingItemException(String.format(
                        "PID %d has name %s but had name %s in previous procranks", pid, name,
                        expectedName));
            }
        }

        mergeProcrankInfoItem(new ProcrankItemInfo(procrank, timestamp, uri));

        Map<Integer, Interval> processInfos;
        for (int pid : procrank.getPids()) {
            final String processName = procrank.getProcessName(pid);
            if (!mProcesses.containsKey(processName)) {
                mProcesses.put(processName, new HashMap<Integer, Interval>());
            }
            processInfos = mProcesses.get(processName);
            if (!processInfos.containsKey(pid)) {
                processInfos.put(pid, new Interval(timestamp));
            } else {
                processInfos.get(pid).updateBounds(timestamp);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean failed() {
        final List<Date> timestamps = getProcrankTimestamps();

        // Check processes created
        for (Date timestamp : timestamps.subList(1, timestamps.size())) {
            if (getProcessesCreated(timestamp) > PROCESSES_CREATED_THRESHOLD) {
                return true;
            }
        }

        // Check processes destroyed
        for (Date timestamp : timestamps.subList(0, timestamps.size() - 1)) {
            if (getProcessesCreated(timestamp) > PROCESSES_DESTROYED_THRESHOLD) {
                return true;
            }
        }

        for (String processName : mProcesses.keySet()) {
            // Check average lifetime
            if (getProcessInstanceCount(processName) > PROCESS_INSTANCE_THRESHOLD &&
                    getAverageLifespan(processName) > PROCESS_LIFESPAN_THRESHOLD) {
                return true;
            }

            // Check average restart latency
            if (getProcessInstanceCount(processName) > PROCESS_INSTANCE_THRESHOLD &&
                    getAverageRestartLatency(processName) > PROCESS_RESTART_LATENCY_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return HEURISTIC_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return HEURISTIC_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        // TODO: Return a summary
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        // TODO: Return details
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        // TODO: Return a proper JSON object
        return super.toJson();
    }

    /**
     * Get the combined process names seen in the procranks.
     *
     * @return The process names.
     */
    Collection<String> getProcessNames() {
        return mProcesses.keySet();
    }

    /**
     * Get the average lifespan in ms for a given process name across all instances of the process.
     *
     * @param process The name of the process
     * @return The average lifespan in ms.
     */
    public long getAverageLifespan(String process) {
        final Map<Integer, Interval> processes = mProcesses.get(process);
        if (processes == null) {
            return 0;
        }
        long totalLifespan = 0;
        for (Interval processInfo : processes.values()) {
            totalLifespan +=
                    processInfo.mStop.getTime() - processInfo.mStart.getTime();
        }
        return totalLifespan / processes.size();
    }

    /**
     * Get the average restart latency for a given process name.
     *
     * @param process The process name.
     * @return The average time it took for a process to be restarted. If there are no restarts or
     * the process was not found, then 0 is returned.
     */
    public long getAverageRestartLatency(String process) {
        final Map<Integer, Interval> processes = mProcesses.get(process);
        if (processes == null) {
            return 0;
        }

        List<Interval> intervals = getMergedIntervals(processes.values());
        if (intervals.size() < 2) {
            return 0;
        }

        long totalRestartLatency = 0;
        Interval interval0;
        Interval interval1;
        for (int i = 0; i < intervals.size() - 1; i++) {
            interval0 = intervals.get(i);
            interval1 = intervals.get(i + 1);
            totalRestartLatency += interval1.mStart.getTime() - interval0.mStop.getTime();
        }
        return totalRestartLatency / (intervals.size() - 1);
    }

    /**
     * Get the number of instances for a given process name.
     *
     * @param process The process name.
     * @return The total number of distinct PIDs.
     */
    public int getProcessInstanceCount(String process) {
        final Map<Integer, Interval> processes = mProcesses.get(process);
        if (processes == null) {
            return 0;
        }

        return processes.size();
    }

    /**
     * Get the number of instances which overlap another instance for a given process name.
     *
     * @param process The process name.
     * @return The total of instances which overlap with another instance of the process.
     */
    public int getProcessOverlapCount(String process) {
        final Map<Integer, Interval> processes = mProcesses.get(process);
        if (processes == null) {
            return 0;
        }

        int count = 0;
        for (Interval interval : getMergedIntervals(processes.values())) {
            int intervalCount = 0;
            for (Interval processInterval : processes.values()) {
                if (interval.contains(processInterval)) {
                    intervalCount++;
                }
            }
            if (intervalCount > 1) {
                count += intervalCount;
            }
        }
        return count;
    }

    /**
     * Get a list of the timestamps for the added procranks in chronological order.
     *
     * @return The {@link List} of {@link Date} objects in chronological order.
     */
    public List<Date> getProcrankTimestamps() {
        List<Date> timestamps = new LinkedList<Date>();
        for (ProcrankItemInfo procrank : mProcranks) {
            timestamps.add(procrank.mTimestamp);
        }
        return timestamps;
    }

    /**
     * Get the number of procranks added.
     *
     * @return The number of procranks added.
     */
    public int getProcrankSize() {
        return mProcranks.size();
    }

    /**
     * Get the {@link ProcrankItem} at the specified position in chronological order.
     *
     * @param index The index of the procrank to return.
     * @return The {@link ProcrankItem}.
     */
    public ProcrankItem getProcrankItem(int index) {
        final ProcrankItemInfo procrank =  mProcranks.get(index);
        if (procrank == null) {
            return null;
        }
        return procrank.mProcrank;
    }

    /**
     * Get the timestamp of the procrank at the specified position in chronological order.
     *
     * @param index The index of the procrank to return.
     * @return The timestamp as a {@link Date}.
     */
    public Date getProcrankTimestamp(int index) {
        final ProcrankItemInfo procrank =  mProcranks.get(index);
        if (procrank == null) {
            return null;
        }
        return procrank.mTimestamp;
    }

    /**
     * Get the URI of the procrank at the specified position in chronological order.
     *
     * @param index The index of the procrank to return.
     * @return The URI as a {@link String}.
     */
    public String getProcrankUri(int index) {
        return mProcranks.get(index).mUri;
    }

    /**
     * Get the number of processes which where created at a given timestamp.
     * <p>
     * This method looks at how many processes (PIDs) were first seen for the given {@link Date}. If
     * a process was missing in one procrank but appeared again in later procrank, it will be
     * considered alive for that duration. The timestamp must match the start date exactly, but the
     * list of timestamps for all added {@link ProcrankItem}s can be obtained with
     * {@link #getProcrankTimestamps()}.
     * </p>
     * @param timestamp The timestamp of the procrank.
     * @return The number of processes created.
     * @see #getProcrankTimestamps()
     * @see #getProcessesDestroyed(Date)
     */
    public int getProcessesCreated(Date timestamp) {
        int count = 0;
        for (Entry<String, Map<Integer, Interval>> entry : mProcesses.entrySet()) {
            if (!PROCESS_BLACKLIST.contains(entry.getKey())) {
                for (Interval i : entry.getValue().values()) {
                    if (i.mStart.equals(timestamp)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Get the number of processes which where destroyed at a given timestamp.
     * <p>
     * This method looks at how many processes (PIDs) were last seen for the given {@link Date}. If
     * a process was missing in one procrank but appeared again in later procrank, it will be
     * considered alive for that duration. The timestamp must match the end date exactly, but the
     * list of timestamps for all added {@link ProcrankItem}s can be obtained with
     * {@link #getProcrankTimestamps()}.
     * </p>
     * @param timestamp The timestamp of the procrank.
     * @return The number of processes destroyed.
     * @see #getProcrankTimestamps()
     * @see #getProcessesCreated(Date)
     */
    public int getProcessesDestroyed(Date timestamp) {
        int count = 0;
        for (Entry<String, Map<Integer, Interval>> entry : mProcesses.entrySet()) {
            if (!PROCESS_BLACKLIST.contains(entry.getKey())) {
                for (Interval i : entry.getValue().values()) {
                    if (i.mStop.equals(timestamp)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Take a collection of intervals and merge them.  All intervals are treated as open intervals.
     * Exposed for unit testing.
     */
    static List<Interval> getMergedIntervals(Collection<Interval> intervals) {
        List<Interval> mergedIntervals = new LinkedList<Interval>();
        for (Interval interval : intervals) {
            mergeInterval(mergedIntervals, interval);
        }
        return mergedIntervals;
    }

    /**
     * Merge an interval into a list of sorted intervals, combining if necessary.  All intervals
     * treated as open intervals.  Exposed for unit testing.
     */
    static void mergeInterval(List<Interval> intervals, Interval interval) {
        Interval workingInterval = new Interval(interval.mStart, interval.mStop);
        if (intervals.size() == 0) {
            intervals.add(workingInterval);
            return;
        }

        int startIndex;
        for (startIndex = 0; startIndex < intervals.size(); startIndex++) {
            if (workingInterval.mStart.before(intervals.get(startIndex).mStart) ||
                    workingInterval.mStart.equals(intervals.get(startIndex).mStart)) {
                break;
            } else if (workingInterval.mStart.after(intervals.get(startIndex).mStart) &&
                    workingInterval.mStart.before(intervals.get(startIndex).mStop)) {
                workingInterval.mStart = intervals.get(startIndex).mStart;
                break;
            }
        }

        int stopIndex;
        for (stopIndex = intervals.size() - 1; stopIndex >= 0; stopIndex--) {
            if (workingInterval.mStop.after(intervals.get(stopIndex).mStop) ||
                    workingInterval.mStop.equals(intervals.get(stopIndex).mStop)) {
                break;
            } else if (workingInterval.mStop.before(intervals.get(stopIndex).mStop) &&
                    workingInterval.mStop.after(intervals.get(stopIndex).mStart)) {
                workingInterval.mStop = intervals.get(stopIndex).mStop;
                break;
            }
        }

        for (int i = 0; i < 1 + stopIndex - startIndex; i++) {
            intervals.remove(startIndex);
        }
        intervals.add(startIndex, workingInterval);
    }

    /**
     * Merge the {@link ProcrankItemInfo} into the list in chronological order.
     */
    private void mergeProcrankInfoItem(ProcrankItemInfo procrank) {
        // Insert the procrank in chronological order, bias for reverse order.
        ListIterator<ProcrankItemInfo> iterator = mProcranks.listIterator(mProcranks.size());
        while (iterator.hasPrevious()) {
            ProcrankItemInfo item = iterator.previous();
            if (procrank.mTimestamp.after(item.mTimestamp)) {
                mProcranks.add(iterator.nextIndex() + 1, procrank);
                return;
            }
        }
        mProcranks.add(0, procrank);
    }
}
