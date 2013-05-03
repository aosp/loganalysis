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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A {@link IHeuristic} to detect if there are any kernel resets.
 */
public class KernelResetHeuristic extends AbstractHeuristic {

    /** Constant for JSON output */
    public static final String KERNEL_RESETS = "KERNEL_RESETS";

    private static final String HEURISTIC_NAME = "Kernel reset";
    private static final String HEURISTIC_TYPE = "KERNEL_RESET_HEURISTIC";

    private KernelLogItem mKernelLog = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addKernelLog(KernelLogItem kernelLog, Date timestamp, String uri) {
        mKernelLog = kernelLog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean failed() {
        return (mKernelLog != null &&
                mKernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET).size() > 0);
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

        return "Found a kernel reset";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDetails() {
        if (!failed()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (MiscKernelLogItem item : mKernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET)) {
            sb.append(String.format("Reason: %s, Time: %.6f\nPreamble:\n%s\n\n", item.getMessage(),
                    item.getEventTime(), item.getPreamble()));
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
            JSONArray kernelResets = new JSONArray();
            for (MiscKernelLogItem item : mKernelLog.getMiscEvents(KernelLogParser.KERNEL_RESET)) {
                kernelResets.put(item.toJson());
            }
            output.put(KERNEL_RESETS, kernelResets);
        } catch (JSONException e) {
            // Ignore
        }
        return output;
    }
}
