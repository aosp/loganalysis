/*
 * Copyright (C) 2011 The Android Open Source Project
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

import com.android.loganalysis.item.LogcatItem;
import com.android.loganalysis.item.MiscLogcatItem;
import com.android.loganalysis.util.ArrayUtil;
import com.android.loganalysis.util.LogPatternUtil;
import com.android.loganalysis.util.LogTailUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link IParser} to handle logcat.  The parser can handle the time and threadtime logcat
 * formats.
 * <p>
 * Since the timestamps in the logcat do not have a year, the year can be set manually when the
 * parser is created or through {@link #setYear(String)}.  If a year is not set, the current year
 * will be used.
 * </p>
 */
public class LogcatParser implements IParser {
    public static final String ANR = "ANR";
    public static final String JAVA_CRASH = "JAVA_CRASH";
    public static final String NATIVE_CRASH = "NATIVE_CRASH";
    public static final String HIGH_CPU_USAGE = "HIGH_CPU_USAGE";
    public static final String HIGH_MEMORY_USAGE = "HIGH_MEMORY_USAGE";
    public static final String RUNTIME_RESTART = "RUNTIME_RESTART";

    /**
     * Match a single line of `logcat -v threadtime`, such as:
     * 05-26 11:02:36.886  5689  5689 D AndroidRuntime: CheckJNI is OFF
     */
    private static final Pattern THREADTIME_LINE = Pattern.compile(
            "^(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3})\\s+" +  /* timestamp [1] */
                "(\\d+)\\s+(\\d+)\\s+([A-Z])\\s+" +  /* pid/tid and log level [2-4] */
                "(.+?)\\s*: (.*)$" /* tag and message [5-6]*/);

    /**
     * Match a single line of `logcat -v time`, such as:
     * 06-04 02:32:14.002 D/dalvikvm(  236): GC_CONCURRENT freed 580K, 51% free [...]
     */
    private static final Pattern TIME_LINE = Pattern.compile(
            "^(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3})\\s+" +  /* timestamp [1] */
                "(\\w)/(.+?)\\(\\s*(\\d+)\\): (.*)$");  /* level, tag, pid, msg [2-5] */

    /**
     * Class for storing logcat meta data for a particular grouped list of lines.
     */
    private class LogcatData {
        public Integer mPid = null;
        public Integer mTid = null;
        public Date mTime = null;
        public String mLevel = null;
        public String mTag = null;
        public String mLastPreamble = null;
        public String mProcPreamble = null;
        public List<String> mLines = new LinkedList<String>();

        public LogcatData(Integer pid, Integer tid, Date time, String level, String tag,
                String lastPreamble, String procPreamble) {
            mPid = pid;
            mTid = tid;
            mTime = time;
            mLevel = level;
            mTag = tag;
            mLastPreamble = lastPreamble;
            mProcPreamble = procPreamble;
        }
    }

    private LogPatternUtil mPatternUtil = new LogPatternUtil();
    private LogTailUtil mPreambleUtil = new LogTailUtil();

    private String mYear = null;

    LogcatItem mLogcat = new LogcatItem();

    Map<String, LogcatData> mDataMap = new HashMap<String, LogcatData>();
    List<LogcatData> mDataList = new LinkedList<LogcatData>();

    private Date mStartTime = null;
    private Date mStopTime = null;

    /**
     * Constructor for {@link LogcatParser}.
     */
    public LogcatParser() {
        initPatterns();
    }

    /**
     * Constructor for {@link LogcatParser}.
     *
     * @param year The year as a string.
     */
    public LogcatParser(String year) {
        this();
        setYear(year);
    }

    /**
     * Sets the year for {@link LogcatParser}.
     *
     * @param year The year as a string.
     */
    public void setYear(String year) {
        mYear = year;
    }

    /**
     * Parse a logcat from a {@link BufferedReader} into an {@link LogcatItem} object.
     *
     * @param input a {@link BufferedReader}.
     * @return The {@link LogcatItem}.
     * @see #parse(List)
     */
    public LogcatItem parse(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            parseLine(line);
        }
        commit();

        return mLogcat;
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link LogcatItem}.
     */
    @Override
    public LogcatItem parse(List<String> lines) {
        for (String line : lines) {
            parseLine(line);
        }
        commit();

        return mLogcat;
    }

    /**
     * Parse a line of input.
     *
     * @param line The line to parse
     */
    private void parseLine(String line) {
        Integer pid = null;
        Integer tid = null;
        Date time = null;
        String level = null;
        String tag = null;
        String msg = null;

        Matcher m = THREADTIME_LINE.matcher(line);
        Matcher tm = TIME_LINE.matcher(line);
        if (m.matches()) {
            time = parseTime(m.group(1));
            pid = Integer.parseInt(m.group(2));
            tid = Integer.parseInt(m.group(3));
            level = m.group(4);
            tag = m.group(5);
            msg = m.group(6);
        } else if (tm.matches()) {
            time = parseTime(tm.group(1));
            level = tm.group(2);
            tag = tm.group(3);
            pid = Integer.parseInt(tm.group(4));
            msg = tm.group(5);
        } else {
            // CLog.w("Failed to parse line '%s'", line);
            return;
        }

        if (mStartTime == null) {
            mStartTime = time;
        }
        mStopTime = time;

        // ANRs are split when START matches a line.  The newest entry is kept in the dataMap
        // for quick lookup while all entries are added to the list.
        if ("E".equals(level) && "ActivityManager".equals(tag)) {
            String key = encodeLine(pid, tid, level, tag);
            LogcatData data;
            if (!mDataMap.containsKey(key) || AnrParser.START.matcher(msg).matches()) {
                data = new LogcatData(pid, tid, time, level, tag, mPreambleUtil.getLastTail(),
                        mPreambleUtil.getIdTail(pid));
                mDataMap.put(key, data);
                mDataList.add(data);
            } else {
                data = mDataMap.get(key);
            }
            data.mLines.add(msg);
        }

        // PID and TID are enough to separate Java and native crashes.
        if (("E".equals(level) && "AndroidRuntime".equals(tag)) ||
                ("I".equals(level) && "DEBUG".equals(tag))) {
            String key = encodeLine(pid, tid, level, tag);
            LogcatData data;
            if (!mDataMap.containsKey(key)) {
                data = new LogcatData(pid, tid, time, level, tag, mPreambleUtil.getLastTail(),
                        mPreambleUtil.getIdTail(pid));
                mDataMap.put(key, data);
                mDataList.add(data);
            } else {
                data = mDataMap.get(key);
            }
            data.mLines.add(msg);
        }

        // Check the message here but add it in commit()
        if (mPatternUtil.checkMessage(msg) != null) {
            LogcatData data = new LogcatData(pid, tid, time, level, tag,
                    mPreambleUtil.getLastTail(), mPreambleUtil.getIdTail(pid));
            data.mLines.add(msg);
            mDataList.add(data);
        }

        // After parsing the line, add it the the buffer for the preambles.
        mPreambleUtil.addLine(pid, line);
    }

    /**
     * Signal that the input has finished.
     */
    private void commit() {
        for (LogcatData data : mDataList) {
            MiscLogcatItem item = null;
            if ("E".equals(data.mLevel) && "ActivityManager".equals(data.mTag)) {
                // CLog.v("Parsing ANR: %s", data.mLines);
                item = new AnrParser().parse(data.mLines);
            } else if ("E".equals(data.mLevel) && "AndroidRuntime".equals(data.mTag)) {
                // CLog.v("Parsing Java crash: %s", data.mLines);
                item = new JavaCrashParser().parse(data.mLines);
            } else if ("I".equals(data.mLevel) && "DEBUG".equals(data.mTag)) {
                // CLog.v("Parsing native crash: %s", data.mLines);
                item = new NativeCrashParser().parse(data.mLines);
            } else {
                String msg = ArrayUtil.join("\n", data.mLines);
                String category = mPatternUtil.checkMessage(msg);
                if (category != null) {
                    MiscLogcatItem logcatItem = new MiscLogcatItem();
                    logcatItem.setCategory(category);
                    logcatItem.setStack(msg);
                    item = logcatItem;
                }
            }
            if (item != null) {
                item.setEventTime(data.mTime);
                item.setPid(data.mPid);
                item.setTid(data.mTid);
                item.setLastPreamble(data.mLastPreamble);
                item.setProcessPreamble(data.mProcPreamble);
                mLogcat.addEvent(item);
            }
        }

        mLogcat.setStartTime(mStartTime);
        mLogcat.setStopTime(mStopTime);
    }

    /**
     * Create an identifier that "should" be unique for a given logcat. In practice, we do use it as
     * a unique identifier.
     */
    private static String encodeLine(Integer pid, Integer tid, String level, String tag) {
        if (tid == null) {
            return String.format("%d|%s|%s", pid, level, tag);
        }
        return String.format("%d|%d|%s|%s", pid, tid, level, tag);
    }

    /**
     * Parse the timestamp and return a {@link Date}.  If year is not set, the current year will be
     * used.
     *
     * @param timeStr The timestamp in the format {@code MM-dd HH:mm:ss.SSS}.
     * @return The {@link Date}.
     */
    private Date parseTime(String timeStr) {
        // If year is null, just use the current year.
        if (mYear == null) {
            DateFormat yearFormatter = new SimpleDateFormat("yyyy");
            mYear = yearFormatter.format(new Date());
        }

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            return formatter.parse(String.format("%s-%s", mYear, timeStr));
        } catch (ParseException e) {
            // CLog.e("Could not parse time string %s", timeStr);
            return null;
        }
    }

    private void initPatterns() {
        // High CPU usage
        mPatternUtil.addPattern(Pattern.compile(".* timed out \\(is the CPU pegged\\?\\).*"),
                HIGH_CPU_USAGE);

        // High memory usage
        mPatternUtil.addPattern(Pattern.compile(
                "GetBufferLock timed out for thread \\d+ buffer .*"), HIGH_MEMORY_USAGE);

        // Runtime restarts
        mPatternUtil.addPattern(Pattern.compile("\\*\\*\\* WATCHDOG KILLING SYSTEM PROCESS.*"),
                RUNTIME_RESTART);
    }
}
