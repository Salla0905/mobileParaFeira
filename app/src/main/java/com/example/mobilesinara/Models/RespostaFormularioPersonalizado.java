package com.example.mobilesinara.Models;

import java.util.List;

public class RespostaFormularioPersonalizado {
    private String id;
    private List<Respostas> respostas;
    private String data;
    private String idForm;
    private int idOperario;
    private int idEmpresa;

    public RespostaFormularioPersonalizado(List<Respostas> respostas, String data, String idForm, int idOperario, int idEmpresa) {
        this.respostas = respostas;
        this.data = data;
        this.idForm = idForm;
        this.idOperario = idOperario;
        this.idEmpresa = idEmpresa;
    }

    public List<Respostas> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<Respostas> respostas) {
        this.respostas = respostas;
    }
}
