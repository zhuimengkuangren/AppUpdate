package com.zhuimeng.appupdate.dialog;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuimeng.appupdate.R;

/**
 * Created by 追梦 on 2017/4/23.
 */

public class CommonDialog extends Dialog {
    public CommonDialog(Context context) {
        super(context);
    }

    public CommonDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public CommonDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder{
        private Context context;
        private String title;
        private String message;
        private String positiveButtonContent;
        private String negativeButtonContent;
        private OnClickListener positiveButtonListener;
        private OnClickListener negativeButtonListener;
        private View contentView;

        /**
         * 建造器的构造方法
         */
        public Builder(Context context){
            this.context = context;
        }

        /**
         * 利用字符串设置title
         * @param title
         * @return
         */
        public Builder setTitle(String title){
            this.title = title;
            return this;//////////////////////
        }

        /**
         * 利用资源id设置title
         * @param title
         * @return
         */
        public Builder setTitle(int title){
            this.title = (String) context.getText(title);
            return this;
        }

        public Builder setMessage(String message){
            this.message = message;
            return this;
        }

        public Builder setPositiveButton(String text, OnClickListener listener){
            this.positiveButtonContent = text;
            this.positiveButtonListener = listener;
            return this;
        }

        private Builder setPositiveButton(int textId, OnClickListener listener){
            this.positiveButtonContent = (String) context.getText(textId);
            this.positiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(String text, OnClickListener listener){
            this.negativeButtonContent = text;
            this.negativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, OnClickListener listener){
            this.negativeButtonContent = (String) context.getText(textId);
            this.negativeButtonListener = listener;
            return this;
        }

        public Builder setContentView(View view){
            this.contentView = view;
            return this;
        }

        public CommonDialog create(){
            /**
             * 利用自定义的样式初始化Dialog
             */
            final CommonDialog dialog = new CommonDialog(context, R.style.Dialog);
            /**
             * 初始化Dialog的布局界面
             */
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.
                    LAYOUT_INFLATER_SERVICE);
            View dialogLayoutView = inflater.inflate(R.layout.layout_dialog,null);
            dialog.addContentView(dialogLayoutView, new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (!TextUtils.isEmpty(title)){
                ((TextView)dialogLayoutView.findViewById(R.id.tv_dialog_title)).setText(title);
            } else {
                Log.w(context.getClass().toString(),"未设置对话框标题！");
            }

            if (!TextUtils.isEmpty(message)){
                ((TextView)dialogLayoutView.findViewById(R.id.dialog_content)).setText(message);
            } else if (contentView != null){
                ((LinearLayout)dialogLayoutView.findViewById(R.id.dialog_layout_content)).
                        removeAllViews();
                ((LinearLayout)dialogLayoutView.findViewById(R.id.dialog_layout_content)).addView(
                        contentView, new ActionBar.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                Log.w(context.getClass().toString(), "未设置对话框提示内容!");
            }

            if (!TextUtils.isEmpty(positiveButtonContent)){
                ((TextView)dialogLayoutView.findViewById(R.id.tv_dialog_pos)).
                        setText(positiveButtonContent);
                if (positiveButtonListener != null){
                    ((TextView)dialogLayoutView.findViewById(R.id.tv_dialog_pos)).
                            setOnClickListener(new View.OnClickListener(){

                                @Override
                                public void onClick(View v) {
                                    positiveButtonListener.onClick(dialog, -1);
                                }
                            });
                }
            } else {
                ((TextView)dialogLayoutView.findViewById(R.id.tv_dialog_pos)).
                        setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(negativeButtonContent)) {
                ((TextView) dialogLayoutView.findViewById(R.id.tv_dialog_neg)).
                        setText(negativeButtonContent);
                if (positiveButtonListener != null) {
                    ((TextView) dialogLayoutView.findViewById(R.id.tv_dialog_neg)).
                            setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    negativeButtonListener.onClick(dialog, -2);
                                }
                            });
                }
            } else {
                ((TextView)dialogLayoutView.findViewById(R.id.tv_dialog_neg)).
                        setVisibility(View.GONE);
            }

            /**
             * 将初始化完整的布局添加到dialog中
             */
            dialog.setContentView(dialogLayoutView);

            /**
             * 禁止点击Dialog以外的区域时Dialog消失
             */
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

}
