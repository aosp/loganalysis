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

import com.android.loganalysis.item.BugreportItem;
import com.android.loganalysis.item.DumpsysItem;
import com.android.loganalysis.item.IItem;
import com.android.loganalysis.item.KernelLogItem;
import com.android.loganalysis.item.LogcatItem;
import com.android.loganalysis.item.MemInfoItem;
import com.android.loganalysis.item.ProcrankItem;
import com.android.loganalysis.item.TopItem;

import org.json.JSONObject;

import java.util.Date;

/**
 * Interface for all heuristics used to determine if there are any problems in the logs.
 * <p>
 * Certain heuristics will care about different {@link IItem}s.  For example, a heuristic looking
 * for runtime restarts will look at the logcat and procrank but ignore the rest. It is recommended
 * to use {@link AbstractHeuristic} and implement the methods to only add the {@link IItem}s that
 * the heuristic cares about.
 * </p><p>
 * The {@code add()} methods are used to add {@link IItem}s which the heuristic cares about.  When
 * {@link #failed()} is called, it will only evaluate the {@link IItem}s which have been
 * added.  The same is true for {{@link #getSummary()} and {{@link #getDetails()}.
 * </p>
 */
public interface IHeuristic {

    /** Constant for JSON output */
    public static final String TYPE = "TYPE";
    /** Constant for JSON output */
    public static final String NAME = "NAME";
    /** Constant for JSON output */
    public static final String STATUS = "STATUS";
    /** Constant for JSON output */
    public static final String PASSED = "PASSED";
    /** Constant for JSON output */
    public static final String FAILED = "FAILED";

    /**
     * Add a bugreport item to be checked.
     */
    public void addBugreport(Date timestamp, BugreportItem bugreport);

    /**
     * Add a logcat item to be checked.
     */
    public void addLogcat(Date timestamp, LogcatItem logcat);

    /**
     * Add a kernel log item to be checked.
     */
    public void addKernelLog(Date timestamp, KernelLogItem kernelLog);

    /**
     * Add a memory info item to be checked.
     */
    public void addMemInfo(Date timestamp, MemInfoItem meminfo);

    /**
     * Add a procrank item to be checked.
     */
    public void addProcrank(Date timestamp, ProcrankItem procrank);

    /**
     * Add a top item to be checked.
     */
    public void addTop(Date timestamp, TopItem top);

    /**
     * Add a dumpsys item to be checked.
     */
    public void addDumpsys(Date timestamp, DumpsysItem dumpsys);

    /**
     * Checks to see if there are any problems.
     *
     * @return {@code true} if there is a problem, {@code false} if there is not.
     */
    public boolean failed();

    /**
     * Get the type of the heuristic.
     *
     * @return The type.
     */
    public String getType();

    /**
     * Get the name of the heuristic.
     *
     * @return The name of the heuristic, to be used for the output.
     */
    public String getName();

    /**
     * Get the summary of the problem.
     *
     * @return The summary as a string or {@code null} if there is no problem.
     */
    public String getSummary();

    /**
     * Get the problem details.
     *
     * @return The details as a string or {@code null} if there is no problem.
     */
    public String getDetails();

    /**
     * Get the JSON representation of the problem.
     *
     * @return The {@link JSONObject} representing the heuristic. JSON object must contain at least
     * the key {@link #STATUS} with either the value {@link #PASSED} or {@link #FAILED}, as well as
     * any additional information about the status.
     */
    public JSONObject toJson();
}
