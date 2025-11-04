package com.example.mobilesinara.Models;

import java.util.Date;

public class FormularioPadrao {
    private String id;
    private int idEmpresa;
    private Date dataPreenchimento;
    private double cloroResidual;
    private double corAguaBruta;
    private double corAguaTratada;
    private double fluoreto;
    private double nitrato;
    private double phAguaBruta;
    private double phAguaTratada;
    private double turbinezAguaBruta;
    private double turbidezAguaTratada;
    private String qualidade;
    private int idFuncionario;
    public FormularioPadrao(){}
    public FormularioPadrao(int idEmpresa, Date dataPreenchimento, double cloroResidual, double corAguaBruta, double corAguaTratada, double fluoreto, double nitrato, double phAguaBruta, double phAguaTratada, double turbinezAguaBruta, double turbidezAguaTratada, String qualidade, int idFuncionario){
        this.idEmpresa = idEmpresa;
        this.dataPreenchimento = dataPreenchimento;
        this.cloroResidual = cloroResidual;
        this.corAguaBruta = corAguaBruta;
        this.corAguaTratada = corAguaTratada;
        this.fluoreto = fluoreto;
        this.nitrato = nitrato;
        this.phAguaBruta = phAguaBruta;
        this.phAguaTratada = phAguaTratada;
        this.turbinezAguaBruta = turbinezAguaBruta;
        this.turbidezAguaTratada = turbidezAguaTratada;
        this.qualidade = qualidade;
        this.idFuncionario = idFuncionario;
    }

    public String getId() {
        return id;
    }

    public Date getDataPreenchimento() {
        return dataPreenchimento;
    }

    public double getCloroResidual() {
        return cloroResidual;
    }

    public double getCorAguaBruta() {
        return corAguaBruta;
    }

    public double getCorAguaTratada() {
        return corAguaTratada;
    }

    public double getFluoreto() {
        return fluoreto;
    }

    public double getNitrato() {
        return nitrato;
    }

    public double getPhAguaBruta() {
        return phAguaBruta;
    }

    public double getPhAguaTratada() {
        return phAguaTratada;
    }

    public double getTurbidezAguaTratada() {
        return turbidezAguaTratada;
    }

    public double getTurbinezAguaBruta() {
        return turbinezAguaBruta;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public int getIdFuncionario() {
        return idFuncionario;
    }

    public String getQualidade() {
        return qualidade;
    }

    public void setCloroResidual(double cloroResidual) {
        this.cloroResidual = cloroResidual;
    }

    public void setCorAguaBruta(double corAguaBruta) {
        this.corAguaBruta = corAguaBruta;
    }

    public void setCorAguaTratada(double corAguaTratada) {
        this.corAguaTratada = corAguaTratada;
    }

    public void setDataPreenchimento(Date dataPreenchimento) {
        this.dataPreenchimento = dataPreenchimento;
    }

    public void setFluoreto(double fluoreto) {
        this.fluoreto = fluoreto;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setIdFuncionario(int idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public void setNitrato(double nitrato) {
        this.nitrato = nitrato;
    }

    public void setPhAguaBruta(double phAguaBruta) {
        this.phAguaBruta = phAguaBruta;
    }

    public void setPhAguaTratada(double phAguaTratada) {
        this.phAguaTratada = phAguaTratada;
    }

    public void setQualidade(String qualidade) {
        this.qualidade = qualidade;
    }

    public void setTurbidezAguaTratada(double turbidezAguaTratada) {
        this.turbidezAguaTratada = turbidezAguaTratada;
    }

    public void setTurbinezAguaBruta(double turbinezAguaBruta) {
        this.turbinezAguaBruta = turbinezAguaBruta;
    }
}
