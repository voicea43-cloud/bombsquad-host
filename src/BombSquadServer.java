import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
public class BombSquadServer {
    private static final int PORT = 29248;
    private static final String SERVER_IP = "157.180.102.182";
    private static final String ADMIN_ID = "pb-IF5dUBAKDg==";
    private static final int MAX_PLAYERS = 16;
    private static final String SERVER_NAME = "BOMBSQUAD ARABIC SERVER";
    public static Map<String, ClientHandler> players = new ConcurrentHashMap<>();
    public static Set<String> bannedPlayers = ConcurrentHashMap.newKeySet();
    public static Set<String> mutedPlayers = ConcurrentHashMap.newKeySet();
    public static long startTime = System.currentTimeMillis();
    public static String currentMap = "Football Stadium";
    public static String gameMode = "free-for-all";
    public static void main(String[] args) {
        System.out.println("=== " + SERVER_NAME + " ===");
        System.out.println("Port: " + PORT);
        System.out.println("Admin: " + ADMIN_ID);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setSoTimeout(1000);
            System.out.println("Server started!");
            new Thread(BombSquadServer::consoleHandler).start();
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if (players.size() < MAX_PLAYERS) {
                        new Thread(new ClientHandler(clientSocket)).start();
                    } else {
                        clientSocket.getOutputStream().write("Server full!\n".getBytes());
                        clientSocket.close();
                    }
                } catch (SocketTimeoutException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    private static void consoleHandler() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("\nAdmin> ");
                String cmd = scanner.nextLine().trim().toLowerCase();
                if (cmd.equals("quit") || cmd.equals("exit")) { broadcast("Server shutting down!"); System.exit(0); }
                else if (cmd.equals("status")) { System.out.println("Players: " + players.size() + "/" + MAX_PLAYERS + " | Map: " + currentMap); }
                else if (cmd.equals("players")) { for (ClientHandler p : players.values()) System.out.println("  " + (p.isAdmin()?"[A] ":"") + p.getPlayerName()); }
                else if (cmd.startsWith("kick ")) { String n = cmd.substring(5); for (ClientHandler p : players.values()) if (p.getPlayerName().equalsIgnoreCase(n)) { p.kick("Kicked by admin"); return; } }
                else if (cmd.startsWith("ban ")) { String n = cmd.substring(4); for (ClientHandler p : players.values()) if (p.getPlayerName().equalsIgnoreCase(n)) { bannedPlayers.add(p.getPlayerId()); p.kick("Banned"); return; } }
                else if (cmd.startsWith("say ")) { broadcast("ADMIN: " + cmd.substring(4)); }
                else if (cmd.equals("help")) { System.out.println("status | players | kick [name] | ban [name] | say [msg] | quit"); }
            } catch (Exception e) {}
        }
    }
    public static void broadcast(String message) { broadcast(message, null); }
    public static void broadcast(String message, String excludeId) { for (ClientHandler p : players.values()) if (excludeId == null || !p.getPlayerId().equals(excludeId)) p.sendMessage(message); }
    public static boolean isBanned(String id) { return bannedPlayers.contains(id); }
    public static boolean isMuted(String id) { return mutedPlayers.contains(id); }
    public static boolean isAdmin(String id) { return id.equals(ADMIN_ID); }
    public static void addPlayer(String id, ClientHandler h) { players.put(id, h); }
    public static void removePlayer(String id) { players.remove(id); }
}
