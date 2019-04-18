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

    protected static final int[] OPS = new int[]{26, 24, 27, 0, 1};
    private final List<AppOpItem> mActiveItems = new ArrayList();
    private final AppOpsManager mAppOps;
    private BGHandler mBGHandler;
    private final List<Callback> mCallbacks = new ArrayList();
    private final ArrayMap<Integer, Set<Callback>> mCallbacksByCode = new ArrayMap();
    private final Context mContext;
    private final List<AppOpItem> mNotedItems = new ArrayList();

    protected final class BGHandler extends Handler {

		private AppOpsControllerImpl mAppOpsControllerImpl;

        BGHandler(AppOpsControllerImpl opController, Looper looper) {
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
        mAppOps = (AppOpsManager) context.getSystemService("appops");
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
    public void addCallback(int[] iArr, Callback callback) {
        int length = iArr.length;
        int i = 0;
        boolean i2 = false;
        while (i < length) {
            if (mCallbacksByCode.containsKey(Integer.valueOf(iArr[i]))) {
                ((Set) mCallbacksByCode.get(Integer.valueOf(iArr[i]))).add(callback);
                i2 = true;
            }
            i++;
        }
        if (i2) {
            mCallbacks.add(callback);
        }
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
                AppOpItem appOpItem2 = new AppOpItem(code, uid, packageName, System.currentTimeMillis());
                mNotedItems.add(appOpItem2);
            }
        }
        mBGHandler.scheduleRemoval(appOpItem, 5000);
    }

    @Override
    public List<AppOpItem> getActiveAppOpsForUser(int i) {
        int i2;
        ArrayList arrayList = new ArrayList();
        synchronized (mActiveItems) {
            int size = mActiveItems.size();
            i2 = 0;
            for (int i3 = 0; i3 < size; i3++) {
                AppOpItem appOpItem = (AppOpItem) mActiveItems.get(i3);
                if (UserHandle.getUserId(appOpItem.getUid()) == i) {
                    arrayList.add(appOpItem);
                }
            }
        }
        synchronized (mNotedItems) {
            int size2 = mNotedItems.size();
            while (i2 < size2) {
                AppOpItem appOpItem2 = (AppOpItem) mNotedItems.get(i2);
                if (UserHandle.getUserId(appOpItem2.getUid()) == i) {
                    arrayList.add(appOpItem2);
                }
                i2++;
            }
        }
        return arrayList;
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
            for (Callback cb : (Set) mCallbacksByCode.get(Integer.valueOf(code))) {
                cb.onActiveStateChanged(code, uid, packageName, active);
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        printWriter.println("AppOpsController state:");
        printWriter.println("  Active Items:");
        int i = 0;
        int i2 = 0;
        while (true) {
            str = "    ";
            if (i2 >= mActiveItems.size()) {
                break;
            }
            AppOpItem appOpItem = (AppOpItem) mActiveItems.get(i2);
            printWriter.print(str);
            printWriter.println(appOpItem.toString());
            i2++;
        }
        printWriter.println("  Noted Items:");
        while (i < mNotedItems.size()) {
            AppOpItem appOpItem2 = (AppOpItem) mNotedItems.get(i);
            printWriter.print(str);
            printWriter.println(appOpItem2.toString());
            i++;
        }
    }
}
