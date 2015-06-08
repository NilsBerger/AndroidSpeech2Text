package com.morkout.bluetooth;

import android.bluetooth.BluetoothServerSocket;

import java.io.IOException;

/**
 * Created by Nils on 5/31/2015.
 */
public class AcceptThreadSingleton extends Thread {


    private static AcceptThreadSingleton _INSTANCE;
    private BluetoothServerSocket mServerSocket;

    private AcceptThreadSingleton()
    {

    }

    public static synchronized AcceptThreadSingleton getInstance()
    {
        if(AcceptThreadSingleton._INSTANCE == null){
            AcceptThreadSingleton._INSTANCE = new AcceptThreadSingleton();
        }
        return AcceptThreadSingleton._INSTANCE;
    }


}
