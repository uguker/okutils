package com.uguke.android.okgo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uguke.okgo.R;

/**
 * 功能描述：Loading对话框
 * @author LeiJue
 */
public class LoadingDialog extends Dialog {

    private Window mWindow;
    private TextView mText;
    private ProgressBar mLoading;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.OkLoadingTheme);
        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 窗口
        mWindow = getWindow();
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // 设置布局
        setContentView(R.layout.dlg_ok_loading);
        // 取消点击
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        // 获取控件
        mLoading = findViewById(R.id.ok_loading);
        mText = findViewById(R.id.ok_text);
        mText.setTextColor(Color.parseColor("#F0F0F0"));
    }

    public LoadingDialog dimEnable(boolean enable) {
        if (enable) {
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            mText.setTextColor(Color.parseColor("#F0F0F0"));
        } else {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            mText.setTextColor(Color.parseColor("#999999"));
        }
        return this;
    }

    public LoadingDialog color(@ColorInt int color) {
        int temp = color;
        if (color == Color.TRANSPARENT) {
            temp = ContextCompat.getColor(getContext(), R.color.colorAccent);
        }
        DrawableCompat.setTint(mLoading.getIndeterminateDrawable(), temp);
        return this;
    }

    public LoadingDialog text(CharSequence text) {
        mText.setText(text);
        return this;
    }

    public LoadingDialog text(@StringRes int resId) {
        mText.setText(resId);
        return this;
    }

    public LoadingDialog size(float size) {
        float density = getContext().getResources().getDisplayMetrics().density;
        mLoading.getLayoutParams().width = (int) (size * density);
        mLoading.getLayoutParams().height = (int) (size * density);
        mText.setTextSize(size / 4);
        mText.setPadding(0, (int) (size / 10 * density), 0, 0);
        return this;
    }

}