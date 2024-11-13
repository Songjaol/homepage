package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import game.ImagePanel;
import java.util.List;

public class GameClient {
	private JFrame frame;
	private JPanel boardPanel;
	private JLabel playerInfo;
	private Map<Integer, JLabel> tiles;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private int playerId;
	private JLayeredPane layeredPane;
	private JPanel dicePanel;
	private String playerName;
	private JTextArea ownedTilesArea; // 소유 타일을 표시할 텍스트 영역
	private List<Integer> ownedTiles = new ArrayList<>();
	private int playerPosition = 0; // 플레이어 위치
	private int money = 1500;
	private boolean isMyTurn = false;
	private Map<Integer, Integer> playerPositions = new HashMap<>(); // 플레이어 ID별 위치

	public GameClient(String serverAddress) {
		SwingUtilities.invokeLater(() -> { // UI 초기화
			initializeUI(serverAddress);
		});
	}

	private void initializeUI(String serverAddress) {
		playerName = JOptionPane.showInputDialog("Enter your name:");

		try {
			socket = new Socket(serverAddress, 12345);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new Thread(new Receiver()).start();

			sendMessage("NAME:" + playerName);

			frame = new JFrame("부루마블 게임");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());

			// Player 정보 라벨을 먼저 초기화하여 프레임에 추가
			playerInfo = new JLabel("플레이어 정보: 위치 - " + playerPosition + ", 자금 - " + money);
			frame.add(playerInfo, BorderLayout.NORTH);

			// 소유 타일 목록을 표시할 텍스트 영역 설정
			ownedTilesArea = new JTextArea();
			ownedTilesArea.setEditable(false); // 사용자가 수정할 수 없도록 설정
			ownedTilesArea.setBorder(BorderFactory.createTitledBorder("소유한 타일 목록"));
			frame.add(ownedTilesArea, BorderLayout.EAST); // 프레임의 오른쪽에 추가

			layeredPane = new JLayeredPane();
			layeredPane.setPreferredSize(new Dimension(800, 800));
			frame.add(layeredPane, BorderLayout.CENTER);

			// 보드 패널을 설정하고 5x5 그리드로 타일 배치
			boardPanel = new JPanel(new GridLayout(5, 5));
			boardPanel.setBounds(0, 0, 800, 800);
			initializeBoard();
			layeredPane.add(boardPanel, Integer.valueOf(0));

			dicePanel = new JPanel();
			dicePanel.setOpaque(false);
			dicePanel.setBounds(350, 350, 100, 100);
			layeredPane.add(dicePanel, Integer.valueOf(1));

			JButton rollButton = new JButton("Roll Dice");
			rollButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (isMyTurn) {
						int roll = (int) (Math.random() * 6) + 1;
						sendMessage("ROLL:" + roll);
						showDiceResult(roll); // 주사위 결과에 맞는 이미지 표시
					} else {
						JOptionPane.showMessageDialog(frame, "아직 당신의 차례가 아닙니다!");
					}
				}
			});
			frame.add(rollButton, BorderLayout.SOUTH);
			frame.setSize(820, 880);
			frame.setVisible(true);
			frame.revalidate();
			frame.repaint();
			frame.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					frame.revalidate();
					frame.repaint();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showDiceResult(int roll) {
		dicePanel.removeAll(); // 기존 주사위 이미지 제거

		// 주사위 이미지 파일 경로 설정
		String imagePath = "rollimage/dice" + roll + ".png";
		System.out.println("이미지 경로: " + imagePath); // 디버그: 이미지 경로 출력

		// 이미지가 제대로 로드되지 않았는지 확인하기 위해 파일 존재 여부 확인

		// 이미지 로드
		ImageIcon diceIcon = new ImageIcon(imagePath);
		if (diceIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
			System.out.println("이미지를 로드할 수 없습니다: " + imagePath);
			return;
		}

		// 주사위 이미지를 dicePanel 크기에 맞게 조정
		JLabel diceLabel = new JLabel();
		Image resizedImage = diceIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
		diceLabel.setIcon(new ImageIcon(resizedImage));

		dicePanel.add(diceLabel);
		dicePanel.revalidate();
		dicePanel.repaint();
	}

	private void initializeBoard() {
		tiles = new HashMap<>();
		boardPanel.removeAll();
		boardPanel.setLayout(new GridLayout(5, 5)); // BorderLayout으로 설정

		// 시계 방향으로 타일 이름 지정
		String[] tileNames = { "보드칸 0", "보드칸 1", "보드칸 2", "보드칸 3", "보드칸 4", "보드칸 15", "", "", "", "보드칸 5", "보드칸 14", "",
				"", "", "보드칸 6", "보드칸 13", "", "", "", "보드칸 7", "보드칸 12", "보드칸 11", "보드칸 10", "보드칸 9", "보드칸 8" };

		for (int i = 0; i < 25; i++) {
			JLabel tileLabel;
			if (!tileNames[i].isEmpty()) { // 타일이 있는 경우에만 생성
				tileLabel = new JLabel(tileNames[i], SwingConstants.CENTER);

				// 이미지 파일 경로 설정
				String imagePath = "tileimage/" + tileNames[i] + ".png";
				ImageIcon icon = new ImageIcon(imagePath);

				// 타일 크기에 맞춰 이미지 크기 조정
				Image resizedImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
				tileLabel.setIcon(new ImageIcon(resizedImage));

				tileLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				tileLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
				tileLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

				tiles.put(Integer.parseInt(tileNames[i].replace("보드칸 ", "")), tileLabel); // 타일 인덱스로 tiles 맵에 저
				// 위치에 따라 타일 배치

				boardPanel.add(tileLabel); // 보드에 타일 추가
			} else {
				boardPanel.add(new JLabel());
			}
		}
		updatePlayerPieces(); // 초기 말 위치 설정
		boardPanel.revalidate();
		boardPanel.repaint();
	}

	// 플레이어의 위치를 보드에 업데이트하는 메서드
	private void updatePlayerPieces() {
		// 모든 타일의 텍스트 초기화 (플레이어 표시는 지우고 기본 텍스트로 초기화)
		for (int i = 0; i < tiles.size(); i++) {
			JLabel tileLabel = tiles.get(i);
			if (tileLabel != null) {
				tileLabel.setText("보드칸 " + i); // 기본 텍스트로 초기화
			}
		}

		// 각 플레이어의 위치에 따라 표시 업데이트
		for (Map.Entry<Integer, Integer> entry : playerPositions.entrySet()) {
			int playerId = entry.getKey();
			int position = entry.getValue();
			JLabel currentTile = tiles.get(position);
			if (currentTile != null) {
				String existingText = currentTile.getText();
				currentTile.setText(existingText + " [P" + playerId + "]"); // 플레이어 ID 표시
			}
		}

		// 플레이어 정보 업데이트
		updatePlayerInfo();
	}

	public void sendMessage(String message) {
		out.println(message);
	}

	private class Receiver implements Runnable {
		@Override
		public void run() {
			try {
				String message;
				while ((message = in.readLine()) != null) {
					String finalMessage = message;
					SwingUtilities.invokeLater(() -> processMessage(finalMessage));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void processMessage(String message) {
			if (message.startsWith("CONNECTED")) {
				playerId = Integer.parseInt(message.split(":")[1]);
			} else if (message.startsWith("MOVE")) {
				String[] parts = message.split(":");
				int movedPlayerId = Integer.parseInt(parts[1]);
				int newPosition = Integer.parseInt(parts[2]);
				if (movedPlayerId == playerId) { // 자신의 ID와 일치하는 경우
		            playerPosition = newPosition; // 위치 업데이트
		            updatePlayerInfo(); // 업데이트된 위치 반영
		        }
				playerPositions.put(movedPlayerId, newPosition);
				updatePlayerPieces();
				updatePlayerInfo();
			} else if (message.startsWith("AVAILABLE_FOR_PURCHASE")) {
				int tileIndex = Integer.parseInt(message.split(":")[1]);
				showPurchaseOption(tileIndex); // 구매 창 표시
			} else if (message.startsWith("OWNED")) {
				String[] parts = message.split(":");
				int tileIndex = Integer.parseInt(parts[1]);
				String owner = parts[2];

				// 플레이어가 소유한 타일 목록에 추가
				if (owner.equals(String.valueOf(playerId))) { // 본인 소유 여부 확인
					if (!ownedTiles.contains(tileIndex)) { // 이미 추가되지 않았다면 추가
						ownedTiles.add(tileIndex);
					}
				}
				updateTileOwnership(tileIndex, owner);
				updatePlayerInfo(); // 플레이어 정보 업데이트
			} else if (message.startsWith("TURN")) {
				int turnPlayerId = Integer.parseInt(message.split(":")[1]);
				isMyTurn = (turnPlayerId == playerId);
				if (isMyTurn) {
					JOptionPane.showMessageDialog(frame, "당신의 차례입니다!");
				}
			} else if (message.startsWith("PAY_RENT")) {
				String[] parts = message.split(":");
				int rentAmount = Integer.parseInt(parts[2]);
				money -= rentAmount;
				updatePlayerInfo();
				JOptionPane.showMessageDialog(frame, "임대료 " + rentAmount + "원을 지불했습니다.");
			} else if (message.startsWith("GAME_OVER")) {
				JOptionPane.showMessageDialog(frame, "게임이 종료되었습니다.");
				frame.dispose();
				closeConnection();
			} else if (message.startsWith("NOT_PURCHASABLE")) {
				int tileIndex = Integer.parseInt(message.split(":")[1]);
				JOptionPane.showMessageDialog(frame, "보드칸 " + tileIndex + "은(는) 구매할 수 없는 타일입니다.");
			} else if (message.startsWith("BALANCE_UPDATE")) {
				int newBalance = Integer.parseInt(message.split(":")[1]);
				money = newBalance; // 자금 업데이트
				updatePlayerInfo(); // 자금 상태가 UI에 반영되도록 업데이트
			} else if (message.startsWith("INSUFFICIENT_FUNDS")) {
				int tileIndex = Integer.parseInt(message.split(":")[1]);
				JOptionPane.showMessageDialog(frame, "자금이 부족하여 보드칸 " + tileIndex + "을(를) 구매할 수 없습니다.", "구매 실패",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void showPurchaseOption(int tileIndex) {
		int cost = 100; // 예제: 타일 구매 비용 설정

		int option = JOptionPane.showConfirmDialog(frame, "이 타일을 구매하시겠습니까? 비용: " + cost, "타일 구매",
				JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			sendMessage("PURCHASE:" + tileIndex); // 서버에 구매 요청 메시지 전송
		}
	}

	private void updateTileOwnership(int tileIndex, String owner) {
		JLabel tileLabel = tiles.get(tileIndex);
		if (tileLabel != null) {
			tileLabel.setText("보드칸" + tileIndex + " (" + owner + ")");
		}
	}

	private void updatePlayerInfo() {
		playerInfo.setText("플레이어 정보: 위치: " + playerPosition + ", 자금 : " + money);

		StringBuilder tilesText = new StringBuilder();
		for (int tile : ownedTiles) {
			tilesText.append("보드칸 ").append(tile).append("\n");
		}
		ownedTilesArea.setText(tilesText.toString()); // 소유한 타일 목록을 UI에 반영
		frame.repaint();
	}

	private void closeConnection() {
		try {
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GameClient("localhost");
	}
}
