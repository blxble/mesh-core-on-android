package com.blxble.meshpanel.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.blxble.meshpanel.R;
/**
* 
*
*/
public class AlertInfoDialog extends Dialog implements View.OnClickListener {

    public static interface OnOKClickListener {
        public void onOKClick();
    }

    private final Context mContext;
    private TextView mTitleView;
    private TextView mTextView;

    private Button mButtonOk;
    private CharSequence mTitle;

    private CharSequence mText;

    private OnOKClickListener mClickListener;

    public AlertInfoDialog(final Context context, final String title,
            final String text) {
        super(context, R.style.Dialog);
        this.mContext = context;
        this.mTitle = title;
        this.mText = text;
    }

    private void init() {
        setContentView(R.layout.dialog_alert);

        this.mTitleView = (TextView) findViewById(R.id.title);
        final TextPaint tp = this.mTitleView.getPaint();
        tp.setFakeBoldText(true);
        this.mTitleView.setText(this.mTitle);

        this.mTextView = (TextView) findViewById(R.id.text);
        this.mTextView.setText(this.mText);

        this.mButtonOk = (Button) findViewById(R.id.button_ok);
        this.mButtonOk.setOnClickListener(this);

    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.button_ok:
            cancel();
            if (this.mClickListener != null) {
                this.mClickListener.onOKClick();
            }
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBlurEffect();
        init();
    }

    protected void setBlurEffect() {
        final Window window = getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        // lp.alpha=0.8f;
        lp.dimAmount = 0.6f;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }

    public void setMessage(final CharSequence message) {
        this.mText = message;
        this.mTextView.setText(this.mText);
    }

    public void setMessage(final int resId) {
        this.mText = this.mContext.getResources().getText(resId);
        this.mTextView.setText(this.mText);
    }

    public void setOnClickListener(final OnOKClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    @Override
    public void setTitle(final CharSequence title) {
        this.mTitle = title;
        this.mTitleView.setText(this.mTitle);
    }

    @Override
    public void setTitle(final int resId) {
        this.mTitle = this.mContext.getResources().getText(resId);
        this.mTitleView.setText(this.mTitle);
    }

}