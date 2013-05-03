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
import com.android.loganalysis.parser.LogcatParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A {@link IHeuristic} to detect if the memory usage is abnormally high.
 */
public class MemoryUsageHeuristic extends AbstractHeuristic {

    /** Constant for JSON output */
    public static final String CUTOFF = "CUTOFF";
    /** Constant for JSON output */
    public static final String USAGE = "USAGE";
    /** Constant for JSON output */
    public static final String MEM_INFO = "MEM_INFO";

    private static final String HEURISTIC_NAME = "Memory usage";
    private static final String HEURISTIC_TYPE = "MEMORY_USAGE_HEURISTIC";

    // TODO: Make this value configurable.
    private static final double MEMORY_USAGE_CUTOFF = 0.99;

    private LogcatItem mLogcat = null;
    private MemInfoItem mMemInfo = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLogcat(LogcatItem logcat, Date timestamp, String uri) {
        mLogcat = logcat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMemInfo(MemInfoItem top, Date timestamp, String uri) {
        mMemInfo = top;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean failed() {
        if (mMemInfo != null && getUsage() > MEMORY_USAGE_CUTOFF) {
            return true;
        }

        if (mLogcat != null && mLogcat.getMiscEvents(LogcatParser.HIGH_MEMORY_USAGE).size() > 0) {
            return true;
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

        return String.format("Memory usage at %.0f%% (over %.0f%%)", 100.0 * getUsage(),
                100.0 * MEMORY_USAGE_CUTOFF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        // TODO: List the top memory using processes.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        JSONObject output = super.toJson();
        try {
            output.put(CUTOFF, MEMORY_USAGE_CUTOFF);
            if (mMemInfo != null) {
                output.put(USAGE, getUsage());
                output.put(MEM_INFO, mMemInfo.toJson());
            }
        } catch (JSONException e) {
            // Ignore
        }
        return output;
    }

    /**
     * Get the memory usage as a fraction between 0 and 1.
     */
    private double getUsage() {
        if (mMemInfo == null) {
            return 0.0;
        }

        return ((double) (mMemInfo.get("MemTotal") - mMemInfo.get("MemFree"))) /
                mMemInfo.get("MemTotal");
    }

    /**
     * Get the memory usage threshold.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    double getCutoff() {
        return MEMORY_USAGE_CUTOFF;
    }
}
