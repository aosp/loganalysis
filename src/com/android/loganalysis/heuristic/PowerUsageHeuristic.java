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

import com.android.loganalysis.item.DumpsysBatteryInfoItem;
import com.android.loganalysis.item.DumpsysBatteryInfoItem.WakeLock;
import com.android.loganalysis.item.DumpsysItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * A {@link IHeuristic} to detect if there are any power issues such as held wake locks.
 */
public class PowerUsageHeuristic extends AbstractHeuristic {

    /** Constant for JSON output */
    public static final String CUTOFF = "CUTOFF";
    /** Constant for JSON output */
    public static final String KERNEL_WAKE_LOCKS = "KERNEL_WAKE_LOCKS";
    /** Constant for JSON output */
    public static final String WAKE_LOCKS = "WAKE_LOCKS";

    private static final String HEURISTIC_NAME = "Power usage";
    private static final String HEURISTIC_TYPE = "POWER_USAGE_HEURISTIC";

    // TODO: Make this value configurable.
    private static final long WAKE_LOCK_TIME_CUTOFF = 30 * 60 * 1000;  // 30 minutes

    private DumpsysBatteryInfoItem mBatteryInfo = null;

    /**
     * {@inheritDoc}
     */
    public void addDumpsys(Date timestamp, DumpsysItem dumpsys) {
        mBatteryInfo = dumpsys.getBatteryInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean failed() {
        if (mBatteryInfo == null) {
            return false;
        }

        for (WakeLock wakeLock : mBatteryInfo.getLastUnpluggedWakeLocks()) {
            if (wakeLock.getHeldTime() > WAKE_LOCK_TIME_CUTOFF) {
                return true;
            }
        }

        for (WakeLock wakeLock :
                mBatteryInfo.getLastUnpluggedKernelWakeLocks()) {
            if (wakeLock.getHeldTime() > WAKE_LOCK_TIME_CUTOFF) {
                return true;
            }
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

        int wakeLockCounter = 0;
        for (WakeLock wakeLock : mBatteryInfo.getLastUnpluggedWakeLocks()) {
            if (wakeLock.getHeldTime() > WAKE_LOCK_TIME_CUTOFF) {
                wakeLockCounter++;
            }
        }
        int kernelWakeLockCounter = 0;
        for (WakeLock wakeLock : mBatteryInfo.getLastUnpluggedKernelWakeLocks()) {
            if (wakeLock.getHeldTime() > WAKE_LOCK_TIME_CUTOFF) {
                kernelWakeLockCounter++;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (wakeLockCounter > 0) {
            sb.append(String.format("%d wake lock%s helder longer than %s", wakeLockCounter,
                    wakeLockCounter == 1 ? "" : "s", formatTime(WAKE_LOCK_TIME_CUTOFF)));
        }
        if (wakeLockCounter >= 0 && kernelWakeLockCounter >= 0) {
            sb.append(", ");
        }
        if (kernelWakeLockCounter > 0) {
            sb.append(String.format("%d kernel wake lock%s helder longer than %s",
                    kernelWakeLockCounter, kernelWakeLockCounter == 1 ? "" : "s",
                    formatTime(WAKE_LOCK_TIME_CUTOFF)));
        }

        return sb.toString();
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
        for (WakeLock wakeLock : mBatteryInfo.getLastUnpluggedWakeLocks()) {
            if (wakeLock.getHeldTime() > WAKE_LOCK_TIME_CUTOFF) {
                sb.append(String.format("Wake lock \"%s\" #%d held for %s (%s times)\n",
                        wakeLock.getName(), wakeLock.getNumber(),
                        formatTime(wakeLock.getHeldTime()), wakeLock.getLockedCount()));
            }
        }
        for (WakeLock wakeLock : mBatteryInfo.getLastUnpluggedKernelWakeLocks()) {
            if (wakeLock.getHeldTime() > WAKE_LOCK_TIME_CUTOFF) {
                sb.append(String.format("Kernel wake lock \"%s\" held for %s (%s times)\n",
                        wakeLock.getName(), formatTime(wakeLock.getHeldTime()),
                        wakeLock.getLockedCount()));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJson() {
        JSONObject output = super.toJson();
        try {
            output.put(CUTOFF, WAKE_LOCK_TIME_CUTOFF);

            JSONArray kernelWakeLocks = new JSONArray();
            for (WakeLock wakeLock : mBatteryInfo.getLastUnpluggedKernelWakeLocks()) {
                kernelWakeLocks.put(wakeLock.toJson());
            }
            output.put(KERNEL_WAKE_LOCKS, kernelWakeLocks);

            JSONArray wakeLocks = new JSONArray();
            for (WakeLock wakeLock : mBatteryInfo.getLastUnpluggedWakeLocks()) {
                wakeLocks.put(wakeLock.toJson());
            }
            output.put(WAKE_LOCKS, wakeLocks);
        } catch (JSONException e) {
            // Ignore
        }
        return output;
    }

    /**
     * Convert the time in milliseconds into a readable string broken into d/h/m/s/ms.
     */
    private static String formatTime(long time) {
        long msecs = time % 1000;
        time /= 1000;  // Convert to secs
        long secs = time % 60;
        time /= 60;  // Convert to mins
        long mins = time % 60;
        time /= 60;  // Convert to hours
        long hours = time % 24;
        time /= 24;  // Convert to days

        StringBuilder sb = new StringBuilder();
        if (time != 0) {
            sb.append(time);
            sb.append("d ");
        }
        if (hours != 0) {
            sb.append(hours);
            sb.append("h ");
        }
        if (mins != 0) {
            sb.append(mins);
            sb.append("m ");
        }
        if (secs != 0) {
            sb.append(secs);
            sb.append("s ");
        }
        if (msecs != 0) {
            sb.append(msecs);
            sb.append("ms ");
        }
        return sb.toString().trim();
    }

    /**
     * Get the wake lock held time threshold.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    long getCutoff() {
        return WAKE_LOCK_TIME_CUTOFF;
    }
}
