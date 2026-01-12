package org.example.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mensagem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String remetente;
    private String destinatario;
    private String conteudo;
    private LocalDateTime dataHora;

    public Mensagem() {
        this.dataHora = LocalDateTime.now();
    }

    public Mensagem(String remetente, String destinatario, String conteudo) {
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.conteudo = conteudo;
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters
    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    // Utilizado para formatar a hora na interface
    public String getHoraFormatada() {
        if (dataHora == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dataHora.format(formatter);
    }

    @Override
    public String toString() {
        return "[" + getHoraFormatada() + "] " + remetente + ": " + conteudo;
    }
}
