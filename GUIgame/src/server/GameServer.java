package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import game.MonopolyBoard;
import game.Tile;

public class GameServer {
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();
    static int currentPlayerIndex = 0;
    private static final MonopolyBoard board = new MonopolyBoard();

    public static void main(String[] args) {
        System.out.println("부루마블 서버 시작...");
        initializeBoard();

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            while (clients.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size(), clients, board);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                System.out.println("플레이어가 연결되었습니다. 총 플레이어 수: " + clients.size());

                if (clients.size() == 2) {
                    clients.get(0).sendMessage("TURN:0");
                }
            }
            System.out.println("2명의 플레이어가 모두 연결되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeBoard() {
        board.addTile(new Tile("보드칸 0", 0,false));      // 출발 타일
        board.addTile(new Tile("보드칸 1", 100,true));
        board.addTile(new Tile("보드칸 2", 100,true));
        board.addTile(new Tile("보드칸 3", 100,true));
        board.addTile(new Tile("보드칸 4", 100,true));
        board.addTile(new Tile("보드칸 5", 100,true));
        board.addTile(new Tile("보드칸 6", 100,true));
        board.addTile(new Tile("보드칸 7", 100,true));
        board.addTile(new Tile("보드칸 8", 100,true));
        board.addTile(new Tile("보드칸 9", 100,true));
        board.addTile(new Tile("보드칸 10", 100,true));
        board.addTile(new Tile("보드칸 11", 100,true));
        board.addTile(new Tile("보드칸 12", 100,true));
        board.addTile(new Tile("보드칸 13", 100,true));
        board.addTile(new Tile("보드칸 14", 100,true));
        board.addTile(new Tile("보드칸 15", 100,true));
    }
    
    public static synchronized void nextTurn() {
        if (isGameOver()) {
            for (ClientHandler client : clients) {
                client.sendMessage("GAME_OVER");
                client.closeConnection();
            }
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        clients.get(currentPlayerIndex).sendMessage("TURN:" + currentPlayerIndex);
    }

    private static boolean isGameOver() {
        for (ClientHandler client : clients) {
            if (client.getPlayer().getMoney() <= 0) {
                return true;
            }
        }
        return false;
    }
}
