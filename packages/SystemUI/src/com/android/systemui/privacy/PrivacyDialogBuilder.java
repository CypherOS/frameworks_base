package com.android.systemui.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.android.systemui.R;
import com.android.systemui.privacy.aoscp.Comparisons;
import com.android.systemui.privacy.aoscp.IComparisons;

import kotlin.Pair;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

public class PrivacyDialogBuilder {

    private PrivacyApplication mApp;
    private List<Pair<PrivacyApplication, List<PrivacyType>>> mAppsAndTypes;
    private Context mContext;
    private String mLastSeparator;
    private String mSeparator;
    private List<PrivacyType> mTypes;

    public PrivacyDialogBuilder(Context context, List<PrivacyItem> list) {
		Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(list, "itemsList");
        mContext = context;
		mLastSeparator = mContext.getString(R.string.ongoing_privacy_dialog_last_separator);
		mSeparator = mContext.getString(R.string.ongoing_privacy_dialog_separator);
        LinkedHashMap privacyItems = new LinkedHashMap();
        for (PrivacyItem privacyItem : list) {
            PrivacyApplication app = privacyItem.getApplication();
            ArrayList privacyApp = (ArrayList) privacyItems.get(app);
            if (privacyApp == null) {
                privacyApp = new ArrayList();
                privacyItems.put(app, privacyApp);
            }
            privacyApp.add(privacyItem.getPrivacyType());
        }
		List toList = new ArrayList(privacyItems.values());
        IComparisons[] types = new IComparisons[2];
        types[0] = OngoingPrivacyTypeCamera.INSTANCE;
        types[1] = OngoingPrivacyTypeMicrophone.INSTANCE;
        mAppsAndTypes = CollectionsKt.sortedWith(toList, Comparisons.compareBy(types));
        ArrayList privacyTypes = new ArrayList(CollectionsKt.collectionSizeOrDefault(list, 10));
        for (PrivacyItem privacyType : list) {
            privacyTypes.add(privacyType.getPrivacyType());
        }
        mTypes = CollectionsKt.sorted(CollectionsKt.distinct(privacyTypes));
		mApp = mAppsAndTypes.size() != 1 ? null : (PrivacyApplication) ((Pair) mAppsAndTypes.get(0)).getFirst();
    }

    public List<Pair<PrivacyApplication, List<PrivacyType>>> getAppsAndTypes() {
        return mAppsAndTypes;
    }

    public List<PrivacyType> getTypes() {
        return mTypes;
    }

    public PrivacyApplication getApp() {
        return mApp;
    }

    public List<Drawable> generateIconsForApp(List<? extends PrivacyType> list) {
		Intrinsics.checkParameterIsNotNull(list, "types");
        List<PrivacyType> sorted = CollectionsKt.sorted(list);
        ArrayList generatedForApp = new ArrayList(CollectionsKt.collectionSizeOrDefault(sorted, 10));
        for (PrivacyType icon : sorted) {
            generatedForApp.add(icon.getIcon(mContext));
        }
        return generatedForApp;
    }

    public List<Drawable> generateIcons() {
        ArrayList generated = new ArrayList(CollectionsKt.collectionSizeOrDefault(mTypes, 10));
        for (PrivacyType icon : mTypes) {
            generated.add(icon.getIcon(mContext));
        }
        return generated;
    }

    private <T> StringBuilder joinWithAnd(List<? extends T> list) {
        List subList = list.subList(0, list.size() - 1);
        Appendable sbAppend = new StringBuilder();
		Intrinsics.checkExpressionValueIsNotNull(mSeparator, "separator");
        CollectionsKt.joinTo(subList, sbAppend, mSeparator, null, null, 0, null, null);
        StringBuilder sb = (StringBuilder) sbAppend;
        sb.append(mLastSeparator);
        sb.append(CollectionsKt.last(list));
        return sb;
    }

    public String joinTypes() {
        if (mTypes.size() == 0) {
            return "";
        }
        String sb;
        if (mTypes.size() != 1) {
            List<PrivacyType> list = mTypes;
            ArrayList privacyName = new ArrayList(CollectionsKt.collectionSizeOrDefault(list, 10));
            for (PrivacyType name : list) {
                privacyName.add(name.getName(mContext));
            }
            sb = joinWithAnd(privacyName).toString();
			Intrinsics.checkExpressionValueIsNotNull(sb, "types.map { it.getName(c….joinWithAnd().toString()");
            return sb;
        }
        sb = ((PrivacyType) mTypes.get(0)).getName(mContext);
		Intrinsics.checkExpressionValueIsNotNull(sb, "types[0].getName(context)");
        return sb;
    }

    public String getDialogTitle() {
        String title;
        if (mApp != null) {
            title = mContext.getString(R.string.ongoing_privacy_dialog_single_app_title, new Object[]{joinTypes()});
			Intrinsics.checkExpressionValueIsNotNull(title, "context.getString(R.stri…e_app_title, joinTypes())");
            return title;
        }
        title = mContext.getString(R.string.ongoing_privacy_dialog_multiple_apps_title, new Object[]{joinTypes()});
		Intrinsics.checkExpressionValueIsNotNull(title, "context.getString(R.stri…e_app_title, joinTypes())");
        return title;
    }
}
