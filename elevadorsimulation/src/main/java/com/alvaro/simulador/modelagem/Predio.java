package com.alvaro.simulador.modelagem;

import java.io.Serializable;

import com.alvaro.simulador.controle.CentralDeControle;
import com.alvaro.simulador.enums.Direcao;
import com.alvaro.simulador.enums.ModeloHeuristica;
import com.alvaro.simulador.enums.TipoPainel;
import com.alvaro.simulador.tads.Lista;

/**
 * Classe que representa um Prédio no sistema de simulação.
 * Ele contém andares, elevadores e uma central de controle responsável
 * por gerenciar chamadas e movimentação.
 */
public class Predio extends EntidadeSimulavel implements Serializable {
    private static final long serialVersionUID = 1L;
    private int numeroAndares;
    private Lista<Andar> andares;
    private CentralDeControle centralDeControle;
    private TipoPainel tipoPainel;
    private Lista<Elevador> elevadores;

    /**
     * Construtor do prédio.
     * Inicializa os andares, elevadores e a central de controle.
     */
    public Predio(int numeroAndares, int numeroElevadores, int capacidadeElevador,
                  TipoPainel tipoPainel, int tempoDeslocamentoPadrao, int tempoDeslocamentoPico,
                  int consumoEnergiaPorDeslocamento, int consumoEnergiaPorParada,
                  ModeloHeuristica modeloHeuristica, int tempoMaximoEspera) {

        if (numeroAndares < 5) {
            throw new IllegalArgumentException("O prédio deve ter pelo menos 5 andares");
        }

        this.numeroAndares = numeroAndares;
        this.andares = new Lista<>();
        this.elevadores = new Lista<>();
        this.tipoPainel = tipoPainel;

        // Cria os andares
        for (int i = 0; i < numeroAndares; i++) {
            Andar andar = new Andar(i, numeroAndares, tipoPainel);
            andares.adicionar(andar);
        }

        // Inicializa a central de controle com os parâmetros fornecidos
        this.centralDeControle = new CentralDeControle(
                numeroElevadores, numeroAndares, tipoPainel,
                capacidadeElevador, tempoDeslocamentoPadrao,
                tempoDeslocamentoPico, consumoEnergiaPorDeslocamento,
                consumoEnergiaPorParada, modeloHeuristica,
                tempoMaximoEspera
        );
        this.centralDeControle.definirPredio(this);
    }

    /**
     * Atualiza o estado do prédio no minuto simulado.
     */
    @Override
    public void atualizar(int minutoSimulado) {
        centralDeControle.atualizar(minutoSimulado);

        // Atualiza todos os andares
        for (int i = 0; i < andares.tamanho(); i++) {
            Andar andar = andares.obter(i);
            andar.atualizar(minutoSimulado);
        }

        // Processa as chamadas de todos os andares
        for (int i = 0; i < andares.tamanho(); i++) {
            Andar andar = andares.obter(i);
            centralDeControle.processarChamadas(andar);
        }

        // Transfere pessoas para os elevadores que estão parados
        Lista<Elevador> elevadores = centralDeControle.getElevadores();
        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);
            int andarAtualElevador = elevador.getAndarAtual();

            if (!elevador.isEmMovimento()) {
                Andar andarAtual = andares.obter(andarAtualElevador);
                centralDeControle.transferirPessoasParaElevador(andarAtual, elevador);
            }
        }
    }

    /**
     * Permite chamar um elevador diretamente para um andar e direção.
     */
    public boolean chamarElevador(int andarOrigem, Direcao direcao) {
        if (andarOrigem < 0 || andarOrigem >= numeroAndares) {
            return false;
        }

        Andar andar = andares.obter(andarOrigem);
        PainelChamadas painelChamadas = andar.getPainelChamadas();

        boolean chamadaRegistrada;

        // Chamada depende do tipo de painel
        if (tipoPainel == TipoPainel.UNICO_BOTAO) {
            chamadaRegistrada = painelChamadas.registrarChamada(Direcao.PARADO, -1);
        } else {
            chamadaRegistrada = painelChamadas.registrarChamada(direcao, -1);
        }

        // Processa a chamada se foi registrada com sucesso
        if (chamadaRegistrada) {
            centralDeControle.processarChamadas(andar);
            return true;
        }

        return false;
    }

    /**
     * Adiciona uma nova pessoa ao andar de origem com destino e características.
     */
    public Pessoa adicionarPessoa(int andarOrigem, int andarDestino, boolean cadeirante,
                                  boolean idoso, int momentoChegada) {
        if (andarOrigem < 0 || andarOrigem >= numeroAndares
                || andarDestino < 0 || andarDestino >= numeroAndares
                || andarOrigem == andarDestino) {
            return null;
        }

        Pessoa pessoa = new Pessoa(andarOrigem, andarDestino, cadeirante, idoso, momentoChegada);
        Andar andarOrigem_obj = andares.obter(andarOrigem);
        andarOrigem_obj.adicionarPessoa(pessoa);

        // Solicita elevador após adicionar pessoa
        centralDeControle.processarChamadas(andarOrigem_obj);

        return pessoa;
    }

    // Getters
    public int getNumeroAndares() {
        return numeroAndares;
    }

    public Andar getAndar(int numero) {
        if (numero < 0 || numero >= numeroAndares) {
            return null;
        }

        return andares.obter(numero);
    }

    public Lista<Andar> getAndares() {
        return andares;
    }

    public CentralDeControle getCentralDeControle() {
        return centralDeControle;
    }

    public TipoPainel getTipoPainel() {
        return tipoPainel;
    }

    /**
     * Exibe o estado atual do prédio: andares, elevadores e pessoas esperando.
     */
    public void exibirEstado() {
        System.out.println("\n=== ESTADO DO PRÉDIO ===");

        for (int i = numeroAndares - 1; i >= 0; i--) {
            Andar andar = andares.obter(i);

            StringBuilder elevadoresNoAndar = new StringBuilder();
            Lista<Elevador> elevadores = centralDeControle.getElevadores();

            for (int j = 0; j < elevadores.tamanho(); j++) {
                Elevador elevador = elevadores.obter(j);

                if (elevador.getAndarAtual() == i) {
                    elevadoresNoAndar.append(String.format("[E%d:%d/%d]",
                            elevador.getId(),
                            elevador.getNumPassageiros(),
                            elevador.getCapacidadeMaxima()));
                } else {
                    elevadoresNoAndar.append("       ");
                }
            }

            System.out.printf("Andar %2d: %s | Pessoas: %d %s\n",
                    i,
                    elevadoresNoAndar.toString(),
                    andar.getNumPessoasEsperando(),
                    andar.getPainelChamadas().toString());
        }

        System.out.println("\nConsumo total de energia: " + centralDeControle.getConsumoEnergiaTotal());

        // Exibe o estado de todos os elevadores
        System.out.println("\n=== ELEVADORES ===");
        Lista<Elevador> elevadores = centralDeControle.getElevadores();
        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);
            System.out.println(elevador.toString());

            if (elevador.getNumPassageiros() > 0) {
                System.out.print("  Passageiros: ");
                for (int j = 0; j < elevador.getPassageiros().tamanho(); j++) {
                    Pessoa passageiro = elevador.getPassageiros().obter(j);
                    System.out.print(passageiro.toString() + " ");
                }
                System.out.println();
            }
        }

        // Exibe pessoas esperando nos andares
        System.out.println("\n=== PESSOAS ESPERANDO ===");
        for (int i = 0; i < andares.tamanho(); i++) {
            Andar andar = andares.obter(i);
            if (andar.temPessoasEsperando()) {
                System.out.printf("Andar %d (%d pessoas): ", i, andar.getNumPessoasEsperando());

                Lista<Pessoa> pessoasEsperando = andar.getPessoasEsperando();
                for (int j = 0; j < pessoasEsperando.tamanho(); j++) {
                    Pessoa pessoa = pessoasEsperando.obter(j);
                    System.out.print(pessoa.toString() + " ");
                }

                System.out.println();
            }
        }

        System.out.println("=====================\n");
    }
}
