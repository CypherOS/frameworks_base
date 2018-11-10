package com.android.systemui.smartspace;

import android.util.Log;

public class SmartSpaceData {

    SmartSpaceCard mCurrentCard;
    SmartSpaceCard mWeatherCard;

    public boolean hasWeather() {
        return mWeatherCard != null;
    }

    public boolean hasCurrent() {
        return mCurrentCard != null;
    }

    public long getExpiresAtMillis() {
        if (hasCurrent() && hasWeather()) {
            return Math.min(mCurrentCard.getExpiration(), mWeatherCard.getExpiration());
        }
        if (hasCurrent()) {
            return mCurrentCard.getExpiration();
        }
        if (hasWeather()) {
            return mWeatherCard.getExpiration();
        }
        return 0;
    }

    public void clear() {
        mWeatherCard = null;
        mCurrentCard = null;
    }

    public boolean handleExpire() {
        StringBuilder stringBuilder;
        boolean anyExpired = false;
        if (hasWeather() && mWeatherCard.isExpired()) {
            if (SmartSpaceController.DEBUG) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("weather expired ");
                stringBuilder.append(mWeatherCard.getExpiration());
                Log.d("SmartspaceData", stringBuilder.toString());
            }
            mWeatherCard = null;
            anyExpired = true;
        }
        if (!hasCurrent() || !mCurrentCard.isExpired()) {
            return anyExpired;
        }
        if (SmartSpaceController.DEBUG) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("current expired ");
            stringBuilder.append(mCurrentCard.getExpiration());
            Log.d("SmartspaceData", stringBuilder.toString());
        }
        mCurrentCard = null;
        return true;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append(mCurrentCard);
        stringBuilder.append(",");
        stringBuilder.append(mWeatherCard);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public SmartSpaceCard getWeatherCard() {
        return mWeatherCard;
    }

    public SmartSpaceCard getCurrentCard() {
        return mCurrentCard;
    }
}
