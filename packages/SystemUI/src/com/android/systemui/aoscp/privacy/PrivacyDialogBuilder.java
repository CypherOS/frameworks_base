/*
 * Copyright (C) 2019 CypherOS
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

package com.android.systemui.aoscp.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.android.systemui.R;
import com.android.systemui.aoscp.privacy.types.OpTypeCamera;
import com.android.systemui.aoscp.privacy.types.OpTypeMicrophone;
import com.android.systemui.aoscp.privacy.utils.IOpComparator;
import com.android.systemui.aoscp.privacy.utils.OpComparator;
import com.android.systemui.aoscp.privacy.utils.OpPair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class PrivacyDialogBuilder {

    private PrivacyApplication mApp;
    private List<OpPair<PrivacyApplication, List<PrivacyType>>> mAppsAndTypes;
    private Context mContext;
    private String mLastSeparator;
    private String mSeparator;
    private List<PrivacyType> mTypes;

    public PrivacyDialogBuilder(Context context, List<PrivacyItem> list) {
        mContext = context;
		mLastSeparator = mContext.getString(R.string.ongoing_privacy_dialog_last_separator);
		mSeparator = mContext.getString(R.string.ongoing_privacy_dialog_separator);
		LinkedHashMap items = new LinkedHashMap();
		for (PrivacyItem item : list) {
			PrivacyApplication app = item.getApplication();
			ArrayList appsPerItem = (ArrayList) items.get(app);
			if (appsPerItem == null) {
                appsPerItem = new ArrayList();
                items.put(app, appsPerItem);
            }
			appsPerItem.add(item.getPrivacyType());
		}
		IOpComparator[] opTypes = new IOpComparator[2];
        opTypes[0] = OpTypeCamera.INSTANCE;
        opTypes[1] = OpTypeMicrophone.INSTANCE;
		mAppsAndTypes = OpUtils.sortedWith(OpUtils.toListMap(items), OpComparator.compareBy(opTypes));
		ArrayList types = new ArrayList(list instanceof Collection ? ((Collection) list).size() : 10);
		for (PrivacyItem privacyType : list) {
            types.add(privacyType.getPrivacyType());
        }
		mTypes = generateAndSort(OpUtils.distinctInList(types));
		mApp = mAppsAndTypes.size() != 1 ? null : (PrivacyApplication) ((OpPair) mAppsAndTypes.get(0)).getFirst();
    }

    public List<OpPair<PrivacyApplication, List<PrivacyType>>> getAppsAndTypes() {
        return mAppsAndTypes;
    }

    public List<PrivacyType> getTypes() {
        return mTypes;
    }

    public PrivacyApplication getApp() {
        return mApp;
    }

    public List<Drawable> generateIconsForApp(List<? extends PrivacyType> list) {
        List<PrivacyType> icons = generateAndSort(list);
		ArrayList generatedForApp = new ArrayList(icons instanceof Collection ? ((Collection) icons).size() : 10);
        for (PrivacyType icon : icons) {
            generatedForApp.add(icon.getIcon(mContext));
        }
        return generatedForApp;
    }

    public List<Drawable> generateIcons() {
		if (mTypes == null) return null;
		ArrayList generated = new ArrayList(mTypes instanceof Collection ? ((Collection) mTypes).size() : 10);
        for (PrivacyType type : mTypes) {
            generated.add(type.getIcon(mContext));
        }
        return generated;
    }

	private <T extends Comparable<? super T>> List<T> generateAndSort(Iterable<? extends T> list) {
		if (list instanceof Collection) {
			Collection collection = (Collection) list;
            if (collection.size() <= 1) {
                return OpUtils.toList(list);
            }
            Object[] toArray = collection.toArray(new Comparable[0]);
            if (toArray != null) {
                Comparable[] cArray = (Comparable[]) toArray;
                if (cArray != null) {
                    Arrays.sort(cArray);
                    return OpUtils.asList(cArray);
                }
            }
        }
		List toMutableList = OpUtils.toMutableList((Iterable) list);
		Collections.sort(toMutableList);
		return toMutableList;
	}

    private <T> StringBuilder joinWithAnd(List<? extends T> list) {
        List subList = list.subList(0, list.size() - 1);
        Appendable sbAppend = new StringBuilder();
        joinTo(subList, sbAppend, mSeparator);
        StringBuilder sb = (StringBuilder) sbAppend;
        sb.append(mLastSeparator);
        sb.append(list.get(list.size() - 1));
        return sb;
    }

    public String joinTypes() {
        if (mTypes.size() == 0) {
            return "";
        }
        String sb;
        if (mTypes.size() != 1) {
            List<PrivacyType> list = mTypes;
            ArrayList privacyName = new ArrayList(list instanceof Collection ? ((Collection) list).size() : 10);
            for (PrivacyType name : list) {
                privacyName.add(name.getName(mContext));
            }
            sb = joinWithAnd(privacyName).toString();
            return sb;
        }
        sb = ((PrivacyType) mTypes.get(0)).getName(mContext);
        return sb;
    }

	private <T, A extends Appendable> A joinTo(Iterable<? extends T> iterable, A a, String s) {
		int size = 0;
		for (Object obj : iterable) {
            size++;
            if (size > 1) {
				try {
					a.append(s);
				} catch(IOException e) {
				}
            }
			if (obj != null) {
				try {
					a.append(String.valueOf(obj));
				} catch(IOException e) {
				}
			}
        }
        return a;
	}

    public String getDialogTitle() {
        String title;
        if (mApp != null) {
            title = mContext.getString(R.string.ongoing_privacy_dialog_single_app_title, new Object[]{joinTypes()});
            return title;
        }
        title = mContext.getString(R.string.ongoing_privacy_dialog_multiple_apps_title, new Object[]{joinTypes()});
        return title;
    }
}
