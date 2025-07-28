import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPChatClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private String username;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Thread receiveThread;
    private Thread pingThread;

    // Thread class for receiving messages
    private class ReceiveThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];

            while (running.get()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());

                    // Don't display PONG responses
                    if (!message.equals("PONG")) {
                        System.out.println(message);
                    }

                } catch (SocketTimeoutException e) {
                    // Timeout is normal, continue
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("Error receiving message: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Thread class for sending periodic pings
    private class PingThread extends Thread {
        public void run() {
            while (running.get()) {
                try {
                    Thread.sleep(15000); // Send ping every 15 seconds
                    if (running.get()) {
                        sendMessage("PING");
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    System.err.println("Error sending ping: " + e.getMessage());
                }
            }
        }
    }

    public UDPChatClient(String serverHost, int serverPort, String username)
            throws SocketException, UnknownHostException {
        // Create socket - by default binds to any available port on all interfaces
        // For security in production, you might want to bind to localhost only:
        // this.socket = new DatagramSocket(0, InetAddress.getByName("127.0.0.1"));
        this.socket = new DatagramSocket();

        // Resolve server address
        // For learning: use "localhost" or "127.0.0.1"
        // For network chat: use the server's IP address
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.username = username;

        // Set timeout for receive operations
        socket.setSoTimeout(1000); // 1 second timeout

        // Inform user about connection type
        if (this.serverAddress.isLoopbackAddress()) {
            System.out.println("Connecting to local server (localhost) - perfect for learning!");
        } else {
            System.out.println("Connecting to remote server at " + serverHost);
            System.out.println("Make sure the server allows external connections and firewall permits UDP port " + serverPort);
        }
    }

    public void connect() throws IOException {
        // Send join message
        String joinMessage = "JOIN:" + username;
        sendMessage(joinMessage);

        // Start receiving messages
        running.set(true);
        startReceiveThread();
        startPingThread();

        System.out.println("Connected to chat server as: " + username);
        System.out.println("Server: " + serverAddress.getHostAddress() + ":" + serverPort);
        System.out.println("\nCommands: /list (show users), /quit (exit), or just type to chat");
    }

    private void startReceiveThread() {
        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    private void startPingThread() {
        pingThread = new PingThread();
        pingThread.setDaemon(true);
        pingThread.start();
    }

    private void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                serverAddress,
                serverPort
        );
        socket.send(packet);
    }

    public void sendChatMessage(String message) throws IOException {
        if (message.trim().isEmpty()) {
            return;
        }

        if (message.equalsIgnoreCase("/list")) {
            sendMessage("LIST");
        } else if (message.equalsIgnoreCase("/quit")) {
            disconnect();
        } else {
            sendMessage("MSG:" + message);
        }
    }

    public void disconnect() {
        if (running.get()) {
            running.set(false);

            try {
                sendMessage("LEAVE");
            } catch (IOException e) {
                // Ignore errors when leaving
            }

            // Wait for threads to finish
            try {
                if (receiveThread != null) {
                    receiveThread.join(2000);
                }
                if (pingThread != null) {
                    pingThread.interrupt();
                    pingThread.join(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public static void main(String[] args) {
        // Default to localhost for safe learning environment
        String serverHost = "localhost";
        int serverPort = 5001;

        Scanner scanner = new Scanner(System.in);

        // Get server details from command line or user input
        if (args.length >= 1) {
            serverHost = args[0];
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 5001.");
            }
        }

        System.out.println("\n=== UDP Chat Client ===");
        System.out.println("Default server: " + serverHost + ":" + serverPort);
        System.out.println("\nNOTE: For learning, connect to 'localhost' or '127.0.0.1'");
        System.out.println("      For network chat, use the server's IP address");
        System.out.println("      Example: java UDPChatClient 192.168.1.100 5001\n");

        // Get username
        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.err.println("Username cannot be empty!");
            scanner.close();
            return;
        }

        try {
            UDPChatClient client = new UDPChatClient(serverHost, serverPort, username);

            // Connect to server
            client.connect();

            // Main message loop
            while (true) {
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("/quit")) {
                    break;
                }

                try {
                    client.sendChatMessage(message);
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                    if (!serverHost.equals("localhost") && !serverHost.equals("127.0.0.1")) {
                        System.err.println("TIP: Check if the server is reachable and firewall allows UDP traffic.");
                    }
                }
            }

            client.disconnect();

        } catch (SocketException e) {
            System.err.println("Could not create client: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
            System.err.println("TIP: Use 'localhost' for local testing or verify the server address.");
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.err.println("TIP: Make sure the server is running on " + serverHost + ":" + serverPort);
        } finally {
            scanner.close();
        }

        System.out.println("Chat client closed.");
    }
}
