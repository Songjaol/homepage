package game;

public class Tile {
	private String name;
	private int baseRent;
	private String owner;
	private boolean purchasable;

	public Tile(String name, int baseRent,boolean purcahsable) {
        this.name = name;
        this.baseRent = baseRent;
        this.owner = null;
        this.purchasable=purcahsable;
    }

	public String getName() {
		return name;
	}

	public int getBaseRent() {
		return baseRent;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isPurchasable() {
		return purchasable;
	}
}
