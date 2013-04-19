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
import java.util.Set;

/**
 * An {@link IItem} used to store the output of the dumpsys section of the bugreport.
 */
public class DumpsysItem extends GenericItem {

    /** Constant for JSON output */
    private static final String BATTERY_INFO = "BATTERY_INFO";

    private static final Set<String> ATTRIBUTES = new HashSet<String>(Arrays.asList(BATTERY_INFO));

    /**
     * The constructor for {@link BugreportItem}.
     */
    public DumpsysItem() {
        super(ATTRIBUTES);
    }

    /**
     * Get the battery info section of the dumpsys.
     */
    public DumpsysBatteryInfoItem getBatteryInfo() {
        return (DumpsysBatteryInfoItem) getAttribute(BATTERY_INFO);
    }

    /**
     * Set the battery info section of the dumpsys.
     */
    public void setBatteryInfo(DumpsysBatteryInfoItem batteryInfo) {
        setAttribute(BATTERY_INFO, batteryInfo);
    }
}
