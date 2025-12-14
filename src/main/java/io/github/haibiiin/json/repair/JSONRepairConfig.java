/*
 * Copyright 2024 HAibiiin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.haibiiin.json.repair;

import java.util.Properties;

public class JSONRepairConfig {

    private final Properties properties;

    public JSONRepairConfig() {
        this.properties = new Properties();
        this.properties.put(Property.MAX_TRY_TIMES.name(), 20);
        this.properties.put(Property.EXTRACT_JSON.name(), false);
        this.properties.put(Property.LINE_FEED.name(), true);
    }

    public int maxTryTimes() {
        return (int) this.properties.getOrDefault(Property.MAX_TRY_TIMES.name(), 20);
    }

    public JSONRepairConfig maxTryTimes(int value) {
        this.properties.put(Property.MAX_TRY_TIMES.name(), value);
        return this;
    }

    public boolean extractJSON() {
        return (boolean) this.properties.getOrDefault(Property.EXTRACT_JSON.name(), false);
    }

    public JSONRepairConfig enableExtractJSON() {
        this.properties.put(Property.EXTRACT_JSON.name(), true);
        return this;
    }

    public boolean lineFeed() {
        return (boolean) this.properties.getOrDefault(Property.LINE_FEED.name(), true);
    }

    public JSONRepairConfig removeLineFeed() {
        this.properties.put(Property.LINE_FEED.name(), false);
        return this;
    }

    public enum Property {
        EXTRACT_JSON,
        MAX_TRY_TIMES,
        LINE_FEED;
    }
}
