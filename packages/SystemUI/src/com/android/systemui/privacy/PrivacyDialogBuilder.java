package com.android.systemui.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jotlin.aoscp.Collections;
import jotlin.aoscp.Comparisons;
import jotlin.aoscp.Pair;

import com.android.systemui.R;

import kotlin.comparisons.ComparisonsKt__ComparisonsKt;

public class PrivacyDialogBuilder {

    private PrivacyApplication mApp;
    private List<Pair<PrivacyApplication, List<PrivacyType>>> mAppsAndTypes;
    private Context mContext;
    private String mLastSeparator;
    private String mSeparator;
    private List<PrivacyType> mTypes;

    static final class PrimaryItemRank implements Method<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, Integer> {

        public static final PrimaryItemRank INSTANCE = new PrimaryItemRank();

        PrimaryItemRank() {
            super(1);
        }

        @Override
        public int invoke(Pair<PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
            return -((List) pair.getSecond()).size();
        }
    }

    static final class SecondaryItemRank implements Method<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, PrivacyType> {

        public static final SecondaryItemRank INSTANCE = new SecondaryItemRank();

        SecondaryItemRank() {
            super(1);
        }

        @Override
        public PrivacyType invoke(Pair<PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
            return (PrivacyType) Collections.min((Iterable) pair.getSecond());
        }
    }

    public PrivacyDialogBuilder(Context context, List<PrivacyItem> list) {
        mContext = context;
		mLastSeparator = mContext.getString(R.string.ongoing_privacy_dialog_last_separator);
		mSeparator = mContext.getString(R.string.ongoing_privacy_dialog_separator);
        LinkedHashMap privacyItems = new LinkedHashMap();
        for (PrivacyItem privacyItem : list) {
            PrivacyApplication app = privacyItem.getApplication();
            ArrayList privacyApp = privacyItems.get(app);
            if (privacyApp == null) {
                privacyApp = new ArrayList();
                privacyItems.put(app, privacyApp);
            }
            privacyApp.add(privacyItem.getPrivacyType());
        }
		List toList = new ArrayList(privacyItems.values());
        Method[] topRank = new Method[2];
        topRank[0] = PrimaryItemRank.INSTANCE;
        topRank[1] = SecondaryItemRank.INSTANCE;
        mAppsAndTypes = Collections.sortedWith(toList, Comparisons.compareBy(topRank));
        ArrayList privacyTypes = new ArrayList(Collections.collectionSizeOrDefault(list, 10));
        for (PrivacyItem privacyType : list) {
            privacyTypes.add(privacyType.getPrivacyType());
        }
        mTypes = Collections.sorted(Collections.distinct(privacyTypes));
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
        List<PrivacyType> sorted = Collections.sorted(list);
        ArrayList generatedForApp = new ArrayList(Collections.collectionSizeOrDefault(sorted, 10));
        for (PrivacyType icon : sorted) {
            generatedForApp.add(icon.getIcon(mContext));
        }
        return generatedForApp;
    }

    public List<Drawable> generateIcons() {
        ArrayList generated = new ArrayList(Collections.collectionSizeOrDefault(mTypes, 10));
        for (PrivacyType icon : mTypes) {
            generated.add(icon.getIcon(mContext));
        }
        return generated;
    }

    private <T> StringBuilder joinWithAnd(List<? extends T> list) {
        List subList = list.subList(0, list.size() - 1);
        Appendable sbAppend = new StringBuilder();
        Collections.joinTo(subList, sbAppend, mSeparator, null, null, 0, null, null, com.android.internal.R.styleable.windowNoTitle, null);
        StringBuilder sb = (StringBuilder) sbAppend;
        sb.append(mLastSeparator);
        sb.append(Collections.last(list));
        return sb;
    }

    public String joinTypes() {
        if (mTypes.size() == 0) {
            return "";
        }
        String sb;
        if (mTypes.size() != 1) {
            List<PrivacyType> list = mTypes;
            ArrayList privacyName = new ArrayList(Collections.collectionSizeOrDefault(list, 10));
            for (PrivacyType name : list) {
                privacyName.add(name.getName(mContext));
            }
            sb = joinWithAnd(privacyName).toString();
            return sb;
        }
        sb = ((PrivacyType) mTypes.get(0)).getName(mContext);
        return sb;
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
