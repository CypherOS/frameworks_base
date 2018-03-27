package com.android.server.cm;

import android.annotation.Nullable;
import android.content.Context;
import android.content.cm.IColorManager;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;

import com.android.internal.util.ConcurrentUtils;

import com.android.server.SystemServerInitThreadPool;
import com.android.server.SystemService;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

public class ColorManagerService extends SystemService {

    private static final String TAG = ColorManagerService.class.getSimpleName();
    static final String THEME_PACKAGE = "co.aoscp.theme";
    static final String ACCENT_PACKAGE = "co.aoscp.accent";

    private Context mContext;
    private IOverlayManager mOverlayManager;
    private OverlayInfo mOverlayInfo;

    private Future<?> mInitCompleteSignal;
	private final Object mLock = new Object();

    public ColorManagerService(Context context) {
        super(context);
        mContext = context;
        mInitCompleteSignal = SystemServerInitThreadPool.get().submit(() -> {
            mOverlayManager = IOverlayManager.Stub.asInterface(
                    ServiceManager.getService(Context.OVERLAY_SERVICE));

            publishBinderService(Context.OVERLAY_SERVICE, mService);
            publishLocalService(ColorManagerService.class, this);
        }, "Init ColorManagerService");
    }

    @Override
    public void onStart() {
        // Intentionally left empty.
    }

    @Override
    public void onBootPhase(int phase) {
        if (phase == PHASE_SYSTEM_SERVICES_READY) {
            ConcurrentUtils.waitForFutureNoInterrupt(mInitCompleteSignal,
                    "Wait for ColorManagerService init");
            mInitCompleteSignal = null;
        }
    }

    private void enforceColorManagerPackagesPermission() {
        mContext.enforceCallingOrSelfPermission(
                 android.Manifest.permission.CHANGE_OVERLAY_PACKAGES, "Permission denied");
    }

    private final IBinder mService = new IColorManager.Stub() {
        @Override
        public boolean updateTheme(@Nullable final String themePackage, int userId) throws RemoteException {
            enforceColorManagerPackagesPermission();
            if (themePackage == null) {
                return false;
            }

            final long identity = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    return mOverlayManager.setEnabled(themePackage, true, userId);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @Override
        public boolean updateAccent(@Nullable final String accentPackage, int userId) throws RemoteException {
            enforceColorManagerPackagesPermission();
            if (accentPackage == null) {
                return false;
            }

            final long ident = Binder.clearCallingIdentity();
            try {
                synchronized (mLock) {
                    return mOverlayManager.setEnabledExclusive(accentPackage, true, userId);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override
        public boolean restoreDefaultAccent() {
            try {
                String packageName = mOverlayInfo.packageName;
                List<OverlayInfo> infos = mOverlayManager.getOverlayInfosForTarget("android",
                            UserHandle.myUserId());
                for (int i = 0, size = infos.size(); i < size; i++) {
                    if (infos.get(i).isEnabled() &&
                                packageName.startsWith(ACCENT_PACKAGE)) {
                        mOverlayManager.setEnabled(infos.get(i).packageName, false, UserHandle.myUserId());
                    }
                }
                return true;
            } catch (RemoteException e) {
            }
            return false;
        }
    };
}
