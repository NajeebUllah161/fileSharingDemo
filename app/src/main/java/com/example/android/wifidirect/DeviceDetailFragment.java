package com.example.android.wifidirect;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Utils.FilesUtil;
import Utils.PathUtil;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_REQUEST_CODE = 20;
    protected static final int CHOOSE_APK_REQUEST_CODE = 30;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    ArrayList<Uri> mArrayUri;
    Uri imageUri;
    ArrayList<Long> filesLength = new ArrayList<>();
    ArrayList<String> fileNames = new ArrayList<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(FileTransferService.RESULT);

                Toast.makeText(context.getApplicationContext(),
                        "File : " + resultCode + " Sent",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mArrayUri = new ArrayList<>();
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        // Disconnect with user
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                v -> ((DeviceActionListener) getActivity()).disconnect());

        // Selecting Image Intent
        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                v -> {
                    // Allow user to pick an image from Gallery or other
                    // registered apps
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, CHOOSE_FILE_REQUEST_CODE);
                });

        // Selecting Apk Intent
        mContentView.findViewById(R.id.btn_select_apk).setOnClickListener(
                v -> {
                    // Allow user to pick an apk from File or other
                    // registered apps
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/vnd.android.package-archive");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, CHOOSE_APK_REQUEST_CODE);
                });

        // Sending data on Peer
        mContentView.findViewById(R.id.send_files).setOnClickListener(
                v -> {
                    Toast.makeText(getActivity(), "Sending Files!", Toast.LENGTH_SHORT).show();
                    sendFiles();
                    clearArrayLists();
//                        Log.d("CheckingInfo", "File Name: " + fileNames + " File Length : " + filesLength + "Uri: " + mArrayUri);
//
                });

        return mContentView;
    }

    // Clear all array lists to avoid duplication of files
    private void clearArrayLists() {
        fileNames.clear();
        filesLength.clear();
        mArrayUri.clear();
    }

    // Send Files to connected device using P2P technology
    public void sendFiles() {

//        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
//        statusText.setText("Sending: " + image);
//        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
        if (!(fileNames.isEmpty() && filesLength.isEmpty() && mArrayUri.isEmpty())) {

            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            Log.d("NajeebDeviceDetailFragment", mArrayUri.toString());
            // sending Uris, FileName and FileLength to FileTransfer Activity
            serviceIntent.putParcelableArrayListExtra(FileTransferService.EXTRAS_FILE_PATH, mArrayUri);
            serviceIntent.putStringArrayListExtra(FileTransferService.EXTRAS_FILE_NAME, fileNames);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_LENGTH, filesLength);

//        Log.d(WiFiDirectActivity.TAG, "ArrayList of Uri " + mArrayUri.toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
            getActivity().startService(serviceIntent);
        } else {
            Toast.makeText(getActivity(), "Please Select Files First.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Successfully picked image, now transfer URI to an intent service which does the rest
        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.

        if (requestCode == CHOOSE_FILE_REQUEST_CODE) {
            if (data.getClipData() != null) {
                ClipData myClipData = data.getClipData();
                int count = myClipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    // adding imageUri in an array
                    imageUri = myClipData.getItemAt(i).getUri();
                    mArrayUri.add(imageUri);


                    String fileName =
                            PathUtil.getPath(getContext(), myClipData.getItemAt(i).getUri());
                    filesLength.add(new File(fileName).length());
                    fileName = FilesUtil.getFileName(fileName);
                    fileNames.add(fileName);

                    Log.d("File URI", myClipData.getItemAt(i).getUri().toString());
                    Log.d("File Path", fileName);

                }

                TextView statusText = mContentView.findViewById(R.id.status_text);
                statusText.setText("Sending: " + imageUri);
                Log.d(WiFiDirectActivity.TAG, "Intent----------- " + imageUri);

            } else {

                Uri imageUri = data.getData();
                mArrayUri.add(imageUri);

                String fileName = PathUtil.getPath(getContext(), imageUri);
                Log.d("Najeeb", fileName);
                filesLength.add(new File(fileName).length());

                fileName = FilesUtil.getFileName(fileName);
                fileNames.add(fileName);

                TextView statusText = mContentView.findViewById(R.id.status_text);
                statusText.setText("Sending: " + imageUri);
                Log.d(WiFiDirectActivity.TAG, "Intent----------- " + imageUri);

            }
        } else if (requestCode == CHOOSE_APK_REQUEST_CODE) {

            if (data.getClipData() != null) {
                ClipData myClipData = data.getClipData();
                int count = myClipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    // adding imageUri in an array
                    imageUri = myClipData.getItemAt(i).getUri();
                    mArrayUri.add(imageUri);

                    String fileName =
                            PathUtil.getPath(getContext(), myClipData.getItemAt(i).getUri());
                    filesLength.add(new File(fileName).length());
                    fileName = FilesUtil.getFileName(fileName);
                    fileNames.add(fileName);

                    Log.d("File URI", myClipData.getItemAt(i).getUri().toString());
                    Log.d("File Path", fileName);

                }

                TextView statusText = mContentView.findViewById(R.id.status_text);
                statusText.setText(getString(R.string.sending_txt) + imageUri);
                Log.d(WiFiDirectActivity.TAG, "Intent----------- " + imageUri);

            } else {
                Uri imageUri = data.getData();
                mArrayUri.add(imageUri);

                String fileName = PathUtil.getPath(getContext(), imageUri);
                Log.d("Najeeb", fileName);
                filesLength.add(new File(fileName).length());

                fileName = FilesUtil.getFileName(fileName);
                fileNames.add(fileName);

                TextView statusText = mContentView.findViewById(R.id.status_text);
                statusText.setText(getString(R.string.sending_txt) + imageUri);
                Log.d(WiFiDirectActivity.TAG, "Intent----------- " + imageUri);
            }
        } else {
            // Complimentary Else
            Log.d("Complimentary else", "Check Complimentary Else comment for troubleshooting");
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.btn_select_apk).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.send_files).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_select_apk).setVisibility(View.GONE);
        mContentView.findViewById(R.id.send_files).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(
                FileTransferService.NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, String, String> {
        /**
         * RECEIVER SIDE
         */
        private final Context context;
        private final TextView statusText;

        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            int len;
            byte[] buf = new byte[8192];
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");

                InputStream inputstream = client.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputstream);
                File globalFile = null;
                // we get size of item to be received
                int sizeOfItems = objectInputStream.readInt();

                // Get filenames and size for now to read the file
                ArrayList<String> fileNames = (ArrayList<String>) objectInputStream.readObject();
                ArrayList<Long> fileSizes = (ArrayList<Long>) objectInputStream.readObject();

                Long fileSize;
                Long fileSizeOriginal;

                for (int i = 0; i < sizeOfItems; i++) {
                    String fileName = fileNames.get(i);
                    fileSize = fileSizes.get(i);
                    fileSizeOriginal = fileSizes.get(i);


                    if (fileName.endsWith(".apk")) {
//                        Log.d("fileName", fileName);
                        globalFile = new File(context.getExternalFilesDir("received"),
                                "wifip2pshared-" + System.currentTimeMillis()
                                        + ".apk");
                    } else {
                        globalFile = new File(context.getExternalFilesDir("received"),
                                "wifip2pshared-" + System.currentTimeMillis()
                                        + ".jpg");
                    }

                    File dirs = new File(globalFile.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    globalFile.createNewFile();

                    Log.d(WiFiDirectActivity.TAG, "server: copying files " + globalFile.toString());

                    OutputStream outputStream = new FileOutputStream(globalFile);
                    while (fileSize > 0 && (len = objectInputStream.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                        outputStream.write(buf, 0, len);
//                        outputStream.flush();
                        fileSize -= len;

                    }
                    outputStream.close();
//                    int iter = 0;
//                    Log.d("FileProgressReceiver", String.valueOf(iter));

                    publishProgress(Integer.toString(i));
                }

                // close input stream one time from receiver side
                objectInputStream.close();
                serverSocket.close();
                return globalFile.getAbsolutePath();
            } catch (IOException | ClassNotFoundException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(context.getApplicationContext(), "File " + values + " shared.", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);

                File recvFile = new File(result);
                Uri fileUri = FileProvider.getUriForFile(
                        context,
                        "com.example.android.wifidirect.fileprovider",
                        recvFile);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setDataAndType(fileUri, "image/*");
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                context.startActivity(intent);
            }

        }

        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

}
