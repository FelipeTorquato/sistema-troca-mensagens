package org.example.client.ui;

import org.example.client.network.P2PClient;
import org.example.client.network.P2PServer;
import org.example.client.network.RMIClient;
import org.example.common.Mensagem;
import org.example.common.MessageListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChatWindow extends JFrame implements MessageListener {
    // Componentes de UI
    private JTextArea areaChat;
    private JTextField campoMensagem;
    private DefaultListModel<Contato> modeloListaContatos;
    private JList<Contato> listaContatos;
    private JToggleButton btnToggleOnline;
    private JLabel lblStatus;

    // Dados do Usuário
    private String nomeUsuario;
    private int minhaPortaP2P;

    // Componentes de Rede
    private RMIClient rmiClient;
    private P2PServer p2pServer;
    private P2PClient p2pClient;
    private boolean isOnline = false;

    public ChatWindow() {
        configurarUsuario();
        inicializarRede();
        inicializarUI();
    }

    private void configurarUsuario() {
        // Pede o nome e a porta local ao iniciar
        JTextField fieldNome = new JTextField();
        JTextField fieldPorta = new JTextField();
        Object[] message = {
                "Seu Nome de Contato:", fieldNome,
                "Sua Porta Local (P2P):", fieldPorta
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            this.nomeUsuario = fieldNome.getText();
            try {
                this.minhaPortaP2P = Integer.parseInt(fieldPorta.getText());
            } catch (NumberFormatException e) {
                this.minhaPortaP2P = 5000; // Porta padrão se errar
            }
        } else {
            System.exit(0);
        }
    }

    private void inicializarRede() {
        this.p2pClient = new P2PClient();

        // Conecta ao RMI e Garante que a fila existe
        this.rmiClient = new RMIClient();
        this.rmiClient.registrarUsuario(this.nomeUsuario);
    }

    private void inicializarUI() {
        setTitle("Chat PPD - Usuário: " + nomeUsuario + " (Porta: " + minhaPortaP2P + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Painel Esquerdo: Lista de Amigos ---
        JPanel panelEsquerdo = new JPanel(new BorderLayout());
        panelEsquerdo.setBorder(BorderFactory.createTitledBorder("Amigos"));
        panelEsquerdo.setPreferredSize(new Dimension(200, 0));

        modeloListaContatos = new DefaultListModel<>();
        listaContatos = new JList<>(modeloListaContatos);
        panelEsquerdo.add(new JScrollPane(listaContatos), BorderLayout.CENTER);

        JPanel panelBotoesAmigos = new JPanel(new GridLayout(1, 2));
        JButton btnAdd = new JButton("+");
        JButton btnRem = new JButton("-");
        panelBotoesAmigos.add(btnAdd);
        panelBotoesAmigos.add(btnRem);
        panelEsquerdo.add(panelBotoesAmigos, BorderLayout.SOUTH);

        add(panelEsquerdo, BorderLayout.WEST);

        // --- Painel Central: Chat ---
        JPanel panelCentral = new JPanel(new BorderLayout());
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        panelCentral.add(new JScrollPane(areaChat), BorderLayout.CENTER);

        JPanel panelEnvio = new JPanel(new BorderLayout());
        campoMensagem = new JTextField();
        JButton btnEnviar = new JButton("Enviar");
        panelEnvio.add(campoMensagem, BorderLayout.CENTER);
        panelEnvio.add(btnEnviar, BorderLayout.EAST);
        panelCentral.add(panelEnvio, BorderLayout.SOUTH);

        add(panelCentral, BorderLayout.CENTER);

        // --- Painel Superior: Status ---
        JPanel panelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnToggleOnline = new JToggleButton("Ficar Online");
        lblStatus = new JLabel("Status: OFFLINE");
        lblStatus.setForeground(Color.RED);
        panelTopo.add(btnToggleOnline);
        panelTopo.add(lblStatus);
        add(panelTopo, BorderLayout.NORTH);

        // --- Eventos ---

        // Botão Online/Offline
        btnToggleOnline.addActionListener(e -> alternarStatus());

        // Adicionar Amigo
        btnAdd.addActionListener(e -> adicionarContato());

        // Remover Amigo
        btnRem.addActionListener(e -> {
            Contato selecionado = listaContatos.getSelectedValue();
            if (selecionado != null) modeloListaContatos.removeElement(selecionado);
        });

        // Enviar Mensagem
        btnEnviar.addActionListener(e -> enviarMensagem());
        campoMensagem.addActionListener(e -> enviarMensagem()); // Enviar com Enter
    }

    private void alternarStatus() {
        if (btnToggleOnline.isSelected()) {
            // Ficar Online
            isOnline = true;
            lblStatus.setText("Status: ONLINE");
            lblStatus.setForeground(Color.GREEN);
            btnToggleOnline.setText("Ficar Offline");

            // Inicia Servidor P2P
            p2pServer = new P2PServer(minhaPortaP2P, this);
            p2pServer.start();

            // Busca mensagens pendentes no Servidor Offline
            buscarMensagensOffline();

        } else {
            // Ficar Offline
            isOnline = false;
            lblStatus.setText("Status: OFFLINE");
            lblStatus.setForeground(Color.RED);
            btnToggleOnline.setText("Ficar Online");

            // Para o servidor P2P
            if (p2pServer != null) {
                p2pServer.parar();
            }
        }
    }

    private void buscarMensagensOffline() {
        new Thread(() -> {
            try {
                // Recupera lista de strings do servidor
                List<String> pendentes = rmiClient.buscarMensagensPendentes(nomeUsuario);
                SwingUtilities.invokeLater(() -> {
                    if (!pendentes.isEmpty()) {
                        areaChat.append("--- Mensagens Offline Recebidas ---\n");
                        for (String s : pendentes) {
                            areaChat.append(s + "\n");
                        }
                        areaChat.append("-----------------------------------\n");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void adicionarContato() {
        JTextField nomeField = new JTextField();
        JTextField ipField = new JTextField("127.0.0.1");
        JTextField portaField = new JTextField();

        Object[] message = {
                "Nome:", nomeField,
                "IP:", ipField,
                "Porta:", portaField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Adicionar Amigo", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String nome = nomeField.getText();
                String ip = ipField.getText();
                int porta = Integer.parseInt(portaField.getText());
                modeloListaContatos.addElement(new Contato(nome, ip, porta));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dados inválidos!");
            }
        }
    }

    private void enviarMensagem() {
        Contato destinatario = listaContatos.getSelectedValue();
        String texto = campoMensagem.getText();

        if (destinatario == null) {
            JOptionPane.showMessageDialog(this, "Selecione um amigo na lista!");
            return;
        }
        if (texto.isEmpty()) return;

        Mensagem msg = new Mensagem(this.nomeUsuario, destinatario.getNome(), texto);

        // Lógica de Envio
        boolean enviadoP2P = false;

        // Só tenta P2P se eu achar que ele pode estar online
        enviadoP2P = p2pClient.enviarMensagemDireta(destinatario.getIp(), destinatario.getPorta(), msg);

        if (enviadoP2P) {
            areaChat.append("[Eu -> " + destinatario.getNome() + " (P2P)]: " + texto + "\n");
        } else {
            // Falhou P2P -> Envia para Servidor Offline (MOM)
            areaChat.append("[Eu -> " + destinatario.getNome() + " (Offline Server)]: " + texto + "\n");
            rmiClient.enviarMensagemOffline(msg);
        }

        campoMensagem.setText("");
    }

    // Callback recebido do P2PServer
    @Override
    public void onMessageReceived(Mensagem msg) {
        SwingUtilities.invokeLater(() -> {
            areaChat.append("[" + msg.getRemetente() + " disse]: " + msg.getConteudo() + "\n");
        });
    }
}
