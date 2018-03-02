/*
 * Copyright (C) 2017-2018 Google Inc.
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
 
package com.android.systemui.ambientmusic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.ambientmusic.AmbientIndicationInflateListener;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.doze.DozeReceiver;
import com.android.systemui.statusbar.phone.StatusBar;

public class AmbientIndicationContainer extends AutoReinflateContainer implements DozeReceiver {

    private View mAmbientIndication;
    private boolean mDozing;
    private ImageView mIcon;
    private StatusBar mStatusBar;
    private TextView mText;
    private Context mContext;

	// Ambient Play
    private String mTrackName;
    private String mArtistName;
	
	// Ambient Weather
    private String mTemp;
    private String mCity;
	private boolean mIsAmbientPlay;
	private Drawable mConditionCode;

    public AmbientIndicationContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
    }

    public void hideIndication() {
        setIndication(null, null);
    }
	
	public void hideWeatherIndication() {
        setWeatherIndication(null, null, null);
    }

    public void initializeView(StatusBar statusBar) {
        mStatusBar = statusBar;
        addInflateListener(new AmbientIndicationInflateListener(this));
    }

    public void updateAmbientIndicationView(View view) {
        mAmbientIndication = findViewById(R.id.ambient_indication);
        mText = (TextView)findViewById(R.id.ambient_indication_text);
        mIcon = (ImageView)findViewById(R.id.ambient_indication_icon);
        setIndication(mTrackName, mArtistName);
		setWeatherIndication(mTemp, mCity, mConditionCode);
    }

    @Override
    public void setDozing(boolean dozing) {
        mDozing = dozing;
    }

    public void setIndication(String trackName, String artistName) {
        mText.setText(String.format(mContext.getResources().getString(R.string.ambient_play_track_information),
                      trackName, artistName));
		mIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_music_note_24dp));
        mTrackName = trackName;
        mArtistName = artistName;
        mAmbientIndication.setClickable(false);
        if (trackName == null && artistName == null) {
			mIsAmbientPlay = false;
            mAmbientIndication.setVisibility(View.INVISIBLE);
        } else {
			mIsAmbientPlay = true;
            mAmbientIndication.setVisibility(View.VISIBLE);
        }
    }
	
	public void setWeatherIndication(String temp, String city, Drawable conditionCode) {
        mText.setText(String.format(mContext.getResources().getString(R.string.ambient_weather_condition_information),
                      temp, city));
		if (conditionCode instanceof VectorDrawable) {
			conditionCode.setTint(mContext.getResources().getColor(android.R.color.white););
		}
		mIcon.setImageDrawable(conditionCode);
        mTemp = temp;
        mCity = city;
		mConditionCode = conditionCode;
        mAmbientIndication.setClickable(false);
        if (temp == null && city == null) {
            mAmbientIndication.setVisibility(View.INVISIBLE);
        } else {
			if (!mIsAmbientPlay) {
				mAmbientIndication.setVisibility(View.VISIBLE);
			}
        }
    }
}