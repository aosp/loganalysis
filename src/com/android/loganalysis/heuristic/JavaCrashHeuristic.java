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

import com.android.loganalysis.item.JavaCrashItem;
import com.android.loganalysis.item.LogcatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A {@link IHeuristic} to detect if there are any Java crashes.
 */
public class JavaCrashHeuristic extends AbstractHeuristic {

    /** Constant for JSON output */
    public static final String JAVA_CRASHES = "JAVA_CRASHES";

    private static final String HEURISTIC_NAME = "Java crash";
    private static final String HEURISTIC_TYPE = "JAVA_CRASH_HEURISTIC";

    private LogcatItem mLogcat = null;

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
    public boolean failed() {
        if (mLogcat == null) {
            return false;
        }

        return (mLogcat.getJavaCrashes().size() > 0);
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

        return String.format("Found %d Java crash%s", mLogcat.getJavaCrashes().size(),
                mLogcat.getJavaCrashes().size() == 1 ? "" : "es");
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
        for (JavaCrashItem crash : mLogcat.getJavaCrashes()) {
            sb.append(crash.getStack());
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
            JSONArray javaCrashes = new JSONArray();
            for (JavaCrashItem jc : mLogcat.getJavaCrashes()) {
                javaCrashes.put(jc.toJson());
            }
            output.put(JAVA_CRASHES, javaCrashes);
        } catch (JSONException e) {
            // Ignore
        }
        return output;
    }
}
