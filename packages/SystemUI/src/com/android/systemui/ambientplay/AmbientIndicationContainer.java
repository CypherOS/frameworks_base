package com.android.systemui.ambientplay;

import android.ambientplay.AmbientPlayRecognition.PrintResult;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.ambientplay.AmbientIndicationInflateListener;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.doze.DozeReceiver;
import com.android.systemui.statusbar.phone.StatusBar;

public class AmbientIndicationContainer extends AutoReinflateContainer implements DozeReceiver {
	
    private View mAmbientIndication;
    private boolean mDozing;
    private ImageView mIcon;
    private AmbientPlayRecognition.PrintResult mIndication;
    private StatusBar mStatusBar;
    private TextView mText;
    private Context mContext;

    public AmbientIndicationContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
    }

    public void hideIndication() {
        setIndication(null);
    }

    public void initializeView(StatusBar statusBar) {
        mStatusBar = statusBar;
        addInflateListener(new AmbientIndicationInflateListener(this));
    }

    public void updateAmbientIndicationView(View view) {
        mAmbientIndication = findViewById(R.id.ambient_indication);
        mText = (TextView)findViewById(R.id.ambient_indication_text);
        mIcon = (ImageView)findViewById(R.id.ambient_indication_icon);
        setIndication(mIndication);
    }

    @Override
    public void setDozing(boolean dozing) {
        mDozing = dozing;
    }

    public void setIndication(AmbientPlayRecognition.PrintResult result) {
        mText.setText(String.format(mContext.getResources().getString(R.string.ambient_track_info),
		                            result.TrackName, result.ArtistName));
        mIndication = result;
        mAmbientIndication.setClickable(false);
        if (result.TrackName == null && result.ArtistName == null) {
            mAmbientIndication.setVisibility(View.INVISIBLE);
        } else {
            mAmbientIndication.setVisibility(View.VISIBLE);
        }
    }
}