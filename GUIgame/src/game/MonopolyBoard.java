package game;

import java.util.ArrayList;
import java.util.List;

public class MonopolyBoard {
    private List<Tile> tiles;

    public MonopolyBoard() {
        tiles = new ArrayList<>();
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public Tile getTile(int index) {
        return tiles.get(index);
    }

    public List<Tile> getAllTiles() {
        return new ArrayList<>(tiles); // 전체 타일 목록을 반환
    }
}
