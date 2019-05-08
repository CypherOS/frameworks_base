package com.android.systemui.appops;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpActiveChangedListener;
import android.app.AppOpsManager.OnOpNotedListener;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;

import com.android.systemui.Dumpable;
import com.android.systemui.appops.AppOpsController.Callback;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AppOpsControllerImpl implements AppOpsController, OnOpActiveChangedListener, OnOpNotedListener, Dumpable {

	protected static final int[] OPS = new int[] {
		    AppOpsManager.OP_CAMERA,
			AppOpsManager.OP_SYSTEM_ALERT_WINDOW,
			AppOpsManager.OP_RECORD_AUDIO,
			AppOpsManager.OP_COARSE_LOCATION,
			AppOpsManager.OP_FINE_LOCATION
	};
    private final List<AppOpItem> mActiveItems = new ArrayList();
    private final AppOpsManager mAppOps;
    private BGHandler mBGHandler;
    private final List<Callback> mCallbacks = new ArrayList();
    private final ArrayMap<Integer, Set<Callback>> mCallbacksByCode = new ArrayMap();
    private final Context mContext;
    private final List<AppOpItem> mNotedItems = new ArrayList();

    protected final class BGHandler extends Handler {

		private AppOpsControllerImpl mAppOpsControllerImpl;

        BGHandler(AppOpsControllerImpl appOpsControllerImpl, Looper looper) {
            super(looper);
			mAppOpsControllerImpl = appOpsControllerImpl;
        }

        public void scheduleRemoval(final AppOpItem appOpItem, long duration) {
            removeCallbacksAndMessages(appOpItem);
            postDelayed(new Runnable() {
				@Override
                public void run() {
                    mAppOpsControllerImpl.removeNoted(appOpItem.getCode(), appOpItem.getUid(), appOpItem.getPackageName());
                }
            }, appOpItem, duration);
        }
    }

    public AppOpsControllerImpl(Context context, Looper looper) {
        mContext = context;
        mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mBGHandler = new BGHandler(this, looper);
        for (int valueOf : OPS) {
            mCallbacksByCode.put(Integer.valueOf(valueOf), new ArraySet());
        }
    }

    @VisibleForTesting
    protected void setBGHandler(BGHandler bgHandler) {
        mBGHandler = bgHandler;
    }

    @VisibleForTesting
    protected void setListening(boolean watch) {
        if (watch) {
            mAppOps.startWatchingActive(OPS, this);
            mAppOps.startWatchingNoted(OPS, this);
            return;
        }
        mAppOps.stopWatchingActive(this);
        mAppOps.stopWatchingNoted(this);
    }

    @Override
    public void addCallback(int[] opItems, Callback callback) {
        int length = opItems.length;
        boolean hasCode = false;
		for (int code = 0; code < opItems.length - 1; code++) {
			if (mCallbacksByCode.containsKey(Integer.valueOf(opItems[code]))) {
				((Set) mCallbacksByCode.get(Integer.valueOf(opItems[code]))).add(callback);
			}
		}
		// Todo: Properly check if registered callback has a match OP list [if (hasCode) mCallbacks.add(callback);]
		// For now we assume it does since we work with one
		mCallbacks.add(callback);
        if (!mCallbacks.isEmpty()) {
            setListening(true);
        }
    }

    private AppOpItem getAppOpItem(List<AppOpItem> list, int code, int uid, String packageName) {
        int size = list.size();
        for (int items = 0; items < size; items++) {
            AppOpItem appOpItem = (AppOpItem) list.get(items);
            if (appOpItem.getCode() == code && appOpItem.getUid() == uid && appOpItem.getPackageName().equals(packageName)) {
                return appOpItem;
            }
        }
        return null;
    }

    private boolean updateActives(int code, int uid, String packageName, boolean active) {
        synchronized (mActiveItems) {
            AppOpItem appOpItem = getAppOpItem(mActiveItems, code, uid, packageName);
            if (appOpItem == null && active) {
                mActiveItems.add(new AppOpItem(code, uid, packageName, System.currentTimeMillis()));
                return true;
            } else if (appOpItem == null || active) {
                return false;
            } else {
                mActiveItems.remove(appOpItem);
                return true;
            }
        }
    }

    private void removeNoted(int code, int uid, String packageName) {
        synchronized (mNotedItems) {
            AppOpItem appOpItem = getAppOpItem(mNotedItems, code, uid, packageName);
            if (appOpItem == null) {
                return;
            }
            mNotedItems.remove(appOpItem);
            notifySuscribers(code, uid, packageName, false);
        }
    }

    private void addNoted(int code, int uid, String packageName) {
        AppOpItem appOpItem;
        synchronized (mNotedItems) {
            appOpItem = getAppOpItem(mNotedItems, code, uid, packageName);
            if (appOpItem == null) {
                appOpItem = new AppOpItem(code, uid, packageName, System.currentTimeMillis());
                mNotedItems.add(appOpItem);
            }
        }
        mBGHandler.scheduleRemoval(appOpItem, 5000);
    }

    @Override
    public List<AppOpItem> getActiveAppOpsForUser(int uid) {
        int notedItems;
        ArrayList activeOps = new ArrayList();
        synchronized (mActiveItems) {
            int activeSize = mActiveItems.size();
            notedItems = 0;
            for (int activeItems = 0; activeItems < activeSize; activeItems++) {
                AppOpItem activeOp = (AppOpItem) mActiveItems.get(activeItems);
                if (UserHandle.getUserId(activeOp.getUid()) == uid) {
                    activeOps.add(activeOp);
                }
            }
        }
        synchronized (mNotedItems) {
            int notedSize = mNotedItems.size();
            while (notedItems < notedSize) {
                AppOpItem notedOps = (AppOpItem) mNotedItems.get(notedItems);
                if (UserHandle.getUserId(notedOps.getUid()) == uid) {
                    activeOps.add(notedOps);
                }
                notedItems++;
            }
        }
        return activeOps;
    }

    @Override
    public void onOpActiveChanged(int code, int uid, String packageName, boolean active) {
        if (updateActives(code, uid, packageName, active)) {
            notifySuscribers(code, uid, packageName, active);
        }
    }

    @Override
    public void onOpNoted(int code, int uid, String packageName, int active) {
        if (active == 0) {
            addNoted(code, uid, packageName);
            notifySuscribers(code, uid, packageName, true);
        }
    }

    private void notifySuscribers(int code, int uid, String packageName, boolean active) {
        if (mCallbacksByCode.containsKey(Integer.valueOf(code))) {
            for (Callback cb : mCallbacksByCode.get(Integer.valueOf(code))) {
                cb.onActiveStateChanged(code, uid, packageName, active);
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        printWriter.println("AppOpsController state:");
        printWriter.println("  Active Items:");
        int notedItems = 0;
        int activeItems = 0;
        while (true) {
            str = "    ";
            if (activeItems >= mActiveItems.size()) {
                break;
            }
            AppOpItem active = (AppOpItem) mActiveItems.get(activeItems);
            printWriter.print(str);
            printWriter.println(active.toString());
            activeItems++;
        }
        printWriter.println("  Noted Items:");
        while (notedItems < mNotedItems.size()) {
            AppOpItem noted = (AppOpItem) mNotedItems.get(notedItems);
            printWriter.print(str);
            printWriter.println(noted.toString());
            notedItems++;
        }
    }
}
