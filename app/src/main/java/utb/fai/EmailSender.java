package utb.fai;

import java.net.*;
import java.io.*;

public class EmailSender {

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    /*
     * Constructor opens Socket to host/port. If the Socket throws an exception
     * during opening,
     * the exception is not handled in the constructor.
     */
    public EmailSender(String host, int port) throws UnknownHostException, IOException {
        socket = new Socket(host, port);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        // uvitaci zprava serveru
        sleep(100);
        readResponse();

        sendCommand("EHLO localhost\r\n");
    }

    /*
     * Sends email from an email address to an email address with some subject and
     * text.
     * If the Socket throws an exception during sending, the exception is not
     * handled by this method.
     */
    public void send(String from, String to, String subject, String text) throws IOException {
        sendCommand("MAIL FROM:<" + from + ">\r\n");
        sendCommand("RCPT TO:<" + to + ">\r\n");
        sendCommand("DATA\r\n");

        String message = "From: " + from + "\r\n"
                + "To: " + to + "\r\n"
                + "Subject: " + subject + "\r\n"
                + "\r\n"
                + text + "\r\n"
                + ".\r\n";
        sendCommand(message);
    }

    /*
     * Sends QUIT and closes the socket
     */
    public void close() {
        try {
            sendCommand("QUIT\r\n");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCommand(String message) throws IOException {
        out.write(message.getBytes());
        out.flush();

        sleep(100);
        readResponse();
    }

    private void readResponse() throws IOException {
        final byte[] buffer = new byte[256];
        int len;
        while (in.available() > 0) {
            len = in.read(buffer, 0, 256);
            System.out.write(buffer, 0, len);
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
