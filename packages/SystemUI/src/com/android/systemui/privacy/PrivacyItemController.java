package com.android.systemui.privacy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;

import com.android.internal.annotations.VisibleForTesting;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.appops.AppOpItem;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.appops.AppOpsController.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kotlin.collections.Collection;
import kotlin.collections.Iterable;
import kotlin.collections.MutableCollection;
import kotlin.jvm.internal.Intrinsics;

public class PrivacyItemController implements Callback {

    public static final Companion mCompanion = new Companion();
    private static final int[] OPS = new int[]{26, 27, 0, 1};
    private static final List<String> mIntents = Collection.listOf("android.intent.action.USER_FOREGROUND", "android.intent.action.MANAGED_PROFILE_ADDED", "android.intent.action.MANAGED_PROFILE_REMOVED");
    private final AppOpsController mAppOpsController = ((AppOpsController) Dependency.get(AppOpsController.class));
    private final Handler mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
    private final List<WeakReference<Callback>> mCallbacks;

    private final Context mContext;
    private List<Integer> mCurrentUserIds = Collection.emptyList();
    private boolean mListening;
    private final Runnable mNotifyChanges;
    private List<PrivacyItem> mPrivacyList = Collection.emptyList();
    private final PrivacyApplication mPrivacyApp;
    private final Handler mUiHandler = ((Handler) Dependency.get(Dependency.MAIN_HANDLER));
    private final Runnable mUpdateListAndNotifyChanges;
    private final UserManager mUserManager;
    private Receiver mUserSwitcherReceiver;

    public interface Callback {
        void privacyChanged(List<PrivacyItem> list);
    }

    public static final class Companion {
        private Companion() {
        }

        public List<String> getIntents() {
            return PrivacyItemController.mIntents;
        }
    }

    private static final class NotifyChangesToCallback implements Runnable {
	
        private Callback mCallback;
        private List<PrivacyItem> mList;

        public NotifyChangesToCallback(Callback callback, List<PrivacyItem> list) {
            mCallback = callback;
            mList = list;
        }

        @Override
        public void run() {
            if (mCallback != null) {
                mCallback.privacyChanged(mList);
            }
        }
    }

    public class Receiver extends BroadcastReceiver {

		private PrivacyItemController mPrivacyItemController;

		public Receiver(PrivacyItemController privacyItemController) {
			mPrivacyItemController = privacyItemController;
		}

		@Override
        public void onReceive(Context context, Intent intent) {
            if (Collection.contains(mPrivacyItemController.mCompanion.getIntents(), intent != null ? intent.getAction() : null)) {
                mPrivacyItemController.update(true);
            }
        }
    }

    @VisibleForTesting
    public final void setListening(boolean z) {
    }

    public PrivacyItemController(Context context) {
        mContext = context;
        String services = mContext.getString(R.string.device_services);
        mPrivacyApp = new PrivacyApplication(services, 1000, mContext);
        mCallbacks = new ArrayList();
        mNotifyChanges = new Runnable() {
			@Override
			public void run() {
                for (WeakReference weakRef : mCallbacks) {
                    Callback cb = (Callback) weakRef.get();
                    if (cb != null) {
                        cb.privacyChanged(mPrivacyList);
                    }
                }
			}
		};
        mUpdateListAndNotifyChanges = new Runnable() {
			@Override
			public void run() {
                updatePrivacyList();
				mUiHandler.post(mNotifyChanges);
			}
		};
        mUserSwitcherReceiver = new Receiver(this);
		mUserManager = ((UserManager) mContext.getSystemService(UserManager.class));
    }

    private void update(boolean fromReceiver) {
        if (fromReceiver) {
            List<UserInfo> profiles = mUserManager.getProfiles(ActivityManager.getCurrentUser());
            ArrayList ids = new ArrayList(Iterable.collectionSizeOrDefault(profiles, 10));
            for (UserInfo userInfo : profiles) {
                ids.add(Integer.valueOf(userInfo.id));
            }
            mCurrentUserIds = ids;
        }
        mBgHandler.post(mUpdateListAndNotifyChanges);
    }

    private void addCallback(WeakReference<Callback> weakReference) {
        mCallbacks.add(weakReference);
        if (!mCallbacks.isEmpty() || mListening) {
            mUiHandler.post(new NotifyChangesToCallback((Callback) weakReference.get(), mPrivacyList));
        } else {
            setListening(true);
        }
    }

    private void removeCallback(WeakReference<Callback> weakReference) {
        mCallbacks.removeIf(mCallbacks != null && mCallbacks.equals((Callback) weakReference.get()));
        if (mCallbacks.isEmpty()) {
            setListening(false);
        }
    }

    public void addCallback(Callback callback) {
        addCallback(new WeakReference(callback));
    }

    public void removeCallback(Callback callback) {
        removeCallback(new WeakReference(callback));
    }

    private void updatePrivacyList() {
        ArrayList<AppOpItem> opItems = new ArrayList();
        for (Integer userIds : mCurrentUserIds) {
            MutableCollection.addAll(opItems, mAppOpsController.getActiveAppOpsForUser(userIds.intValue()));
        }
        ArrayList list = new ArrayList();
        for (AppOpItem appOpItem : opItems) {
            PrivacyItem toPrivacyItem = toPrivacyItem(appOpItem);
            if (toPrivacyItem != null) {
                list.add(toPrivacyItem);
            }
        }
        mPrivacyList = Collection.distinct(list);
    }

    private PrivacyItem toPrivacyItem(AppOpItem appOpItem) {
        PrivacyType type;
        int code = appOpItem.getCode();
        if (code == 0) {
            type = PrivacyType.TYPE_LOCATION;
        } else if (code == 1) {
            type = PrivacyType.TYPE_LOCATION;
        } else if (code == 26) {
            type = PrivacyType.TYPE_CAMERA;
        } else if (code != 27) {
            return null;
        } else {
            type = PrivacyType.TYPE_MICROPHONE;
        }
        if (appOpItem.getUid() == 1000) {
            return new PrivacyItem(type, mPrivacyApp);
        }
        String opPkgName = appOpItem.getPackageName();
        return new PrivacyItem(type, new PrivacyApplication(opPkgName, appOpItem.getUid(), mContext));
    }

	@Override
	public void onActiveStateChanged(int i, int userId, String str, boolean z) {
        if (mCurrentUserIds.contains(Integer.valueOf(UserHandle.getUserId(userId)))) {
            update(false);
        }
    }
}
