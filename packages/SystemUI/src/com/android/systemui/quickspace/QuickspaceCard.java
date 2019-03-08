/*
 * Copyright (C) 2018 CypherOS
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

package com.android.systemui.quickspace;

import com.android.internal.R;

import java.util.HashMap;
import java.util.Map;

public class QuickspaceCard {

    private int status;
    private String conditions;
    private int temperatureMetric;
    private int temperatureImperial;

    private int eventType;
    private String eventTitle;
    private String eventAction;

    public QuickspaceCard(int status, String conditions, int temperatureMetric, int temperatureImperial,
                int eventType, String eventTitle, String eventAction) {
        this.status = status;
        this.conditions = conditions;
        this.temperatureMetric = temperatureMetric;
        this.temperatureImperial = temperatureImperial;
        this.eventType = eventType;
        this.eventTitle = eventTitle;
        this.eventAction = eventAction;
    }

    public int getTemperature(boolean metric) {
        return metric ? this.temperatureMetric : this.temperatureImperial;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getConditions() {
        return this.conditions;
    }

    public int getEventType() {
        return this.eventType;
    }

    public String getEventTitle() {
        return this.eventTitle;
    }

    public String getEventAction() {
        return this.eventAction;
    }

    public int getWeatherIcon() {
        Map<String, Integer> conditions = new HashMap<>();
        conditions.put("partly-cloudy", R.drawable.weather_partly_cloudy);
        conditions.put("partly-cloudy-night", R.drawable.weather_partly_cloudy_night);
        conditions.put("mostly-cloudy", R.drawable.weather_mostly_cloudy);
        conditions.put("mostly-cloudy-night", R.drawable.weather_mostly_cloudy_night);
        conditions.put("cloudy", R.drawable.weather_cloudy);
        conditions.put("clear-night", R.drawable.weather_clear_night);
        conditions.put("mostly-clear-night", R.drawable.weather_mostly_clear_night);
        conditions.put("sunny", R.drawable.weather_sunny);
        conditions.put("mostly-sunny", R.drawable.weather_mostly_sunny);
        conditions.put("scattered-showers", R.drawable.weather_scattered_showers);
        conditions.put("scattered-showers-night", R.drawable.weather_scattered_showers_night);
        conditions.put("rain", R.drawable.weather_rain);
        conditions.put("windy", R.drawable.weather_windy);
        conditions.put("snow", R.drawable.weather_snow);
        conditions.put("scattered-thunderstorms", R.drawable.weather_isolated_scattered_thunderstorms);
        conditions.put("scattered-thunderstorms-night", R.drawable.weather_isolated_scattered_thunderstorms_night);
        conditions.put("isolated-thunderstorms", R.drawable.weather_isolated_scattered_thunderstorms);
        conditions.put("isolated-thunderstorms-night", R.drawable.weather_isolated_scattered_thunderstorms_night);
        conditions.put("thunderstorms", R.drawable.weather_thunderstorms);
        conditions.put("foggy", R.drawable.weather_foggy);
        for (String condition : conditions.keySet()) {
            if (getConditions().equals(condition)) {
                return conditions.get(condition);
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "QuickspaceCard: " +
                "status=" + getStatus() + "," +
                "conditions=" + getConditions() + "," +
                "temperatureMetric=" + getTemperature(true) + "," +
                "temperatureImperial=" + getTemperature(false) + "," +
                "eventType=" + getEventType() + "," +
                "eventTitle=" + getEventTitle() + "," +
                "eventAction=" + getEventAction();
    }
}