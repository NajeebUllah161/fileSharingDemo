// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_FILE_NAME = "file_name";
    public static final String EXTRAS_FILE_LENGTH = "file_length";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */

    /**
     * We extract Uri, get stream from socketServer and write it to file
     **/
    @Override
    protected void onHandleIntent(Intent intent) {
    /** SENDER SIDE*/
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            ArrayList<Parcelable> fileUri = intent.getExtras().getParcelableArrayList(EXTRAS_FILE_PATH);
            ArrayList<Long> filesLength = (ArrayList<Long>) intent.getSerializableExtra(EXTRAS_FILE_LENGTH);
            ArrayList<String> fileNames = intent.getStringArrayListExtra(EXTRAS_FILE_NAME);
            int len;
            byte buf[] = new byte[8192];

            Log.d("NajeebFileTransferService", fileUri.toString());
            Log.d("NajeebFileTransferService", fileUri.get(0).toString());
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                ContentResolver cr = context.getContentResolver();

                objectOutputStream.writeInt(fileUri.size());
                objectOutputStream.writeObject(fileNames);
//                objectOutputStream.flush();
                objectOutputStream.writeObject(filesLength);
//                objectOutputStream.flush();


                InputStream inputStream = null;
                for (Parcelable singleUri : fileUri) {
                    Log.d("NajeebFileTransferService", String.valueOf(singleUri));
                    try {
                        inputStream = cr.openInputStream(Uri.parse(String.valueOf(singleUri)));
                        while ((len = inputStream.read(buf)) != -1) {
                            objectOutputStream.write(buf, 0, len);

                        }
                        inputStream.close();
                    } catch (FileNotFoundException e) {
                        Log.d(WiFiDirectActivity.TAG, e.toString());
                    }
                    int i = 0;
                    Log.d("FileProgressSender", String.valueOf(i));
                    i++;
//                    DeviceDetailFragment.copyFile(inputStream, outputStream);
                }
                // close output stream one time from sender side
                outputStream.close();
                Log.d(WiFiDirectActivity.TAG, "Client: Data written");

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}