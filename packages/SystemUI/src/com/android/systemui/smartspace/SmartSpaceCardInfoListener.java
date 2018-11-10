package com.android.systemui.smartspace;

public final class SmartSpaceCardInfoListener implements Runnable {

    private final SmartSpaceController mController;
    private final NewCardInfo mNewCardInfo;

    public SmartSpaceCardInfoListener(SmartSpaceController smartSpaceController, NewCardInfo newCardInfo) {
        mController = smartSpaceController;
        mNewCardInfo = newCardInfo;
    }

    public final void run() {
        SmartSpaceController.onNewCard(mController, mNewCardInfo);
    }
}
