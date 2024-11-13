package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import game.MonopolyBoard;
import game.Tile;
import game.Player;

public class ClientHandler implements Runnable {
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private int playerId;
	private int playerPosition = 0;
	private Player player;
	private ArrayList<ClientHandler> clients;
	private MonopolyBoard board;

	public ClientHandler(Socket socket, int playerId, ArrayList<ClientHandler> clients, MonopolyBoard board)
			throws IOException {
		this.socket = socket;
		this.playerId = playerId;
		this.clients = clients;
		this.board = board;
		this.player = new Player("Player " + playerId, 1500);
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		sendMessage("CONNECTED:" + playerId);
	}

	@Override
	public void run() {
		try {
			String message;
			while ((message = in.readLine()) != null) {
				processMessage(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	private synchronized void processMessage(String message) {
		if (message.startsWith("ROLL:")) {
			handleRoll(Integer.parseInt(message.split(":")[1]));
		} else if (message.startsWith("PURCHASE:")) {
			int tileIndex = Integer.parseInt(message.split(":")[1]);
			handleTilePurchase(tileIndex);
		}
	}

	private synchronized void handleTilePurchase(int tileIndex) {
		Tile tile = board.getTile(tileIndex);
		if (!tile.isPurchasable()) { // 타일이 구매 불가인 경우
			sendMessage("NOT_PURCHASABLE:" + tileIndex); // 클라이언트에 구매 불가 알림 전송
			return;
		}
		// 타일이 소유되지 않은 경우에만 구매 가능
		if (tile.getOwner() == null) {
			int purchaseCost = tile.getBaseRent();
			if (player.getMoney() >= purchaseCost) {
				player.spendMoney(purchaseCost); // 자금 차감
				System.out.println("Player " + playerId + " purchased tile " + tileIndex + " for " + purchaseCost); // 디버그
																													// 메시지

				tile.setOwner(String.valueOf(playerId)); // 소유자 설정
				sendMessage("OWNED:" + tileIndex + ":" + playerId);
				sendMessage("BALANCE_UPDATE:" + player.getMoney()); // 자금 업데이트 전송
				broadcast("OWNED:" + tileIndex + ":" + playerId);
			} else {
				sendMessage("INSUFFICIENT_FUNDS:" + tileIndex);
			}
		} else {
			sendMessage("ALREADY_OWNED:" + tileIndex); // 이미 소유된 타일인 경우 메시지 전송
		}
	}

	private synchronized void handleRoll(int rollValue) {
		if (playerId != GameServer.currentPlayerIndex) {
			sendMessage("WAIT: Not your turn.");
			return;
		}

		int boardSize = board.getAllTiles().size(); // 보드 크기를 16으로 고정
		playerPosition = (playerPosition + rollValue) % boardSize;
		player.setPosition(playerPosition);

		Tile landedTile = board.getTile(playerPosition);

		broadcast("MOVE:" + playerId + ":" + playerPosition);

		if (landedTile.getOwner() == null) {
			sendMessage("AVAILABLE_FOR_PURCHASE:" + playerPosition); // 구매 가능 메시지 전송
		} else if (!landedTile.getOwner().equals(String.valueOf(playerId))) {
			int rent = landedTile.getBaseRent();
			sendMessage("PAY_RENT:" + landedTile.getOwner() + ":" + rent);
			player.spendMoney(rent);
			checkEndGameCondition();
		}

		GameServer.nextTurn();
	}

	private void checkEndGameCondition() {
		if (player.getMoney() <= 0) {
			broadcast("GAME_OVER: Player " + playerId + " lost.");
			GameServer.nextTurn();
		}
	}

	public void sendMessage(String message) {
		out.println(message);
	}

	public void closeConnection() {
		try {
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void broadcast(String message) {
		for (ClientHandler client : clients) {
			client.sendMessage(message);
		}
	}

	public Player getPlayer() {
		return player;
	}
}
