package UI;

import model.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ServicesView extends JDialog {
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$");

    private final MainView mainView;
    private JTable table;
    private JPopupMenu popupMenu;

    private final Object[] tableHeaders = {"ID", "Услуга", "Цена"};

    private final Action createService = new AbstractAction() {
        List<Service> services;

        private boolean checkIfExist(String name) {
            for (var service : services) {
                if (Objects.equals(service.getName(), name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            services = mainView.handler.getServicesList();

            //Название
            var name = "";
            do {
                name = JOptionPane.showInputDialog(
                        null,
                        "Название услуги:",
                        "Новая услуга",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (name == null) {
                    return;
                }
            }
            while (checkIfExist(name));

            //Цена
            var price = "";

            while (Objects.equals(price, "")) {
                price = JOptionPane.showInputDialog(
                        null,
                        "Цена:",
                        "Новая услуга",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (price == null) {
                    return;
                }
                if (!FLOAT_PATTERN.matcher(price).matches()) {
                    price = "";
                }
            }

            var servicesList = mainView.handler.getServicesList();
            var service = new Service(
                    servicesList.size() != 0 ? servicesList.get(servicesList.size() - 1).getId() + 1 : 0,
                    name,
                    Float.parseFloat(price)
            );

            mainView.handler.addService(service);
            showServicesList();
        }
    };

    private final Action removeService = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int choose = JOptionPane.showConfirmDialog(
                    null,
                    "Вы действительно хотите удалить запись?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION);
            if (choose == JOptionPane.NO_OPTION) {
                return;
            }
            var number = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            mainView.handler.deleteService(number);
            showServicesList();
            mainView.showRecordsList();
        }
    };

    private final Action changeName = new AbstractAction() {
        List<Service> services;

        private boolean checkIfExist(String name) {
            for (var service : services) {
                if (Objects.equals(service.getName(), name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            services = mainView.handler.getServicesList();
            var id = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            var name = "";
            //Тип
            do {
                name = JOptionPane.showInputDialog(
                        null,
                        "Название услуги:",
                        "Изменение",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (name == null) {
                    return;
                }
            }
            while (checkIfExist(name));

            mainView.handler.editServiceName(id, name);
            showServicesList();
        }
    };

    private final Action changePrice = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            var id = (int) table.getModel().getValueAt(table.getSelectedRow(), 0);

            var price = "";

            while (Objects.equals(price, "")) {
                price = JOptionPane.showInputDialog(
                        null,
                        "Цена:",
                        "Новая услуга",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (price == null) {
                    return;
                }
                if (!FLOAT_PATTERN.matcher(price).matches()) {
                    price = "";
                }
            }

            mainView.handler.editServicePrice(id, Float.parseFloat(price));
            mainView.showRecordsList();
            showServicesList();
        }
    };

    private void showServicesList() {
        var services = mainView.handler.getServicesList();
        var model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (var service : services) {
            model.addRow(new Object[] {
                    service.getId(),
                    service.getName(),
                    service.getPrice()
            });
        }
    }

    public ServicesView(MainView mainView) {
        this.mainView = mainView;
        setTitle("Список услуг");
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

        var createMenu = new JMenu("Добавить услугу");
        var showCreateItem = new JMenuItem("Добавить");
        showCreateItem.addActionListener(createService);
        createMenu.add(showCreateItem);
        menuBar.add(createMenu);

        setJMenuBar(menuBar);

        popupMenu = new JPopupMenu();

        var removeItem = new JMenuItem("Удалить услугу");
        var changeMenu = new JMenu("Изменить");
        var changeNameItem = new JMenuItem("Название");
        var changePriceItem = new JMenuItem("Цену");

        removeItem.addActionListener(removeService);
        changeNameItem.addActionListener(changeName);
        changePriceItem.addActionListener(changePrice);

        changeMenu.add(changeNameItem);
        changeMenu.add(changePriceItem);

        popupMenu.add(removeItem);
        popupMenu.add(changeMenu);

        var model = new DefaultTableModel(new Object[0][3], tableHeaders){
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
        table.setPreferredScrollableViewportSize(new Dimension(400,130));
        container.add(scrollPane);

        showServicesList();
    }
}
