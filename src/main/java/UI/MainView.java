package UI;

import model.Record;
import db.DbHandler;
import model.ServiceOrder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

public class MainView extends JFrame {
    private final Pattern DATE_PATTERN = Pattern.compile(
            "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$");//шаблон создания даты
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//приведение даты в нужный формат

    public DbHandler handler;//база данных

    private final MainView mainView = this;

    private JTable table;

    private JPopupMenu popupMenu;


    private final Object[] tableHeaders = {
        "ID", "Номер", "Тип номера", "Вместимость", "Цена в сутки", "ФИО клиента",
        "Прибытие", "Отбытие", "Стоимость услуг", "Общая стоимость"
    };

    //слушатель для обработки добавления услуги
    private final Action addService = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var id = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);
            var record = handler.getRecord(id);

            var services = handler.getServicesList();
            Object[] servicesString = new Object[services.size()];
            for (var i = 0; i < services.size(); i++) {
                var service = services.get(i);
                servicesString[i] = String.format("%s. %s - %s руб./сутки",
                        service.getId(), service.getName(), service.getPrice()
                );
            }

            if (services.size() == 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Список услуг пуст",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            var service = (String) JOptionPane.showInputDialog(
                    null,
                    "Услуга:",
                    "Услуга",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    servicesString,
                    servicesString[0]
            );
            if (service == null) {
                return;
            }

            var date = "";

            while (Objects.equals(date, "")) {
                date = JOptionPane.showInputDialog(
                        null,
                        "Дата оказания услуги (пример 2022-01-31):",
                        "Услуга",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (date == null) {
                    return;
                }
                if (!DATE_PATTERN.matcher(date).matches() || !checkIfDateInBookPeriod(id, date)) {
                    date = "";
                }
            }

            var serviceObject = handler.getServiceById(Integer.parseInt(service.split(" ")[0].replaceAll("\\.", "")));

            var servicesOrders = handler.getServiceOrdersList();
            var servicesOrder = new ServiceOrder(
                    servicesOrders.size() != 0 ? servicesOrders.get(servicesOrders.size() - 1).getId() + 1 : 0,
                    serviceObject,
                    record,
                    date
            );

            handler.addServiceOrder(servicesOrder);
            showRecordsList();
        }
    };

    //слушатель для поиска постояльца
    private final Action searchByName = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var person = JOptionPane.showInputDialog(
                    null,
                    "ФИО клиента:",
                    "Поиск",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (person == null) {
                return;
            }

            var records = handler.getRecordsList();
            var result = new ArrayList<Record>();

            for (var record : records) {
                if (record.getPerson().contains(person)) {
                    result.add(record);
                }
            }

            var model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            for (var record : result) {
                model.addRow (new Object[] {
                        record.getId(),
                        record.getRoom().getNumber(),
                        record.getRoom().getType(),
                        record.getRoom().getCapacity(),
                        record.getRoom().getPrice(),
                        record.getPerson(),
                        record.getArrivalDate(),
                        record.getDepartureDate(),
                        calculateServices(record),
                        calculateSum(record)
                });
            }
            setTitle("Результат поиска: ФИО = " + person);
        }
    };

    private final Action resetSearch = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setTitle("Гостиница");
            showRecordsList();
        }
    };

    //слушатель для создания записи
    private final Action createRecord = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //Комната
            var rooms = handler.getRoomsList();
            Object[] roomsString = new Object[rooms.size()];
            for (var i = 0; i < rooms.size(); i++) {
                var room = rooms.get(i);
                roomsString[i] = String.format("Номер %s - %s руб./сутки", room.getNumber(), room.getPrice());
            }

            if (rooms.size() == 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Список номеров пуст",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            var room = (String) JOptionPane.showInputDialog(
                    null,
                    "Номер:",
                    "Новая запись",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    roomsString,
                    roomsString[0]
            );
            if (room == null) {
                return;
            }
            var roomObject = handler.getRoom(Integer.parseInt(room.split(" ")[1]));

            //Клиент
            var person = JOptionPane.showInputDialog(
                    null,
                    "ФИО клиента:",
                    "Новая запись",
                    JOptionPane.QUESTION_MESSAGE
            );

            //Дата прибытия
            var arrivalString = "";
            var arrivalDate = new Date();

            while (Objects.equals(arrivalString, "")) {
                arrivalString = JOptionPane.showInputDialog(
                        null,
                        "Дата прибытия (пример 2022-01-31):",
                        "Новая запись",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (arrivalString == null) {
                    return;
                }
                try {
                    if (checkIfDateBooked(roomObject.getNumber(), arrivalString, -1)) {
                        arrivalString = "";
                    }
                    else {
                        arrivalDate = dateFormat.parse(arrivalString);
                    }
                }
                catch (ParseException ex) {
                    ex.printStackTrace();
                    arrivalString = "";
                }
            }

            //Дата отбытия
            var departureString = "";
            var departureDate = new Date();

            while (Objects.equals(departureString, "")) {
                departureString = JOptionPane.showInputDialog(
                        null,
                        "Дата отбытия (пример 2022-01-31):",
                        "Новая запись (прибытие " + arrivalString + ")",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (departureString == null) {
                    return;
                }
                try {
                    if (arrivalDate.compareTo(departureDate) >= 0
                        || checkIfDateBooked(roomObject.getNumber(), departureString, -1)
                        || checkIfDateBooked(roomObject.getNumber(), arrivalString, departureString, -1)
                    ) {
                        departureString = "";
                    }

                    else {
                        departureDate = dateFormat.parse(departureString);
                    }
                }
                catch (ParseException ex) {
                    departureString = "";
                }
            }


            var recordsList = handler.getRecordsList();
            var record = new Record(
                    recordsList.size() != 0 ? recordsList.get(recordsList.size() - 1).getId() + 1 : 0,
                    roomObject,
                    person,
                    arrivalString,
                    departureString
            );

            handler.addRecord(record);
            showRecordsList();
        }
    };

    //слушатель для изменения фио постояльца
    private final Action changePerson = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var id = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            var name = "";

            while (Objects.equals(name, "")) {
                name = JOptionPane.showInputDialog(
                        null,
                        "ФИО:",
                        "Изменение заказа",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (name == null) {
                    return;
                }
            }

            mainView.handler.editRecordName(id, name);
            showRecordsList();
        }
    };

    //слушатель для изменения даты отбытия
    private final Action changeDepartureDate = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var record = handler.getRecord(
                (int) table.getModel().getValueAt(table.getSelectedRow(), 0)
            );

            var number = (int) table.getModel().getValueAt(table.getSelectedRow(), 1);
            var arrivalString = (String) table.getModel().getValueAt(table.getSelectedRow(), 6);
            Date arrivalDate = null;
            try {
                arrivalDate = dateFormat.parse(arrivalString);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            var departureString = "";
            var departureDate = new Date();

            while (Objects.equals(departureString, "")) {
                departureString = JOptionPane.showInputDialog(
                        null,
                        "Дата отбытия (пример 2022-01-31):",
                        "Редактирование",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (departureString == null) {
                    return;
                }
                try {
                    departureDate = dateFormat.parse(departureString);
                    assert arrivalDate != null;
                    if (arrivalDate.compareTo(departureDate) >= 0
                        || checkIfDateBooked(number, departureString, record.getId())
                        || checkIfDateBooked(number, record.getArrivalDate(), departureString, record.getId())) {
                        departureString = "";
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    departureString = "";
                }
            }

            mainView.handler.editDepartureDate(record.getId(), departureString);
            showRecordsList();
        }
    };

    //слушатель для удаления записи
    private final Action removeRecord = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            int choose = JOptionPane.showConfirmDialog(
                null,
                "Вы действительно хотите удалить запись?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION);
            if (choose == JOptionPane.NO_OPTION) {
                return;
            }
            var id = (int) table.getModel().getValueAt(row, 0);

            handler.deleteRecord(id);
            showRecordsList();
        }
    };

    private final Action showServices = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            new ServicesView(mainView);
        }
    };

    private final Action showServiceOrders = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var record = handler.getRecord(
                (int) table.getModel().getValueAt(table.getSelectedRow(), 0)
            );
            new ServiceOrdersView(mainView, record);
        }
    };

    private final Action showRooms = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            new RoomsView(mainView);
        }
    };

    private final Action showReport = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            new ServicesReportView(mainView);
        }
    };

    public MainView() {
        try {
             handler = new DbHandler();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        setTitle("Гостиница");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createView(getContentPane());
        setResizable(false);
        setLocationByPlatform(true);
        showRecordsList();
        pack();
        setVisible(true);
    }

    private void createView(Container container) {
        popupMenu = new JPopupMenu();
        var serviceItem = new JMenuItem("Добавить услугу");
        var servicesListItem = new JMenuItem("Список услуг");
        var removeItem = new JMenuItem("Удалить запись");
        var changeMenu = new JMenu("Изменить");
        var changePersonItem = new JMenuItem("ФИО");
        var changeDepartureItem = new JMenuItem("Дату отбытия");

        serviceItem.addActionListener(addService);
        servicesListItem.addActionListener(showServiceOrders);
        removeItem.addActionListener(removeRecord);
        changePersonItem.addActionListener(changePerson);
        changeDepartureItem.addActionListener(changeDepartureDate);

        changeMenu.add(changePersonItem);
        changeMenu.add(changeDepartureItem);

        popupMenu.add(serviceItem);
        popupMenu.add(servicesListItem);
        popupMenu.add(removeItem);
        popupMenu.add(changeMenu);

        var model = new DefaultTableModel(new Object[0][10], tableHeaders){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r >= 0 && r < table.getRowCount()) {
                    table.setRowSelectionInterval(r, r);
                } else {
                    table.clearSelection();
                }

                int selectedRow = table.getSelectedRow();
                if (selectedRow < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        var scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(1390,500));

        var menuBar = new JMenuBar();

        var roomsMenu = new JMenu("Номера");
        var showRoomsItem = new JMenuItem("Список номеров");
        showRoomsItem.addActionListener(showRooms);
        roomsMenu.add(showRoomsItem);

        var servicesMenu = new JMenu("Услуги");
        var showServicesItem = new JMenuItem("Список услуг");
        showServicesItem.addActionListener(showServices);
        servicesMenu.add(showServicesItem);

        var createMenu = new JMenu("Заказы");
        var createRecordItem = new JMenuItem("Оформить заказ");
        createRecordItem.addActionListener(createRecord);
        createMenu.add(createRecordItem);

        var reportsMenu = new JMenu("Отчёты");
        var createReportItem = new JMenuItem("Создать отчёт");
        createReportItem.addActionListener(showReport);
        reportsMenu.add(createReportItem);

        var searchMenu = new JMenu("Поиск");
        var searchByNameItem = new JMenuItem("По ФИО");
        var resetSearchItem = new JMenuItem("Сбросить поиск");
        searchByNameItem.addActionListener(searchByName);
        resetSearchItem.addActionListener(resetSearch);
        searchMenu.add(searchByNameItem);
        searchMenu.add(resetSearchItem);

        menuBar.add(roomsMenu);
        menuBar.add(servicesMenu);
        menuBar.add(createMenu);
        menuBar.add(reportsMenu);
        menuBar.add(searchMenu);

        setJMenuBar(menuBar);

        container.add(scrollPane);
    }

    //для суммирования цен на дополнительные услуги
    private float calculateServices(Record record) {
        var services = handler.getServiceOrdersListByRecord(record.getId());
        var sum = 0;
        for (var service : services) {
            sum += service.getService().getPrice();
        }
        return sum;
    }

    //для суммирования общей суммы вместе с услугами
    private float calculateSum(Record record) {
        Date arrivalDate;
        Date departureDate;
        long daysCount = 0L;
        try {
            arrivalDate = dateFormat.parse(record.getArrivalDate());
            departureDate = dateFormat.parse(record.getDepartureDate());
            daysCount = Math.round((
                    departureDate.getTime() - arrivalDate.getTime()
            ) / (double) 86400000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        var sum = record.getRoom().getPrice() * daysCount;
        return sum + calculateServices(record);
    }

    public void showRecordsList() {
        var recordsList = handler.getRecordsList();
        var model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (var record : recordsList) {
            model.addRow (new Object[] {
                    record.getId(),
                    record.getRoom().getNumber(),
                    record.getRoom().getType(),
                    record.getRoom().getCapacity(),
                    record.getRoom().getPrice(),
                    record.getPerson(),
                    record.getArrivalDate(),
                    record.getDepartureDate(),
                    calculateServices(record),
                    calculateSum(record)
            });
        }
    }

    //проверка свободен ли номер в этот день
    private boolean checkIfDateBooked(int roomNumber, String date, int id) {
        var recordsList = handler.getRecordsList();
        for (var record : recordsList) {
            if (record.getRoom().getNumber() == roomNumber && record.getId() != id) {
                try {
                    var arrivalDate = dateFormat.parse(record.getArrivalDate());
                    var departureDate = dateFormat.parse(record.getDepartureDate());
                    var setDate = dateFormat.parse(date);
                    if (setDate.getTime() >= arrivalDate.getTime()
                            && setDate.getTime() <= departureDate.getTime()) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Выбранный номер занят в этот день",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    //проверка свободен ли номер в период времени
    private boolean checkIfDateBooked(int roomNumber, String firstDate, String lastDate, int id) {
        var recordsList = handler.getRecordsList();
        for (var record : recordsList) {
            if (record.getRoom().getNumber() == roomNumber && record.getId() != id) {
                try {
                    var arrivalDate = dateFormat.parse(record.getArrivalDate());
                    var departureDate = dateFormat.parse(record.getDepartureDate());
                    var first = dateFormat.parse(firstDate);
                    var last = dateFormat.parse(lastDate);
                    if (first.getTime() < arrivalDate.getTime()
                            && last.getTime() > departureDate.getTime()) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Во время выбранного периода, данный номер занят",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    //проверка можно ли предоставить услугу
    private boolean checkIfDateInBookPeriod(int id, String dateString) {
        var record = handler.getRecord(id);
        try {
            var date = dateFormat.parse(dateString);
            var arrivalDate = dateFormat.parse(record.getArrivalDate());
            var departureDate = dateFormat.parse(record.getDepartureDate());
            if (date.getTime() <= departureDate.getTime()
                    && date.getTime() >= arrivalDate.getTime()) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(
                null,
                "Дата оказания услуги выходит за рамки бронирования",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
        );
        return false;
    }
}
