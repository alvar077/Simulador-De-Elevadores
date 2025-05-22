package com.alvaro.simulador.modelagem;

import java.io.Serializable;

public class Pessoa implements Serializable {
    private static int contadorGlobal = 0;

    private int id;
    private int andarOrigem;
    private int andarDestino;
    private boolean cadeirante;
    private boolean idoso;
    private int tempoEspera;
    private int momentoChegada;
    private boolean dentroDoElevador;
    private int elevadorAtual;
    private int inicioDeslocamento;
    private int tempoDeslocamento;

    public Pessoa(int andarOrigem, int andarDestino, boolean cadeirante, boolean idoso, int momentoChegada) {
        this.id = ++contadorGlobal;
        this.andarOrigem = andarOrigem;
        this.andarDestino = andarDestino;
        this.cadeirante = cadeirante;
        this.idoso = idoso;
        this.momentoChegada = momentoChegada;
        this.tempoEspera = 0;
        this.dentroDoElevador = false;
        this.elevadorAtual = -1;
        this.inicioDeslocamento = -1;
        this.tempoDeslocamento = 0;
    }

    public void incrementarTempoEspera() {
        if (!dentroDoElevador) {
            tempoEspera++;
        } else {
            // Se estiver no elevador, incrementa o tempo de deslocamento
            tempoDeslocamento++;
        }
    }

    public void entrarNoElevador(int numeroElevador) {
        this.dentroDoElevador = true;
        this.elevadorAtual = numeroElevador;
        this.inicioDeslocamento = tempoEspera;
    }

    public void sairDoElevador() {
        this.dentroDoElevador = false;
        this.elevadorAtual = -1;
    }

    // Adicionar este novo método para obter o tempo de deslocamento
    public int getTempoDeslocamento() {
        return tempoDeslocamento;
    }

    public int calcularPrioridade() {
        int prioridade = 0;

        if (cadeirante) {
            prioridade += 2;
        }

        if (idoso) {
            prioridade += 1;
        }

        return prioridade;
    }

    public int getId() {
        return id;
    }

    public int getAndarOrigem() {
        return andarOrigem;
    }

    public int getAndarDestino() {
        return andarDestino;
    }

    public boolean isCadeirante() {
        return cadeirante;
    }

    public boolean isIdoso() {
        return idoso;
    }

    public int getTempoEspera() {
        return tempoEspera;
    }

    public int getMomentoChegada() {
        return momentoChegada;
    }

    public boolean isDentroDoElevador() {
        return dentroDoElevador;
    }

    public int getElevadorAtual() {
        return elevadorAtual;
    }

    public String getTipoString() {
        StringBuilder tipo = new StringBuilder();

        if (cadeirante) {
            tipo.append("◊");
        }

        if (idoso) {
            tipo.append("♦");
        }

        if (tipo.length() == 0) {
            tipo.append("•");
        }

        return tipo.toString();
    }

    @Override
    public String toString() {
        return String.format("P%d%s[%d→%d]", id, getTipoString(), andarOrigem, andarDestino);
    }
}