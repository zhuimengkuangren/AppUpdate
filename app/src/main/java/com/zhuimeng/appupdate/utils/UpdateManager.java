package com.zhuimeng.appupdate.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 文件下载调度管理器，调用UpdateDownloadRequest
 * Created by 追梦 on 2017/4/23.
 */

public class UpdateManager {

    private static UpdateManager updateManager;
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest request;

    private UpdateManager(){
        //线程池的初始化
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    static {
        updateManager = new UpdateManager();
    }

    public static UpdateManager getInstance(){
        return updateManager;
    }

    public void startDownloads(String downloadUrl,
                               String localPath,
                               UpdateDownloadListener updateDownloadListener){
        if (request != null){
            return;
        }
        checkLocalPath(localPath);
        //来时下载
        request = new UpdateDownloadRequest(downloadUrl, localPath, updateDownloadListener);
        Future<?> future = threadPoolExecutor.submit(request);
    }

    /**
     * 检查文件路径是否存在
     */
    private void checkLocalPath(String path){
        Log.e("tag", path);
        File dir = new File(path.substring(0,path.lastIndexOf("/") + 1));
        if (!dir.exists()){//路径是否存在
            dir.mkdir();
        }
        File file = new File(path);
        if (!file.exists()){//文件是否存在
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
