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
    // URL padrão do ActiveMQ
    private static final String URL_BROKER = ActiveMQConnection.DEFAULT_BROKER_URL;
    private Session session;
    private Connection connection;

    protected ChatServiceImpl() throws RemoteException {
        super();
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(URL_BROKER);
            factory.setTrustAllPackages(true);
            this.connection = factory.createConnection();
            this.connection.start();
            this.session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao iniciar ActiveMQ: " + e.getMessage());
        }
    }

    @Override
    public void criarFilaUsuario(String nomeUsuario) throws RemoteException {
        // A fila é criada de maneira implícita. Aqui é gerado um log para registro no servidor, mas o JMS cria a fila on-demand
        System.out.println("Servidor: Registrando/Criando fila para o usuário " + nomeUsuario + " ");
    }

    @Override
    public void enviarMensagemOffline(String remetente, String destinatario, String mensagem) throws RemoteException {
        try {
            // A fila terá o nome do destinatário
            Destination destination = session.createQueue(destinatario);
            MessageProducer producer = session.createProducer(destination);
            ObjectMessage message = session.createObjectMessage();

            // Formatamos a mensagem para saber quem mandou
            message.setObject(new Mensagem(remetente, destinatario, mensagem));

            producer.send(message);
            System.out.println("Mensagem offline armazenada para: " + destinatario);

        } catch (JMSException e) {
            throw new RemoteException("Erro JMS ao enviar: " + e.getMessage());
        }
    }

    @Override
    public List<String> recuperarMensagensPendentes(String nomeUsuario) throws RemoteException {
        List<String> mensagensFormatadas = new ArrayList<>();
        MessageConsumer consumer = null;
        try {
            // Acessa a fila do próprio usuário para ler o que tem lá
            Destination destination = session.createQueue(nomeUsuario);
            consumer = session.createConsumer(destination);

//            System.out.println("DEBUG: Buscando mensagens para " + nomeUsuario);

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
                Message message = consumer.receive(1000);

                if (message == null) {
                    break; // Acabaram as mensagens
                }

                if (message != null) {
                    try {
                        if (message instanceof ObjectMessage) {
                            ObjectMessage objMsg = (ObjectMessage) message;
                            Object objeto = objMsg.getObject();

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
            try {
                if (consumer != null) consumer.close();
            } catch (JMSException e) {

            }
        }
        return mensagensFormatadas;
    }

    private void fecharConexao(Connection con) {
        try {
            if (con != null) con.close();
        } catch (JMSException e) {

        }
    }
}
