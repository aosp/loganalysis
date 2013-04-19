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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A {@link IHeuristic} to detect if there are any runtime restarts.
 */
public class RuntimeRestartHeuristic extends AbstractHeuristic {

    /** Constant for JSON output */
    public static final String RUNTIME_RESTARTS = "RUNTIME_RESTARTS";
    /** Constant for JSON output */
    public static final String PROCRANK = "PROCRANK";
    /** Constant for JSON output */
    public static final String PROCRANK_SUMMARY = "PROCRANK_SUMMARY";

    private static final String HEURISTIC_NAME = "Runtime restart";
    private static final String HEURISTIC_TYPE = "RUNTIME_RESTART_HEURISTIC";

    private static final int MAX_SYSTEM_SERVER_PID = 1000;
    private static final String SYSTEM_SERVER_PROCESS_NAME = "system_server";
    private static final String BOOT_ANIMATION_PROCESS_NAME = "/system/bin/bootanimation";

    private LogcatItem mLogcat = null;
    private ProcrankItem mProcrank = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLogcat(Date timestamp, LogcatItem logcat) {
        mLogcat = logcat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProcrank(Date timestamp, ProcrankItem procrank) {
        mProcrank = procrank;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean failed() {
        if (mLogcat != null && mLogcat.getMiscEvents(LogcatParser.RUNTIME_RESTART).size() > 0) {
            return true;
        }

        if (mProcrank != null && mProcrank.getPids().size() > 0) {
            Integer systemServerPid = getPid(SYSTEM_SERVER_PROCESS_NAME);
            Integer bootAnimationPid = getPid(BOOT_ANIMATION_PROCESS_NAME);

            if (systemServerPid == null || systemServerPid > MAX_SYSTEM_SERVER_PID) {
                return true;
            }

            if (bootAnimationPid != null && bootAnimationPid > MAX_SYSTEM_SERVER_PID) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return HEURISTIC_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return HEURISTIC_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        if (!failed()) {
            return null;
        }

        return "Found a runtime restart";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        if (!failed()) {
            return null;
        }

        Integer systemServerPid = getPid(SYSTEM_SERVER_PROCESS_NAME);
        Integer bootAnimtationPid = getPid(BOOT_ANIMATION_PROCESS_NAME);

        StringBuilder sb = new StringBuilder();
        if (mLogcat != null) {
            for (MiscLogcatItem item : mLogcat.getMiscEvents(LogcatParser.RUNTIME_RESTART)) {
                sb.append(String.format("Message: %s, Time: %s\n\nLast lines of logcat:\n%s\n\n" +
                        "Process lines for pid %d:\n%s\n\n", item.getMessage(), item.getEventTime(),
                        item.getLastPreamble(), item.getPid(), item.getProcessPreamble()));
            }
        }

        if (systemServerPid == null) {
            sb.append("Suspected runtime restart detected because system_server is missing from " +
                    "procrank");
        }
        if (systemServerPid != null && systemServerPid > MAX_SYSTEM_SERVER_PID) {
            sb.append(String.format("Suspected runtime restart detected because system_server " +
                    "has a PID of %d (greater than %d)\n", systemServerPid, MAX_SYSTEM_SERVER_PID));
        }
        if (bootAnimtationPid != null && bootAnimtationPid > MAX_SYSTEM_SERVER_PID) {
            sb.append(String.format("Suspected runtime restart detected because " +
                    "/system/bin/bootanimation is present in procrank with a PID of %d (greater " +
                    "than %d)\n", bootAnimtationPid, MAX_SYSTEM_SERVER_PID));
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        JSONObject output = super.toJson();
        try {
            Integer systemServerPid = getPid(SYSTEM_SERVER_PROCESS_NAME);
            Integer bootAnimtationPid = getPid(BOOT_ANIMATION_PROCESS_NAME);

            if (mLogcat != null) {
                JSONArray runtimeRestarts = new JSONArray();
                for (MiscLogcatItem item : mLogcat.getMiscEvents(LogcatParser.RUNTIME_RESTART)) {
                    runtimeRestarts.put(item.toJson());
                }
                output.put(RUNTIME_RESTARTS, runtimeRestarts);
            }

            if (mProcrank != null) {
                output.put(PROCRANK, mProcrank.toJson());
                JSONArray summary = new JSONArray();
                if (systemServerPid == null) {
                    summary.put(String.format("%s is absent from the procrank",
                            SYSTEM_SERVER_PROCESS_NAME));
                }
                if (systemServerPid != null && systemServerPid > MAX_SYSTEM_SERVER_PID) {
                    summary.put(String.format("%s is present in the procrank with a PID greater " +
                            "than %d", SYSTEM_SERVER_PROCESS_NAME, MAX_SYSTEM_SERVER_PID));
                }
                if (bootAnimtationPid != null && bootAnimtationPid > MAX_SYSTEM_SERVER_PID) {
                    summary.put(String.format("%s is present in the procrank with a PID greater " +
                            "than %d", BOOT_ANIMATION_PROCESS_NAME, MAX_SYSTEM_SERVER_PID));
                }
                output.put(PROCRANK, mProcrank.toJson());
                output.put(PROCRANK_SUMMARY, summary);
            }
        } catch (JSONException e) {
            // Ignore
        }
        return output;
    }

    /**
     * Get the PID from a process name
     */
    private Integer getPid(String processName) {
        if (mProcrank == null || processName == null) {
            return null;
        }
        for (Integer pid : mProcrank.getPids()) {
            if (processName.equals(mProcrank.getProcessName(pid))) {
                return pid;
            }
        }
        return null;
    }

    /**
     * Get the system server PID threshold.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    int getCutoff() {
        return MAX_SYSTEM_SERVER_PID;
    }

    /**
     * Get the system server name.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    String getSystemServerName() {
        return SYSTEM_SERVER_PROCESS_NAME;
    }

    /**
     * Get the bootanimation name.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    String getBootAnimationName() {
        return BOOT_ANIMATION_PROCESS_NAME;
    }
}
