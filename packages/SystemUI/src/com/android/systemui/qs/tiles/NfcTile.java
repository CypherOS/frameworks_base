/*
 * Copyright (C) 2016-2017 ParanoidAndroid Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.volume.SegmentedButtons;

/** Quick settings tile: Enable/disable NFC **/
public class NfcTile extends QSTile<QSTile.BooleanState> {
    private static final int NFC_OFF = 0;
    private static final int NFC_MODE_NO_BEAM = 1;
    private static final int NFC_MODE_FULL = 2;

    private NfcAdapter mAdapter;

    private NfcDetailAdapter mDetailAdapter;

    private boolean mListening;

    private int mNfcState;

    public NfcTile(Host host) {
        super(host);
        getAdapter();

        mDetailAdapter = new NfcDetailAdapter();
    }

    @Override
    public DetailAdapter getDetailAdapter() {
        return mDetailAdapter;
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mContext.unregisterReceiver(mNfcReceiver);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        getAdapter();
        if (mListening == listening || mAdapter == null) return;
        mListening = listening;
        if (mListening) {
            mContext.registerReceiver(mNfcReceiver,
                    new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        }
    }

    @Override
    public boolean isAvailable() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_NFC_SETTINGS);
    }

    @Override
    protected void handleClick() {
        if (shouldHide()) return;
        MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);
        showDetail(true);
    }

    @Override
    protected void handleSecondaryClick() {
        handleClick();
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_nfc_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        getAdapter();
        final Drawable mEnable = mContext.getDrawable(R.drawable.ic_qs_nfc_enabled);
        final Drawable mDisable = mContext.getDrawable(R.drawable.ic_qs_nfc_disabled);
        state.label = mContext.getString(R.string.quick_settings_nfc_label);
        if (shouldHide()) {
            Drawable icon = mDisable.mutate();
            final int disabledColor = mContext.getColor(R.color.qs_tile_tint_unavailable);
            icon.setTint(disabledColor);
            state.icon = new DrawableIcon(icon);
            state.label = new SpannableStringBuilder().append(state.label,
                    new ForegroundColorSpan(disabledColor),
                    SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            return;
        }
        state.value = mAdapter.isEnabled();
        final DrawableIcon icon = new DrawableIcon(state.value ? mEnable : mDisable);
        state.icon = icon;
        state.minimalAccessibilityClassName = state.expandedAccessibilityClassName
                = Switch.class.getName();
        state.contentDescription = state.label;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_NFC;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.quick_settings_nfc_on);
        } else {
            return mContext.getString(R.string.quick_settings_nfc_off);
        }
    }

    private boolean shouldHide() {
        return !isAvailable() || mAdapter == null;
    }

    private void setNfcState(int state) {
        if (mAdapter == null) return;
        switch (state) {
            case NFC_MODE_FULL:
                if (!mAdapter.isEnabled() || !mAdapter.isNdefPushEnabled()) {
                    mAdapter.enable();
                    mAdapter.enableNdefPush();
                }
                break;
            case NFC_MODE_NO_BEAM:
                if (!mAdapter.isEnabled()) {
                    mAdapter.enable();
                }
                if (mAdapter.isNdefPushEnabled()) {
                    mAdapter.disableNdefPush();
                }
                break;
            case NFC_OFF:
                mAdapter.disableNdefPush();
                mAdapter.disable();
                break;
            default:
                break;
        }
    }

    private void getAdapter() {
        try {
            mAdapter = NfcAdapter.getNfcAdapter(mContext);
        // Don't crash if NFC is unsupported
        } catch (UnsupportedOperationException e) {
            mAdapter = null;
        }
        mNfcState = getNfcState();
    }

    private int getNfcState() {
        if (mAdapter == null) return NFC_OFF; // Unsupported
        if (mAdapter.isEnabled()) {
            if (mAdapter.isNdefPushEnabled()) {
                return NFC_MODE_FULL;
            }
            return NFC_MODE_NO_BEAM;
        }
        return NFC_OFF;
    }

    private BroadcastReceiver mNfcReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mNfcState = getNfcState();
            refreshState();
        }
    };

    private final class NfcDetailAdapter implements DetailAdapter {
        private SegmentedButtons mButtons;
        private ViewGroup mMessageContainer;
        private TextView mMessageText;

        @Override
        public CharSequence getTitle() {
            return mContext.getString(R.string.quick_settings_nfc_label);
        }

        @Override
        public Boolean getToggleState() {
            return mState.value;
        }

        @Override
        public Intent getSettingsIntent() {
            return new Intent(Settings.ACTION_NFC_SETTINGS);
        }

        @Override
        public void setToggleState(boolean state) {
            MetricsLogger.action(mContext, MetricsEvent.QS_NFC, state);
            if (!state && mAdapter != null) {
                showDetail(false);
                mAdapter.disable();
            }

            fireToggleStateChanged(state);

            switch (mNfcState) {
                case NFC_MODE_FULL:
                    mMessageText.setText(mContext.getString(
                            R.string.quick_settings_nfc_and_beam_detail));
                    mMessageContainer.setVisibility(View.VISIBLE);
                    break;
                case NFC_MODE_NO_BEAM:
                    mMessageText.setText(mContext.getString(
                            R.string.quick_settings_nfc_no_beam_detail));
                    mMessageContainer.setVisibility(View.VISIBLE);
                    break;
                default:
                    mMessageContainer.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.QS_NFC;
        }

        @Override
        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            final LinearLayout mDetails = convertView != null ? (LinearLayout) convertView
                    : (LinearLayout) LayoutInflater.from(context).inflate(
                            R.layout.nfc_mode_panel, parent, false);

            mNfcState = getNfcState();

            if (convertView == null) {
                mButtons = (SegmentedButtons) mDetails.findViewById(R.id.nfc_buttons);
                mButtons.addButton(R.string.quick_settings_nfc_and_beam,
                        R.string.quick_settings_nfc_and_beam_detail,
                        NFC_MODE_FULL);
                mButtons.addButton(R.string.quick_settings_nfc_no_beam,
                        R.string.quick_settings_nfc_no_beam_detail,
                        NFC_MODE_NO_BEAM);
                mButtons.setCallback(mButtonsCallback);
                mMessageContainer = (ViewGroup) mDetails.findViewById(R.id.nfc_introduction);
                mMessageText = (TextView) mDetails.findViewById(R.id.nfc_introduction_message);
            }

            if (mNfcState == NFC_OFF) { // Default to full mode if NFC is disabled
                mNfcState = NFC_MODE_FULL;
            }

            mButtons.setSelectedValue(mNfcState, false /* fromClick */);

            setNfcState(mNfcState);
            setToggleState(true);

            return mDetails;
        }

        private final SegmentedButtons.Callback mButtonsCallback = new SegmentedButtons.Callback() {
            @Override
            public void onSelected(final Object value, boolean fromClick) {
                if (value != null && mButtons.isShown()) {
                    mNfcState = (Integer) value;
                    if (fromClick) {
                        MetricsLogger.action(mContext, MetricsEvent.QS_NFC, mNfcState);
                        setNfcState(mNfcState);
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
