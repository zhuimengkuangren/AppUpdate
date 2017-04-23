package com.zhuimeng.appupdate.utils;

/**
 * Created by 追梦 on 2017/4/23.
 */

public interface UpdateDownloadListener {
    /**
     * 下载请求开始回调
     */
    public void onStarted();

    /**进度更新回调
     *
     * @param progress
     * @param downloadUrl
     */
    public void onProgressChanged(int progress, String downloadUrl);

    /**
     * 下载完成回调
     * @param completeSize
     * @param downloadUrl
     */
    public void onFinished(float completeSize, String downloadUrl);

    /**
     *下载是失败回调
     */
    public void onFailure();
}