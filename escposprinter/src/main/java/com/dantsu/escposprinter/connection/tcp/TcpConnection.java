package com.dantsu.escposprinter.connection.tcp;

import android.util.Log;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpConnection extends DeviceConnection {
    private Socket socket = null;
    private DataInputStream inputStream;
    private String address;
    private int port;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private OnPaperEndListener onPaperEndListener;

    /**
     * Create un instance of TcpConnection.
     *
     * @param address IP address of the device
     * @param port    Port of the device
     */
    public TcpConnection(String address, int port, OnPaperEndListener onPaperEndListener) {
        super();
        this.address = address;
        this.port = port;
        this.onPaperEndListener = onPaperEndListener;
    }

    /**
     * Check if the TCP device is connected by socket.
     *
     * @return true if is connected
     */
    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected() && super.isConnected();
    }

    /**
     * Start socket connection with the TCP device.
     */
    public TcpConnection connect() throws EscPosConnectionException {
        if (this.isConnected()) {
            return this;
        }
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(InetAddress.getByName(this.address), this.port));
            this.stream = this.socket.getOutputStream();
            this.inputStream = new DataInputStream(this.socket.getInputStream());
            listenInputStream();
            this.data = new byte[0];
//            printAllCharacters(0x0a, stream);
        } catch (IOException e) {
            e.printStackTrace();
            this.socket = null;
            this.stream = null;
            throw new EscPosConnectionException("Unable to connect to TCP device.");
        }
        return this;
    }

    /**
     * Close the socket connection with the TCP device.
     */
    public TcpConnection disconnect() {
        this.data = new byte[0];
        if (this.stream != null) {
            try {
                this.stream.close();
                this.stream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
                this.inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.socket != null) {
            try {
                this.socket.close();
                this.socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return this;
    }

    private static void printAllCharacters(int charset, OutputStream os) throws IOException {
        String header = "Characters table, Charset " + charset + "\n\n";
        if (charset == -1)
            header = "Characters table, charset not modified\n\n";

        os.write(header.getBytes("csPC862LatinHebrew"));

        byte[] changeCharset = new byte[]{0x1B, 0x74, (byte)charset};
        os.write(changeCharset);

        os.write("    ".getBytes());
        for (int i = 0; i < 16; i++) {
            os.write((Integer.toHexString(i) + " ").getBytes());
        }

        os.write("\n".getBytes());

        int[] nonPrintable = new int[] {0x0A, 0x0D, 0x1B, 0x1C, 0x1D};

        for (int i = 0; i < 0x10; i++) {
            os.write(("\n" +Integer.toHexString(i) + "   ").getBytes());
            for (int j = 0; j < 0x10; j++) {
                int charCode = (i << 4) | j;
                if (java.util.Arrays.binarySearch(nonPrintable, charCode) >= 0)
                    continue;

                os.write((byte)charCode);
                os.write(" ".getBytes());
            }

        }
        os.write("\n\n\n\n\n\n".getBytes());
    }

    private void listenInputStream() {
        final byte[] buffer = new byte[1024];
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (!executor.isShutdown() && !socket.isClosed()) {
                    try {
                        int bytesCount = inputStream.read(buffer);
                        if (onPaperEndListener != null && Arrays.equals(Arrays.copyOf(buffer, bytesCount), new byte[] { 28, 0, 12, 15 })) {
                            onPaperEndListener.onPaperEnd();
                        }
//                        String bytes = inputStream.readUTF();
//                        Log.i("logger", bytes);

                    } catch (IOException e) {
                        Log.i("logger", e.toString());
                    }
                }
            }
        });
    }
}
