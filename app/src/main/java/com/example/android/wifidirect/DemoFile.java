package com.example.android.wifidirect;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DemoFile {

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case ChooseFile.FILE_TRANSFER_CODE:
//                if (data == null) return;
//
//                ArrayList<Uri> uris = new ArrayList<>();
//                ArrayList<Long> filesLength = new ArrayList<>();
//                ArrayList<String> fileNames = new ArrayList<>();
//
//                try {
//                    ClipData clipData = data.getClipData();
//
//                    if (clipData != null) {
//                        for (int i = 0; i < clipData.getItemCount(); i++) {
//                            uris.add(clipData.getItemAt(i).getUri());
//
//                            String fileName =
//                                    PathUtil.getPath(getApplicationContext(), clipData.getItemAt(i).getUri());
//                            filesLength.add(new File(fileName).length());
//
//                            fileName = FilesUtil.getFileName(fileName);
//                            fileNames.add(fileName);
//
//                            Log.d("File URI", clipData.getItemAt(i).getUri().toString());
//                            Log.d("File Path", fileName);
//                        }
//                    } else {
//                        Uri uri = data.getData();
//                        uris.add(uri);
//
//                        String fileName = PathUtil.getPath(getApplicationContext(), uri);
//                        filesLength.add(new File(fileName).length());
//
//                        fileName = FilesUtil.getFileName(fileName);
//                        fileNames.add(fileName);
//                    }
//                    sendFilesAdapter.notifyAdapter(uris, filesLength, fileNames);
//
//                    TransferData transferData = new TransferData(FileActivity.this,
//                            uris, filesLength, fileNames, (sendFilesAdapter), serverAddress, p2pManager, channel);
//                    transferData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//        }
//    }
//
//
//
//    public TransferData(Context context,
//                        ArrayList<Uri> uris,
//                        ArrayList<Long> filesLength,
//                        ArrayList<String> fileNames,
//                        FilesSendAdapter referenceSendFilesAdapter,
//                        InetAddress serverAddress,
//                        final WifiP2pManager manager,
//                        final WifiP2pManager.Channel channel) {
//        this.context = context;
//        this.channel = channel;
//        this.manager = manager;
//        this.sendFilesAdapter = referenceSendFilesAdapter;
//        this.uris = uris;
//        this.fileNames = fileNames;
//        this.filesLength = filesLength;
//        this.serverAddress = serverAddress;
//
//        Log.d(" DEBUG::::   ", serverAddress.getHostAddress());
//    }
//
//
//
//
//    private void sendData(Context context, ArrayList<Uri> uris) {
//
//        int len = 0;
//        byte buf[] = new byte[8192];
//
//        Log.d("Data Transfer", "Transfer Starter");
//
//        Socket socket = new Socket();
//
//        try {
//            socket.bind(null);
//            Log.d("Client Address", socket.getLocalSocketAddress().toString());
//
//            socket.connect(new InetSocketAddress(serverAddress, 8888));
//            Log.d("Client", "Client Connected 8888");
//
//            OutputStream outputStream = socket.getOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
//            ContentResolver cr = context.getContentResolver();
//
//            objectOutputStream.writeInt(uris.size());
//
//            objectOutputStream.writeObject(fileNames);
//            objectOutputStream.flush();
//
//            objectOutputStream.writeObject(filesLength);
//            objectOutputStream.flush();
//
//            for (int i = 0; i < uris.size(); i++) {
//
//                InputStream inputStream = cr.openInputStream(uris.get(i));
//
//                while ((len = inputStream.read(buf)) != -1) {
//                    objectOutputStream.write(buf, 0, len);
//                    objectOutputStream.flush();
//                }
//                inputStream.close();
//                publishProgress(fileNames.get(i));
//
//                Log.d("TRANSFER", "Writing Data Final   -" + len);
//
//            }
//
//            objectOutputStream.close();
//            socket.close();
//
//        } catch (Exception e) {
//            Log.d("Data Transfer", e.toString());
//            e.printStackTrace();
//        } finally {
//            if (socket.isConnected()) {
//                try {
//                    socket.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }

//    in onCreate on FileActivity:
//
//            //line 122
//            //init sockets for transport servers and callbacks for reinit servers
//            this.initSockets();
//    callbackReInitFileServer = FileActivity.this::initFileServer;
//    callbackReInitDeviceServer = FileActivity.this::initDeviceInfoServers;

//line 198

    // Init Device info server for receiving device name who connected
//    this.initDeviceInfoServers();


// Line 121 and 123
    //init sockets for transport servers and callbacks for reinit servers
//    this.initSockets();
//    callbackReInitFileServer = FileActivity.this::initFileServer;
//    callbackReInitDeviceServer = FileActivity.this::initDeviceInfoServers;
//    this.initFileServer(); // Init file server for receiving data


// Line 152
//    try {
////      DeviceUtil.callHiddenMethod(manager, channel, newName);
//        DeviceUtil.callHiddenMethod(p2pManager, channel,
//                sharedPreferences.getString(Variables.NAME_DEVICE, null));
//        this.initFileServer();
//
//    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//        e.printStackTrace();
//    }


    //function
//    private void initDeviceInfoServers() {
//
//        deviceInfoServerAsyncTask = new DeviceInfoServerAsyncTask(
//                (serverSocketDevice),
//                (FileActivity.this.peersAdapter), callbackReInitDeviceServer);
//        deviceInfoServerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }

//function

//    private void initFileServer() {
//
//        fileServerAsyncTask = new FileServerAsyncTask(
//                (FileActivity.this),
//                (serverSocket),
//                (FileActivity.this.receiveFilesAdapter), callbackReInitFileServer);
//        fileServerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//    }

//
//    private void recieveData() {
//
//        byte buf[] = new byte[8192];
//        int len = 0;
//
//        try {
//            Log.d("Reciever", "Server Listening");
//            Log.d("Reciever Address", serverSocket.getLocalSocketAddress().toString());
//            Log.d("Reciever Port", String.valueOf(serverSocket.getLocalPort()));
//            client = serverSocket.accept();
//            Log.d("Reciever", "Server Connected");
//            if (isCancelled()) return;
//
//            InputStream inputStream = client.getInputStream();
//            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//
//            //We get size of items to be receive
//            int sizeOfItems = objectInputStream.readInt();
//
//            //Get filenames and size for know to read file
//            ArrayList<String> fileNames = (ArrayList<String>) objectInputStream.readObject();
//            ArrayList<Long> fileSizes = (ArrayList<Long>) objectInputStream.readObject();
//
//            for (int i = 0; i < sizeOfItems; i++) {
//
//                String fileName = fileNames.get(i);
//                fileSize = fileSizes.get(i);
//                fileSizeOriginal = fileSizes.get(i);
//                file = new File(sharedPreferences.getString(Variables.APP_TYPE, Environment.getExternalStorageDirectory() + "/"
//                        + context.getApplicationContext().getPackageName()) + "/" + fileName);
//                Log.d("Reciever", file.getPath());
//
//
//                File dir = file.getParentFile();
//                if (!dir.exists()) dir.mkdirs();
//                if (file.exists()) file.delete();
//                if (file.createNewFile()) {
//                    Log.d("Reciever", "File Created");
//
//                } else Log.d("Reciever", "File Not Created");
//
//                OutputStream outputStream = new FileOutputStream(file);
//
//                //customObject need for progress update
//                CustomObject progress = new CustomObject();
//                progress.name = fileName;
//                progress.dataIncrement = 0;
//                progress.totalProgress = 0;
//
//                try {
//
//                    while (fileSize > 0 &&
//                            (len = objectInputStream.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
//
//                        outputStream.write(buf, 0, len);
//                        outputStream.flush();
//
//                        fileSize -= len;
//
//                        progress.dataIncrement = (long) len;
//                        if (((int) (progress.totalProgress * 100 / fileSizeOriginal)) ==
//                                ((int) ((progress.totalProgress + progress.dataIncrement) * 100 / fileSizeOriginal))) {
//                            progress.totalProgress += progress.dataIncrement;
//                            continue;
//                        }
//
//                        progress.totalProgress += progress.dataIncrement;
//                        publishProgress(progress);
//                        if (this.isCancelled()) return;
//
//                    }
//
//                    Log.d("Reciever", "Writing Data Final   -" + len);
//                } catch (Exception e) {
//                    Log.d("Reciever", "oops");
//                    e.printStackTrace();
//                }
//
//                outputStream.flush();
//                outputStream.close();
//
//            }
//
//            objectInputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
