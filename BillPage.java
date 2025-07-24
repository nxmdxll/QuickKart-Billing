/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author tenzi
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillPage extends JFrame {
    private int buyerId;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JLabel lblName, lblEmail, lblPhone, lblAddress;
    private JPanel buyerInfoPanel;
    private JComboBox<String> cbProducts;
    private JTextField tfQuantity;

    public BillPage(int buyerId) {
        this.buyerId = buyerId;

        setTitle("QuickKart Billing Portal");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initHeader();
        initTopPanel();     
        initProductTable();  
        initBottomPanel();   
    }
    private void showBillPreview() {
    StringBuilder billText = new StringBuilder();
    billText.append("========= QuickKart Bill Preview =========\n");
    billText.append("Buyer ID: ").append(buyerId).append("\n");

    
    billText.append(lblName.getText()).append("\n");
    billText.append(lblEmail.getText()).append("\n");
    billText.append(lblPhone.getText()).append("\n");
    billText.append(lblAddress.getText()).append("\n");
    billText.append("-----------------------------------------\n");
    billText.append(String.format("%-20s %-8s %-10s %-10s\n", "Product", "Qty", "Price", "Total"));
    billText.append("-----------------------------------------\n");

    double grandTotal = 0;

    for (int i = 0; i < productTable.getRowCount(); i++) {
        String product = productTable.getValueAt(i, 0).toString();
        String qty = productTable.getValueAt(i, 1).toString();
        String price = productTable.getValueAt(i, 2).toString();
        String total = productTable.getValueAt(i, 3).toString();

        billText.append(String.format("%-20s %-8s ₹%-9s ₹%-9s\n", product, qty, price, total));
        grandTotal += Double.parseDouble(total);
    }

    billText.append("-----------------------------------------\n");
    billText.append(String.format("Total Amount: ₹%.2f\n", grandTotal));
    billText.append("Thank you for shopping with QuickKart!\n");

    JTextArea textArea = new JTextArea(billText.toString());
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(500, 400));

    JOptionPane.showMessageDialog(this, scrollPane, "Bill Preview", JOptionPane.INFORMATION_MESSAGE);
}

  
    private void initHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(0, 102, 102));
        JLabel title = new JLabel("QuickKart Billing", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        JLabel contact = new JLabel("Bangalore • +91-8860468310", SwingConstants.CENTER);
        contact.setForeground(Color.WHITE);
        header.add(title);
        header.add(contact);
        add(header, BorderLayout.NORTH);
    }
 
    private void initTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
       
        JPanel search = new JPanel();
        search.setBackground(new Color(255, 255, 204));
        JLabel lbl = new JLabel("Buyer ID: ");
        JTextField tfSearch = new JTextField(8);
        JButton btn = new JButton("Load Buyer");

        btn.addActionListener(e -> {
            try {
                buyerId = Integer.parseInt(tfSearch.getText().trim());
                loadBuyerInfo();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid Buyer ID.");
            }
        });

        search.add(lbl);
        search.add(tfSearch);
        search.add(btn);
        top.add(search, BorderLayout.NORTH);
        
        buyerInfoPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buyerInfoPanel.setBackground(new Color(204, 255, 204));
        lblName = new JLabel("Name: ");
        lblEmail = new JLabel("Email: ");
        lblPhone = new JLabel("Phone: ");
        lblAddress = new JLabel("Address: ");

        for (JLabel l : new JLabel[]{lblName, lblEmail, lblPhone, lblAddress}) {
            l.setFont(new Font("SansSerif", Font.BOLD, 14));
            buyerInfoPanel.add(l);
        }

        top.add(buyerInfoPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.BEFORE_FIRST_LINE);

        loadBuyerInfo(); 
    }

    private void loadBuyerInfo() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM buyers WHERE id = ?");
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblName.setText("Name: " + rs.getString("name"));
                lblEmail.setText("Email: " + rs.getString("email"));
                lblPhone.setText("Phone: " + rs.getString("phone"));
                lblAddress.setText("Address: " + rs.getString("address"));
            } else {
                lblName.setText("Name: Not Found");
                lblEmail.setText("Email: N/A");
                lblPhone.setText("Phone: N/A");
                lblAddress.setText("Address: N/A");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initProductTable() {
        String[] cols = {"Product", "Qty", "Price", "Total"};
        tableModel = new DefaultTableModel(cols, 0);
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        productTable.setRowHeight(22);
        add(new JScrollPane(productTable), BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel addPanel = new JPanel();
        addPanel.setBackground(new Color(255, 255, 204));

        cbProducts = new JComboBox<>();
        tfQuantity = new JTextField(5);
        JButton btnAdd = new JButton("Add to Bill");

        try (Connection con = DBConnection.getConnection();
             ResultSet rs = con.prepareStatement("SELECT name FROM products").executeQuery()) {
            while (rs.next()) cbProducts.addItem(rs.getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnAdd.addActionListener(e -> addProductToBill());

        addPanel.add(new JLabel("Product:"));
        addPanel.add(cbProducts);
        addPanel.add(new JLabel("Qty:"));
        addPanel.add(tfQuantity);
        addPanel.add(btnAdd);

        
        JPanel actionPanel = new JPanel();
        JButton btnPreview = new JButton("Preview Bill");
        JButton btnPrint = new JButton("Print Bill");
      

        btnPreview.addActionListener(e -> showBillPreview());
        btnPrint.addActionListener(e -> printBill());
       
        
        actionPanel.add(btnPreview);
        actionPanel.add(btnPrint);
       

        bottom.add(addPanel, BorderLayout.NORTH);
        bottom.add(actionPanel, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);
    }

    private void addProductToBill() {
        String product = (String) cbProducts.getSelectedItem();
        String qtyText = tfQuantity.getText().trim();

        try {
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) throw new NumberFormatException();

            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT price, quantity FROM products WHERE name = ?");
                ps.setString(1, product);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("quantity");

                    if (qty > stock) {
                        JOptionPane.showMessageDialog(this, "❌ Not enough stock. Available: " + stock);
                        return;
                    }

                    double total = price * qty;
                    tableModel.addRow(new Object[]{product, qty, price, total});
                    tfQuantity.setText("");
                 
                    PreparedStatement update = con.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE name = ?");
                    update.setInt(1, qty);
                    update.setString(2, product);
                    update.executeUpdate();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive quantity.");
        }
    }
   
    private void printBill() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Add items to bill before printing.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO transactions (buyer_id, product_name, quantity, unit_price, transaction_date) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                ps.setInt(1, buyerId);
                ps.setString(2, tableModel.getValueAt(i, 0).toString());
                ps.setInt(3, (int) tableModel.getValueAt(i, 1));
                ps.setDouble(4, (double) tableModel.getValueAt(i, 2));
                ps.setString(5, "UPI");
                ps.setDate(6, date);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "✅ Bill saved!");
            tableModel.setRowCount(0); 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String input = JOptionPane.showInputDialog(null, "Enter Buyer ID:");
            if (input != null && input.matches("\\d+")) {
                new BillPage(Integer.parseInt(input)).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Buyer ID");
            }
        });
    }
}

