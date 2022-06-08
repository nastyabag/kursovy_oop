package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
//для отображения отчета
public class ServicesReportView extends JDialog {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final MainView mainView;
    private JTable table;

    private String startDateString = "";
    private String endDateString = "";
    private Date startDate;
    private Date endDate;

    private final Object[] tableHeaders = {"ID", "Запись", "Услуга", "Стоимость", "Дата"};

    private String getIncomeAndShowOrders() {
        var serviceOrders = mainView.handler.getServiceOrdersList();
        var model = (DefaultTableModel) table.getModel();
        var date = new Date();
        var sum = 0f;
        model.setRowCount(0);
        for (var order : serviceOrders) {
            try {
                date = dateFormat.parse(order.getDate());
                if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0) {
                    model.addRow(new Object[]{
                            order.getId(),
                            order.getRecord().getId(),
                            order.getService().getName(),
                            order.getService().getPrice(),
                            order.getDate()
                    });
                    sum += order.getService().getPrice();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return String.format("Отчёт за срок с %s по %s:\nЗаработок %s",
                startDateString, endDateString, sum
        );
    }

    public ServicesReportView(MainView mainView) {
        this.mainView = mainView;
        setTitle("Отчёт");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        createView(getContentPane());
        setResizable(false);
        setLocationByPlatform(true);
        pack();
        setVisible(true);
    }

    private void createView(Container container) {
        var model = new DefaultTableModel(new Object[0][5], tableHeaders){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        var scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(500,130));
        container.add(scrollPane);
        getDates();
        if (endDateString == null || startDateString == null) {
            return;
        }
        var textArea = new JTextArea(getIncomeAndShowOrders());
        textArea.setEditable(false);
        container.add(textArea, "North");
    }


    private void getDates() {
        while (Objects.equals(startDateString, "")) {
            startDateString = JOptionPane.showInputDialog(
                    null,
                    "Левая граница (пример 2022-01-31):",
                    "Отчёт",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (startDateString == null) {
                dispose();
                return;
            }
            else {
                try {
                    startDate = dateFormat.parse(startDateString);
                }
                catch (ParseException ex) {
                    startDateString = "";
                }
            }
        }

        while (Objects.equals(endDateString, "")) {
            endDateString = JOptionPane.showInputDialog(
                    null,
                    "Правая граница (пример 2022-01-31):",
                    "Отчёт (левая граница " + startDateString + ")",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (endDateString == null) {
                dispose();
                return;
            }
            try {
                endDate = dateFormat.parse(endDateString);
                if (startDate.compareTo(endDate) >= 0) {
                    endDateString = "";
                }
            }
            catch (ParseException ex) {
                endDateString = "";
            }
        }
    }
}
