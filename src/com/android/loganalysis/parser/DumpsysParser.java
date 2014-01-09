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
import com.android.loganalysis.item.DumpsysItem;

import java.util.List;

/**
 * A {@link IParser} to handle the output of the dumpsys section of the bugreport.
 */
public class DumpsysParser extends AbstractSectionParser {
    private static final String BATTERY_INFO_SECTION_REGEX = "DUMP OF SERVICE batteryinfo:";
    private static final String NOOP_SECTION_REGEX = "DUMP OF SERVICE .*";

    private DumpsysBatteryInfoParser mBatteryInfoParser = new DumpsysBatteryInfoParser();
    private DumpsysItem mDumpsys = null;

    /**
     * {@inheritDoc}
     *
     * @return The {@link DumpsysItem}
     */
    @Override
    public DumpsysItem parse(List<String> lines) {
        setup();
        for (String line : lines) {
            if (mDumpsys == null && !"".equals(line.trim())) {
                mDumpsys = new DumpsysItem();
            }
            parseLine(line);
        }
        commit();

        return mDumpsys;
    }

    /**
     * Sets up the parser by adding the section parsers.
     */
    protected void setup() {
        addSectionParser(mBatteryInfoParser, BATTERY_INFO_SECTION_REGEX);
        addSectionParser(new NoopParser(), NOOP_SECTION_REGEX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void commit() {
        // signal EOF
        super.commit();

        if (mDumpsys != null) {
            mDumpsys.setBatteryInfo(
                    (DumpsysBatteryInfoItem) getSection(mBatteryInfoParser));
        }
    }
}
