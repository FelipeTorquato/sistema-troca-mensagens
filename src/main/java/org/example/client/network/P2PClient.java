package org.example.client.network;

import org.example.common.Mensagem;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class P2PClient {
    public boolean enviarMensagemDireta(String ip, int porta, Mensagem mensagem) {
        // Tenta enviar direto. Se falhar, retorna false para o Controller tentar via RMI.
        try (Socket socket = new Socket(ip, porta);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(mensagem);
            out.flush();
            System.out.println("Mensagem P2P enviada para " + ip + ":" + porta);
            return true;

        } catch (Exception e) {
            System.out.println("Falha no envio P2P (Usuário pode estar offline): " + e.getMessage());
            return false;
        }
    }
}
