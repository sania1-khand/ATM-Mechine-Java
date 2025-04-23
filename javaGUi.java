import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class javaGUi extends JFrame {
    // User accounts (username, pin, balance, isSavings)
    private Map<String, UserAccount> accounts = new HashMap<>();
    private UserAccount currentUser = null;
    private List<String> transactionHistory = new ArrayList<>();
    
    // GUI Components
    private JPanel loginPanel, mainPanel;
    private JTextField usernameField, amountField;
    private JPasswordField pinField;
    private JTextArea displayArea, historyArea;
    private JButton loginButton, logoutButton, resetButton;
    private JButton withdrawButton, depositButton, balanceButton, interestButton;
    
    // Constants
    private final double DAILY_WITHDRAWAL_LIMIT = 50000;
    private final double SAVINGS_INTEREST_RATE = 0.05; // 5% annual interest
    
    public javaGUi() {
        initializeAccounts();
        setupGUI();
    }
    
    private void initializeAccounts() {
        // Initialize some sample accounts
        accounts.put("user1", new UserAccount("user1", "1234", 10000, false));
        accounts.put("user2", new UserAccount("user2", "5678", 50000, true));
        accounts.put("admin", new UserAccount("admin", "0000", 100000, false));
    }
    
    private void setupGUI() {
        setTitle("ATM Machine");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create panels
        loginPanel = createLoginPanel();
        mainPanel = createMainPanel();
        
        // Start with login panel
        getContentPane().add(loginPanel);
        
        // Apply dark theme
        applyDarkTheme();
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("ATM Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);
        
        // PIN
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("PIN:"), gbc);
        
        gbc.gridx = 1;
        pinField = new JPasswordField(15);
        panel.add(pinField, gbc);
        
        // Login Button
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton, gbc);
        
        return panel;
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Top panel for logout and reset
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetFields());
        topPanel.add(resetButton);
        topPanel.add(logoutButton);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel for operations
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Display area
        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Transaction history
        historyArea = new JTextArea(5, 30);
        historyArea.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        centerPanel.add(historyScroll, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(e -> withdraw());
        
        depositButton = new JButton("Deposit");
        depositButton.addActionListener(e -> deposit());
        
        balanceButton = new JButton("Check Balance");
        balanceButton.addActionListener(e -> checkBalance());
        
        interestButton = new JButton("Calculate Interest");
        interestButton.addActionListener(e -> calculateInterest());
        
        buttonPanel.add(withdrawButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(balanceButton);
        buttonPanel.add(interestButton);
        
        // Amount field
        JPanel amountPanel = new JPanel();
        amountPanel.add(new JLabel("Amount:"));
        amountField = new JTextField(10);
        amountPanel.add(amountField);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(amountPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String pin = new String(pinField.getPassword());
        
        if (username.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and PIN", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        UserAccount account = accounts.get(username);
        if (account != null && account.getPin().equals(pin)) {
            currentUser = account;
            transactionHistory.clear();
            addTransaction("Login");
            getContentPane().removeAll();
            getContentPane().add(mainPanel);
            revalidate();
            repaint();
            displayMessage("Welcome, " + username + "!\nPlease select an operation.");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or PIN", "Login Failed", JOptionPane.ERROR_MESSAGE);
            pinField.setText("");
        }
    }
    
    private void logout() {
        currentUser = null;
        transactionHistory.clear();
        resetFields();
        getContentPane().removeAll();
        getContentPane().add(loginPanel);
        revalidate();
        repaint();
    }
    
    private void resetFields() {
        amountField.setText("");
        displayMessage("");
    }
    
    private void withdraw() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            
            if (amount <= 0) {
                displayMessage("Amount must be positive");
                return;
            }
            
            if (amount > DAILY_WITHDRAWAL_LIMIT) {
                displayMessage("Withdrawal exceeds daily limit of " + DAILY_WITHDRAWAL_LIMIT);
                return;
            }
            
            if (amount > currentUser.getBalance()) {
                displayMessage("Insufficient funds");
                return;
            }
            
            currentUser.setBalance(currentUser.getBalance() - amount);
            addTransaction("Withdraw: " + amount);
            displayMessage("Withdrawal successful.\nNew balance: " + currentUser.getBalance());
            amountField.setText("");
        } catch (NumberFormatException e) {
            displayMessage("Invalid amount");
        }
    }
    
    private void deposit() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            
            if (amount <= 0) {
                displayMessage("Amount must be positive");
                return;
            }
            
            currentUser.setBalance(currentUser.getBalance() + amount);
            addTransaction("Deposit: " + amount);
            displayMessage("Deposit successful.\nNew balance: " + currentUser.getBalance());
            amountField.setText("");
        } catch (NumberFormatException e) {
            displayMessage("Invalid amount");
        }
    }
    
    private void checkBalance() {
        displayMessage("Current balance: " + currentUser.getBalance());
        addTransaction("Balance checked");
    }
    
    private void calculateInterest() {
        if (currentUser.isSavingsAccount()) {
            double interest = currentUser.getBalance() * SAVINGS_INTEREST_RATE / 12; // Monthly interest
            displayMessage("Savings account interest (monthly): " + interest + 
                          "\nProjected balance after interest: " + (currentUser.getBalance() + interest));
            addTransaction("Interest calculated");
        } else {
            displayMessage("This is not a savings account. No interest will be applied.");
        }
    }
    
    private void addTransaction(String transaction) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        transactionHistory.add(0, timestamp + " - " + transaction);
        
        // Keep only last 5 transactions
        if (transactionHistory.size() > 5) {
            transactionHistory = transactionHistory.subList(0, 5);
        }
        
        updateHistoryDisplay();
    }
    
    private void updateHistoryDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Last 5 Transactions:\n");
        for (String t : transactionHistory) {
            sb.append(t).append("\n");
        }
        historyArea.setText(sb.toString());
    }
    
    private void displayMessage(String message) {
        displayArea.setText(message);
    }
    
    private void applyDarkTheme() {
        Color darkBackground = new Color(45, 45, 45);
        Color darkForeground = new Color(220, 220, 220);
        Color darkButton = new Color(70, 70, 70);
        
        // Apply to all components
        for (Component c : getComponentsRecursive(this)) {
            if (c instanceof JPanel) {
                c.setBackground(darkBackground);
            } else if (c instanceof JTextArea || c instanceof JTextField || c instanceof JPasswordField) {
                c.setBackground(new Color(60, 60, 60));
                c.setForeground(darkForeground);
                if (c instanceof JTextArea) {
                    ((JTextArea)c).setCaretColor(darkForeground);
                }
            } else if (c instanceof JButton) {
                c.setBackground(darkButton);
                c.setForeground(darkForeground);
            } else if (c instanceof JLabel) {
                c.setForeground(darkForeground);
            }
        }
    }
    
    private List<Component> getComponentsRecursive(Container container) {
        List<Component> components = new ArrayList<>();
        for (Component c : container.getComponents()) {
            components.add(c);
            if (c instanceof Container) {
                components.addAll(getComponentsRecursive((Container)c));
            }
        }
        return components;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            javaGUi atm = new javaGUi();
            atm.setVisible(true);
        });
    }
    
    // Inner class for user accounts
    private static class UserAccount {
        private String username;
        private String pin;
        private double balance;
        private boolean isSavingsAccount;
        
        public UserAccount(String username, String pin, double balance, boolean isSavingsAccount) {
            this.username = username;
            this.pin = pin;
            this.balance = balance;
            this.isSavingsAccount = isSavingsAccount;
        }
        
        public String getUsername() { return username; }
        public String getPin() { return pin; }
        public double getBalance() { return balance; }
        public boolean isSavingsAccount() { return isSavingsAccount; }
        public void setBalance(double balance) { this.balance = balance; }
    }
}