package com.android.systemui.smartspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;


import com.android.systemui.smartspace.nano.SmartspaceProto.SmartspaceUpdate;
import com.android.systemui.smartspace.nano.SmartspaceProto.SmartspaceUpdate.SmartspaceCard;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;

public class SmartSpaceBroadcastReceiver extends BroadcastReceiver {
    private final SmartSpaceController mController;

    public SmartSpaceBroadcastReceiver(SmartSpaceController controller) {
        mController = controller;
    }

    public void onReceive(Context context, Intent intent) {
        InvalidProtocolBufferNanoException e;
        Context context2 = context;
        Intent intent2 = intent;
        if (SmartSpaceController.DEBUG) {
            Log.d("SmartSpaceReceiver", "receiving update");
        }
        int myUserId = UserHandle.myUserId();
        if (myUserId == 0) {
            if (!intent2.hasExtra("uid")) {
                intent2.putExtra("uid", myUserId);
            }
            byte[] bytes = intent2.getByteArrayExtra("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CARD");
            if (bytes != null) {
                SmartspaceUpdate proto = new SmartspaceUpdate();
                try {
                    MessageNano.mergeFrom(proto, bytes);
                    for (SmartspaceCard card : proto.mCard) {
                        boolean isPrimary = card.cardPriority == 1;
                        boolean isSecondary = card.cardPriority == 2;
                        if (isPrimary || isSecondary) {
                            try {
                                notify(card, context2, intent2, isPrimary);
                            } catch (InvalidProtocolBufferNanoException e2) {
                                e = e2;
                            }
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("unrecognized card priority: ");
                            stringBuilder.append(card.cardPriority);
                            Log.w("SmartSpaceReceiver", stringBuilder.toString());
                        }
                    }
                } catch (InvalidProtocolBufferNanoException e3) {
                    e = e3;
                    Log.e("SmartSpaceReceiver", "proto", e);
                }
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("receiving update with no proto: ");
            stringBuilder2.append(intent.getExtras());
            Log.e("SmartSpaceReceiver", stringBuilder2.toString());
        } else if (!intent2.getBooleanExtra("rebroadcast", false)) {
            intent2.putExtra("rebroadcast", true);
            intent2.putExtra("uid", myUserId);
            context2.sendBroadcastAsUser(intent2, UserHandle.ALL);
        }
    }

    private void notify(SmartspaceCard updateCard, Context context, Intent intent, boolean isPrimaryCard) {
        long publishTime = SystemClock.uptimeMillis();
        PackageInfo gsaInfo = null;
        try {
            gsaInfo = context.getPackageManager().getPackageInfo("com.google.android.googlequicksearchbox", 0);
        } catch (NameNotFoundException e) {
            Log.w("SmartSpaceReceiver", "Cannot find GSA", e);
        }
        mController.onNewCard(new NewCardInfo(updateCard, intent, isPrimaryCard, publishTime, gsaInfo));
    }
}
