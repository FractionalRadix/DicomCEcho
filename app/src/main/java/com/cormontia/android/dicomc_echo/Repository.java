package com.cormontia.android.dicomc_echo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {

    private static final String TAG = "DICOM C-ECHO";

    private final Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
    public Handler getMainThreadHandler() { return handler; }

    //TODO?~ Android documentation suggests we put the ExecutorService in a dependency injection container.
    // For now we're putting it in Repository - there's enough to refactor already. (Famous last words...)
    private static ExecutorService executorService;
    private static final int NTHREADS = 3;

    Repository() {
        if (executorService == null) {
            //TODO?~ Is it wise to use a fixed thread pool here...?
            executorService = Executors.newFixedThreadPool(NTHREADS);
        }
    }

    public void sendEchoRequest(String host, int port, EchoRequestCallback callback) {
        Log.d(TAG, "In Repository.sendEchoRequest(host, port, callback).");
        Log.d(TAG, "host=="+host+", port=="+port);
        Log.d(TAG,  "callback=="+callback);
        executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "In Executor.run() to send DICOM C-ECHO request.");
                        DicomEchoRequest.sendEchoRequest(host, port, callback);
                    }
                }
        );
    }
}

