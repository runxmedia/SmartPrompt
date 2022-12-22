package com.sunrun.smartprompt.com;


import static com.google.android.gms.common.util.IOUtils.copyStream;

import static java.lang.Math.min;

import android.content.Context;
import android.os.Handler;
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
import com.sunrun.smartprompt.model.Status;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class NearbyCom { //Handles nearby communication on both control and teleprompter sides

    ConnectionLifecycleCallback connectionCallback;
    ReceivePayloadCallback payloadCallback;
    String endpoint; //Discoverer Endpoint
    ArrayList <RemotePrompter> remotePrompters;
    Context context;
    public NearbyCom(Context context) {
       connectionCallback = null;
       payloadCallback = new ReceivePayloadCallback();
       remotePrompters = new ArrayList<>();
       this.context = context;
    }

    public void startAdvertising() {

        remotePrompters.clear();


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

                        //Start datastream if this is the first connected client
                        RemotePrompter remotePrompter = new RemotePrompter(endpointId);
                        if(remotePrompters.size() == 0){
                            startDataStream();
                        }
                        remotePrompters.add(remotePrompter);


                        //Send script and font size;
                        sendScript(remotePrompter);
                        sendFontSize(remotePrompter);

                        //Notify of new client
                        Status.setControl_clients(remotePrompters.size());
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
            public void onDisconnected(@NonNull String endpointId) {
                //Stop data stream if there are no connected clients
                if(remotePrompters.size() == 1){
                    stopDataStream();
                }
                for(RemotePrompter prompter : remotePrompters){
                    if(prompter.getEndpointID().equals(endpointId)){
                        remotePrompters.remove(prompter);
                        break;
                    }
                }
                //Notify of new client
                Status.setControl_clients(remotePrompters.size());
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

    public void startDiscovery(){


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
                        endpoint = endpointId;
                        stopDiscovery();
                        Status.notifyConnected();
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
                endpoint = null;
                Status.notifyDisconnected();
                startDiscovery();
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
                                    Nearby.getConnectionsClient(context).acceptConnection(advertiserID, payloadCallback);
                                })
                        .addOnFailureListener(
                                (Exception e) -> {
                                    // Nearby Connections failed to request the connection.
                                });
            }

            @Override
            public void onEndpointLost(@NonNull String endpointID) {
                endpoint = null;
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

    public void stopDiscovery(){
        Nearby.getConnectionsClient(context).stopDiscovery();
    }

    public void stopAdvertising(){
        Nearby.getConnectionsClient(context).stopAdvertising();
    }

    public void closeAll(){
        stopAdvertising();
        stopDiscovery();
        Log.d("Nearby", "ending nearby connections. Goodbye!");
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
                                int scrl_pos = 0;
                                while (!Thread.interrupted()) {
                                    if ((SystemClock.elapsedRealtime() - lastRead) >= READ_STREAM_IN_BG_TIMEOUT) {
                                        Log.e("Receiver", "Read data from stream but timed out.");
                                        break;
                                    }

                                    try {
                                        int availableBytes = inputStream.available();
                                        if (availableBytes > 4) {
//                                            lastUpdate = SystemClock.elapsedRealtime();
                                            byte[] bytes = new byte[availableBytes];
                                            boolean all_bytes_read = inputStream.read(bytes) == availableBytes;
                                            if (all_bytes_read) {
                                                lastRead = SystemClock.elapsedRealtime();

                                                for (int i = 0; i < availableBytes; i++){

                                                    if(bytes[i] == -128 && bytes[i+1] == 0){
                                                        scrl_pos = ((bytes[i+1] & 0xFF) << 24) |
                                                                ((bytes[i+2] & 0xFF) << 16) |
                                                                ((bytes[i+3] & 0xFF) << 8) |
                                                                ((bytes[i + 4] & 0xFF));
                                                        Status.setScroll_position(scrl_pos);
//                                                        Status.addToQueue(scrl_pos);
                                                        i+=4;
                                                    }
                                                }
                                            }
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
            else if(payload.getType() == Payload.Type.BYTES){
                byte[] bytes = payload.asBytes();

                /*Identify what kind of data we are receiving
                * The First Byte is the identifier of the data
                * 0 = scroll position data
                * 1 = first page of the script
                * 2 = subsequent pages of the script
                * 3 = end of script
                * 4 = Font Size
                 */
                if (bytes != null) {
                    switch (bytes[0]){
                        case 0:
                            int intBits = bytes[1] << 24 | (bytes[2] & 0xFF) << 16 |
                                    (bytes[3] & 0xFF) << 8 | (bytes[4] & 0xFF);
                            float scroll_position = Float.intBitsToFloat(intBits);
                            Status.setScroll_position(scroll_position);
                            break;
                        case 1:
                            String new_script = new String(bytes, StandardCharsets.UTF_8);
                            new_script = new_script.substring(1);
                            Status.startNewScript(new_script);
                            break;
                        case 2:
                            String script_addition = new String(bytes, StandardCharsets.UTF_8);
                            script_addition = script_addition.substring(1);
                            Status.appendToScript(script_addition);
                            break;
                        case 3:
                            Status.completeScript();
                            break;
                        case 4:
                            int font_bits = bytes[1] << 24 | (bytes[2] & 0xFF) << 16 |
                                    (bytes[3] & 0xFF) << 8 | (bytes[4] & 0xFF);
                            float fontsize = Float.intBitsToFloat(font_bits);
                            Status.setFont_size(fontsize);
                        default:
                            //Unknown Data
                            break;
                    }
                }
            }
        }

    }

    public void updateScript(){
        for (RemotePrompter prompter : remotePrompters){
            sendScript(prompter);
        }
    }
    public void updateFontSize(){
        for (RemotePrompter prompter : remotePrompters){
            sendFontSize(prompter);
        }
    }

    private void sendScript(RemotePrompter prompter){
        int max_chunk_size = 3999;
        byte[] complete_script = Status.getScript().getBytes(StandardCharsets.UTF_8);
        int script_length = complete_script.length;
        for (int i = 0; i < script_length; i+=max_chunk_size){
            byte[] send_bytes = new byte[min(max_chunk_size,script_length - i)+1];
            if(i == 0){//Signal that this is the first chunk of the script
                send_bytes[0] = 1;
            } else { //Signal that this is a follow-up chunk of the script
                send_bytes[0] = 2;
            }

            //copy script to be sent
            System.arraycopy(complete_script,i,send_bytes,1,send_bytes.length-1);

            //Send Script
            Payload bytes_payload = Payload.fromBytes(send_bytes);
            Nearby.getConnectionsClient(context).sendPayload(prompter.getEndpointID(),bytes_payload);
        }
        //Send end of script signal
        byte[] send_bytes = new byte[1];
        send_bytes[0] = 3;
        Payload bytes_payload = Payload.fromBytes(send_bytes);
        Nearby.getConnectionsClient(context).sendPayload(prompter.getEndpointID(),bytes_payload);
    }

    private void sendFontSize(RemotePrompter prompter){
        byte[] send_bytes = new byte[5];
        System.arraycopy(floatToByteArray(Status.getFont_size()),
                0,send_bytes,1,4);
        send_bytes[0] = 4;
        Payload bytes_payload = Payload.fromBytes(send_bytes);
        Nearby.getConnectionsClient(context).sendPayload(prompter.getEndpointID(),bytes_payload);
    }

    public static byte[] floatToByteArray(float value) {
        int intBits =  Float.floatToIntBits(value);
        return new byte[] {
                (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
    }


    //Background Thread to send dataStream
    final private Handler handler = new Handler();
    final private int delay = 7; //milliseconds
    byte[] send_bytes = new byte[5];
    private final Runnable outputStreamRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                System.arraycopy(floatToByteArray(Status.getScroll_position()),
                        0,send_bytes,1,4);
                send_bytes[0] = 0;
                for (RemotePrompter prompter : remotePrompters) {

                    Payload bytes_payload = Payload.fromBytes(send_bytes);
                    Nearby.getConnectionsClient(context).sendPayload(prompter.getEndpointID(),bytes_payload);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, delay);
        }
    };
    public void startDataStream(){
        handler.postDelayed(outputStreamRunnable, delay);
    }
    public void stopDataStream(){
        handler.removeCallbacks(outputStreamRunnable);
    }


}
