package com.zhuimeng.appupdate.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import static com.zhuimeng.appupdate.utils.UpdateDownloadRequest.DownloadResponseHandler.FAILURE_MESSAGE;
import static com.zhuimeng.appupdate.utils.UpdateDownloadRequest.DownloadResponseHandler.FINISH_MESSAGE;
import static com.zhuimeng.appupdate.utils.UpdateDownloadRequest.DownloadResponseHandler.PROGRESS_CHANGED;


/**
 * 文件下载及线程间通讯
 * Created by 追梦 on 2017/4/23.
 */

public class UpdateDownloadRequest implements Runnable {

    private String downloadUrl;
    private String localFilePath;
    private UpdateDownloadListener downloadListener;
    private boolean isDownloading = false;
    private long currentLength;

    private int completeSize = 0;
    private int progress = 0;

    private DownloadResponseHandler downloadResponseHandler;
    private Handler handler;//真正的完成线程间通讯

    public UpdateDownloadRequest(String downloadUrl,
                                 String localFilePath,
                                 UpdateDownloadListener downloadListener) {
        this.downloadUrl = downloadUrl;
        this.localFilePath = localFilePath;
        this.downloadListener = downloadListener;
        this.isDownloading = true;
        this.downloadResponseHandler = new DownloadResponseHandler();

    }

    /**
     * 格式化数字
     *
     * @param value
     * @return
     */
    private String getTwoPointFloatStr(float value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(value);
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (InterruptedIOException e) {
        } catch (IOException e) {
        }
    }

    //真正的去建立连接的方法
    public void makeRequest() throws IOException {
        if (!Thread.currentThread().isInterrupted()) {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.connect();//阻塞我们当前的线程
                currentLength = connection.getContentLength();
                if (!Thread.currentThread().isInterrupted()) {
                    //真正完成文件的下载
                    sendResponseMessage(connection.getInputStream());
//                    downloadResponseHandler.sendResponseMessage(connection.getInputStream());
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    //文件下载方法，会发送各种类型的事件
    void sendResponseMessage(InputStream is) {
        RandomAccessFile randomAccessFile = null;
        completeSize = 0;
        try {
            byte[] buffer = new byte[1024];
            int length = -1; //读写长度
            int limit = 0;
            randomAccessFile = new RandomAccessFile(localFilePath, "rwd");

            while ((length = is.read(buffer)) != -1) {
                if (isDownloading) {
                    randomAccessFile.write(buffer, 0, length);
                    completeSize += length;
                    if (completeSize < currentLength) {
                        Log.e("tag", "completeSize=" + completeSize);
                        Log.e("tag", "currentLength=" + currentLength);
                        progress = (int) Float.parseFloat(getTwoPointFloatStr
                                (completeSize / currentLength));
                        Log.e("tag", "下载进度：" + progress);
                        if (limit % 30 == 0 && progress <= 100) {//隔30次更新一次notification
                            //为了限制一下我们notification的更新频率
                            sendProgressChangedMessage((int) progress);
                        }
                        limit++;
                    }
                }
            }
            sendFinishMessage();
        } catch (IOException e) {
            sendFailureMessage(FailureCode.IO);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                sendFailureMessage(FailureCode.IO);
            }
        }
    }

    /**
     * 下载过程中的异常(枚举)
     * 包含了下载过程中所有可能出现的异常情况
     */
    public enum FailureCode {
        UnknownHost, Socket, SocketTimeout, connectionTimeout, IO, HttpResponse,
        JSON, Interrupted
    }

    /**
     * 用来真正的去下载文件，并发送消息和回调的接口
     */
    public class DownloadResponseHandler {

        protected static final int SUCCESS_MESSAGE = 0;
        protected static final int FAILURE_MESSAGE = 1;
        protected static final int START_MESSAGE = 2;
        protected static final int FINISH_MESSAGE = 3;
        protected static final int NETWORK_OFF = 4;
        protected static final int PROGRESS_CHANGED = 5;

//        private int completeSize = 0;
//        private int  progress = 0;

//        private Handler handler;//真正的完成线程间通讯

        public DownloadResponseHandler() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    handlerSelfMessage(msg);
                }
            };
        }

    }

    /**
     * 用来发送不同的消息对象
     */
    protected void sendFinishMessage() {
        sendMessage(obtainMessage(FINISH_MESSAGE, null));
    }

    private void sendProgressChangedMessage(int progress) {
        sendMessage(obtainMessage(PROGRESS_CHANGED, new Object[]{progress}));
    }

    protected void sendFailureMessage(FailureCode failureCode) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{failureCode}));
    }

    protected void sendMessage(Message msg) {
        if (handler != null) {
            handler.sendMessage(msg);
        } else {
            handlerSelfMessage(msg);
        }
    }

    /**
     * 获取一个消息对象
     *
     * @param responseMessage
     * @param response
     * @return
     */
    protected Message obtainMessage(int responseMessage, Object response) {
        Message msg = null;
        if (handler != null) {
            msg = handler.obtainMessage(responseMessage, response);
        } else {
            msg = Message.obtain();
            msg.what = responseMessage;
            msg.obj = response;
        }
        return msg;
    }

    protected void handlerSelfMessage(Message msg) {
        Object[] response;
        switch (msg.what) {
            case FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                sendFailureMessage((FailureCode) response[0]);
                break;
            case PROGRESS_CHANGED:
                response = (Object[]) msg.obj;
                handleProgressChangedMessage(((Integer) response[0]).intValue());
                break;
            case FINISH_MESSAGE:
                onFinish();
                break;
        }
    }

    protected void handleProgressChangedMessage(int progress) {
        downloadListener.onProgressChanged(progress, downloadUrl);
    }

    protected void onFinish() {
        downloadListener.onFinished(completeSize, "");
    }

    protected void handleFailureMessage(FailureCode failureCode) {
        downloadListener.onFailure();
    }


}
