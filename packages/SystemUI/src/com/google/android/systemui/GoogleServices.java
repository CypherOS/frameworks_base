package com.google.android.systemui;

import android.content.Context;
import android.util.Log;
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

public class GoogleServices
extends VendorServices {
	
	private final String TAG = "GoogleServices";
	
    private ArrayList<Object> mServices = new ArrayList();

    private void addService(Object object) {
        if (object != null) {
            this.mServices.add(object);
        }
    }

    @Override
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] arrstring) {
        for (int i = 0; i < this.mServices.size(); ++i) {
            if (!(this.mServices.get(i) instanceof Dumpable)) continue;
            ((Dumpable)this.mServices.get(i)).dump(fileDescriptor, printWriter, arrstring);
        }
    }

    @Override
    public void start() {
        StatusBar statusBar = SysUiServiceProvider.getComponent(this.mContext, StatusBar.class);
        AmbientIndicationContainer ambientIndicationContainer = (AmbientIndicationContainer)statusBar.getStatusBarWindow().findViewById(R.id.ambient_indication_container);
        ambientIndicationContainer.initializeView(statusBar);
        this.addService((Object)new AmbientIndicationService(this.mContext, ambientIndicationContainer));
		Log.d(TAG, "Started");
    }
}

