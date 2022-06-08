package model;

public class ServiceOrder {
    private int id;
    private Service service;
    private Record record;
    private String date;

    public ServiceOrder(int id, Service service, Record record, String date) {
        this.id = id;
        this.service = service;
        this.record = record;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public Service getService() {
        return service;
    }

    public Record getRecord() {
        return record;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "ServiceOrder{" +
                "id=" + id +
                ", service=" + service +
                ", record=" + record +
                ", date='" + date + '\'' +
                '}';
    }
}
