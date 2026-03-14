import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerId;
    private String playerName;
    private boolean isAdmin;
    private boolean muted;
    public ClientHandler(Socket socket) { this.socket = socket; }
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            playerId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            playerName = "Player_" + (System.currentTimeMillis() % 9999);
            isAdmin = BombSquadServer.isAdmin(playerId);
            if (BombSquadServer.isBanned(playerId)) { sendMessage("You are banned!"); socket.close(); return; }
            BombSquadServer.addPlayer(playerId, this);
            sendMessage("Welcome! Type /help for commands");
            if (isAdmin) sendMessage("You are ADMIN!");
            BombSquadServer.broadcast(playerName + " joined!", playerId);
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("/")) handleCommand(line.substring(1));
                else if (!BombSquadServer.isMuted(playerId)) BombSquadServer.broadcast((isAdmin?"[ADMIN] ":"") + playerName + ": " + line, null);
                else sendMessage("You are muted.");
            }
        } catch (IOException e) {}
        finally { closeConnection(); }
    }
    private void handleCommand(String cmd) {
        String[] p = cmd.split(" ", 2);
        String arg = p.length > 1 ? p[1] : "";
        switch (p[0].toLowerCase()) {
            case "help": sendMessage(isAdmin ? "/kick /ban /mute /unmute /say /players /status" : "/players /status /time /help"); break;
            case "players": StringBuilder sb = new StringBuilder("Players:\n"); for (ClientHandler h : BombSquadServer.players.values()) sb.append("  ").append(h.isAdmin()?"[A] ":"").append(h.getPlayerName()).append("\n"); sendMessage(sb.toString()); break;
            case "status": sendMessage("Players: " + BombSquadServer.players.size() + "/16 | Map: " + BombSquadServer.currentMap); break;
            case "time": sendMessage("Time: " + new SimpleDateFormat("HH:mm:ss").format(new Date())); break;
            case "kick": if (isAdmin && !arg.isEmpty()) { for (ClientHandler h : BombSquadServer.players.values()) if (h.getPlayerName().equalsIgnoreCase(arg)) { h.kick("Kicked"); return; } sendMessage("Not found"); } break;
            case "ban": if (isAdmin && !arg.isEmpty()) { for (ClientHandler h : BombSquadServer.players.values()) if (h.getPlayerName().equalsIgnoreCase(arg)) { BombSquadServer.bannedPlayers.add(h.getPlayerId()); h.kick("Banned"); return; } } break;
            case "mute": if (isAdmin && !arg.isEmpty()) { for (ClientHandler h : BombSquadServer.players.values()) if (h.getPlayerName().equalsIgnoreCase(arg)) { BombSquadServer.mutedPlayers.add(h.getPlayerId()); h.sendMessage("You are muted."); return; } } break;
            case "say": if (isAdmin && !arg.isEmpty()) BombSquadServer.broadcast("ADMIN: " + arg); break;
            default: sendMessage("Unknown command. Type /help");
        }
    }
    public void sendMessage(String msg) { if (out != null) out.println(msg); }
    public void kick(String reason) { sendMessage("Kicked: " + reason); closeConnection(); }
    private void closeConnection() { try { BombSquadServer.removePlayer(playerId); BombSquadServer.broadcast(playerName + " left.", playerId); socket.close(); } catch (IOException e) {} }
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isMuted() { return muted; }
}
