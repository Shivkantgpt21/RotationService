package com.app.ar.rotationservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.app.ar.rotationservice.IRotation;
import com.app.ar.rotationservice.ItaskCallback;
import com.app.ar.rotationservice.lib.Orientation;
import androidx.annotation.Nullable;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RotationService extends Service implements Orientation.Listener {

    private RemoteCallbackList<ItaskCallback> mCallbacks;
    @Inject
    Orientation mOrientation;

    @Override
    public void onCreate() {
        super.onCreate();
        mCallbacks = new RemoteCallbackList();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IRotation.Stub mBinder = new IRotation.Stub() {
        @Override
        public void registerCallback(ItaskCallback cb) throws RemoteException {
            if (cb != null)
                // Register RemoteCallbackList
                mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(ItaskCallback cb) throws RemoteException {
            if (cb != null)
                // Release registration RemoteCallbackList
                mCallbacks.unregister(cb);
        }

        @Override
        public void startCallback() throws RemoteException {
            if (mOrientation != null)
                mOrientation.startListening(RotationService.this);
        }
    };

    @Override
    public void onOrientationChanged(float pitch, float roll) {
        callback(pitch, roll);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOrientation != null) {
            mOrientation.stopListening();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mOrientation != null) {
            mOrientation.stopListening();
        }
        return super.onUnbind(intent);
    }

    void callback(float pitch, float roll) {
        if (mCallbacks != null) {
            int n = mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    mCallbacks.getBroadcastItem(i).valueChanged(pitch, roll);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                    e.printStackTrace();
                }
            }
            mCallbacks.finishBroadcast();
        }

    }


}