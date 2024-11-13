package game;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private int money;
    private int position;
    private List<Tile> ownedProperties;

    public Player(String name, int initialMoney) {
        this.name = name;
        this.money = initialMoney;
        this.position = 0;
        this.ownedProperties = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void move(int steps, int boardSize) {
        position = (position + steps) % boardSize;
    }
    
    public void earnMoney(int amount) {
        money += amount;
    }

    public void spendMoney(int amount) {
        if (amount > money) {
            throw new IllegalArgumentException("Not enough funds");
        }
        money -= amount;
    }
}
