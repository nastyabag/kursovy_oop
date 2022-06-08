package UI;

import model.Record;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//для отображения и добавления услуги к конкретному постояльцу
public class ServiceOrdersView extends JDialog {
    private final MainView mainView;
    private JTable table;
    private JPopupMenu popupMenu;

    private final Record record;

    private final Object[] tableHeaders = {"ID", "Услуга", "Стоимость", "Дата"};

    private final Action removeServiceOrder = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(
                        null,
                        "Заказ не выбран",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            int choose = JOptionPane.showConfirmDialog(
                    null,
                    "Вы действительно хотите удалить заказ",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION);
            if (choose == JOptionPane.NO_OPTION) {
                return;
            }
            var number = (int) table.getModel().getValueAt(row, 0);

            mainView.handler.deleteServiceOrder(number);
            showServicesOrdersList();
            mainView.showRecordsList();
        }
    };

    private void showServicesOrdersList() {
        var serviceOrders = mainView.handler.getServiceOrdersListByRecord(record.getId());
        var model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (var order : serviceOrders) {
            model.addRow(new Object[] {
                    order.getId(),
                    order.getService().getName(),
                    order.getService().getPrice(),
                    order.getDate()
            });
        }
    }

    public ServiceOrdersView(MainView mainView, Record record) {
        this.mainView = mainView;
        this.record = record;
        setTitle(record.getId() + " запись: список заказов");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        createView(getContentPane());
        setResizable(false);
        setLocationByPlatform(true);
        pack();
        setVisible(true);
    }

    private void createView(Container container) {
        popupMenu = new JPopupMenu();
        var removeItem = new JMenuItem("Удалить заказ");
        removeItem.addActionListener(removeServiceOrder);
        popupMenu.add(removeItem);

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
        table.setPreferredScrollableViewportSize(new Dimension(400,130));
        container.add(scrollPane);

        showServicesOrdersList();
    }
}
