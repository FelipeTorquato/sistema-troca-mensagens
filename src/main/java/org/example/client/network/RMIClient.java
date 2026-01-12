package org.example.client.network;

import org.example.common.IChatService;
import org.example.common.Mensagem;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

public class RMIClient {
    private IChatService chatService;
    private String serverUrl = "rmi://localhost:1099/ChatServidor";

    public RMIClient() {
        try {
            // Conecta ao serviço registrado no ServerMain
            this.chatService = (IChatService) Naming.lookup(serverUrl);
            System.out.println("Conectado ao Servidor RMI com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Requisito 7: Solicita criação de fila ao entrar
    public void registrarUsuario(String nomeUsuario) {
        try {
            if (chatService != null) {
                chatService.criarFilaUsuario(nomeUsuario);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao criar fila: " + e.getMessage());
        }
    }

    // Requisito 6 e 10: Envia mensagem para o servidor offline (ActiveMQ)
    public void enviarMensagemOffline(Mensagem msg) {
        try {
            if (chatService != null) {
                // Adaptamos para passar os dados da classe Mensagem
                // Se alterou a Interface para aceitar (String, String, String), use:
                chatService.enviarMensagemOffline(msg.getRemetente(), msg.getDestinatario(), msg.getConteudo());
                System.out.println("Mensagem enviada para o servidor offline.");
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao enviar mensagem offline: " + e.getMessage());
        }
    }

    // Requisito 9: Recupera mensagens quando volta a ficar Online
    public List<String> buscarMensagensPendentes(String nomeUsuario) {
        try {
            if (chatService != null) {
                return chatService.recuperarMensagensPendentes(nomeUsuario);
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao recuperar pendentes: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
