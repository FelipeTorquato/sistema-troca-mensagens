package org.example.client.network;

import org.example.common.Mensagem;
import org.example.common.MessageListener;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PServer extends Thread {
    private int porta;
    private boolean rodando;
    private ServerSocket serverSocket;
    private MessageListener listener; // Interface para atualizar a GUI

    public P2PServer(int porta, MessageListener listener) {
        this.porta = porta;
        this.listener = listener;
        this.rodando = true;
    }

    public void parar() {
        this.rodando = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            // Ignora erro no fechamento
        }
    }

    @Override
    public void run() {
        try {
            // Cria o socket para escutar conexões diretas
            serverSocket = new ServerSocket(porta);
            System.out.println("P2P Server ouvindo na porta " + porta);

            while (rodando) {
                // Bloqueia até receber uma conexão
                Socket clienteSocket = serverSocket.accept();

                // Processa a mensagem recebida
                try (ObjectInputStream ois = new ObjectInputStream(clienteSocket.getInputStream())) {
                    Object objetoRecebido = ois.readObject();

                    if (objetoRecebido instanceof Mensagem) {
                        Mensagem msg = (Mensagem) objetoRecebido;
                        System.out.println("P2P Recebido: " + msg.getConteudo());

                        // Requisito 3: Mensagem entregue instantaneamente
                        // Avisa a interface gráfica para exibir a mensagem
                        if (listener != null) {
                            listener.onMessageReceived(msg);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar mensagem P2P: " + e.getMessage());
                } finally {
                    clienteSocket.close();
                }
            }
        } catch (Exception e) {
            if (rodando) {
                System.err.println("Erro no P2P Server: " + e.getMessage());
            }
        }
    }
}
