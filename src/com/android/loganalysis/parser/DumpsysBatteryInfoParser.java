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
package com.android.loganalysis.parser;

import com.android.loganalysis.item.DumpsysBatteryInfoItem;
import com.android.loganalysis.item.DumpsysBatteryInfoItem.WakeLockCategory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link IParser} to handle the "dumpsys batteryinfo" command output.
 */
public class DumpsysBatteryInfoParser implements IParser {
    private static final Pattern LAST_CHARGED_START_PAT = Pattern.compile(
            "^Statistics since last charge:$");
    private static final Pattern LAST_UNPLUGGED_START_PAT = Pattern.compile(
            "^Statistics since last unplugged:$");
    private static final Pattern WAKE_LOCK_START_PAT = Pattern.compile(
            "^  All partial wake locks:$");

    private static final String WAKE_LOCK_PAT_SUFFIX =
            "((\\d+)d )?((\\d+)h )?((\\d+)m )?((\\d+)s )?((\\d+)ms )?\\((\\d+) times\\) realtime";

    /**
     * Match a valid line such as:
     * "  Kernel Wake lock \"Process\": 1d 2h 3m 4s 5ms (6 times) realtime";
     */
    private static final Pattern KERNEL_WAKE_LOCK_PAT = Pattern.compile(
            "^  Kernel Wake lock \"([^\"]+)\": " + WAKE_LOCK_PAT_SUFFIX);
    /**
     * Match a valid line such as:
     * "  Wake lock #1234 Process: 1d 2h 3m 4s 5ms (6 times) realtime";
     */
    private static final Pattern WAKE_LOCK_PAT = Pattern.compile(
            "^  Wake lock #(\\d+) (.+): " + WAKE_LOCK_PAT_SUFFIX);

    private DumpsysBatteryInfoItem mItem = new DumpsysBatteryInfoItem();

    /**
     * {@inheritDoc}
     */
    @Override
    public DumpsysBatteryInfoItem parse(List<String> lines) {
        WakeLockCategory kernelWakeLockCategory = null;
        WakeLockCategory wakeLockCategory = null;
        boolean inKernelWakeLock = false;
        boolean inWakeLock = false;

        // Look for the section for last unplugged statistics.  Kernel wakelocks are in the lines
        // immediately following, until a blank line. Partial wake locks are in their own block,
        // until a blank line. Return immediately after since there is nothing left to parse.
        for (String line : lines) {
            if (kernelWakeLockCategory == null || wakeLockCategory == null) {
                Matcher m = LAST_CHARGED_START_PAT.matcher(line);
                if (m.matches()) {
                    kernelWakeLockCategory = WakeLockCategory.LAST_CHARGE_KERNEL_WAKELOCK;
                    wakeLockCategory = WakeLockCategory.LAST_CHARGE_WAKELOCK;
                    inKernelWakeLock = true;
                }
                m = LAST_UNPLUGGED_START_PAT.matcher(line);
                if (m.matches()) {
                    kernelWakeLockCategory = WakeLockCategory.LAST_UNPLUGGED_KERNEL_WAKELOCK;
                    wakeLockCategory = WakeLockCategory.LAST_UNPLUGGED_WAKELOCK;
                    inKernelWakeLock = true;
                }
            } else {
                if (inKernelWakeLock) {
                    if ("".equals(line.trim())) {
                        inKernelWakeLock = false;
                    } else {
                        parseKernelWakeLock(line, kernelWakeLockCategory);
                    }
                } else if (inWakeLock) {
                    if ("".equals(line.trim())) {
                        inWakeLock = false;
                        kernelWakeLockCategory = null;
                        wakeLockCategory = null;
                    } else {
                        parseWakeLock(line, wakeLockCategory);
                    }
                } else {
                    Matcher m = WAKE_LOCK_START_PAT.matcher(line);
                    if (m.matches()) {
                        inWakeLock = true;
                    }
                }
            }
        }
        return mItem;
    }

    /**
     * Parse a line of output and add it to the last unplugged kernel wake lock section.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    void parseKernelWakeLock(String line, WakeLockCategory category) {
        Matcher m = KERNEL_WAKE_LOCK_PAT.matcher(line);
        if (!m.matches()) {
            return;
        }

        final String name = m.group(1);
        final long days = parseLongOrZero(m.group(3));
        final long hours = parseLongOrZero(m.group(5));
        final long mins = parseLongOrZero(m.group(7));
        final long secs = parseLongOrZero(m.group(9));
        final long msecs = parseLongOrZero(m.group(11));
        final int timesCalled = Integer.parseInt(m.group(12));

        mItem.addWakeLock(name, getMs(days, hours, mins, secs, msecs), timesCalled, category);
    }

    /**
     * Parse a line of output and add it to the last unplugged wake lock section.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    void parseWakeLock(String line, WakeLockCategory category) {
        Matcher m = WAKE_LOCK_PAT.matcher(line);
        if (!m.matches()) {
            return;
        }

        final int number = Integer.parseInt(m.group(1));
        final String name = m.group(2);
        final long days = parseLongOrZero(m.group(4));
        final long hours = parseLongOrZero(m.group(6));
        final long mins = parseLongOrZero(m.group(8));
        final long secs = parseLongOrZero(m.group(10));
        final long msecs = parseLongOrZero(m.group(12));
        final int timesCalled = Integer.parseInt(m.group(13));

        mItem.addWakeLock(name, number, getMs(days, hours, mins, secs, msecs), timesCalled,
                category);
    }

    /**
     * Get the {@link DumpsysBatteryInfoItem}.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    DumpsysBatteryInfoItem getItem() {
        return mItem;
    }

    /**
     * Convert days/hours/mins/secs/msecs into milliseconds.
     * <p>
     * Exposed for unit testing.
     * </p>
     */
    static long getMs(long days, long hours, long mins, long secs, long msecs) {
        return (((24 * days + hours) * 60 + mins) * 60 + secs) * 1000 + msecs;
    }

    /**
     * Parses a string into a long, or returns 0 if the string is null.
     *
     * @param s a {@link String} containing the long representation to be parsed
     * @return the long represented by the argument in decimal, or 0 if the string is {@code null}.
     * @throws NumberFormatException if the string is not {@code null} or does not contain a
     * parsable long.
     */
    private long parseLongOrZero(String s) {
        if (s == null) {
            return 0;
        }
        return Long.parseLong(s);
    }
}
