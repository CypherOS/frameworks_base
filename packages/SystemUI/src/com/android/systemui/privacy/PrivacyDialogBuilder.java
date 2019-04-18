package com.android.systemui.privacy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.android.systemui.R;

import kotlin.Pair;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

public class PrivacyDialogBuilder {

    private PrivacyApplication mApp;
    private List<Pair<PrivacyApplication, List<PrivacyType>>> mAppsAndTypes;
    private Context mContext;
    private String mLastSeparator;
    private String mSeparator;
    private List<PrivacyType> mTypes;

    static final class PrimaryItemRank extends Lambda implements Function1<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, Integer> {

        public static final PrimaryItemRank INSTANCE = new PrimaryItemRank();

        PrimaryItemRank() {
            super(1);
        }

        @Override
        public int invoke(Pair<PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
            return -((List) pair.getSecond()).size();
        }
    }

    static final class SecondaryItemRank extends Lambda implements Function1<Pair<? extends PrivacyApplication, ? extends List<? extends PrivacyType>>, PrivacyType> {

        public static final SecondaryItemRank INSTANCE = new SecondaryItemRank();

        SecondaryItemRank() {
            super(1);
        }

        @Override
        public PrivacyType invoke(Pair<PrivacyApplication, ? extends List<? extends PrivacyType>> pair) {
            return (PrivacyType) CollectionsKt__CollectionsKt.min((Iterable) pair.getSecond());
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
        Function1[] topRank = new Function1[2];
        topRank[0] = PrimaryItemRank.INSTANCE;
        topRank[1] = SecondaryItemRank.INSTANCE;
        mAppsAndTypes = CollectionsKt__CollectionsKt.sortedWith(toList, ComparisonsKt__ComparisonsKt.compareBy(topRank));
        ArrayList privacyTypes = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (PrivacyItem privacyType : list) {
            privacyTypes.add(privacyType.getPrivacyType());
        }
        mTypes = CollectionsKt__CollectionsKt.sorted(CollectionsKt__CollectionsKt.distinct(privacyTypes));
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
        List<PrivacyType> sorted = CollectionsKt__CollectionsKt.sorted(list);
        ArrayList generatedForApp = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(sorted, 10));
        for (PrivacyType icon : sorted) {
            generatedForApp.add(icon.getIcon(mContext));
        }
        return generatedForApp;
    }

    public List<Drawable> generateIcons() {
        ArrayList generated = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(mTypes, 10));
        for (PrivacyType icon : mTypes) {
            generated.add(icon.getIcon(mContext));
        }
        return generated;
    }

    private <T> StringBuilder joinWithAnd(List<? extends T> list) {
        List subList = list.subList(0, list.size() - 1);
        Appendable sbAppend = new StringBuilder();
        CollectionsKt__CollectionsKt.joinTo(subList, sbAppend, mSeparator, null, null, 0, null, null, com.android.internal.R.styleable.windowNoTitle, null);
        StringBuilder sb = (StringBuilder) sbAppend;
        sb.append(mLastSeparator);
        sb.append(CollectionsKt__CollectionsKt.last(list));
        return sb;
    }

    public String joinTypes() {
        if (mTypes.size() == 0) {
            return "";
        }
        String sb;
        if (mTypes.size() != 1) {
            List<PrivacyType> list = mTypes;
            ArrayList privacyName = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
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
