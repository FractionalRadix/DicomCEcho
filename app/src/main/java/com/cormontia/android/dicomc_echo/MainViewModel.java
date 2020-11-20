package com.cormontia.android.dicomc_echo;

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
        repository.sendEchoRequest(host, port);
    }
}
