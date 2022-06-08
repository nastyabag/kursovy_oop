package db;

import model.Record;
import model.Room;
import model.Service;
import model.ServiceOrder;
import org.sqlite.JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbHandler {
    private static final String CONNECTION_STR = "jdbc:sqlite:C://sqlite/project.db";
    private static DbHandler instance = null;

    private final Connection connection;

    public DbHandler() throws SQLException {
        DriverManager.registerDriver(new JDBC());
        this.connection = DriverManager.getConnection(CONNECTION_STR);
        createTables();
    }

    public static synchronized DbHandler getInstance() throws SQLException {
        if (instance == null) {
            instance = new DbHandler();
        }
        return instance;
    }

    private void createTables() {
        try (var statement = this.connection.createStatement()) {
            statement.executeUpdate("PRAGMA foreign_keys=ON");
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS rooms (" +
                "number INTEGER PRIMARY KEY NOT NULL," +
                "type TEXT NOT NULL," +
                "capacity INTEGER NOT NULL," +
                "price REAL NOT NULL)"
            );statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS services (" +
                "id INTEGER PRIMARY KEY NOT NULL," +
                "name TEXT UNIQUE NOT NULL," +
                "price REAL NOT NULL)"
            );statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS records (" +
                "id INTEGER PRIMARY KEY NOT NULL," +
                "roomId INTEGER NOT NULL," +
                "person TEXT NOT NULL," +
                "arrivalDate CHAR(10) NOT NULL," +
                "departureDate CHAR(10) NOT NULL," +
                "FOREIGN KEY (roomId) REFERENCES rooms(number) ON DELETE CASCADE)"
            );
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS serviceOrders (" +
                "id INTEGER PRIMARY KEY NOT NULL," +
                "serviceId INTEGER NOT NULL," +
                "recordId INTEGER NOT NULL," +
                "date CHAR(10) NOT NULL," +
                "FOREIGN KEY (serviceId) REFERENCES services(id) ON DELETE CASCADE," +
                "FOREIGN KEY (recordId) REFERENCES records(id) ON DELETE CASCADE)"
            );
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public List<Room> getRoomsList() {
        try (var statement = this.connection.createStatement()) {
            var rooms = new ArrayList<Room>();
            var result = statement.executeQuery("SELECT * FROM rooms");
            while (result.next()) {
                rooms.add(new Room(
                        result.getInt("number"),
                        result.getString("type"),
                        result.getInt("capacity"),
                        result.getFloat("price")
                    )
                );
            }
            return rooms;
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Room getRoom(int number) {
        try (var statement = this.connection.prepareStatement(
                "SELECT * FROM rooms WHERE number = ?"
        )) {
            statement.setObject(1, number);
            var result = statement.executeQuery();
            return new Room(
                    result.getInt("number"),
                    result.getString("type"),
                    result.getInt("capacity"),
                    result.getFloat("price")
            );
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void addRoom(Room room) {
        try (var statement = this.connection.prepareStatement(
                "INSERT INTO rooms(number, type, capacity, price) " +
                        "VALUES(?, ?, ?, ?)"
        )) {
            statement.setObject(1, room.getNumber());
            statement.setObject(2, room.getType());
            statement.setObject(3, room.getCapacity());
            statement.setObject(4, room.getPrice());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRoom(int room) {
        try (var statement = this.connection.prepareStatement(
                "DELETE FROM rooms WHERE number = ?"
        )) {
            statement.setObject(1, room);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void editRoomType(int number, String type) {
        try (var statement = this.connection.prepareStatement(
                "UPDATE rooms SET type = ? WHERE number = ?"
        )) {
            statement.setObject(1, type);
            statement.setObject(2, number);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void editRoomCapacity(int number, int capacity) {
        try (var statement = this.connection.prepareStatement(
                "UPDATE rooms SET capacity = ? WHERE number = ?"
        )) {
            statement.setObject(1, capacity);
            statement.setObject(2, number);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void editRoomPrice(int number, float price) {
        try (var statement = this.connection.prepareStatement(
                "UPDATE rooms SET price = ? WHERE number = ?"
        )) {
            statement.setObject(1, price);
            statement.setObject(2, number);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public List<Service> getServicesList() {
       try (var statement = this.connection.createStatement()) {
           var services = new ArrayList<Service>();
           var result = statement.executeQuery("SELECT * FROM services");
           while (result.next()) {
               services.add(new Service(
                       result.getInt("id"),
                       result.getString("name"),
                       result.getFloat("price")
                   )
               );
           }
           return services;
       }
       catch (SQLException exception) {
           exception.printStackTrace();
           return Collections.emptyList();
       }
   }

    public Service getServiceById(int id) {
        try (var statement = this.connection.prepareStatement(
                "SELECT * FROM services WHERE id = ?"
        )) {
            statement.setObject(1, id);
            var result = statement.executeQuery();
            return new Service(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getFloat("price")
            );
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public Service getServiceByName(String name) {
        try (var statement = this.connection.prepareStatement(
                "SELECT * FROM services WHERE name = ?"
        )) {
            statement.setObject(1, name);
            var result = statement.executeQuery();
            return new Service(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getFloat("price")
            );
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void addService(Service service) {
        try (var statement = this.connection.prepareStatement(
                "INSERT INTO services(id, name, price) " +
                        "VALUES(?, ?, ?)"
        )) {
            statement.setObject(1, service.getId());
            statement.setObject(2, service.getName());
            statement.setObject(3, service.getPrice());
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteService(int id) {
        try (var statement = this.connection.prepareStatement(
                "DELETE FROM services WHERE id = ?"
        )) {
            statement.setObject(1, id);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void editServiceName(int id, String name) {
        try (var statement = this.connection.prepareStatement(
                "UPDATE services SET name = ? WHERE id = ?"
        )) {
            statement.setObject(1, name);
            statement.setObject(2, id);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void editServicePrice(int id, float price) {
        try (var statement = this.connection.prepareStatement(
                "UPDATE services SET price = ? WHERE id = ?"
        )) {
            statement.setObject(1, price);
            statement.setObject(2, id);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public List<Record> getRecordsList() {
        try (var statement = this.connection.createStatement()) {
            var records = new ArrayList<Record>();
            var result = statement.executeQuery(
                    "SELECT * FROM records"
            );
            while (result.next()) {
                records.add(new Record(
                                result.getInt("id"),
                                getRoom(result.getInt("roomId")),
                                result.getString("person"),
                                result.getString("arrivalDate"),
                                result.getString("departureDate")
                        )
                );
            }
            return records;
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Record getRecord(int id) {
        try (var statement = this.connection.prepareStatement(
                "SELECT * FROM records WHERE id = ?"
        )) {
            statement.setObject(1, id);
            var result = statement.executeQuery();
            return new Record(
                    result.getInt("id"),
                    getRoom(result.getInt("roomId")),
                    result.getString("person"),
                    result.getString("arrivalDate"),
                    result.getString("departureDate")
            );
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void addRecord(Record record) {
        try (var statement = this.connection.prepareStatement(
                "INSERT INTO records(id, roomId, person, arrivalDate, departureDate) " +
                        "VALUES(?, ?, ?, ?, ?)"
        )) {
            statement.setObject(1, record.getId());
            statement.setObject(2, record.getRoom().getNumber());
            statement.setObject(3, record.getPerson());
            statement.setObject(4, record.getArrivalDate());
            statement.setObject(5, record.getDepartureDate());
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecord(int id) {
        try (var statement = this.connection.prepareStatement(
                "DELETE FROM records WHERE id = ?"
        )) {
            statement.setObject(1, id);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void editRecordName(int id, String person) {
        try (var statement = this.connection.prepareStatement(
                "UPDATE records SET person = ? WHERE id = ?"
        )) {
            statement.setObject(1, person);
            statement.setObject(2, id);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void editDepartureDate(int id, String date) {
        try (var statement = this.connection.prepareStatement(
                "UPDATE records SET departureDate = ? WHERE id = ?"
        )) {
            statement.setObject(1, date);
            statement.setObject(2, id);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public List<ServiceOrder> getServiceOrdersList() {
        try (var statement = this.connection.createStatement()) {
            var serviceOrders = new ArrayList<ServiceOrder>();
            var result = statement.executeQuery(
                    "SELECT * FROM serviceOrders"
            );
            while (result.next()) {
                serviceOrders.add(new ServiceOrder(
                    result.getInt("id"),
                    getServiceById(result.getInt("serviceId")),
                    getRecord(result.getInt("recordId")),
                    result.getString("date")
                ));
            }
            return serviceOrders;
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    public ServiceOrder getServiceOrder(int id) {
        try (var statement = this.connection.prepareStatement(
                "SELECT * FROM serviceOrders WHERE id = ?"
        )) {
            statement.setObject(1, id);
            var result = statement.executeQuery();
            return new ServiceOrder(
                result.getInt("id"),
                getServiceById(result.getInt("serviceId")),
                getRecord(result.getInt("recordId")),
                result.getString("date")
            );
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public List<ServiceOrder> getServiceOrdersListByRecord(int recordId) {
        try (var statement = this.connection.prepareStatement(
                "SELECT * FROM serviceOrders WHERE recordId = ?"
        )) {
            statement.setObject(1, recordId);
            var serviceOrders = new ArrayList<ServiceOrder>();
            var result = statement.executeQuery();
            while (result.next()) {
                serviceOrders.add(new ServiceOrder(
                    result.getInt("id"),
                    getServiceById(result.getInt("serviceId")),
                    getRecord(result.getInt("recordId")),
                    result.getString("date")
                ));
            }
            return serviceOrders;
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void addServiceOrder(ServiceOrder serviceOrder) {
        try (var statement = this.connection.prepareStatement(
                "INSERT INTO serviceOrders(id, serviceId, recordId, date) " +
                        "VALUES(?, ?, ?, ?)"
        )) {
            statement.setObject(1, serviceOrder.getId());
            statement.setObject(2, serviceOrder.getService().getId());
            statement.setObject(3, serviceOrder.getRecord().getId());
            statement.setObject(4, serviceOrder.getDate());
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteServiceOrder(int id) {
        try (var statement = this.connection.prepareStatement(
                "DELETE FROM serviceOrders WHERE id = ?"
        )) {
            statement.setObject(1, id);
            statement.execute();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
