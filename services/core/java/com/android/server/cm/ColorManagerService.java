package com.android.server.cm;

import android.content.Context;
import android.content.cm.IColorManager;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.net.Uri;
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

    private int mCurrentUserId = 0;

    private Future<?> mInitCompleteSignal;

    public ColorManagerService(Context context) {
        super(context);
        mContext = context;
        mOverlayInfo = OverlayInfo.packageName;
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

    private final IBinder mService = new IColorManager.Stub() {
        @Override
        public boolean updateTheme(String themePackage) {
            String packageName = mOverlayInfo;
            if (packageName.startsWith(THEME_PACKAGE)) {
                try {
                    mOverlayManager.setEnabled(themePackage,
                            true, mCurrentUserId);
                    // Enable the overlay
                    return true;
                } catch (RemoteException e) {
                }
            }
            return false;
        }

        @Override
        public boolean updateAccent(String accentPackage) {
            String packageName = mOverlayInfo;
            if (packageName.startsWith(ACCENT_PACKAGE)) {
                try {
                    mOverlayManager.setEnabledExclusive(accentPackage,
                            true, mCurrentUserId);
                    // Enable the overlay
                    return true;
                } catch (RemoteException e) {
                }
            }
            return false;
        }
    };
}
