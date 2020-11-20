package com.cormontia.android.dicomc_echo;

import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private static Repository repository;

    private MutableLiveData<String> echoResult = new MutableLiveData<>("");
    public LiveData<String> getEchoResult() { return echoResult; }

    public MainViewModel() {
        if (repository == null)
        {
            repository = new Repository();
        }

        echoResult.setValue("");
    }

    public void sendEchoRequest(String host, int port) {
        RepositoryCallback callback = new RepositoryCallback() {
            @Override
            public void onComplete(EchoResult result) {
                //TODO!~ Update the ViewModel. More specifically, update some LiveData in the ViewModel.
                // That LiveData will then be watched by our View.
                // ...but... then I don't need a Handler and a Looper, right?!
                Log.d("ECHO RESULT!!", "Callback result: " + result.getMessage());

                //TODO!+ "Cannot invoke setValue on a background thread".
                // So, we need a looper just to call setValue...
                //   echoResult.setValue(result.getMessage());
                Handler handler = repository.getMainThreadHandler();
                handler.post(() -> echoResult.postValue(result.getMessage()));
            }
        };

        repository.sendEchoRequest(host, port, callback);
    }
}
