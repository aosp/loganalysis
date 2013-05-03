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
import com.android.loganalysis.item.ConflictingItemException;
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
     * Add a {@link BugreportItem} to be checked.
     *
     * @param bugreport The {@link BugreportItem}.
     * @param timestamp The {@link Date} of the bugreport, may be {@code null}.
     * @param uri The URI of the bugreport, may be {@code null}.
     * @throws ConflictingItemException If the {@link BugreportItem} contains information which does
     * not match previously added {@link BugreportItem}s. This should only occur if the bugreports
     * did not come from the same run. For example, if there was a device restart or if the
     * bugreports are from multiple devices.
     */
    public void addBugreport(BugreportItem bugreport, Date timestamp, String uri)
            throws ConflictingItemException;

    /**
     * Add a {@link LogcatItem} to be checked.
     *
     * @param logcat The {@link LogcatItem}.
     * @param timestamp The {@link Date} of the logcat, may be {@code null}.
     * @param uri The URI of the logcat, may be {@code null}.
     * @throws ConflictingItemException If the {@link LogcatItem} contains information which does
     * not match previously added {@link LogcatItem}s. This should only occur if the logcats did not
     * come from the same run. For example, if there was a device restart or if logcats are from
     * multiple devices.
     */
    public void addLogcat(LogcatItem logcat, Date timestamp, String uri)
            throws ConflictingItemException;

    /**
     * Add a {@link KernelLogItem} to be checked.
     *
     * @param kernelLog The {@link KernelLogItem}.
     * @param timestamp The {@link Date} of the kernel log, may be {@code null}.
     * @param uri The URI of the kernel log, may be {@code null}.
     * @throws ConflictingItemException If the {@link KernelLogItem} contains information which does
     * not match previously added {@link KernelLogItem}s. This should only occur if the kernel logs
     * did not come from the same run. For example, if there was a device restart or if kernel logs
     * come from multiple devices.
     */
    public void addKernelLog(KernelLogItem kernelLog, Date timestamp, String uri)
            throws ConflictingItemException;

    /**
     * Add a {@link MemInfoItem} to be checked.
     *
     * @param meminfo The {@link MemInfoItem}.
     * @param timestamp The {@link Date} of the kernel log, may be {@code null}.
     * @param uri The URI of the kernel log, may be {@code null}.
     * @throws ConflictingItemException If the {@link MemInfoItem} contains information which does
     * not match previously added {@link MemInfoItem}s. This should only occur if the mem info did
     * not come from the same run. For example, if there was a device restart or if mem info came
     * from multiple devices.
     */
    public void addMemInfo(MemInfoItem meminfo, Date timestamp, String uri)
            throws ConflictingItemException;

    /**
     * Add a {@link ProcrankItem} to be checked.
     *
     * @param procrank The {@link ProcrankItem}.
     * @param timestamp The {@link Date} of the procrank output, may be {@code null}.
     * @param uri The URI of the procrank output, may be {@code null}.
     * @throws ConflictingItemException If the {@link ProcrankItem} contains information which does
     * not match previously added {@link ProcrankItem}s. This should only occur if the procranks
     * did not come from the same run. For example, if there was a device restart or if procranks
     * come from multiple devices.
     */
    public void addProcrank(ProcrankItem procrank, Date timestamp, String uri)
            throws ConflictingItemException;

    /**
     * Add a {@link TopItem} to be checked.
     *
     * @param top The {@link TopItem}.
     * @param timestamp The {@link Date} of the top output, may be {@code null}.
     * @param uri The URI of the top output, may be {@code null}.
     * @throws ConflictingItemException If the {@link TopItem} contains information which does not
     * match previously added {@link TopItem}s. This should only occur if the top info did not come
     * from the same run. For example, if there was a device restart or if top info came from
     * multiple devices.
     */
    public void addTop(TopItem top, Date timestamp, String uri)
            throws ConflictingItemException;

    /**
     * Add a {@link DumpsysItem} to be checked.
     *
     * @param dumpsys The {@link DumpsysItem}.
     * @param timestamp The {@link Date} of the dumpsys, may be {@code null}.
     * @param uri The URI of the dumpsys, may be {@code null}.
     * @throws ConflictingItemException If the {@link DumpsysItem} contains information which does
     * not match previously added {@link DumpsysItem}s. This should only occur if the dumpsys did
     * not come from the same run.  For example, if there was a device restart or if dumpsys came
     * from multiple devices.
     */
    public void addDumpsys(DumpsysItem dumpsys, Date timestamp, String uri)
            throws ConflictingItemException;

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
