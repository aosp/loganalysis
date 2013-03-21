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
 * An {@link IItem} used to store miscellaneous logcat info.
 */
public class MiscLogcatItem extends GenericLogcatItem {
    public static final String TYPE = "MISC LOGCAT";

    private static final String CATEGORY = "CATEGORY";
    private static final String MESSAGE = "MESSAGE";

    private static final Set<String> ATTRIBUTES = new HashSet<String>(Arrays.asList(
            CATEGORY, MESSAGE));

    /**
     * The constructor for {@link MiscLogcatItem}.
     */
    public MiscLogcatItem() {
        super(TYPE, ATTRIBUTES);
    }

    /**
     * Get the category of the event.
     */
    public String getCategory() {
        return (String) getAttribute(CATEGORY);
    }

    /**
     * Set the category of the event.
     */
    public void setCategory(String category) {
        setAttribute(CATEGORY, category);
    }

    /**
     * Get the message for the event.
     */
    public String getMessage() {
        return (String) getAttribute(MESSAGE);
    }

    /**
     * Set the message for the event.
     */
    public void setMessage(String message) {
        setAttribute(MESSAGE, message);
    }
}
