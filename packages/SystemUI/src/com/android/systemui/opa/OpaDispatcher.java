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
 * limitations under the License
 */

package com.android.systemui.opa;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;

import com.android.systemui.OpaLayout;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.phone.StatusBar;

import java.util.ArrayList;

public class OpaDispatcher {
	
	private Context mContext;
	private Handler mHandler;
	private OpaObserver mOpaObserver;

	public OpaDispatcher(Context context) {
		mContext = context;
		mHandler = new Handler();

		mOpaObserver = new OpaObserver(mHandler);
		mOpaObserver.observe();
		mOpaObserver.updateOpaStatus();
	}

    private void dispatchOpa(boolean enabled) {
        StatusBar bar = (StatusBar) SysUiServiceProvider.getComponent(context, StatusBar.class);
        if (bar != null && bar.getNavigationBarView() != null) {
            ArrayList<View> views = bar.getNavigationBarView().getHomeButton().getViews();
            for (int i = 0; i < views.size(); i++) {
                ((OpaLayout) ((View) views.get(i))).setOpaEnabled(enabled);
            }
        }
    }

    private class OpaObserver extends ContentObserver {
        OpaObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(
                    Settings.System.OPA_ENABLED),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Settings.System.getUriFor(Settings.System.OPA_ENABLED))) {
                updateOpaStatus();
            }
        }

        public void updateOpaStatus() {
            mOpaEnabled = Settings.System.getInt(mContext.getContentResolver(), Settings.System.OPA_ENABLED, 1, UserHandle.USER_CURRENT) != 0;
			dispatchOpa(mOpaEnabled);
        }
    }
}
