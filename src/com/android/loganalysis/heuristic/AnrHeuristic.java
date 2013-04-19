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

import com.android.loganalysis.item.AnrItem;
import com.android.loganalysis.item.LogcatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A {@link IHeuristic} to detect if there are any ARNs.
 */
public class AnrHeuristic extends AbstractHeuristic {

    /** Constant for JSON output */
    public static final String ANRS = "ANRS";

    private static final String HEURISTIC_NAME = "ANR";
    private static final String HEURISTIC_TYPE = "ANR_HEURISTIC";

    private LogcatItem mLogcat = null;

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
    public boolean failed() {
        if (mLogcat == null) {
            return false;
        }

        return (mLogcat.getAnrs().size() > 0);
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

        return String.format("Found %d ANR%s", mLogcat.getAnrs().size(),
                mLogcat.getAnrs().size() == 1 ? "" : "s");
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
        for (AnrItem anr : mLogcat.getAnrs()) {
            sb.append(anr.getStack());
            sb.append("\n\nLast lines of logcat\n");
            sb.append(anr.getLastPreamble());
            sb.append(String.format("\n\nLast lines of logcat for PID %d\n", anr.getPid()));
            sb.append(anr.getProcessPreamble());
            sb.append("\n\n");
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
            JSONArray anrs = new JSONArray();
            for (AnrItem anr : mLogcat.getAnrs()) {
                anrs.put(anr.toJson());
            }
            output.put(ANRS, anrs);
        } catch (JSONException e) {
            // Ignore
        }
        return output;
    }
}
