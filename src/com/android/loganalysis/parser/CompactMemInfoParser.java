/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.loganalysis.item.CompactMemInfoItem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the compact meminfo output, from the 'dumpsys meminfo -c' command.
 * The output is a csv file which contains the information about how processes use memory.
 * For now we are only interested in the pss of the processes. So we only parse the lines
 * that start with proc and skip everything else.
 *
 * The format of the line is as follows:
 * "proc,[type],[name],[pid],[pss],[activities].
 *
 * Type is the type of the process for example native, cached, foreground, etc.
 * Name is the name of the process.
 * Activities indicates if a process has any activities associated with it.
 *
 */
public class CompactMemInfoParser implements IParser {

    private static final Pattern PROC_PREFIX = Pattern.compile(
            "proc,(.+),(.+),(\\d+),(\\d+),(.?)");

    /**
     * Parse compact meminfo log. Output a CompactMemInfoItem which contains
     * the list of processes, their pids and their pss.
     */
    @Override
    public CompactMemInfoItem parse(List<String> lines) {
        CompactMemInfoItem item = new CompactMemInfoItem();
        for (String line : lines) {
            Matcher m = PROC_PREFIX.matcher(line);
            if (!m.matches()) continue;

            if (m.groupCount() != 5) continue;

            String type = m.group(1);
            String name = m.group(2);
            int pid = Integer.parseInt(m.group(3));
            long pss = Long.parseLong(m.group(4));
            boolean activities = "a".equals(m.group(5));
            item.addPid(pid, name, type, pss, activities);
        }
        return item;
    }
}
