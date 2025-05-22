package com.alvaro.simulador.graficos;

import java.io.Serializable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import com.alvaro.simulador.controle.Simulador;
import com.alvaro.simulador.enums.ModeloHeuristica;
import com.alvaro.simulador.enums.TipoPainel;

public class ConfiguracaoSimuladorGUI extends JFrame implements Serializable {

    private static final long serialVersionUID = 1L;

    // Spinners para configuração numérica
    private JSpinner spinnerAndares;
    private JSpinner spinnerElevadores;
    private JSpinner spinnerPessoas;
    private JSpinner spinnerCapacidade;
    private JSpinner spinnerTempoDeslocamentoPadrao;
    private JSpinner spinnerVelocidadeSimulacao;
    private JSpinner spinnerTempoLimite;
    private JSpinner spinnerTempoDeslocamentoPico;
    private JSpinner spinnerConsumoEnergiaPorDeslocamento;
    private JSpinner spinnerConsumoEnergiaPorParada;
    private JSpinner spinnerTempoMaximoEspera;

    // Combos para seleção
    private JComboBox<String> comboTipoPainel;
    private JComboBox<String> comboHeuristica;

    // Checkbox para geração automática
    private JCheckBox checkGeracaoAutomatica;
    private JSpinner spinnerIntervaloGeracao;
    private JSpinner spinnerQtdPorGeracao;

    public ConfiguracaoSimuladorGUI() {
        setTitle("Configuração do Simulador de Elevadores");
        setSize(650, 650);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializar componentes
        inicializarComponentes();

        // Mostrar a janela
        setVisible(true);
    }

    private void inicializarComponentes() {
        // Painel principal utilizando BorderLayout
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Painel de configurações em grid
        JPanel painelConfiguracoes = new JPanel(new GridLayout(0, 2, 10, 10));
        painelConfiguracoes.setBorder(BorderFactory.createTitledBorder("Configurações da Simulação"));

        painelConfiguracoes.add(new JLabel("Número de andares:"));
        spinnerAndares = new JSpinner(new SpinnerNumberModel(8, 1, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerAndares);

        painelConfiguracoes.add(new JLabel("Número de elevadores:"));
        spinnerElevadores = new JSpinner(new SpinnerNumberModel(6, 5, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerElevadores);

        painelConfiguracoes.add(new JLabel("Quantidade inicial de pessoas:"));
        spinnerPessoas = new JSpinner(new SpinnerNumberModel(20, 0, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerPessoas);

        painelConfiguracoes.add(new JLabel("Capacidade máxima dos elevadores:"));
        spinnerCapacidade = new JSpinner(new SpinnerNumberModel(6, 1, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerCapacidade);

        painelConfiguracoes.add(new JLabel("Tempo limite (minutos):"));
        spinnerTempoLimite = new JSpinner(new SpinnerNumberModel(150, 10, Integer.MAX_VALUE, 10));
        painelConfiguracoes.add(spinnerTempoLimite);

        painelConfiguracoes.add(new JLabel("Tipo de painel:"));
        comboTipoPainel = new JComboBox<>(new String[]{
                "Único botão",
                "Dois botões (subir/descer)",
                "Painel numérico"
        });
        comboTipoPainel.setSelectedIndex(1); // Dois botões como padrão
        painelConfiguracoes.add(comboTipoPainel);

        painelConfiguracoes.add(new JLabel("Tempo deslocamento padrão (minutos):"));
        spinnerTempoDeslocamentoPadrao = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerTempoDeslocamentoPadrao);

        painelConfiguracoes.add(new JLabel("Tempo deslocamento pico (minutos):"));
        spinnerTempoDeslocamentoPico = new JSpinner(new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerTempoDeslocamentoPico);

        painelConfiguracoes.add(new JLabel("Consumo energia por deslocamento:"));
        spinnerConsumoEnergiaPorDeslocamento = new JSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerConsumoEnergiaPorDeslocamento);

        painelConfiguracoes.add(new JLabel("Consumo energia por parada:"));
        spinnerConsumoEnergiaPorParada = new JSpinner(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));
        painelConfiguracoes.add(spinnerConsumoEnergiaPorParada);

        painelConfiguracoes.add(new JLabel("Tempo máximo de espera (minutos):"));
        spinnerTempoMaximoEspera = new JSpinner(new SpinnerNumberModel(10, 5, Integer.MAX_VALUE, 5));
        painelConfiguracoes.add(spinnerTempoMaximoEspera);

        painelConfiguracoes.add(new JLabel("Modelo de heurística:"));
        comboHeuristica = new JComboBox<>(new String[]{
                "Sem heurística (atendimento na ordem de chegada)",
                "Otimização do tempo de espera",
                "Otimização do consumo de energia"
        });
        comboHeuristica.setSelectedIndex(0); // Otimização de tempo como padrão
        painelConfiguracoes.add(comboHeuristica);

        painelConfiguracoes.add(new JLabel("Velocidade da simulação (ms):"));
        spinnerVelocidadeSimulacao = new JSpinner(new SpinnerNumberModel(1000, 100, Integer.MAX_VALUE, 100));
        painelConfiguracoes.add(spinnerVelocidadeSimulacao);

        // Painel para geração automática
        JPanel painelGeracaoAutomatica = new JPanel(new GridLayout(0, 2, 10, 5));
        painelGeracaoAutomatica.setBorder(BorderFactory.createTitledBorder("Geração Automática de Pessoas"));

        checkGeracaoAutomatica = new JCheckBox("Ativar geração automática de pessoas");
        painelGeracaoAutomatica.add(checkGeracaoAutomatica);
        painelGeracaoAutomatica.add(new JLabel("")); // Placeholder para alinhamento

        painelGeracaoAutomatica.add(new JLabel("Intervalo de geração (ciclos):"));
        spinnerIntervaloGeracao = new JSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1));
        painelGeracaoAutomatica.add(spinnerIntervaloGeracao);

        painelGeracaoAutomatica.add(new JLabel("Quantidade por geração:"));
        spinnerQtdPorGeracao = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
        painelGeracaoAutomatica.add(spinnerQtdPorGeracao);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton botaoIniciar = new JButton("Iniciar Simulação");
        botaoIniciar.setPreferredSize(new Dimension(150, 40));
        botaoIniciar.addActionListener(e -> iniciarSimulacao());

        JButton botaoCancelar = new JButton("Cancelar");
        botaoCancelar.setPreferredSize(new Dimension(100, 40));
        botaoCancelar.addActionListener(e -> System.exit(0));

        painelBotoes.add(botaoIniciar);
        painelBotoes.add(botaoCancelar);

        // Adicionar os painéis ao painel principal
        painelPrincipal.add(painelConfiguracoes, BorderLayout.NORTH);
        painelPrincipal.add(painelGeracaoAutomatica, BorderLayout.CENTER);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        // Adicionar o painel principal à janela
        add(painelPrincipal);
    }

    private void iniciarSimulacao() {
        // Obter valores da configuração
        int numeroAndares = (int) spinnerAndares.getValue();
        int numeroElevadores = (int) spinnerElevadores.getValue();
        int capacidadeElevador = (int) spinnerCapacidade.getValue();
        int quantidadePessoas = (int) spinnerPessoas.getValue();
        int tempoDeslocamentoPadrao = (int) spinnerTempoDeslocamentoPadrao.getValue();
        int tempoDeslocamentoPico = (int) spinnerTempoDeslocamentoPico.getValue();
        int consumoEnergiaPorDeslocamento = (int) spinnerConsumoEnergiaPorDeslocamento.getValue();
        int consumoEnergiaPorParada = (int) spinnerConsumoEnergiaPorParada.getValue();
        int tempoMaximoEspera = (int) spinnerTempoMaximoEspera.getValue();
        int velocidadeSimulacao = (int) spinnerVelocidadeSimulacao.getValue();
        int tempoLimite = (int) spinnerTempoLimite.getValue();
        boolean geracaoAutomatica = checkGeracaoAutomatica.isSelected();
        int intervaloGeracao = (int) spinnerIntervaloGeracao.getValue();
        int qtdPorGeracao = (int) spinnerQtdPorGeracao.getValue();

        // Obter tipo de painel
        TipoPainel tipoPainel;
        switch (comboTipoPainel.getSelectedIndex()) {
            case 0:
                tipoPainel = TipoPainel.UNICO_BOTAO;
                break;
            case 2:
                tipoPainel = TipoPainel.PAINEL_NUMERICO;
                break;
            default:
                tipoPainel = TipoPainel.DOIS_BOTOES;
        }

        // Obter modelo de heurística
        ModeloHeuristica modeloHeuristica;
        switch (comboHeuristica.getSelectedIndex()) {
            case 0:
                modeloHeuristica = ModeloHeuristica.SEM_HEURISTICA;
                break;
            case 2:
                modeloHeuristica = ModeloHeuristica.OTIMIZACAO_CONSUMO_ENERGIA;
                break;
            default:
                modeloHeuristica = ModeloHeuristica.OTIMIZACAO_TEMPO_ESPERA;
        }

        // Criar o simulador com as configurações incluindo tempo limite
        Simulador simulador = new Simulador(
                numeroAndares,
                numeroElevadores,
                capacidadeElevador,
                tipoPainel,
                tempoDeslocamentoPadrao,
                tempoDeslocamentoPico,
                consumoEnergiaPorDeslocamento,
                consumoEnergiaPorParada,
                modeloHeuristica,
                tempoMaximoEspera,
                tempoLimite
        );

        // Fechar esta janela
        this.dispose();

        // Iniciar o simulador GUI com as configurações
        SimuladorGUI simuladorGUI = new SimuladorGUI(simulador);
        simuladorGUI.setVisible(true);

        // Configurar a velocidade da simulação
        simuladorGUI.setVelocidadeSimulacao(velocidadeSimulacao);

        // Configurar geração automática
        if (geracaoAutomatica) {
            simuladorGUI.configurarGeracaoAutomatica(geracaoAutomatica, intervaloGeracao, qtdPorGeracao);
        }

        // Adicionar pessoas iniciais se necessário
        if (quantidadePessoas > 0) {
            simuladorGUI.adicionarPessoasAleatorias(quantidadePessoas);
        }
    }
}