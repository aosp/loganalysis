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
package com.android.loganalysis.item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contains a list of processes and how much memory they are using. Generated from parsing
 * compact mem info file. Refer to CompactMemInfoParser for more details.
 */
public class CompactMemInfoItem implements IItem {
    private Map<Integer, Map<String, Object>> mPids = new HashMap<Integer, Map<String, Object>>();

    @Override
    public IItem merge(IItem other) throws ConflictingItemException {
        throw new ConflictingItemException("Compact meminfo items cannot be merged");
    }

    @Override
    public boolean isConsistent(IItem other) {
       return false;
    }

    @Override
    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONArray processes = new JSONArray();
        for(int pid : getPids()) {
            JSONObject proc = new JSONObject();
            try {
                proc.put("pid", pid);
                proc.put("name", getName(pid));
                proc.put("pss", getPss(pid));
                proc.put("type", getType(pid));
                proc.put("activities", hasActivities(pid));
                processes.put(proc);
            } catch (JSONException e) {
                // ignore
            }
        }
        try {
            object.put("processes", processes);
        } catch (JSONException e) {
            // ignore
        }
        return object;
    }

    /**
     * Get the list of pids of the processes that were added so far.
     * @return
     */
    public Set<Integer> getPids() {
        return mPids.keySet();
    }

    private Map<String, Object> get(int pid) {
        return mPids.get(pid);
    }

    /**
     * Adds a process to the list stored in this item.
     */
    public void addPid(int pid, String name, String type, long pss, boolean activities) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("name", name);
        attributes.put("type", type);
        attributes.put("pss", pss);
        attributes.put("activities", activities);
        mPids.put(pid, attributes);
    }

    /**
     * Returns the name of the process with a given pid.
     */
    public String getName(int pid) {
        return (String)get(pid).get("name");
    }

    /**
     * Return pss of the process with a given name.
     */
    public long getPss(int pid) {
        return (Long)get(pid).get("pss");
    }

    /**
     * Returns the type of the process with a given pid. Some possible types are native, cached,
     * foreground and etc.
     */
    public String getType(int pid) {
        return (String)get(pid).get("type");
    }

    /**
     * Returns true if a process has any activities assosiated with it. False otherwise.
     */
    public boolean hasActivities(int pid) {
        return (Boolean)get(pid).get("activities");
    }
}
