package com.example.mobilesinara.Models;

public class Respostas {
    private String campoLabel;
    private String campoTipo;
    private String valor;

    public Respostas(String campoLabel, String campoTipo, String valor) {
        this.campoLabel = campoLabel;
        this.campoTipo = campoTipo;
        this.valor = valor;
    }

    public String getCampoLabel() {
        return campoLabel;
    }

    public String getCampoTipo() {
        return campoTipo;
    }

    public String getValor() {
        return valor;
    }

}
