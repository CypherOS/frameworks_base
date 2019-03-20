/*
 * Copyright (C) 2018-2019 CypherOS
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

package com.android.systemui.quickspace;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.android.systemui.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuickspaceController implements Callback {

    public static final String TAG = "QuickspaceController";
    public static final String MISERVICES_PACKAGE = "co.aoscp.miservices";

    private static final Uri QUICKSPACE_PROVIDER = Uri.parse("content://co.aoscp.miservices.providers.quickspace/card");
    private static final String[] DATA = new String[] {
            "status",
            "conditions",
            "temperatureMetric",
            "temperatureImperial",
            "eventType",
            "eventTitle"
    };

    private static final int MSG_UPDATE_QUICKSPACE_CARD = 0;
    private static final int MSG_UPDATE_QUICKSPACE = 1;

    private static QuickspaceController sController;
    private final HandlerThread mWorker = new HandlerThread(QuickspaceController.class.getSimpleName());
    private final Handler mWorkerThread;
    private final Handler mUiThread = new Handler(Looper.getMainLooper(), this);
    private Context mContext;
    private final ArrayList<QuickspaceCard> mQuickspaceCard = new ArrayList();
    private final ArrayList<QuickspaceCard> mProccessedCard = new ArrayList();

    private final ContentObserver DATA_OBSERVER = new ContentObserver(mUiThread) {
        @Override
        public void onChange(boolean enabled) {
            updateCard();
        }
    };

	private List<IQuickspace> mListeners;

    class MiServicesReceiver extends BroadcastReceiver {
        MiServicesReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            getQuickspaceProvider();
        }
    }

    private static class CardData {
        int status;
        String conditions;
        int temperatureMetric;
        int temperatureImperial;
        int eventType;
        String eventTitle;

        private CardData() {
        }

        CardData(byte b) {
            this();
        }
    }

    public static QuickspaceController get(Context context) {
        Assert.isMainThread();
        if (sController == null) {
            sController = new QuickspaceController(context);
        }
        return sController;
    }

    private QuickspaceController(Context context) {
        mContext = context;
		mListeners = new ArrayList<>();
        mWorker.start();
        mWorkerThread = new Handler(mWorker.getLooper(), this);
        getQuickspaceProvider();
        context.registerReceiver(new MiServicesReceiver(), QuickBits.getPackageIntentInfo(MISERVICES_PACKAGE, "android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED", "android.intent.action.PACKAGE_DATA_CLEARED", "android.intent.action.PACKAGE_RESTARTED"));
    }

    private void getQuickspaceProvider() {
        ContentResolver resolver = mContext.getContentResolver();
        resolver.unregisterContentObserver(DATA_OBSERVER);
        try {
            resolver.registerContentObserver(QUICKSPACE_PROVIDER, true, DATA_OBSERVER);
        } catch (SecurityException e) {
            Log.d(TAG, "Quickspace provider not found");
        }
    }

    private static ArrayList<QuickspaceCard> add(ArrayList<QuickspaceCard> arrayList) {
        ArrayList<QuickspaceCard> card = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            QuickspaceCard cardInfo = (QuickspaceCard) it.next();
            card.add(cardInfo);
        }
        return card;
    }

    private void updateCard() {
        Message.obtain(mWorkerThread, MSG_UPDATE_QUICKSPACE_CARD).sendToTarget();
    }

    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_UPDATE_QUICKSPACE_CARD:
                Message.obtain(mUiThread, MSG_UPDATE_QUICKSPACE, 0, 0, add(newCardInfo())).sendToTarget();
                break;
            case MSG_UPDATE_QUICKSPACE:
                mProccessedCard.clear();
                mProccessedCard.addAll((ArrayList) message.obj);
				for (IQuickspace listeners : mListeners) {
                    try {
                        listeners.onNewCard(mProccessedCard);
                    } catch (Exception ignored) {
                    }
					break;
                }
                break;
        }
        return true;
    }

    private ArrayList<QuickspaceCard> newCardInfo() {
        mQuickspaceCard.clear();
        Cursor provider = mContext.getContentResolver().query(QUICKSPACE_PROVIDER, DATA,
                null, null, null);
        if (provider != null) {
            try {
                int count = provider.getCount();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        provider.moveToPosition(i);
                        if (i == 0) {
                            CardData data = new CardData();
                            data.status = provider.getInt(0);
                            data.conditions = provider.getString(1);
                            data.temperatureMetric = provider.getInt(2);
                            data.temperatureImperial = provider.getInt(3);
                            data.eventType = provider.getInt(4);
                            data.eventTitle = provider.getString(5);

                            try {
                                QuickspaceCard card = new QuickspaceCard(data.status, data.conditions,
                                        data.temperatureMetric, data.temperatureImperial,
                                        data.eventType, data.eventTitle);
                                mQuickspaceCard.add(card);
                            } catch (Throwable ignored) {
                                Log.d(TAG, "Cannot create Quickspace card");
                            }
                        }
                    }
                }
            } finally {
                provider.close();
            }
            return mQuickspaceCard;
        } else {
            Log.d(TAG, "Quickspace provider not found");
        }
        return mQuickspaceCard;
    }

    public ArrayList<QuickspaceCard> getCard() {
        return mProccessedCard;
    }

    public void setListener(IQuickspace iQuickspace) {
		mListeners.add(iQuickspace);
    }
}
