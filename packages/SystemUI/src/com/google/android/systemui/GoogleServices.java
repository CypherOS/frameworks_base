package com.google.android.systemui;

import android.content.Context;
import android.view.View;

import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.VendorServices;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowView;

import com.google.android.systemui.ambientmusic.AmbientIndicationContainer;
import com.google.android.systemui.ambientmusic.AmbientIndicationService;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GoogleServices extends VendorServices {
	
    private ArrayList<Object> mServices = new ArrayList();

    private void addService(Object object) {
        if (object != null) {
            mServices.add(object);
        }
    }

    @Override
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] arrstring) {
        for (int i = 0; i < mServices.size(); ++i) {
            if (!(mServices.get(i) instanceof Dumpable)) continue;
            ((Dumpable)mServices.get(i)).dump(fileDescriptor, printWriter, arrstring);
        }
    }

    @Override
    public void start() {
        StatusBar statusBar = SysUiServiceProvider.getComponent(mContext, StatusBar.class);
        AmbientIndicationContainer ambientIndicationContainer = (AmbientIndicationContainer)statusBar.getStatusBarWindow().findViewById(R.id.ambient_indication_container);
        ambientIndicationContainer.initializeView(statusBar);
        addService((Object)new AmbientIndicationService(mContext, ambientIndicationContainer));
    }
}

