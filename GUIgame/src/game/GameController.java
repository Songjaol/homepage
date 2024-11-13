package game;

import java.util.List;

public class GameController {
	private List<Player> players;
    private MonopolyBoard board;
    private Dice dice;
    private int currentPlayerIndex;

    public GameController(List<Player> players, MonopolyBoard board) {
        this.players = players;
        this.board = board;
        this.dice = new Dice();
        this.currentPlayerIndex = 0;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void playTurn() {
    	Player currentPlayer = getCurrentPlayer();
        int roll = dice.roll();
        System.out.println(currentPlayer.getName() + " rolled a " + roll);

        int boardSize = board.getAllTiles().size(); // 보드 크기 가져오기
        currentPlayer.move(roll, boardSize); // 수정된 move 메서드 호출

        Tile landedTile = board.getTile(currentPlayer.getPosition());
        System.out.println(currentPlayer.getName() + " landed on " + landedTile.getName());

        handleTileEvent(currentPlayer, landedTile);
        nextTurn();
    }

    private void handleTileEvent(Player player, Tile tile) {
        if (tile.getOwner() == null) {
            System.out.println(tile.getName() + " is available for purchase.");
        } else if (!tile.getOwner().equals(player)) {
            System.out.println("This tile is owned by " + tile.getOwner() + ".");
        }
    }

    private void nextTurn() {
    	currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}
