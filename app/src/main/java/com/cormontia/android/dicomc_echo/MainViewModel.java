package com.cormontia.android.dicomc_echo;

import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends ViewModel {

    private static ExecutorService executorService;
    private static final int NTHREADS = 3;

    public MainViewModel() {
        if (executorService == null) {
            //TODO?~ Is it wise to use a fixed thread pool here...?
            executorService = Executors.newFixedThreadPool(NTHREADS);
        }
    }

    //TODO?~ Not sure if "execute(Runnable)" is the one we should use here.
    void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}
