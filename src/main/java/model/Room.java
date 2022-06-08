package model;

public class Room {
    private int number;
    private String type;
    private int capacity;
    private float price;

    public Room(int number, String type, int capacity, float price) {
        this.number = number;
        this.type = type;
        this.capacity = capacity;
        this.price = price;
    }

    public int getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    public float getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Room{" +
                "number=" + number +
                ", type='" + type + '\'' +
                ", capacity=" + capacity +
                ", price=" + price +
                '}';
    }
}
