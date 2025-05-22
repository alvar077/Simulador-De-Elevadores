package com.alvaro.simulador.graficos;

import java.io.Serializable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.alvaro.simulador.controle.Simulador;
import com.alvaro.simulador.enums.TipoPainel;
import com.alvaro.simulador.modelagem.Andar;
import com.alvaro.simulador.modelagem.Elevador;
import com.alvaro.simulador.modelagem.Pessoa;
import com.alvaro.simulador.modelagem.Predio;
import com.alvaro.simulador.relatorioSimulacao.RelatorioSimulacao;
import com.alvaro.simulador.tads.Lista;
import com.alvaro.simulador.graficos.Logs;

public class SimuladorGUI extends JFrame implements Serializable{

    private static final long serialVersionUID = 1L;
    private Simulador simulador;
    private JPanel painelPredio;
    private JPanel painelEstatisticas;
    private JLabel labelTempo;
    private JLabel labelEnergia;
    private JLabel labelEspera;
    private JLabel labelGeracaoAutomatica;
    private JLabel labelStatus;
    private JLabel labelTempoLimite;
    private JButton botaoIniciarPausar;
    private Timer timerSimulacao;
    private Timer timerPessoas;
    private Random random = new Random();
    private boolean geracaoAutomatica = false;
    private int intervaloGeracaoPessoas = 10;
    private int quantidadePessoasPorGeracao = 2;
    private boolean simulacaoIniciada = false;
    private JTextArea areaLog;
    private JScrollPane scrollPaneLog;
    private Logs logs;

    public SimuladorGUI(Simulador simulador) {
        this.simulador = simulador;
        inicializarGUI();
    }

    private void inicializarGUI() {
        configurarJanela();
        criarPaineis();
        criarBotoesPrincipais();
        criarMenu();
        criarTimers();
        setVisible(true);
        setSize(1200, 650);
    }

    private void configurarJanela() {
        setTitle("Simulador de Elevadores");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void criarPaineis() {
        // Painel principal do prédio
        painelPredio = new JPanel();
        painelPredio.setLayout(new GridLayout(simulador.getPredio().getNumeroAndares(), 1, 0, 5));
        painelPredio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel de estatísticas
        painelEstatisticas = new JPanel();
        painelEstatisticas.setLayout(new GridLayout(1, 6, 10, 0));
        painelEstatisticas.setBorder(BorderFactory.createTitledBorder("Estatísticas"));

        labelTempo = new JLabel("Tempo: 0 min");
        labelEnergia = new JLabel("Energia: 0");
        labelEspera = new JLabel("Pessoas esperando: 0");
        labelGeracaoAutomatica = new JLabel("Geração automática: Desativada");
        labelStatus = new JLabel("Status: Parado");
        labelTempoLimite = new JLabel("Limite: - min");

        JPanel painelLog = new JPanel(new BorderLayout());
        painelLog.setBorder(BorderFactory.createTitledBorder("Log de Eventos"));

        areaLog = new JTextArea(8, 50);
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPaneLog = new JScrollPane(areaLog);
        scrollPaneLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        painelLog.add(scrollPaneLog, BorderLayout.CENTER);
        add(painelLog, BorderLayout.EAST);

// Configurar o manipulador de log personalizado
        logs = new Logs(areaLog);
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(logs);
        Logger simuladorLogger = Logger.getLogger("com.alvaro.simulador");
        simuladorLogger.setLevel(Level.INFO);

        painelEstatisticas.add(labelTempo);
        painelEstatisticas.add(labelEnergia);
        painelEstatisticas.add(labelEspera);
        painelEstatisticas.add(labelGeracaoAutomatica);
        painelEstatisticas.add(labelStatus);
        painelEstatisticas.add(labelTempoLimite);

        add(painelPredio, BorderLayout.CENTER);
        add(painelEstatisticas, BorderLayout.SOUTH);

        atualizarVisualizacao();
    }

    private void criarBotoesPrincipais() {
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        botaoIniciarPausar = new JButton("Iniciar");
        botaoIniciarPausar.addActionListener(e -> alternarEstadoSimulacao());

        JButton botaoEncerrar = new JButton("Encerrar");
        botaoEncerrar.addActionListener(e -> encerrarSimulacao());

        JButton botaoSalvar = new JButton("Salvar");
        botaoSalvar.addActionListener(e -> salvarSimulacao());

        JButton botaoCarregar = new JButton("Carregar");
        botaoCarregar.addActionListener(e -> carregarSimulacao());

        JButton botaoRelatorio = new JButton("Gerar Relatório");
        botaoRelatorio.addActionListener(e -> gerarRelatorio());

        JButton botaoConfigurar = new JButton("Configurar");
        botaoConfigurar.addActionListener(e -> voltarParaTelaConfiguracao());

        JButton botaoLimparLog = new JButton("Limpar Log");
        botaoLimparLog.addActionListener(e -> limparLog());


        painelBotoes.add(botaoIniciarPausar);
        painelBotoes.add(botaoEncerrar);
        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoCarregar);
        painelBotoes.add(botaoRelatorio);
        painelBotoes.add(botaoConfigurar);
        painelBotoes.add(botaoLimparLog);


        add(painelBotoes, BorderLayout.NORTH);
    }

    private void criarMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Simulação
        JMenu menuSimulacao = new JMenu("Simulação");

        adicionarItemMenu(menuSimulacao, "Iniciar/Pausar", KeyEvent.VK_F5, 0, e -> alternarEstadoSimulacao());
        adicionarItemMenu(menuSimulacao, "Encerrar Simulação", KeyEvent.VK_F6, 0, e -> encerrarSimulacao());

        menuSimulacao.addSeparator();

        adicionarItemMenu(menuSimulacao, "Executar Um Ciclo", KeyEvent.VK_F7, 0, e -> executarCicloUnico());
        adicionarItemMenu(menuSimulacao, "Executar Múltiplos Ciclos", -1, 0, e -> executarMultiplosCiclos());

        menuSimulacao.addSeparator();

        adicionarItemMenu(menuSimulacao, "Salvar Simulação", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, e -> salvarSimulacao());
        adicionarItemMenu(menuSimulacao, "Carregar Simulação", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, e -> carregarSimulacao());

        menuSimulacao.addSeparator();

        adicionarItemMenu(menuSimulacao, "Gerar Relatório", KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, e -> gerarRelatorio());

        menuSimulacao.addSeparator();

        adicionarItemMenu(menuSimulacao, "Sair", KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK, e -> System.exit(0));

        // Menu Configurações
        JMenu menuConfiguracoes = new JMenu("Configurações");
        adicionarItemMenu(menuConfiguracoes, "Velocidade de Simulação", -1, 0, e -> configurarVelocidade());

        menuBar.add(menuSimulacao);
        menuBar.add(menuConfiguracoes);
        setJMenuBar(menuBar);
    }

    private void adicionarItemMenu(JMenu menu, String titulo, int tecla, int modificador, java.awt.event.ActionListener acao) {
        JMenuItem item = new JMenuItem(titulo);
        if (tecla != -1) {
            item.setAccelerator(KeyStroke.getKeyStroke(tecla, modificador));
        }
        item.addActionListener(acao);
        menu.add(item);
    }

    private void criarTimers() {
        timerSimulacao = new Timer(1000, e -> {
            simulador.executarCiclo();
            atualizarVisualizacao();
        });

        timerPessoas = new Timer(1000, e -> {
            if (simulador.getTempoSimulado() % intervaloGeracaoPessoas == 0) {
                adicionarPessoasAleatorias(quantidadePessoasPorGeracao);
            }
        });
    }

    private void alternarEstadoSimulacao() {
        // Verifica se a simulação já terminou por tempo limite
        if (simulador.getTempoSimulado() >= simulador.getTempoLimiteSimulacao()) {
            JOptionPane.showMessageDialog(this,
                    "A simulação já atingiu o tempo limite. Por favor, encerre e inicie uma nova simulação.",
                    "Simulação Finalizada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!simulacaoIniciada) {
            iniciarSimulacao();
            simulacaoIniciada = true;
            botaoIniciarPausar.setText("Pausar");
        } else if (simulador.isSimulacaoAtiva()) {
            pausarSimulacao();
            botaoIniciarPausar.setText("Retomar");
        } else {
            iniciarSimulacao();
            botaoIniciarPausar.setText("Pausar");
        }
    }

    private void iniciarSimulacao() {
        if (!simulador.isSimulacaoAtiva()) {
            simulador.iniciarSimulacao();
            timerSimulacao.start();
            if (geracaoAutomatica) {
                timerPessoas.start();
            }
            labelStatus.setText("Status: Em execução");
        }
    }

    private void pausarSimulacao() {
        if (simulador.isSimulacaoAtiva()) {
            simulador.pararSimulacao();
            timerSimulacao.stop();
            timerPessoas.stop();
            labelStatus.setText("Status: Pausado");
        }
    }

    private void encerrarSimulacao() {
        timerSimulacao.stop();
        timerPessoas.stop();

        try {
            simulador.pararSimulacao();

            // Obter o tempo limite atual
            int tempoLimiteAtual = simulador.getTempoLimiteSimulacao();

            // Recria simulador com mesmas configurações
            Predio predio = simulador.getPredio();
            Elevador primeiroElevador = predio.getCentralDeControle().getElevadores().obter(0);

            simulador = new Simulador(
                    predio.getNumeroAndares(),
                    predio.getCentralDeControle().getElevadores().tamanho(),
                    primeiroElevador.getCapacidadeMaxima(),
                    predio.getTipoPainel(),
                    2, 4, 8, 3, // valores padrão
                    simulador.getModeloHeuristica(),
                    30,
                    tempoLimiteAtual // Mantém o mesmo tempo limite
            );

            simulacaoIniciada = false;
            botaoIniciarPausar.setText("Iniciar");
            botaoIniciarPausar.setEnabled(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao reiniciar a simulação: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        atualizarVisualizacao();
        labelStatus.setText("Status: Encerrado");
    }

    private void salvarSimulacao() {
        boolean estaAtiva = simulador.isSimulacaoAtiva();
        if (estaAtiva) {
            pausarSimulacao();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Simulação");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Simulação (*.sim)", "sim"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getAbsolutePath().endsWith(".sim")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".sim");
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo))) {
                oos.writeObject(simulador);
                JOptionPane.showMessageDialog(this, "Simulação salva com sucesso!",
                        "Salvar Simulação", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (estaAtiva) {
            iniciarSimulacao();
        }
    }

    private void carregarSimulacao() {
        pausarSimulacao();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Carregar Simulação");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Simulação (*.sim)", "sim"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(fileChooser.getSelectedFile()))) {
                simulador = (Simulador) ois.readObject();
                atualizarVisualizacao();
                labelStatus.setText("Status: Carregado");
                simulacaoIniciada = false;
                botaoIniciarPausar.setText("Iniciar");
                botaoIniciarPausar.setEnabled(true);

                // Exibe informação sobre o tempo limite
                JOptionPane.showMessageDialog(this,
                        "Simulação carregada com sucesso!\nTempo limite: " + simulador.getTempoLimiteSimulacao() + " minutos",
                        "Carregar Simulação", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executarCicloUnico() {
        simulador.executarCiclo();
        atualizarVisualizacao();
    }

    private void executarMultiplosCiclos() {
        String input = JOptionPane.showInputDialog(this,
                "Digite o número de ciclos a executar:", "Executar Ciclos", JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int ciclos = Integer.parseInt(input.trim());
                if (ciclos > 0) {
                    simulador.executarCiclos(ciclos);
                    atualizarVisualizacao();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um número válido.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void configurarVelocidade() {
        String[] opcoes = {"Lenta (2s)", "Normal (1s)", "Rápida (0.5s)", "Muito rápida (0.1s)"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Selecione a velocidade de simulação:", "Velocidade de Simulação",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[1]);

        if (escolha >= 0) {
            int[] delays = {2000, 1000, 500, 100};
            setVelocidadeSimulacao(delays[escolha]);
        }
    }

    public void setVelocidadeSimulacao(int velocidadeMs) {
        timerSimulacao.setDelay(velocidadeMs);
    }

    public void configurarGeracaoAutomatica(boolean ativar, int intervalo, int quantidade) {
        geracaoAutomatica = ativar;
        intervaloGeracaoPessoas = intervalo;
        quantidadePessoasPorGeracao = quantidade;
        labelGeracaoAutomatica.setText("Geração automática: " + (geracaoAutomatica ? "Ativada" : "Desativada"));
    }

    private void atualizarVisualizacao() {
        // Atualizar estatísticas
        int tempoAtual = simulador.getTempoSimulado();
        int tempoLimite = simulador.getTempoLimiteSimulacao();

        labelTempo.setText("Tempo: " + tempoAtual + " min");
        labelEnergia.setText("Energia: " + simulador.getPredio().getCentralDeControle().getConsumoEnergiaTotal());

        // Contar pessoas esperando
        int totalPessoasEsperando = 0;
        for (int i = 0; i < simulador.getPredio().getNumeroAndares(); i++) {
            totalPessoasEsperando += simulador.getPredio().getAndar(i).getNumPessoasEsperando();
        }
        labelEspera.setText("Pessoas esperando: " + totalPessoasEsperando);

        if (simulador.getTempoSimulado() % 10 == 0) {
            registrarEvento("Minuto " + simulador.getTempoSimulado() + ": " +
                    totalPessoasEsperando + " pessoas esperando");
        }

        // Atualização do tempo limite
        int tempoRestante = tempoLimite - tempoAtual;
        labelTempoLimite.setText("Limite: " + tempoLimite + " min (" + tempoRestante + " restantes)");

        // Mudar cor baseado no tempo restante
        double percentualRestante = (double) tempoRestante / tempoLimite;
        if (percentualRestante <= 0.1) { // 10% ou menos
            labelTempoLimite.setForeground(Color.RED);
        } else if (percentualRestante <= 0.25) { // 25% ou menos
            labelTempoLimite.setForeground(Color.ORANGE);
        } else {
            labelTempoLimite.setForeground(Color.BLACK);
        }

        // Verificação para parar quando atinge o limite
        if (tempoAtual >= tempoLimite) {
            timerSimulacao.stop();
            timerPessoas.stop();
            simulador.pararSimulacao();
            botaoIniciarPausar.setText("Encerrada");
            botaoIniciarPausar.setEnabled(false);
            labelStatus.setText("Status: Encerrado (limite atingido)");
            registrarEvento("SIMULAÇÃO ENCERRADA: Tempo limite atingido");

            // Oferecer para gerar relatório automaticamente
            int resposta = JOptionPane.showConfirmDialog(this,
                    "A simulação atingiu o tempo limite de " + tempoLimite + " minutos. Deseja gerar o relatório?",
                    "Simulação Encerrada", JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                gerarRelatorio();
            }
        }

        // Reconstruir visualização do prédio
        painelPredio.removeAll();

        for (int i = simulador.getPredio().getNumeroAndares() - 1; i >= 0; i--) {
            painelPredio.add(criarPainelAndar(i));
        }

        painelPredio.revalidate();
        painelPredio.repaint();
    }

    private JPanel criarPainelAndar(int numeroAndar) {
        Andar andar = simulador.getPredio().getAndar(numeroAndar);
        Lista<Elevador> elevadores = simulador.getPredio().getCentralDeControle().getElevadores();

        JPanel painelAndar = new JPanel(new BorderLayout());
        painelAndar.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Label do andar
        String nomeAndar = numeroAndar == 0 ? "Térreo" : "Andar " + numeroAndar;
        JLabel labelAndar = new JLabel(nomeAndar);
        labelAndar.setPreferredSize(new Dimension(80, 40));
        labelAndar.setHorizontalAlignment(JLabel.CENTER);
        painelAndar.add(labelAndar, BorderLayout.WEST);

        // Painel dos elevadores
        JPanel painelElevadores = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);
            painelElevadores.add(criarPainelElevador(elevador, numeroAndar));
        }

        painelAndar.add(painelElevadores, BorderLayout.CENTER);

        // Painel de pessoas esperando
        painelAndar.add(criarPainelPessoas(andar, numeroAndar), BorderLayout.EAST);

        return painelAndar;
    }

    private JPanel criarPainelElevador(Elevador elevador, int numeroAndar) {
        JPanel painelElevador = new JPanel();
        painelElevador.setPreferredSize(new Dimension(80, 40));
        painelElevador.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        if (elevador.getAndarAtual() == numeroAndar) {
            painelElevador.setBackground(elevador.isEmMovimento() ? Color.YELLOW : Color.GREEN);

            painelElevador.add(new JLabel("E" + elevador.getId()));
            painelElevador.add(new JLabel(elevador.getNumPassageiros() + "/" + elevador.getCapacidadeMaxima()));

            String direcao = "";
            switch (elevador.getDirecao()) {
                case SUBINDO:
                    direcao = "↑";
                    break;
                case DESCENDO:
                    direcao = "↓";
                    break;
                default:
                    direcao = "•";
                    break;
            }
            painelElevador.add(new JLabel(direcao));
        } else {
            painelElevador.setBackground(Color.LIGHT_GRAY);
            painelElevador.add(new JLabel("│"));
        }

        return painelElevador;
    }

    private JPanel criarPainelPessoas(Andar andar, int numeroAndar) {
        JPanel painelPessoas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelPessoas.setPreferredSize(new Dimension(300, 40));
        painelPessoas.setBorder(BorderFactory.createTitledBorder("Esperando"));

        if (andar.temPessoasEsperando()) {
            TipoPainel tipoPainel = andar.getPainelChamadas().getTipoPainel();
            StringBuilder pessoasStr = new StringBuilder();
            Lista<Pessoa> pessoas = andar.getPessoasEsperando();

            for (int i = 0; i < pessoas.tamanho(); i++) {
                Pessoa pessoa = pessoas.obter(i);

                // Tipo de pessoa
                pessoasStr.append(pessoa.isCadeirante() ? "◊" : (pessoa.isIdoso() ? "♦" : "•"));

                // Destino/direção
                if (tipoPainel == TipoPainel.PAINEL_NUMERICO) {
                    pessoasStr.append("→").append(pessoa.getAndarDestino());
                } else if (tipoPainel == TipoPainel.DOIS_BOTOES) {
                    pessoasStr.append(pessoa.getAndarDestino() > numeroAndar ? "↑" : "↓");
                }

                if (i < pessoas.tamanho() - 1) {
                    pessoasStr.append(" ");
                }
            }

            painelPessoas.add(new JLabel(pessoasStr.toString()));
            painelPessoas.add(new JLabel(" (" + pessoas.tamanho() + ")"));
        } else {
            painelPessoas.add(new JLabel("Vazio"));
        }

        return painelPessoas;
    }

    public void adicionarPessoasAleatorias(int quantidade) {
        int numeroAndares = simulador.getPredio().getNumeroAndares();

        for (int i = 0; i < quantidade; i++) {
            int andarOrigem = random.nextInt(numeroAndares);
            int andarDestino;

            do {
                andarDestino = random.nextInt(numeroAndares);
            } while (andarDestino == andarOrigem);

            boolean cadeirante = random.nextDouble() < 0.1;
            boolean idoso = random.nextDouble() < 0.2;

            simulador.adicionarPessoa(andarOrigem, andarDestino, cadeirante, idoso);

            Pessoa pessoa = simulador.adicionarPessoa(andarOrigem, andarDestino, cadeirante, idoso);
            if (pessoa != null) {
                String tipoEspecial = "";
                if (cadeirante) tipoEspecial += " (cadeirante)";
                if (idoso) tipoEspecial += " (idoso)";

                registrarEvento("Nova pessoa" + tipoEspecial + " do andar " + andarOrigem +
                        " para o andar " + andarDestino);
            }
        }

        atualizarVisualizacao();
    }

    private void gerarRelatorio() {
        // Pausa a simulação temporariamente se estiver em execução
        boolean estaAtiva = simulador.isSimulacaoAtiva();
        if (estaAtiva) {
            pausarSimulacao();
        }

        try {
            RelatorioSimulacao relatorio = new RelatorioSimulacao(simulador);
            String conteudoRelatorio = relatorio.gerarRelatorio();

            // Mostrar em uma janela de diálogo
            JTextArea areaTexto = new JTextArea(conteudoRelatorio);
            areaTexto.setEditable(false);
            areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(areaTexto);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Relatório da Simulação", JOptionPane.INFORMATION_MESSAGE);

            // Pergunta se quer salvar o relatório
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Deseja salvar o relatório em um arquivo?",
                    "Salvar Relatório", JOptionPane.YES_NO_OPTION);

            if (resposta == JOptionPane.YES_OPTION) {
                salvarRelatorioEmArquivo(relatorio);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar relatório: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        // Retoma a simulação se estava ativa
        if (estaAtiva) {
            iniciarSimulacao();
        }
    }

    private void salvarRelatorioEmArquivo(RelatorioSimulacao relatorio) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt"));

        // Define nome padrão com timestamp
        String nomeArquivoPadrao = "relatorio_simulacao_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
        fileChooser.setSelectedFile(new File(nomeArquivoPadrao));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getAbsolutePath().endsWith(".txt")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".txt");
            }

            try {
                relatorio.salvarRelatorio(arquivo.getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                        "Relatório salvo com sucesso!",
                        "Salvar Relatório", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao salvar o relatório: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void voltarParaTelaConfiguracao() {
        // Verifica se a simulação está ativa e pergunta ao usuário se deseja realmente voltar
        if (simulacaoIniciada) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "A simulação será encerrada. Deseja voltar para a tela de configuração?",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Primeiro para qualquer timer que esteja rodando
        if (timerSimulacao.isRunning()) {
            timerSimulacao.stop();
        }

        if (timerPessoas.isRunning()) {
            timerPessoas.stop();
        }

        // Define simulação como não ativa (em vez de chamar pararSimulacao())
        simulador.pararSimulacao();
        simulacaoIniciada = false;

        // Fecha a janela atual
        this.dispose();

        // Inicia a nova tela de configuração diretamente, sem usar SwingUtilities
        try {
            new ConfiguracaoSimuladorGUI();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Erro ao abrir a tela de configuração: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void registrarEvento(String mensagem) {
        if (logs != null) {
            Logger logger = Logger.getLogger(SimuladorGUI.class.getName());
            logger.info(mensagem);
        }
    }

    private void limparLog() {
        if (areaLog != null) {
            areaLog.setText("");
        }
    }

}
