package org.example.client.ui;

public class Contato {
    private String nome;
    private String ip;
    private int porta;

    public Contato(String nome, String ip, int porta) {
        this.nome = nome;
        this.ip = ip;
        this.porta = porta;
    }

    public String getNome() {
        return nome;
    }

    public String getIp() {
        return ip;
    }

    public int getPorta() {
        return porta;
    }

    @Override
    public String toString() {
        // O que aparece na JList
        return nome + " (" + ip + ":" + porta + ")";
    }
}
