package com.mikaeel.mcsrunner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.prefs.Preferences;

public final class Main extends JFrame {
    private final JTextField scriptField = new JTextField();
    private final JTextArea log = new JTextArea();
    private final JLabel status = new JLabel(" آماده برای اجرا");
    private final JButton runButton = new JButton("▶  Run server");
    private final JButton stopButton = new JButton("■  Stop");
    private Process process;
    private final Preferences prefs = Preferences.userNodeForPackage(Main.class);

    public Main() {
        super("Minecraft Server Runner");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 500));
        setSize(820, 570);
        setLocationRelativeTo(null);
        buildUi();
        String saved = prefs.get("script", "");
        if (!saved.isBlank()) scriptField.setText(saved);
        updateButtons(false);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(18, 20, 18, 20));
        root.setBackground(new Color(19, 24, 31));
        setContentPane(root);

        JLabel title = new JLabel("Minecraft Server Runner");
        title.setForeground(new Color(235, 244, 238));
        title.setFont(new Font("Dialog", Font.BOLD, 25));
        JLabel subtitle = new JLabel("فایل start.bat را یک‌بار انتخاب کن، بعد با یک کلیک سرور را اجرا کن.");
        subtitle.setForeground(new Color(158, 174, 166));
        subtitle.setFont(new Font("Dialog", Font.PLAIN, 14));
        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        root.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        JPanel chooser = new JPanel(new BorderLayout(8, 0));
        chooser.setOpaque(false);
        scriptField.setToolTipText("مسیر فایل start.bat");
        scriptField.setFont(new Font("Dialog", Font.PLAIN, 14));
        scriptField.setBackground(new Color(31, 39, 48));
        scriptField.setForeground(new Color(235, 244, 238));
        scriptField.setCaretColor(Color.WHITE);
        scriptField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 82, 75)), new EmptyBorder(9, 10, 9, 10)));
        JButton browse = new JButton("Choose start.bat…");
        browse.addActionListener(e -> chooseScript());
        chooser.add(scriptField, BorderLayout.CENTER);
        chooser.add(browse, BorderLayout.EAST);
        center.add(chooser, BorderLayout.NORTH);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBackground(new Color(12, 16, 21));
        log.setForeground(new Color(191, 214, 199));
        log.setBorder(new EmptyBorder(12, 12, 12, 12));
        center.add(new JScrollPane(log), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        runButton.setBackground(new Color(70, 171, 105));
        runButton.setForeground(Color.WHITE);
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> runServer());
        stopButton.setBackground(new Color(172, 74, 74));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.addActionListener(e -> stopServer());
        actions.add(runButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(stopButton);
        bottom.add(actions, BorderLayout.WEST);
        status.setForeground(new Color(158, 174, 166));
        bottom.add(status, BorderLayout.CENTER);
        JButton copy = new JButton("Copy log");
        copy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(log.getText()), null);
            status.setText(" log کپی شد");
        });
        bottom.add(copy, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
        append("Minecraft Server Runner آماده است.");
    }

    private void chooseScript() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("انتخاب start.bat");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (!selected.getName().toLowerCase().endsWith(".bat")) {
                JOptionPane.showMessageDialog(this, "لطفاً یک فایل .bat انتخاب کن.", "فایل نامعتبر", JOptionPane.WARNING_MESSAGE);
                return;
            }
            scriptField.setText(selected.getAbsolutePath());
            prefs.put("script", selected.getAbsolutePath());
            append("فایل انتخاب شد: " + selected.getAbsolutePath());
        }
    }

    private void runServer() {
        String path = scriptField.getText().trim();
        if (path.isEmpty()) { chooseScript(); path = scriptField.getText().trim(); }
        if (path.isEmpty()) return;
        File script = new File(path);
        if (!script.isFile()) {
            JOptionPane.showMessageDialog(this, "فایل پیدا نشد. دوباره انتخابش کن.", "مسیر نامعتبر", JOptionPane.ERROR_MESSAGE);
            return;
        }
        prefs.put("script", script.getAbsolutePath());
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "call", script.getAbsolutePath());
            pb.directory(script.getParentFile());
            pb.redirectErrorStream(true);
            process = pb.start();
            updateButtons(true);
            status.setText(" سرور در حال اجراست");
            append("شروع شد: " + script.getName());
            streamOutput(process.getInputStream());
            new Thread(() -> {
                try {
                    int exit = process.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        append("\nفرآیند متوقف شد. exit code: " + exit);
                        updateButtons(false);
                        status.setText(" سرور متوقف است");
                    });
                } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }, "server-waiter").start();
        } catch (IOException ex) {
            append("خطا: " + ex.getMessage());
            updateButtons(false);
        }
    }

    private void streamOutput(InputStream input) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String message = line;
                    SwingUtilities.invokeLater(() -> append(message));
                }
            } catch (IOException ex) { SwingUtilities.invokeLater(() -> append("خطا در خواندن log: " + ex.getMessage())); }
        }, "server-output").start();
    }

    private void stopServer() {
        if (process != null && process.isAlive()) {
            process.destroy();
            append("درخواست توقف ارسال شد.");
        }
    }

    private void updateButtons(boolean running) {
        runButton.setEnabled(!running);
        stopButton.setEnabled(running);
        scriptField.setEnabled(!running);
    }

    private void append(String text) {
        log.append(text + System.lineSeparator());
        log.setCaretPosition(log.getDocument().getLength());
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
