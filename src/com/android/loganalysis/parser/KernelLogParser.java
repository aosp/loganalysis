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

import com.android.loganalysis.item.KernelLogItem;
import com.android.loganalysis.item.MiscKernelLogItem;
import com.android.loganalysis.util.LogPatternUtil;
import com.android.loganalysis.util.LogTailUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* A {@link IParser} to parse {@code /proc/last_kmsg} and the output from {@code dmsg}.
*/
public class KernelLogParser implements IParser {
    public static final String KERNEL_RESET = "KERNEL_RESET";
    public static final String SELINUX_DENIAL = "SELINUX_DENIAL";

    /**
     * Matches: [     0.000000] Message<br />
     * Matches: &lt;3&gt;[     0.000000] Message
     */
    private static final Pattern LOG_LINE = Pattern.compile(
            "^(<\\d+>)?\\[\\s*(\\d+.\\d{6})\\] (.*)$");

    private KernelLogItem mKernelLog = null;
    private Double mStartTime = null;
    private Double mStopTime = null;

    private LogPatternUtil mPatternUtil = new LogPatternUtil();
    private LogTailUtil mPreambleUtil = new LogTailUtil();

    public KernelLogParser() {
        initPatterns();
    }

    /**
     * Parse a kernel log from a {@link BufferedReader} into an {@link KernelLogItem} object.
     *
     * @return The {@link KernelLogItem}.
     * @see #parse(List)
     */
    public KernelLogItem parse(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            parseLine(line);
        }
        commit();

        return mKernelLog;
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link KernelLogItem}.
     */
    @Override
    public KernelLogItem parse(List<String> lines) {
        for (String line : lines) {
            parseLine(line);
        }
        commit();

        return mKernelLog;
    }

    /**
     * Parse a line of input.
     *
     * @param line The line to parse
     */
    private void parseLine(String line) {
        if ("".equals(line.trim())) {
            return;
        }
        if (mKernelLog == null) {
            mKernelLog = new KernelLogItem();
        }
        Matcher m = LOG_LINE.matcher(line);
        if (m.matches()) {
            Double time = Double.parseDouble(m.group(2));
            String msg = m.group(3);

            if (mStartTime == null) {
                mStartTime = time;
            }
            mStopTime = time;

            checkAndAddKernelEvent(msg);

            mPreambleUtil.addLine(null, line);
        } else {
            checkAndAddKernelEvent(line);
        }
    }

    /**
     * Checks if a kernel log message matches a pattern and add a kernel event if it does.
     */
    private void checkAndAddKernelEvent(String message) {
        String category = mPatternUtil.checkMessage(message);
        if (category != null) {
            MiscKernelLogItem kernelLogItem = new MiscKernelLogItem();
            kernelLogItem.setEventTime(mStopTime);
            kernelLogItem.setPreamble(mPreambleUtil.getLastTail());
            kernelLogItem.setStack(message);
            kernelLogItem.setCategory(category);
            mKernelLog.addEvent(kernelLogItem);
        }
    }

    /**
     * Signal that the input has finished.
     */
    private void commit() {
        if (mKernelLog == null) {
            return;
        }
        mKernelLog.setStartTime(mStartTime);
        mKernelLog.setStopTime(mStopTime);
    }

    private void initPatterns() {
        // Kernel resets
        // TODO: Separate out device specific patterns
        mPatternUtil.addPattern(Pattern.compile("smem: DIAG.*"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("smsm: AMSS FATAL ERROR.*"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("kernel BUG at .*"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("PC is at .*"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("Internal error:.*"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile(
                "PVR_K:\\(Fatal\\): Debug assertion failed! \\[.*\\]"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("Kernel panic.*"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("BP panicked"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("WROTE DSP RAMDUMP"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("tegra_wdt: last reset due to watchdog timeout.*"),
                KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("Last reset was MPU Watchdog Timer reset.*"),
                KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("\\[MODEM_IF\\].*CRASH.*"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile(
                "Last boot reason: (?:kernel_panic|watchdogr?|hw_reset(?:$|\n)|PowerKey|Watchdog" +
                "|Panic)"), KERNEL_RESET);
        mPatternUtil.addPattern(Pattern.compile("Last reset was system watchdog timer reset"),
                KERNEL_RESET);

        // SELINUX denials
        mPatternUtil.addPattern(Pattern.compile(".*avc:\\s.*"), SELINUX_DENIAL);

    }

    /**
     * Get the internal {@link LogPatternUtil}. Exposed for unit testing.
     */
    LogPatternUtil getLogPatternUtil() {
        return mPatternUtil;
    }
}
