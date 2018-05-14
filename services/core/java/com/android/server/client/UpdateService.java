/**
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
package com.android.server.client;

import android.Manifest;
import android.client.UpdateManager;
import android.client.UpdateManager.UpdateInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.client.IUpdateService;
import android.pocket.PocketConstants;
import android.pocket.PocketManager;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Slog;

import com.android.server.SystemService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import static android.provider.Settings.System.POCKET_JUDGE;

/**
 * A basic update service to initiate a looped check interval from the
 * specified interval. The check logic is handled in UpdateManager;
 *
 * @author Chris Crump
 * @hide
 */
public class UpdateService extends SystemService implements IBinder.DeathRecipient {

    private static final String TAG = UpdateService.class.getSimpleName();
	
	public static final int DEFAULT_CHECK_TIME = 18000000; // 5 Hours
	
    private Context mContext;
    private boolean mSystemBooted;
	
	private UpdateManager mUpdateManager;
    private UpdateHandler mHandler;

    public UpdateService(Context context) {
        super(context);
        mContext = context;
        HandlerThread handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        mHandler = new UpdateHandler(handlerThread.getLooper());
		mUpdateManager = new UpdateManager(context, this);
    }

    private class UpdateHandler extends Handler {

        public static final int MSG_SYSTEM_BOOTED = 0;

        public UpdateHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SYSTEM_BOOTED:
                    handleSystemBooted();
                    break;
                default:
                    Slog.w(TAG, "Unknown message:" + msg.what);
            }
        }
    }

    @Override
    public void onBootPhase(int phase) {
        switch(phase) {
            case PHASE_BOOT_COMPLETED:
                mHandler.sendEmptyMessage(UpdateHandler.MSG_SYSTEM_BOOTED);
                break;
            default:
                Slog.w(TAG, "Un-handled boot phase:" + phase);
                break;
        }
    }

    @Override
    public void onStart() {
        publishBinderService(Context.COTA_SERVICE, new ServiceWrapper());
    }

    @Override
    public void binderDied() {
        synchronized (mCallbacks) {
            mCallbacks.clear();
        }
    }

    private final class ServiceWrapper {
		
        @Override // Binder call
        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (mContext.checkCallingOrSelfPermission(Manifest.permission.DUMP)
                    != PackageManager.PERMISSION_GRANTED) {
                pw.println("Permission Denial: can't dump Pocket from from pid="
                        + Binder.getCallingPid()
                        + ", uid=" + Binder.getCallingUid());
                return;
            }

            final long ident = Binder.clearCallingIdentity();
            try {
                dumpInternal(pw);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

    }

	private Runnable mCheckForUpdates = new Runnable() {
        @Override
        public void run() {
            doCheckLoop();
        }
    };
	
	private void doCheckLoop() {
		mUpdateManager.checkForUpdates();
		mHandler.postDelayed(mCheckForUpdates, DEFAULT_CHECK_TIME);
	}

    private void handleSystemBooted() {
        Log.d(TAG, "PHASE_BOOT_COMPLETED");
        mSystemBooted = true;
        doCheckLoop();
    }

    private void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "COTA");
        } catch (JSONException e) {
            Slog.e(TAG, "dump formatting failure", e);
        } finally {
            pw.println(dump);
        }
    }
}