package utb.fai;

public class App {

    public static void main(String[] args) {
        if (args.length < 6) {
            System.err.println("Pouziti: java -jar app.jar <host> <port> <od> <komu> <predmet> <text>");
            System.exit(1);
        }

        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Neplatne cislo portu: " + args[1]);
            System.exit(1);
            return;
        }
        String from = args[2];
        String to = args[3];
        String subject = args[4];
        String text = args[5];

        try {
            EmailSender sender = new EmailSender(host, port);
            try {
                sender.send(from, to, subject, text);
                System.out.println("Email odeslan.");
            } finally {
                sender.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
