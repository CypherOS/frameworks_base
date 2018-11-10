package com.android.systemui.smartspace;

public final class SmartSpaceCardListener implements Runnable {

    private final SmartSpaceController mController;
    private final NewCardInfo mNewCardInfo;
    private final SmartSpaceCard mSmartSpaceCard;

    public SmartSpaceCardListener(SmartSpaceController smartSpaceController, NewCardInfo newCardInfo, SmartSpaceCard smartSpaceCard) {
        mController = smartSpaceController;
        mNewCardInfo = newCardInfo;
        mSmartSpaceCard = smartSpaceCard;
    }

    public final void run() {
        SmartSpaceController.onNewCard(mController, mNewCardInfo, mSmartSpaceCard);
    }
}
