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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A {@link IItem} used to store kernel log info.
 */
public class KernelLogItem extends GenericItem {

    /** Constant for JSON output */
    public static final String START_TIME = "START_TIME";
    /** Constant for JSON output */
    public static final String STOP_TIME = "STOP_TIME";
    /** Constant for JSON output */
    public static final String EVENTS = "EVENTS";

    private static final Set<String> ATTRIBUTES = new HashSet<String>(Arrays.asList(
            START_TIME, STOP_TIME, EVENTS));

    private class ItemList extends LinkedList<IItem> {
        private static final long serialVersionUID = -441685822528904595L;
    }

    /**
     * The constructor for {@link KernelLogItem}.
     */
    public KernelLogItem() {
        super(ATTRIBUTES);

        setAttribute(EVENTS, new ItemList());
    }

    /**
     * Get the start time of the kernel log.
     */
    public Double getStartTime() {
        return (Double) getAttribute(START_TIME);
    }

    /**
     * Set the start time of the kernel log.
     */
    public void setStartTime(Double time) {
        setAttribute(START_TIME, time);
    }

    /**
     * Get the stop time of the kernel log.
     */
    public Double getStopTime() {
        return (Double) getAttribute(STOP_TIME);
    }

    /**
     * Set the stop time of the kernel log.
     */
    public void setStopTime(Double time) {
        setAttribute(STOP_TIME, time);
    }

    /**
     * Get the list of all {@link IItem} events.
     */
    public List<IItem> getEvents() {
        return (ItemList) getAttribute(EVENTS);
    }

    /**
     * Add an {@link IItem} event to the end of the list of events.
     */
    public void addEvent(IItem event) {
        ((ItemList) getAttribute(EVENTS)).add(event);
    }

    /**
     * Get the list of all {@link MiscKernelLogItem} events for a category.
     */
    public List<MiscKernelLogItem> getMiscEvents(String category) {
        List<MiscKernelLogItem> items = new LinkedList<MiscKernelLogItem>();
        for (IItem item : getEvents()) {
            if (item instanceof MiscKernelLogItem &&
                    ((MiscKernelLogItem) item).getCategory().equals(category)) {
                items.add((MiscKernelLogItem) item);
            }
        }
        return items;
    }
}
