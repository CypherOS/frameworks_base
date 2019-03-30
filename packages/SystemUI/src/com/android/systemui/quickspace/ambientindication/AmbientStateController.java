/*
 * Copyright (C) 2019 CypherOS
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
 * limitations under the License
 */

package com.android.systemui.quickspace.ambientindication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.FloatProperty;
import android.view.animation.Interpolator;

import com.android.systemui.Interpolators;

public class AmbientStateController {

    private static AmbientStateController sController;
    private static final FloatProperty<AmbientStateController> SET_DARK_AMOUNT_PROPERTY = new FloatProperty<AmbientStateController>("mDozeAmount") {
        public void setValue(AmbientStateController controller, float ammount) {
            controller.setDozeAmountInternal(ammount);
        }

        public Float get(AmbientStateController controller) {
            return Float.valueOf(controller.mDozeAmount);
        }
    };

    private ValueAnimator mDarkAnimator;
    private float mDozeAmount;
    private float mDozeAmountTarget;
    private Interpolator mDozeInterpolator = Interpolators.FAST_OUT_SLOW_IN;
    private boolean mIsDozing;
    private StateListener mListener;

    public static AmbientStateController getInstance(Context context) {
        if (sController == null) {
            sController = new AmbientStateController(context);
        }
        return sController;
    }

    public AmbientStateController(Context context) {
        sController = this;
    }

    public boolean setDozing(boolean isDozing) {
        if (mIsDozing == isDozing) {
            return false;
        }
        mIsDozing = isDozing;
        if (mListener != null) {
            mListener.onDozingChanged(isDozing);
        }
        return true;
    }

    public void setDozeAmount(float ammount, boolean animate) {
        if (mDarkAnimator != null && mDarkAnimator.isRunning()) {
            if (!animate || mDozeAmountTarget != ammount) {
                mDarkAnimator.cancel();
            } else {
                return;
            }
        }
        mDozeAmountTarget = ammount;
        if (animate) {
            startDozeAnimation();
        } else {
            setDozeAmountInternal(ammount);
        }
    }

    private void startDozeAnimation() {
        if (mDozeAmount == 0.0f || mDozeAmount == 1.0f) {
            mDozeInterpolator = mIsDozing ? Interpolators.FAST_OUT_SLOW_IN : Interpolators.TOUCH_RESPONSE_REVERSE;
        }
        mDarkAnimator = ObjectAnimator.ofFloat(this, SET_DARK_AMOUNT_PROPERTY, new float[]{mDozeAmountTarget});
        mDarkAnimator.setInterpolator(Interpolators.LINEAR);
        mDarkAnimator.setDuration(500);
        mDarkAnimator.start();
    }

    private void setDozeAmountInternal(float ammount) {
        mDozeAmount = ammount;
        ammount = mDozeInterpolator.getInterpolation(ammount);
        if (mListener != null) {
            mListener.onDozeAmountChanged(mDozeAmount, ammount);
        }
    }

    public void addListener(StateListener listener) {
        mListener = listener;
    }
  
    public void removeListener() {
        mListener = null;
    }

    public interface StateListener {
        void onDozeAmountChanged(float oldAmmount, float newAmmount);

        void onDozingChanged(boolean isDozing);
    }
}