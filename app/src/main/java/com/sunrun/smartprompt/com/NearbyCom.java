package com.sunrun.smartprompt.com;


import static com.google.android.gms.common.util.IOUtils.copyStream;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedReader;
import java.io.PipedWriter;

public class NearbyCom {

    ConnectionLifecycleCallback connectionCallback;
    ReceivePayloadCallback payloadCallback;



    public NearbyCom() {
       connectionCallback = null;
       payloadCallback = new ReceivePayloadCallback();
    }

    public void startAdvertising(Context context) {

        connectionCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                // Automatically accept the connection on both sides.
                Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                switch (result.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
//                        InputStream inputStream = new PipedInputStream();
//                        Payload streamPayload = Payload.fromStream(inputStream);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        break;
                    default:
                        // Unknown status code
                }
            }

            @Override
            public void onDisconnected(@NonNull String s) {

            }
        };



        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();


        Nearby.getConnectionsClient(context)
                .startAdvertising(
                        "SmartPromptControl", "com.sunrun.smartprompt", connectionCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're advertising!
                            Log.d("Nearby", "We're Advertising");
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We were unable to start advertising.
                            Log.d("Nearby", "We're NOT Advertising");

                        });
    }

    public void startDiscovery(Context context){

        connectionCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                // Automatically accept the connection on both sides.
                Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
                Log.d("Nearby","Connection Initiated");

            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                Log.d("Nearby","Connection Result: " + result.getStatus().toString());
                switch (result.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        break;
                    default:
                        // Unknown status code
                }
            }

            @Override
            public void onDisconnected(@NonNull String s) {

            }
        };

        //setup callback for discoverer
        EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String advertiserID, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                Log.d("Nearby","Endpoint Found");
                // An endpoint was found. We request a connection to it.
                Nearby.getConnectionsClient(context)
                        .requestConnection("SmartPromptPromptr", advertiserID, connectionCallback)
                        .addOnSuccessListener(
                                (Void unused) -> {
                                    // We successfully requested a connection. Now both sides
                                    // must accept before the connection is established.
                                })
                        .addOnFailureListener(
                                (Exception e) -> {
                                    // Nearby Connections failed to request the connection.
                                });
            }

            @Override
            public void onEndpointLost(@NonNull String s) {

            }
        };

        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(context)
                .startDiscovery("com.sunrun.smartprompt", endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're discovering!
                            Log.d("Nearby", "We're Discovering!");
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We're unable to start discovering.
                            Log.d("Nearby", "We're Not Discovering!");
                        });
    }


    //Stream Payload Callback Class
    static class ReceivePayloadCallback extends PayloadCallback {
        private final SimpleArrayMap<Long, Thread> backgroundThreads = new SimpleArrayMap<>();

        private static final long READ_STREAM_IN_BG_TIMEOUT = 5000;

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, PayloadTransferUpdate update) {
            if (backgroundThreads.containsKey(update.getPayloadId())
                    && update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                backgroundThreads.get(update.getPayloadId()).interrupt();
            }
        }

        @Override
        public void onPayloadReceived(@NonNull String endpointId, Payload payload) {

            //Receiving Stream Payloads
            if (payload.getType() == Payload.Type.STREAM) {
                // Read the available bytes in a while loop to free the stream pipe in time. Otherwise, the
                // bytes will block the pipe and slow down the throughput.
                Thread backgroundThread =
                        new Thread() {
                            @Override
                            public void run() {
                                InputStream inputStream = payload.asStream().asInputStream();
                                long lastRead = SystemClock.elapsedRealtime();
                                while (!Thread.interrupted()) {
                                    if ((SystemClock.elapsedRealtime() - lastRead) >= READ_STREAM_IN_BG_TIMEOUT) {
                                        Log.e("MyApp", "Read data from stream but timed out.");
                                        break;
                                    }

                                    try {
                                        int availableBytes = inputStream.available();
                                        if (availableBytes > 0) {
                                            byte[] bytes = new byte[availableBytes];
                                            if (inputStream.read(bytes) == availableBytes) {
                                                lastRead = SystemClock.elapsedRealtime();
                                                // Do something with is here...
                                            }
                                        } else {
                                            // Sleep or just continue.
                                        }
                                    } catch (IOException e) {
                                        Log.e("MyApp", "Failed to read bytes from InputStream.", e);
                                        break;
                                    } // try-catch
                                } // while
                            }
                        };
                backgroundThread.start();
                backgroundThreads.put(payload.getId(), backgroundThread);
            }
        }
    }
}
