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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An {@link IItem} used to store the battery info part of the dumpsys output.
 */
public class DumpsysBatteryInfoItem implements IItem {

    /** Constant for JSON output */
    public static final String WAKELOCKS = "WAKELOCKS";
    /** Constant for JSON output */
    public static final String KERNEL_WAKELOCKS = "KERNEL_WAKELOCKS";

    /**
     * A class designed to store information related to wake locks and kernel wake locks.
     */
    public static class WakeLock extends GenericItem {

        /** Constant for JSON output */
        public static final String NAME = "NAME";
        /** Constant for JSON output */
        public static final String NUMBER = "NUMBER";
        /** Constant for JSON output */
        public static final String HELD_TIME = "HELD_TIME";
        /** Constant for JSON output */
        public static final String LOCKED_COUNT = "LOCKED_COUNT";

        private static final Set<String> ATTRIBUTES = new HashSet<String>(Arrays.asList(
                NAME, NUMBER, HELD_TIME, LOCKED_COUNT));

        /**
         * The constructor for {@link WakeLock}
         *
         * @param name The name of the wake lock
         * @param heldTime The amount of time held in milliseconds
         * @param lockedCount The number of times the wake lock was locked
         */
        public WakeLock(String name, long heldTime, int lockedCount) {
            this(name, null, heldTime, lockedCount);
        }

        /**
         * The constructor for {@link WakeLock}
         *
         * @param name The name of the wake lock
         * @param number The number of the wake lock
         * @param heldTime The amount of time held in milliseconds
         * @param lockedCount The number of times the wake lock was locked
         */
        public WakeLock(String name, Integer number, long heldTime, int lockedCount) {
            super(ATTRIBUTES);

            setAttribute(NAME, name);
            setAttribute(NUMBER, number);
            setAttribute(HELD_TIME, heldTime);
            setAttribute(LOCKED_COUNT, lockedCount);
        }

        /**
         * Get the name of the wake lock.
         */
        public String getName() {
            return (String) getAttribute(NAME);
        }

        /**
         * Get the number of the wake lock.
         */
        public Integer getNumber() {
            return (Integer) getAttribute(NUMBER);
        }

        /**
         * Get the time the wake lock was held in milliseconds.
         */
        public long getHeldTime() {
            return (Long) getAttribute(HELD_TIME);
        }

        /**
         * Get the number of times the wake lock was locked.
         */
        public int getLockedCount() {
            return (Integer) getAttribute(LOCKED_COUNT);
        }
    }

    private List<WakeLock> mLastUnpluggedKernelWakeLocks = new LinkedList<WakeLock>();
    private List<WakeLock> mLastUnpluggedWakeLocks = new LinkedList<WakeLock>();

    /**
     * Add a kernel wake lock from the last unplugged section of the battery info.
     */
    public void addLastUnpluggedKernelWakeLock(String name, long heldTime, int timesCalled) {
        mLastUnpluggedKernelWakeLocks.add(new WakeLock(name, heldTime, timesCalled));
    }

    /**
     * Add a wake lock from the last unplugged section of the battery info.
     */
    public void addLastUnpluggedWakeLock(String name, int number, long heldTime, int timesCalled) {
        mLastUnpluggedWakeLocks.add(new WakeLock(name, number, heldTime, timesCalled));
    }

    /**
     * Get the list of kernel wake locks from the last unplugged section of the battery info.
     */
    public List<WakeLock> getLastUnpluggedKernelWakeLocks() {
        return mLastUnpluggedKernelWakeLocks;
    }

    /**
     * Get the list of wake locks from the last unplugged section of the battery info.
     */
    public List<WakeLock> getLastUnpluggedWakeLocks() {
        return mLastUnpluggedWakeLocks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IItem merge(IItem other) throws ConflictingItemException {
        throw new ConflictingItemException("Dumpsys battery info items cannot be merged");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConsistent(IItem other) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            JSONArray kernelWakeLocks = new JSONArray();
            for (WakeLock wakeLock : mLastUnpluggedKernelWakeLocks) {
                kernelWakeLocks.put(wakeLock.toJson());
            }
            object.put(KERNEL_WAKELOCKS, kernelWakeLocks);

            JSONArray wakeLocks = new JSONArray();
            for (WakeLock wakeLock : mLastUnpluggedWakeLocks) {
                wakeLocks.put(wakeLock.toJson());
            }
            object.put(WAKELOCKS, wakeLocks);
        } catch (JSONException e) {
            // Ignore
        }
        return object;
    }
}
