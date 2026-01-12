package org.example.server;

import org.example.common.IChatService;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServerMain extends JFrame {
    private JButton btnIniciar;
    private JTextArea areaLog;

    public ServerMain() {
        configurarJanela();
        configurarComponentes();
        redirecionarLogs();
    }

    private void configurarJanela() {
        setTitle("Servidor de Mensagens PPD (RMI + ActiveMQ)");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Centraliza a janela na tela
        setLocationRelativeTo(null);
    }

    private void configurarComponentes() {
        // --- Área de Log (Centro) ---
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(Color.GREEN);
        JScrollPane scrollPane = new JScrollPane(areaLog);

        // Auto-scroll para o final sempre que chegar texto novo
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> e.getAdjustable().setValue(e.getAdjustable().getMaximum()));

        add(scrollPane, BorderLayout.CENTER);

        // --- Botão de Iniciar (Topo) ---
        JPanel panelTopo = new JPanel(new FlowLayout());
        btnIniciar = new JButton("Iniciar Servidor");
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 14));

        btnIniciar.addActionListener(e -> acaoIniciarServidor());

        panelTopo.add(btnIniciar);
        add(panelTopo, BorderLayout.NORTH);
    }

    private void acaoIniciarServidor() {
        // Desativa o botão
        btnIniciar.setEnabled(false);
        btnIniciar.setText("Servidor Iniciando...");

        // Roda a inicialização em uma Thread separada
        new Thread(() -> {
            try {
                System.out.println("--- Iniciando Configuração do Servidor ---");

                // Inicia o registro RMI na porta 1099
                try {
                    LocateRegistry.createRegistry(1099);
                    System.out.println("[OK] RMI Registry iniciado na porta 1099.");
                } catch (Exception e) {
                    System.out.println("[INFO] RMI Registry já parece estar rodando: " + e.getMessage());
                }

                // Instancia o objeto remoto (Conecta ao ActiveMQ internamente)
                System.out.println("Conectando ao ActiveMQ e criando serviços...");
                IChatService chatService = new ChatServiceImpl();

                // Disponibiliza o serviço com o nome ChatServidor
                Naming.rebind("rmi://localhost:1099/ChatServidor", chatService);

                System.out.println("[SUCESSO] Servidor RMI registrado como 'ChatServidor'.");
                System.out.println("Aguardando conexões dos clientes...");

                // Atualiza texto do botão na GUI
                SwingUtilities.invokeLater(() -> btnIniciar.setText("Servidor Rodando"));

            } catch (Exception e) {
                System.err.println("[ERRO CRÍTICO] Falha ao iniciar servidor: " + e.toString());
                e.printStackTrace();

                // Reabilita o botão caso dê erro, para tentar de novo
                SwingUtilities.invokeLater(() -> {
                    btnIniciar.setText("Tentar Iniciar Novamente");
                    btnIniciar.setEnabled(true);
                });
            }
        }).start();
    }


    // Redireciona System.out e System.err para o JTextArea da interface
    private void redirecionarLogs() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                atualizarTexto(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                atualizarTexto(new String(b, off, len));
            }

            private void atualizarTexto(String texto) {
                SwingUtilities.invokeLater(() -> areaLog.append(texto));
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ServerMain().setVisible(true);
        });
    }
}
