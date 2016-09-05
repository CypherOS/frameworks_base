/*
 * Copyright (C) 2015 The ParanoidAndroid Project
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

package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.QSTile;
import com.android.systemui.volume.SegmentedButtons;

/** Quick settings tile: Immersive mode **/
public class ImmersiveTile extends QSTile<QSTile.BooleanState> {
    private static final int IMMERSIVE_OFF = 0;
    private static final int IMMERSIVE_FLAGS_FULL = View.SYSTEM_DESIGN_FLAG_IMMERSIVE_STATUS |
                                          View.SYSTEM_DESIGN_FLAG_IMMERSIVE_NAV;

    private final AnimationIcon mDisableFull =
            new AnimationIcon(R.drawable.ic_immersive_full_disable_animation,
                     R.drawable.ic_qs_immersive_off);
    private final AnimationIcon mDisableStatusBar =
            new AnimationIcon(R.drawable.ic_immersive_status_bar_disable_animation,
                     R.drawable.ic_qs_immersive_off);
    private final AnimationIcon mDisableNavBar =
            new AnimationIcon(R.drawable.ic_immersive_nav_bar_disable_animation,
                     R.drawable.ic_qs_immersive_off);

    private final SecureSetting mSetting;
    private final ImmersiveDetailAdapter mDetailAdapter;

    private boolean mListening;
    private int mLastState;

    public ImmersiveTile(Host host) {
        super(host);

        mSetting = new SecureSetting(mContext, mHandler, Secure.SYSTEM_DESIGN_FLAGS) {
            @Override
            protected void handleValueChanged(int value, boolean observedChange) {
                handleRefreshState(value);
            }
        };
        mDetailAdapter = new ImmersiveDetailAdapter();
        mLastState = Secure.getIntForUser(mContext.getContentResolver(),
                Secure.LAST_SYSTEM_DESIGN_FLAGS,
                IMMERSIVE_FLAGS_FULL, UserHandle.USER_CURRENT);
    }

    @Override
    public DetailAdapter getDetailAdapter() {
        return mDetailAdapter;
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
        mSetting.setUserId(newUserId);
        handleRefreshState(mSetting.getValue());
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent();
    }

    @Override
    public void handleClick() {
        MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);
        setEnabled(!mState.value);
        if (!mState.value) showDetail(true);
    }

    @Override
    protected void handleSecondaryClick() {
        // Secondary clicks are header clicks, just toggle.
        MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);
        setEnabled(!mState.value);
    }

    private void setEnabled(boolean enabled) {
        mSetting.setValue(enabled ? mLastState : IMMERSIVE_OFF);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_immersive_mode_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final int value = mSetting.getValue();
        state.value = value != IMMERSIVE_OFF;
        switch (value) {
            case IMMERSIVE_FLAGS_FULL:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_immersive_full);
                state.label = mContext.getString(R.string.quick_settings_immersive_mode_label_hide_all);
                state.contentDescription =  mContext.getString(
                        R.string.accessibility_quick_settings_immersive_mode_full);
                break;
            case View.SYSTEM_DESIGN_FLAG_IMMERSIVE_NAV:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_immersive_nav_bar);
                state.contentDescription =  mContext.getString(
                        R.string.accessibility_quick_settings_immersive_mode_hide_status_bar);
                break;
            case View.SYSTEM_DESIGN_FLAG_IMMERSIVE_STATUS:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_immersive_status_bar);
                state.label = mContext.getString(R.string.quick_settings_immersive_mode_label_hide_status);
                state.contentDescription =  mContext.getString(
                        R.string.accessibility_quick_settings_immersive_mode_hide_nav_bar);
                break;
            default:
                if (state.icon
                        == ResourceIcon.get(R.drawable.ic_immersive_status_bar_enable_animation)) {
                    state.icon = mDisableStatusBar;
                } else if (state.icon
                        == ResourceIcon.get(R.drawable.ic_immersive_nav_bar_enable_animation)) {
                    state.icon = mDisableNavBar;
                } else {
                    state.icon = mDisableFull;
                }
                state.label = mContext.getString(R.string.quick_settings_immersive_mode_label);
                state.contentDescription =  mContext.getString(
                        R.string.accessibility_quick_settings_immersive_mode_off);
                break;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_IMMERSIVE;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(
                    R.string.accessibility_quick_settings_immersive_mode_changed_on);
        } else {
            return mContext.getString(
                    R.string.accessibility_quick_settings_immersive_mode_changed_off);
        }
    }

    @Override
    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        mSetting.setListening(listening);
    }

    private final class ImmersiveDetailAdapter implements DetailAdapter {

        private SegmentedButtons mButtons;
        private ViewGroup mMessageContainer;
        private TextView mMessageText;

        @Override
        public CharSequence getTitle() {
            return mContext.getString(R.string.quick_settings_immersive_mode_label);
        }

        @Override
        public Boolean getToggleState() {
            return mState.value;
        }

        @Override
        public Intent getSettingsIntent() {
            return null;
        }

        @Override
        public void setToggleState(boolean state) {
            MetricsLogger.action(mContext, MetricsEvent.QS_IMMERSIVE_TOGGLE, state);
            if (!state) {
                showDetail(false);
            }
            setEnabled(state);
            fireToggleStateChanged(state);

            switch (mLastState) {
                case IMMERSIVE_FLAGS_FULL:
                    mMessageText.setText(mContext.getString(
                            R.string.quick_settings_immersive_introduce_hide_all));
                    mMessageContainer.setVisibility(View.VISIBLE);
                    break;
                case View.SYSTEM_DESIGN_FLAG_IMMERSIVE_NAV:
                    mMessageText.setText(mContext.getString(
                            R.string.quick_settings_immersive_introduce_hide_status));
                    mMessageContainer.setVisibility(View.VISIBLE);
                    break;
                case View.SYSTEM_DESIGN_FLAG_IMMERSIVE_STATUS:
                    mMessageText.setText(mContext.getString(
                            R.string.quick_settings_immersive_introduce_hide_nav));
                    mMessageContainer.setVisibility(View.VISIBLE);
                    break;
                default:
                    mMessageContainer.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.QS_IMMERSIVE_DETAILS;
        }

        @Override
        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            final LinearLayout mDetails = convertView != null ? (LinearLayout) convertView
                    : (LinearLayout) LayoutInflater.from(context).inflate(
                            R.layout.immersive_mode_panel, parent, false);

            if (convertView == null) {
                mButtons = (SegmentedButtons) mDetails.findViewById(R.id.immersive_buttons);
                mButtons.addButton(R.string.quick_settings_immersive_mode_label_hide_all_twoline,
                        R.string.quick_settings_immersive_mode_detail_hide_all,
                        IMMERSIVE_FLAGS_FULL);
                mButtons.addButton(R.string.quick_settings_immersive_mode_label_hide_status_twoline,
                        R.string.quick_settings_immersive_mode_detail_hide_status,
                        View.SYSTEM_DESIGN_FLAG_IMMERSIVE_STATUS);
                mButtons.addButton(R.string.quick_settings_immersive_mode_label_hide_nav_twoline,
                        R.string.quick_settings_immersive_mode_detail_hide_nav,
                        View.SYSTEM_DESIGN_FLAG_IMMERSIVE_NAV);
                mButtons.setCallback(mButtonsCallback);
                mMessageContainer = (ViewGroup) mDetails.findViewById(R.id.immersive_introduction);
                mMessageText = (TextView) mDetails.findViewById(R.id.immersive_introduction_message);
                mButtons.setSelectedValue(mLastState, false /* fromClick */);
            }

            setToggleState(true);

            return mDetails;
        }

        private final SegmentedButtons.Callback mButtonsCallback = new SegmentedButtons.Callback() {
            @Override
            public void onSelected(final Object value, boolean fromClick) {
                if (value != null && mButtons.isShown()) {
                    mLastState = (Integer) value;
                    if (fromClick) {
                        MetricsLogger.action(mContext, MetricsEvent.QS_IMMERSIVE_TOGGLE, mLastState);
                        setToggleState(true);
                    }
                }
            }

            @Override
            public void onInteraction() {
            }
        };
    }
}
