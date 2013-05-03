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
import com.android.loganalysis.item.TopItem;
import com.android.loganalysis.parser.LogcatParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A {@link IHeuristic} to detect if the CPU usage is abnormally high.
 */
public class CpuUsageHeuristic extends AbstractHeuristic {

    /** Constant for JSON output */
    public static final String CUTOFF = "CUTOFF";
    /** Constant for JSON output */
    public static final String USAGE = "USAGE";
    /** Constant for JSON output */
    public static final String TOP = "TOP";

    private static final String HEURISTIC_NAME = "CPU usage";
    private static final String HEURISTIC_TYPE = "CPU_USAGE_HEURISTIC";

    // TODO: Make this value configurable.
    private static final double TOP_USAGE_CUTOFF = 0.8;

    private LogcatItem mLogcat = null;
    private TopItem mTop = null;

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
    public void addTop(TopItem top, Date timestamp, String uri) {
        mTop = top;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean failed() {
        if (mTop != null && getUsage() > TOP_USAGE_CUTOFF) {
            return true;
        }

        if (mLogcat != null && mLogcat.getMiscEvents(LogcatParser.HIGH_CPU_USAGE).size() > 0) {
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

        return String.format("CPU usage at %.0f%% (over %.0f%%)", 100.0 * getUsage(),
                100.0 * TOP_USAGE_CUTOFF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        // TODO: List the top cpu using processes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        JSONObject output = super.toJson();
        try {
            output.put(CUTOFF, TOP_USAGE_CUTOFF);
            if (mTop != null) {
                output.put(USAGE, getUsage());
                output.put(TOP, mTop.toJson());
            }
        } catch (JSONException e) {
            // Ignore
        }
        return output;
    }

    /**
     * Get the usage as a fraction between 0 and 1.
     */
    private double getUsage() {
        if (mTop == null) {
            return 0.0;
        }

        return ((double) (mTop.getTotal() - mTop.getIdle())) / mTop.getTotal();
    }

    /**
     * Get the CPU usage threshold.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    double getCutoff() {
        return TOP_USAGE_CUTOFF;
    }
}
