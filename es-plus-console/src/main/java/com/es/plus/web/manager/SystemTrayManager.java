package com.es.plus.web.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
 * 系统托盘管理器
 * @author hzh
 */
public class SystemTrayManager {
    private static TrayIcon trayIcon;
    private static ConfigurableApplicationContext context;
    private static final String APP_NAME = "ES-Plus Console";
    private static final String APP_URL = "http://localhost:8989";

    /**
     * 初始化系统托盘
     */
    public static void initSystemTray(ConfigurableApplicationContext applicationContext) {
        context = applicationContext;
        System.out.println("开始初始化系统托盘...");
        System.out.println("GraphicsEnvironment.isHeadless(): " + java.awt.GraphicsEnvironment.isHeadless());
        System.out.println("SystemTray.isSupported(): " + SystemTray.isSupported());
        if (!SystemTray.isSupported()) {
            System.out.println("系统不支持托盘图标");
            System.out.println("可能的原因：");
            System.out.println("1. Java运行在无头模式（headless mode）");
            System.out.println("2. 系统托盘功能被禁用");
            System.out.println("3. 显示驱动问题");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = createTrayIcon();
                PopupMenu popup = createPopupMenu();
                trayIcon = new TrayIcon(image, APP_NAME, popup);
                trayIcon.setImageAutoSize(true);
                trayIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                            openBrowser();
                        }
                    }
                });
                tray.add(trayIcon);
                showMessage("启动成功", APP_NAME + " 已启动，双击图标打开管理界面", TrayIcon.MessageType.INFO);
                System.out.println("系统托盘图标已成功创建");
            } catch (Exception e) {
                System.err.println("创建系统托盘图标失败：" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 创建托盘图标
     */
    private static Image createTrayIcon() {
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(52, 152, 219));
        g.fillOval(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString("E", 4, 12);
        g.dispose();
        return image;
    }

    /**
     * 创建右键菜单
     */
    private static PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 12);
        MenuItem openItem = new MenuItem("Open Console");
        openItem.setFont(font);
        openItem.addActionListener(e -> openBrowser());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setFont(font);
        exitItem.addActionListener(e -> exitApplication());
        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        return popup;
    }

    /**
     * 打开浏览器
     */
    private static void openBrowser() {
        try {
            Desktop.getDesktop().browse(new URI(APP_URL));
        } catch (Exception e) {
            showMessage("错误", "无法打开浏览器: " + e.getMessage(), TrayIcon.MessageType.ERROR);
        }
    }

    /**
     * 退出应用
     */
    private static void exitApplication() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        if (context != null) {
            SpringApplication.exit(context, () -> 0);
        }
        System.exit(0);
    }

    /**
     * 显示托盘消息
     */
    private static void showMessage(String title, String message, TrayIcon.MessageType messageType) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, messageType);
        }
    }
}