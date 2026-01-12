package org.example.client.network;

import org.example.common.Mensagem;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class P2PClient {
    /**
     * Tenta enviar uma mensagem diretamente para outro cliente (Socket).
     *
     * @param ip       IP do destinatário (ex: "127.0.0.1")
     * @param porta    Porta do destinatário (ex: 5000)
     * @param mensagem Objeto mensagem a ser enviado
     * @return true se enviou com sucesso, false se falhou (destinatário offline ou erro de rede)
     */
    public boolean enviarMensagemDireta(String ip, int porta, Mensagem mensagem) {
        // Requisito 3 e 6: Tenta enviar direto. Se falhar, retorna false para o Controller tentar via RMI.
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
