package com.cormontia.android.dicomc_echo;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private static final String TAG = "DICOM C-ECHO";

    private static Repository repository;

    private MutableLiveData<String> echoResult = new MutableLiveData<>("");
    public LiveData<String> getEchoResult() { return echoResult; }
    int counter = 1; //TODO!- Just used to check if every update is observe()-d.

    public MainViewModel() {
        if (repository == null)
        {
            repository = new Repository();
        }

        echoResult.setValue("");
    }

    public void sendEchoRequest(String host, int port) {
        Log.d(TAG, "In MainViewModel.sendEchoRequest(host,port).");
        EchoRequestCallback callback = new EchoRequestCallback() {
            @Override
            public void onComplete(NetworkingFailure result) {
                // "setValue()" cannot be invoked on a background thread, so we need either postValue() or a Handler.
                Log.d(TAG, "In EchoRequestCallback.onComplete(EchoResult).");
                //echoResult.postValue("(Message #" + counter + ") " + result.getMessage());
                repository.getMainThreadHandler().post(() -> echoResult.setValue("(Message #" + counter + ") " + result.getMessage()));
                counter++;
            }
        };

        repository.sendEchoRequest(host, port, callback);
    }
}
