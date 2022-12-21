package com.sunrun.smartprompt.com;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class RemotePrompter { //Class to encapsulate all data related to connected teleprompters

    private String endpointID;
    private final PipedInputStream inputStream;
    private final PipedOutputStream outputStream;

    public RemotePrompter(String endpointID) {
        this.endpointID = endpointID;
        this.inputStream = new PipedInputStream();
        this.outputStream = new PipedOutputStream();
        try {
            outputStream.connect(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEndpointID() {
        return endpointID;
    }

    public void setEndpointID(String endpointID) {
        this.endpointID = endpointID;
    }

    public PipedInputStream getInputStream() {
        return inputStream;
    }

    public PipedOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        inputStream.close();
        outputStream.close();
    }
}
