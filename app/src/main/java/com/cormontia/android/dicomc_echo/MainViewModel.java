package com.cormontia.android.dicomc_echo;

import android.util.Log;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private static Repository repository;

    public MainViewModel() {
        if (repository == null)
        {
            repository = new Repository();
        }
    }

    public static void sendEchoRequest(String host, int port) {
        repository.sendEchoRequest(host, port, new RepositoryCallback() {
            @Override
            public void onComplete(EchoResult result) {
                Log.d("ECHO RESULT!!","Callback result: " + result.getMessage());
            }
        });
    }
}
