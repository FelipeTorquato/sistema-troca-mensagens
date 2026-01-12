package org.example.server;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.common.IChatService;
import org.example.common.Mensagem;

import javax.jms.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChatServiceImpl extends UnicastRemoteObject implements IChatService {
    // URL padrão do ActiveMQ (verifique se o seu está nesta porta)
    private static final String URL_BROKER = ActiveMQConnection.DEFAULT_BROKER_URL;
    private final ConnectionFactory connectionFactory;

    protected ChatServiceImpl() throws RemoteException {
        super();
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(URL_BROKER);
            factory.setTrustAllPackages(true);
            // Inicializa a conexão com o Middleware Orientado a Mensagens
            this.connectionFactory = factory;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao conectar no ActiveMQ: " + e.getMessage());
        }
    }

    @Override
    public void criarFilaUsuario(String nomeUsuario) throws RemoteException {
        // No ActiveMQ, criar uma fila é implícito ao tentar acessá-la,
        // mas podemos forçar uma conexão para garantir que está tudo OK.
        System.out.println("Servidor: Registrando/Criando fila para o usuário " + nomeUsuario + " ");
        // Lógica de "Dummy send" ou apenas log, pois o JMS cria on-demand.
    }

    @Override
    public void enviarMensagemOffline(String remetente, String destinatario, String mensagem) throws RemoteException {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // A fila terá o nome do destinatário (Requisito 5 e 6 [cite: 11, 13])
            Destination destination = session.createQueue(destinatario);
            MessageProducer producer = session.createProducer(destination);

            ObjectMessage message = session.createObjectMessage();
            // Formatamos a mensagem para saber quem mandou
            message.setObject(new Mensagem(remetente, destinatario, mensagem));

            producer.send(message);
            System.out.println("Mensagem offline armazenada para: " + destinatario);

        } catch (JMSException e) {
            throw new RemoteException("Erro JMS ao enviar: " + e.getMessage());
        } finally {
            fecharConexao(connection);
        }
    }

    @Override
    public List<String> recuperarMensagensPendentes(String nomeUsuario) throws RemoteException {
//        List<String> mensagens = new ArrayList<>();
        List<String> mensagensFormatadas = new ArrayList<>();
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
//            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            // Acessa a fila do próprio usuário para ler o que tem lá
            Destination destination = session.createQueue(nomeUsuario);
            MessageConsumer consumer = session.createConsumer(destination);

            System.out.println("DEBUG: Buscando mensagens para " + nomeUsuario);

//            // Loop para pegar todas as mensagens pendentes
//            while (true) {
//                // receive(1000) espera 1 segundo. Se null, acabou a fila.
//                Message message = consumer.receive(1000);
//                if (message != null && message instanceof TextMessage) {
//                    TextMessage textMessage = (TextMessage) message;
//                    mensagens.add(textMessage.getText());
//                } else {
//                    break;
//                }
//            }
//            System.out.println(nomeUsuario + " recuperou " + mensagens.size() + " mensagens offline.");

            while (true) {
                Message message = consumer.receive(500);
                if (message != null) {
                    try {
                        if (message instanceof ObjectMessage) {
                            ObjectMessage objMsg = (ObjectMessage) message;
                            Object objeto = objMsg.getObject(); // <--- O erro costuma dar AQUI

                            if (objeto instanceof Mensagem) {
                                Mensagem m = (Mensagem) objeto;
                                mensagensFormatadas.add(m.toString());
                                System.out.println("DEBUG: Mensagem recuperada: " + m.getConteudo());
                            } else {
                                System.out.println("DEBUG: Objeto recebido não é 'Mensagem': " + objeto.getClass().getName());
                            }
                        }
                        // Se leu com sucesso, avisa o broker que pode remover a mensagem
                        message.acknowledge();
                    } catch (Exception ex) {
                        System.err.println("DEBUG: Erro ao ler conteúdo da mensagem: " + ex.getMessage());
                        // Não damos acknowledge se der erro, assim ela volta pra fila (ou vai pra DLQ)
                    }
                } else {
                    break;
                }
            }

        } catch (JMSException e) {
            throw new RemoteException("Erro JMS ao receber: " + e.getMessage());
        } finally {
            fecharConexao(connection);
        }
        return mensagensFormatadas;
    }

    private void fecharConexao(Connection con) {
        try {
            if (con != null) con.close();
        } catch (JMSException e) { /* Ignora */ }
    }
}
