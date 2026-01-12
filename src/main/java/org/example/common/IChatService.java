package org.example.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IChatService extends Remote {
    void criarFilaUsuario(String nomeUsuario) throws RemoteException;

    void enviarMensagemOffline(String remetente, String destinatario, String mensagem) throws RemoteException;

    List<String> recuperarMensagensPendentes(String nomeUsuario) throws RemoteException;
}
