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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.android.loganalysis.parser.SmartMonkeyLogParser;

/**
 * An {@link IItem} used to store monkey log info.
 */
public class SmartMonkeyLogItem extends GenericItem {

    private class DateSet extends HashSet<Date> {
        private static final long serialVersionUID = -22L;
    }

    private static final String TYPE = "SMART_MONKEY_LOG";

    private static final String START_TIME = "START_TIME";
    private static final String STOP_TIME = "STOP_TIME";
    private static final String APPLICATIONS = "APPS";
    private static final String PACKAGES = "PACKAGES";
    private static final String THROTTLE = "THROTTLE";
    private static final String TARGET_INVOCATIONS = "TARGET_INVOCATIONS";
    private static final String TOTAL_DURATION = "TOTAL_TIME";
    private static final String START_UPTIME_DURATION = "START_UPTIME";
    private static final String STOP_UPTIME_DURATION = "STOP_UPTIME";
    private static final String IS_FINISHED = "IS_FINISHED";
    private static final String ABORTED = "ABORTED";
    private static final String INTERMEDIATE_COUNT = "INTERMEDIATE_COUNT";
    private static final String FINAL_COUNT = "FINAL_COUNT";
    private static final String ANR_TIMES = "ANR_TIMES";
    private static final String CRASH_TIMES = "CRASH_TIMES";

    private static final Set<String> ATTRIBUTES = new HashSet<String>(Arrays.asList(
            START_TIME, STOP_TIME, PACKAGES, THROTTLE, TARGET_INVOCATIONS, ABORTED,
            TOTAL_DURATION, START_UPTIME_DURATION, STOP_UPTIME_DURATION, APPLICATIONS,
            IS_FINISHED, INTERMEDIATE_COUNT, FINAL_COUNT, ANR_TIMES, CRASH_TIMES));

    /**
     * The constructor for {@link MonkeyLogItem}.
     */
    public SmartMonkeyLogItem() {
        super(TYPE, ATTRIBUTES);

        setAttribute(APPLICATIONS, new ArrayList<String>());
        setAttribute(PACKAGES, new ArrayList<String>());
        setAttribute(CRASH_TIMES, new DateSet());
        setAttribute(ANR_TIMES, new DateSet());
        setAttribute(THROTTLE, 0);
        setAttribute(FINAL_COUNT, 0);
        setAttribute(IS_FINISHED, false);
        setAttribute(ABORTED, false);
        setAttribute(INTERMEDIATE_COUNT, 0);
        setAttribute(START_UPTIME_DURATION, 0L);
        setAttribute(STOP_UPTIME_DURATION, 0L);
    }

    /**
     * Get the start time of the monkey log.
     */
    public Date getStartTime() {
        return (Date) getAttribute(START_TIME);
    }

    /**
     * Set the start time of the monkey log.
     */
    public void setStartTime(Date time) {
        setAttribute(START_TIME, time);
    }

    /**
     * Get the stop time of the monkey log.
     */
    public Date getStopTime() {
        return (Date) getAttribute(STOP_TIME);
    }

    /**
     * Set the stop time of the monkey log.
     */
    public void setStopTime(Date time) {
        setAttribute(STOP_TIME, time);
    }

    /**
     * Get the set of packages that the monkey is run on.
     */
    @SuppressWarnings("unchecked")
    public List<String> getPackages() {
        return (List<String>) getAttribute(PACKAGES);
    }

    /**
     * Add a package to the set that the monkey is run on.
     */
    @SuppressWarnings("unchecked")
    public void addPackage(String thePackage) {
        ((List<String>) getAttribute(PACKAGES)).add(thePackage);
    }

    /**
     * Get the set of packages that the monkey is run on.
     */
    @SuppressWarnings("unchecked")
    public List<String> getApplications() {
        return (List<String>) getAttribute(APPLICATIONS);
    }

    /**
     * Add a package to the set that the monkey is run on.
     */
    @SuppressWarnings("unchecked")
    public void addApplication(String theApp) {
        ((List<String>) getAttribute(APPLICATIONS)).add(theApp);
    }

    /**
     * Get the throttle for the monkey run.
     */
    public int getThrottle() {
        return (Integer) getAttribute(THROTTLE);
    }

    /**
     * Set the throttle for the monkey run.
     */
    public void setThrottle(int throttle) {
        setAttribute(THROTTLE, throttle);
    }

    /**
     * Get the target sequence invocations for the monkey run.
     */
    public int getTargetInvocations() {
        return (Integer) getAttribute(TARGET_INVOCATIONS);
    }

    /**
     * Set the target sequence invocations for the monkey run.
     */
    public void setTargetInvocations(int count) {
        setAttribute(TARGET_INVOCATIONS, count);
    }

    /**
     * Get the total duration of the monkey run in milliseconds.
     */
    public long getTotalDuration() {
        return (Long) getAttribute(TOTAL_DURATION);
    }

    /**
     * Set the total duration of the monkey run in milliseconds.
     */
    public void setTotalDuration(long time) {
        setAttribute(TOTAL_DURATION, time);
    }

    /**
     * Get the start uptime duration of the monkey run in milliseconds.
     */
    public long getStartUptimeDuration() {
        return (Long) getAttribute(START_UPTIME_DURATION);
    }

    /**
     * Set the start uptime duration of the monkey run in milliseconds.
     */
    public void setStartUptimeDuration(long uptime) {
        setAttribute(START_UPTIME_DURATION, uptime);
    }

    /**
     * Get the stop uptime duration of the monkey run in milliseconds.
     */
    public long getStopUptimeDuration() {
        return (Long) getAttribute(STOP_UPTIME_DURATION);
    }

    /**
     * Set the stop uptime duration of the monkey run in milliseconds.
     */
    public void setStopUptimeDuration(long uptime) {
        setAttribute(STOP_UPTIME_DURATION, uptime);
    }

    /**
     * Get if the monkey run finished without crashing.
     */
    public boolean getIsFinished() {
        return (Boolean) getAttribute(IS_FINISHED);
    }

    /**
     * Set if the monkey run finished without crashing.
     */
    public void setIsFinished(boolean finished) {
        setAttribute(IS_FINISHED, finished);
    }

    /**
     * Get the intermediate count for the monkey run.
     * <p>
     * This count starts at 0 and increments every 100 events. This number should be within 100 of
     * the final count.
     * </p>
     */
    public int getIntermediateCount() {
        return (Integer) getAttribute(INTERMEDIATE_COUNT);
    }

    /**
     * Set the intermediate count for the monkey run.
     * <p>
     * This count starts at 0 and increments every 100 events. This number should be within 100 of
     * the final count.
     * </p>
     */
    public void setIntermediateCount(int count) {
        setAttribute(INTERMEDIATE_COUNT, count);
    }

    /**
     * Get the final count for the monkey run.
     */
    public int getFinalCount() {
        return (Integer) getAttribute(FINAL_COUNT);
    }

    /**
     * Set the final count for the monkey run.
     */
    public void setFinalCount(int count) {
        setAttribute(FINAL_COUNT, count);
    }

    /**
     * Get ANR times
     */
    public Set<Date> getAnrTimes() {
        return (DateSet) getAttribute(ANR_TIMES);
    }

    /**
     * Add ANR time
     */
    public void addAnrTime(String time) {
        ((DateSet) getAttribute(ANR_TIMES)).add(SmartMonkeyLogParser.parseTime(time));
    }

    /**
     * Get Crash times
     */
    public Set<Date> getCrashTimes() {
        return (DateSet) getAttribute(CRASH_TIMES);
    }

    /**
     * Add Crash time
     */
    public void addCrashTime(String time) {
        ((DateSet) getAttribute(CRASH_TIMES)).add(SmartMonkeyLogParser.parseTime(time));
    }

    /**
     * Get the status of no sequences abort
     */
    public boolean getAborted() {
        return (Boolean) getAttribute(ABORTED);
    }

    /**
     * Set the status of no sequences abort
     * @param noSeq
     */
    public void setAborted(boolean noSeq) {
        setAttribute(ABORTED, noSeq);
    }
}
