package utb.fai;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class EmailSender {

    private final Socket socket;
    private final BufferedReader reader;
    private final Writer writer;

    /*
     * Constructor opens Socket to host/port. If the Socket throws an exception
     * during opening,
     * the exception is not handled in the constructor.
     */
    public EmailSender(String host, int port) throws UnknownHostException, IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(10000);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        // uvitaci zprava serveru (220)
        expect(220);
        sendCommand("EHLO " + InetAddress.getLocalHost().getHostName());
        expect(250);
    }

    /*
     * Sends email from an email address to an email address with some subject and
     * text.
     * If the Socket throws an exception during sending, the exception is not
     * handled by this method.
     */
    public void send(String from, String to, String subject, String text) throws IOException {
        sendCommand("MAIL FROM:<" + from + ">");
        expect(250);
        sendCommand("RCPT TO:<" + to + ">");
        expect(250, 251);
        sendCommand("DATA");
        expect(354);

        StringBuilder message = new StringBuilder();
        message.append("From: ").append(from).append("\r\n");
        message.append("To: ").append(to).append("\r\n");
        message.append("Subject: ").append(subject).append("\r\n");
        message.append("Content-Type: text/plain; charset=UTF-8\r\n");
        message.append("\r\n");

        // radky zacinajici teckou se musi zdvojit, jinak by server bral "." jako konec zpravy
        String[] lines = text.split("\r?\n", -1);
        for (String line : lines) {
            if (line.startsWith(".")) {
                message.append('.');
            }
            message.append(line).append("\r\n");
        }
        message.append(".");

        sendCommand(message.toString());
        expect(250);
    }

    /*
     * Sends QUIT and closes the socket
     */
    public void close() {
        try {
            sendCommand("QUIT");
            expect(221);
        } catch (IOException e) {
            // spojeni se zavira tak jako tak
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // nelze nic delat
            }
        }
    }

    private void sendCommand(String command) throws IOException {
        writer.write(command + "\r\n");
        writer.flush();
    }

    /*
     * Precte odpoved serveru (i viceradkovou, napr. "250-...") a overi navratovy kod.
     */
    private String expect(int... expectedCodes) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                throw new IOException("Server ukoncil spojeni");
            }
            response.append(line).append('\n');
        } while (line.length() > 3 && line.charAt(3) == '-');

        int code;
        try {
            code = Integer.parseInt(line.substring(0, 3));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IOException("Neplatna odpoved serveru: " + line);
        }
        for (int expected : expectedCodes) {
            if (code == expected) {
                return response.toString();
            }
        }
        throw new IOException("Neocekavana odpoved serveru: " + response.toString().trim());
    }
}
