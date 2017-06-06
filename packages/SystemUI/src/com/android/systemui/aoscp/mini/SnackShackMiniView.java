/*
 * Copyright 2016 ParanoidAndroid Project
 * Copyright 2017 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.aoscp.mini;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.aoscp.mini.SnackShackMiniHelper;

/**
 * Snackshack view for use with OnTheSpot & SnackShackMini.
 */
public class SnackShackMiniView extends RelativeLayout {

    /** Whether to output debugging information to logs. */
    private static final boolean DEBUG = SnackShackMiniHelper.DEBUG;
    /** Log output tag. */
    private static final String LOG_TAG = SnackShackMiniHelper.LOG_TAG;

    /** Amount of milliseconds for how long the animations should last. */
    private static final int ANIMATION_DURATION = 250;
    /** Amount of milliseconds for how long to show the snackshack view. */
    private static final int TIMEOUT_DURATION = 30000;

    /** Runnable for scheduling hiding. */
    private final Runnable mHideRunnable = new Runnable() {

        @Override
        public void run() {
            hide();
        }

    };

    /** Main handler to do any serious main work in. Fallback for callbacks. */
    private final Handler mMainHandler;
	
    /** Description text view for informing the user. */
    private TextView mDescription = null;

    /** Handler to send callbacks on. */
    private Handler mCallbackHandler = null;

    /**
     * Constructs the snackshack view object.
     *
     * @param context  {@link Context} the view is going to run in
     * @param attrs  {@link AttributeSet} of the XML tag that is inflating the view
     */
    public SnackShackMiniView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setTranslationY(getHeight());
        setVisibility(View.GONE);

        mDescription = (TextView) findViewById(R.id.mini_description);
    }

    /**
     * Shows the snackshack.
     *
     * @param message  {@link String} message to display to the user
     * @param listener  {@link SnackShackMiniHelper.OnSettingChoiceListener} to notify
     *                  about the choice
     * @param handler  {@link Handler} to notify the listener on,
     *                 or null to notify it on the UI thread instead
     */
    public void show(final String message, Handler handler) {
        if (message == null) {
            throw new IllegalArgumentException("message == null");
        }
        if (handler == null) {
            handler = mMainHandler;
        }

        if (DEBUG) Log.d(LOG_TAG, "Showing the snackshack view");

        mDescription.setText(message);
        mCallbackHandler = handler;

        animate().translationY(getHeight())
                .setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                        android.R.interpolator.fast_out_slow_in))
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(final Animator animation) {
                        setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        animate().translationY(0f)
                                .setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                                        android.R.interpolator.fast_out_slow_in))
                                .setDuration(ANIMATION_DURATION)
                                .start();
                    }
                }).start();

        mMainHandler.removeCallbacks(mHideRunnable);
        mMainHandler.postDelayed(mHideRunnable, TIMEOUT_DURATION);
    }

    /**
     * Hides the snackshack.
     */
    public void hide() {
        if (DEBUG) Log.d(LOG_TAG, "Hiding the snackshack view");

        animate().translationY(getHeight())
                .setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                        android.R.interpolator.fast_out_slow_in))
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        setVisibility(View.GONE);
                    }
                }).start();
    }

}