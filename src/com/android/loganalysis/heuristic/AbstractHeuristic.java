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
import com.android.loganalysis.item.KernelLogItem;
import com.android.loganalysis.item.LogcatItem;
import com.android.loganalysis.item.MemInfoItem;
import com.android.loganalysis.item.ProcrankItem;
import com.android.loganalysis.item.TopItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * An abstract {@link IHeuristic} which implements empty methods for all the add methods.
 */
public abstract class AbstractHeuristic implements IHeuristic {

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBugreport(BugreportItem bugreport, Date timestamp, String uri)
            throws ConflictingItemException {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLogcat(LogcatItem logcat, Date timestamp, String uri)
            throws ConflictingItemException {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addKernelLog(KernelLogItem kernelLog, Date timestamp, String uri)
            throws ConflictingItemException {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMemInfo(MemInfoItem meminfo, Date timestamp, String uri)
            throws ConflictingItemException {
        // Ignore
    }

    /**
     * {@inheritDoc}
     * @throws ConflictingItemException
     */
    @Override
    public void addProcrank(ProcrankItem procrank, Date timestamp, String uri)
            throws ConflictingItemException {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTop(TopItem top, Date timestamp, String uri)
            throws ConflictingItemException {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDumpsys(DumpsysItem dumpsys, Date timestamp, String uri)
            throws ConflictingItemException {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put(TYPE, getType());
            object.put(NAME, getName());
            object.put(STATUS, failed() ? FAILED : PASSED);
        } catch (JSONException e) {
            // Ignore
        }
        return object;
    }
}