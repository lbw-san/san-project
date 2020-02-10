package com.openfire.imchat.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MegService extends Service {
    public MegService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
