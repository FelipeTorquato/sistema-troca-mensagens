package org.example.client;

import org.example.client.ui.ChatWindow;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        // Swing deve ser iniciado na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Tenta usar o estilo visual do sistema operacional
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ChatWindow janela = new ChatWindow();
            janela.setVisible(true);
        });
    }
}
