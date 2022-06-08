package model;

public class Record {
    private int id;
    private Room room;
    private String person;
    private String arrivalDate;
    private String departureDate;

    public Record(int id, Room room, String person, String arrivalDate, String departureDate) {
        this.id = id;
        this.room = room;
        this.person = person;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }

    public int getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public String getPerson() {
        return person;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", roomId=" + room.toString() +
                ", person='" + person + '\'' +
                ", arrivalDate='" + arrivalDate + '\'' +
                ", departureDate='" + departureDate + '\'' +
                '}';
    }
}
