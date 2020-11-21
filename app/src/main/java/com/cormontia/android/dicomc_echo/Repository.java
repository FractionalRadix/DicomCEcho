package com.cormontia.android.dicomc_echo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {

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
        executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        DicomEchoRequest.sendEchoRequest(host, port, callback);
                    }
                }
        );
    }
}

