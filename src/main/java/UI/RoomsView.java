package UI;

import model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.List;

public class RoomsView extends JDialog {
    private static final Pattern INT_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$");

    private final MainView mainView;
    private JTable table;
    private JPopupMenu popupMenu;

    private final Object[] tableHeaders = {"Номер", "Тип", "Вместимость", "Цена за сутки"};

    private final Action createRoom = new AbstractAction() {
        List<Room> rooms;

        private boolean checkIfExist(int number) {
            for (var room : rooms) {
                if (room.getNumber() == number) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            rooms = mainView.handler.getRoomsList();

            //Номер
            var number = "";

            while (Objects.equals(number, "")) {
                number = JOptionPane.showInputDialog(
                        null,
                        "Номер номера (?):",
                        "Новый номер",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (number == null) {
                    return;
                }
                if (!INT_PATTERN.matcher(number).matches() || checkIfExist(Integer.parseInt(number))) {
                    number = "";
                }
            }

            //Тип
            var type = "";

            while (Objects.equals(type, "")) {
                type = (String) JOptionPane.showInputDialog(
                        null,
                        "Тип номера:",
                        "Новый номер",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (type == null) {
                    return;
                }
            }


            //Дата прибытия
            var capacity = "";

            while (Objects.equals(capacity, "")) {
                capacity = JOptionPane.showInputDialog(
                        null,
                        "Вместимость номера:",
                        "Новый номер",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (capacity == null) {
                    return;
                }
                if (!INT_PATTERN.matcher(capacity).matches()) {
                    capacity = "";
                }
            }

            //Цена
            var price = "";

            while (Objects.equals(price, "")) {
                price = JOptionPane.showInputDialog(
                        null,
                        "Цена за сутки:",
                        "Новый номер",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (price == null) {
                    return;
                }
                if (!FLOAT_PATTERN.matcher(price).matches()) {
                    price = "";
                }
            }

            var room = new Room(
                    Integer.parseInt(number),
                    type,
                    Integer.parseInt(capacity),
                    Float.parseFloat(price)
            );

            mainView.handler.addRoom(room);
            showRoomsList();
        }
    };

    private final Action removeRoom = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int choose = JOptionPane.showConfirmDialog(
                    null,
                    "Вы действительно хотите удалить номер?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION);
            if (choose == JOptionPane.NO_OPTION) {
                return;
            }
            var number = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            mainView.handler.deleteRoom(number);
            showRoomsList();
            mainView.showRecordsList();
        }
    };

    private final Action editRoomType = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var type = "";

            while (Objects.equals(type, "")) {
                type = (String) JOptionPane.showInputDialog(
                        null,
                        "Тип номера:",
                        "Редактирование",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (type == null) {
                    return;
                }
            }

            var number = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            mainView.handler.editRoomType(number, type);
            showRoomsList();
            mainView.showRecordsList();
        }
    };

    private final Action editRoomCapacity = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var capacity = "";

            while (Objects.equals(capacity, "")) {
                capacity = JOptionPane.showInputDialog(
                        null,
                        "Вместимость номера:",
                        "Новый номер",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (capacity == null) {
                    return;
                }
                if (!INT_PATTERN.matcher(capacity).matches()) {
                    capacity = "";
                }
            }

            var number = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            mainView.handler.editRoomCapacity(number, Integer.parseInt(capacity));
            showRoomsList();
            mainView.showRecordsList();
        }
    };

    private final Action editRoomPrice = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var price = "";

            while (Objects.equals(price, "")) {
                price = JOptionPane.showInputDialog(
                        null,
                        "Цена за сутки:",
                        "Новый номер",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (price == null) {
                    return;
                }
                if (!FLOAT_PATTERN.matcher(price).matches()) {
                    price = "";
                }
            }

            var number = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            mainView.handler.editRoomPrice(number, Float.parseFloat(price));
            showRoomsList();
            mainView.showRecordsList();
        }
    };

    private void showRoomsList() {
        var rooms = mainView.handler.getRoomsList();
        var model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (var room : rooms) {
            model.addRow(new Object[] {
                    room.getNumber(),
                    room.getType(),
                    room.getCapacity(),
                    room.getPrice()
            });
        }
    }

    public RoomsView(MainView mainView) {
        this.mainView = mainView;
        setTitle("Список номеров");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        createView(getContentPane());
        setResizable(false);
        setLocationByPlatform(true);
        pack();
        setVisible(true);
    }

    private void createView(Container container) {
        var menuBar = new JMenuBar();

        var createMenu = new JMenu("Добавить номер");
        var showCreateItem = new JMenuItem("Добавить");
        showCreateItem.addActionListener(createRoom);
        createMenu.add(showCreateItem);

        menuBar.add(createMenu);
        setJMenuBar(menuBar);

        popupMenu = new JPopupMenu();
        var removeItem = new JMenuItem("Удалить номер");
        var changeMenu = new JMenu("Изменить");
        var changeTypeItem = new JMenuItem("Тип");
        var changeCapacityItem = new JMenuItem("Вместимость");
        var changePriceItem = new JMenuItem("Цену");
        changeTypeItem.addActionListener(editRoomType);
        changeCapacityItem.addActionListener(editRoomCapacity);
        changePriceItem.addActionListener(editRoomPrice);
        removeItem.addActionListener(removeRoom);
        changeMenu.add(changeTypeItem);
        changeMenu.add(changeCapacityItem);
        changeMenu.add(changePriceItem);
        popupMenu.add(removeItem);
        popupMenu.add(changeMenu);

        var model = new DefaultTableModel(new Object[0][4], tableHeaders){
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
        table.setPreferredScrollableViewportSize(new Dimension(600,200));
        container.add(scrollPane);

        showRoomsList();
    }
}
