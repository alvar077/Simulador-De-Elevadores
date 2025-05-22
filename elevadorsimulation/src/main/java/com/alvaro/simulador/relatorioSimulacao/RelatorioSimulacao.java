package com.alvaro.simulador.relatorioSimulacao;

import java.io.Serializable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alvaro.simulador.controle.Simulador;
import com.alvaro.simulador.modelagem.Andar;
import com.alvaro.simulador.modelagem.Elevador;
import com.alvaro.simulador.modelagem.Pessoa;
import com.alvaro.simulador.modelagem.Predio;
import com.alvaro.simulador.tads.Lista;

public class RelatorioSimulacao implements Serializable {

    private Simulador simulador;
    private StringBuilder relatorio;
    private int totalPessoasTransportadas = 0;
    private int totalPessoasNoSistema = 0;
    private double tempoEsperaMedio = 0;
    private double tempoDeslocamentoMedio = 0;
    private int tempoEsperaMaximo = 0;
    private int tempoDeslocamentoMaximo = 0;
    private int totalTempoEspera = 0;
    private int totalTempoDeslocamento = 0;

    public RelatorioSimulacao(Simulador simulador) {
        this.simulador = simulador;
        this.relatorio = new StringBuilder();
        calcularEstatisticasTempos();
    }

    private void calcularEstatisticasTempos() {
        Predio predio = simulador.getPredio();
        totalPessoasNoSistema = 0;
        totalTempoEspera = 0;
        totalTempoDeslocamento = 0;
        tempoEsperaMaximo = 0;
        tempoDeslocamentoMaximo = 0;

        // 1. Calcular estatísticas para pessoas ainda esperando nos andares
        for (int i = 0; i < predio.getNumeroAndares(); i++) {
            Andar andar = predio.getAndar(i);
            Lista<Pessoa> pessoasEsperando = andar.getPessoasEsperando();

            for (int j = 0; j < pessoasEsperando.tamanho(); j++) {
                Pessoa pessoa = pessoasEsperando.obter(j);
                int tempoEspera = pessoa.getTempoEspera();

                totalTempoEspera += tempoEspera;
                if (tempoEspera > tempoEsperaMaximo) {
                    tempoEsperaMaximo = tempoEspera;
                }

                totalPessoasNoSistema++;
            }
        }

        // 2. Calcular estatísticas para pessoas dentro dos elevadores
        Lista<Elevador> elevadores = predio.getCentralDeControle().getElevadores();
        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);
            Lista<Pessoa> passageiros = elevador.getPassageiros();

            for (int j = 0; j < passageiros.tamanho(); j++) {
                Pessoa pessoa = passageiros.obter(j);
                int tempoEspera = pessoa.getTempoEspera();
                int tempoDeslocamento = pessoa.getTempoDeslocamento();

                totalTempoEspera += tempoEspera;
                totalTempoDeslocamento += tempoDeslocamento;

                if (tempoEspera > tempoEsperaMaximo) {
                    tempoEsperaMaximo = tempoEspera;
                }

                if (tempoDeslocamento > tempoDeslocamentoMaximo) {
                    tempoDeslocamentoMaximo = tempoDeslocamento;
                }

                totalPessoasNoSistema++;
            }
        }

        // Calcular médias se houver pessoas no sistema
        if (totalPessoasNoSistema > 0) {
            tempoEsperaMedio = (double) totalTempoEspera / totalPessoasNoSistema;
            tempoDeslocamentoMedio = (double) totalTempoDeslocamento / totalPessoasNoSistema;
        }
    }

    public String gerarRelatorio() {
        relatorio.setLength(0); // Limpa o relatório

        // Cabeçalho
        relatorio.append("========================================\n");
        relatorio.append("      RELATÓRIO DA SIMULAÇÃO\n");
        relatorio.append("========================================\n\n");

        // Data e hora
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        relatorio.append("Data/Hora: ").append(formatter.format(new Date())).append("\n");
        relatorio.append("Tempo simulado: ").append(simulador.getTempoSimulado()).append(" minutos\n\n");

        // Configurações gerais
        adicionarConfiguracoes();

        // Estatísticas de elevadores
        adicionarEstatisticasElevadores();

        // Estatísticas de pessoas
        adicionarEstatisticasPessoas();

        // Adicionar estatísticas de tempo de espera e deslocamento
        adicionarEstatisticasTempo();

        // Resumo
        adicionarResumo();

        return relatorio.toString();
    }

    private void adicionarConfiguracoes() {
        Predio predio = simulador.getPredio();

        relatorio.append("CONFIGURAÇÕES DA SIMULAÇÃO\n");
        relatorio.append("==========================\n");
        relatorio.append("Número de andares: ").append(predio.getNumeroAndares()).append("\n");
        relatorio.append("Número de elevadores: ").append(predio.getCentralDeControle().getElevadores().tamanho()).append("\n");
        relatorio.append("Tipo de painel: ").append(predio.getTipoPainel()).append("\n");
        relatorio.append("Modelo heurístico: ").append(simulador.getModeloHeuristica()).append("\n\n");
    }

    private void adicionarEstatisticasElevadores() {
        Lista<Elevador> elevadores = simulador.getPredio().getCentralDeControle().getElevadores();

        relatorio.append("ESTATÍSTICAS DOS ELEVADORES\n");
        relatorio.append("===========================\n");

        int totalConsumoEnergia = 0;

        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);
            relatorio.append("Elevador ").append(elevador.getId()).append(":\n");
            relatorio.append("  - Consumo de energia: ").append(elevador.getConsumoEnergiaTotal()).append("\n");
            relatorio.append("  - Passageiros atuais: ").append(elevador.getNumPassageiros()).append("/")
                    .append(elevador.getCapacidadeMaxima()).append("\n");
            relatorio.append("  - Andar atual: ").append(elevador.getAndarAtual()).append("\n\n");

            totalConsumoEnergia += elevador.getConsumoEnergiaTotal();
        }

        relatorio.append("Consumo total de energia: ").append(totalConsumoEnergia).append("\n\n");
    }

    private void adicionarEstatisticasPessoas() {
        Lista<Andar> andares = simulador.getPredio().getAndares();

        relatorio.append("ESTATÍSTICAS DE PESSOAS\n");
        relatorio.append("=======================\n");

        int totalPessoasEsperando = 0;

        for (int i = 0; i < andares.tamanho(); i++) {
            Andar andar = andares.obter(i);
            int numPessoas = andar.getNumPessoasEsperando();
            totalPessoasEsperando += numPessoas;

            if (numPessoas > 0) {
                relatorio.append("Andar ").append(andar.getNumero()).append(": ")
                        .append(numPessoas).append(" pessoa(s) esperando\n");
            }
        }

        relatorio.append("\nTotal de pessoas ainda esperando: ").append(totalPessoasEsperando).append("\n\n");
    }

    private void adicionarEstatisticasTempo() {
        relatorio.append("ESTATÍSTICAS DE TEMPO\n");
        relatorio.append("====================\n");
        relatorio.append(String.format("Tempo médio de espera: %.2f minutos\n", tempoEsperaMedio));
        relatorio.append(String.format("Tempo médio de deslocamento: %.2f minutos\n", tempoDeslocamentoMedio));
        relatorio.append("Tempo máximo de espera: ").append(tempoEsperaMaximo).append(" minutos\n");
        relatorio.append("Tempo máximo de deslocamento: ").append(tempoDeslocamentoMaximo).append(" minutos\n\n");
    }

    private void adicionarResumo() {
        relatorio.append("RESUMO DA SIMULAÇÃO\n");
        relatorio.append("===================\n");
        relatorio.append("Duração total: ").append(simulador.getTempoSimulado()).append(" minutos\n");
        relatorio.append("Consumo total de energia: ")
                .append(simulador.getPredio().getCentralDeControle().getConsumoEnergiaTotal()).append("\n");
        relatorio.append("Total de pessoas no sistema: ").append(totalPessoasNoSistema).append("\n");

        // Calcula a utilização dos elevadores
        Lista<Elevador> elevadores = simulador.getPredio().getCentralDeControle().getElevadores();
        double utilizacaoMedia = 0;

        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);
            double utilizacao = (double) elevador.getNumPassageiros() / elevador.getCapacidadeMaxima();
            utilizacaoMedia += utilizacao;
        }

        utilizacaoMedia = (utilizacaoMedia / elevadores.tamanho()) * 100;
        relatorio.append("Utilização média dos elevadores: ").append(String.format("%.1f", utilizacaoMedia)).append("%\n");
    }

    public void salvarRelatorio(String nomeArquivo) throws IOException {
        FileWriter writer = new FileWriter(new File(nomeArquivo));
        writer.write(gerarRelatorio());
        writer.close();
    }
}