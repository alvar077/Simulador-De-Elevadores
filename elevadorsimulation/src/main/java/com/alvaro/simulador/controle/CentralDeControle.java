package com.alvaro.simulador.controle;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.alvaro.simulador.enums.Direcao;
import com.alvaro.simulador.enums.ModeloHeuristica;
import com.alvaro.simulador.enums.TipoPainel;
import com.alvaro.simulador.modelagem.Andar;
import com.alvaro.simulador.modelagem.Elevador;
import com.alvaro.simulador.modelagem.EntidadeSimulavel;
import com.alvaro.simulador.modelagem.PainelChamadas;
import com.alvaro.simulador.modelagem.Pessoa;
import com.alvaro.simulador.tads.Lista;
import com.alvaro.simulador.modelagem.Predio;

public class CentralDeControle extends EntidadeSimulavel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CentralDeControle.class.getName());

    private Lista<Elevador> elevadores;
    private ModeloHeuristica modeloHeuristica;
    private int tempoMaximoEspera;
    private int numeroTotalAndares;

    // Matriz para rastrear qual elevador está designado para cada andar/direção
    // [andar][direcao] = id do elevador (ou -1 se nenhum)
    private int[][] elevadorDesignadoPara;

    // Lista de andares com chamadas pendentes
    private Lista<Integer> andaresPendentes;

    // Contador de ciclos para distribuição alternada
    private int contadorCiclos;

    // Novos campos para suporte às heurísticas
    private int[][] temposEsperaAndares;  // Armazena tempo de espera por andar
    private int[] ultimoAndarVisitado;    // Último andar visitado por cada elevador

    public CentralDeControle(int numeroElevadores, int numeroTotalAndares, TipoPainel tipoPainel,
                             int capacidadeMaxima, int tempoDeslocamentoPadrao, int tempoDeslocamentoPico,
                             int consumoEnergiaPorDeslocamento, int consumoEnergiaPorParada,
                             ModeloHeuristica modeloHeuristica, int tempoMaximoEspera) {
        this.elevadores = new Lista<>();
        this.modeloHeuristica = modeloHeuristica;
        this.tempoMaximoEspera = tempoMaximoEspera;
        this.numeroTotalAndares = numeroTotalAndares;
        this.contadorCiclos = 0;

        // Inicializar matriz de controle
        this.elevadorDesignadoPara = new int[numeroTotalAndares][3];

        // Inicializar com -1 (nenhum elevador designado)
        for (int a = 0; a < numeroTotalAndares; a++) {
            for (int d = 0; d < 3; d++) {
                elevadorDesignadoPara[a][d] = -1;
            }
        }

        this.andaresPendentes = new Lista<>();

        // Inicialização dos novos campos para heurísticas
        this.temposEsperaAndares = new int[numeroTotalAndares][2]; // [andar][0=subindo, 1=descendo]
        this.ultimoAndarVisitado = new int[numeroElevadores];

        for (int i = 0; i < numeroElevadores; i++) {
            Elevador elevador = new Elevador(i, numeroTotalAndares, tipoPainel, capacidadeMaxima,
                    tempoDeslocamentoPadrao, tempoDeslocamentoPico,
                    consumoEnergiaPorDeslocamento, consumoEnergiaPorParada,
                    modeloHeuristica);
            elevadores.adicionar(elevador);
            ultimoAndarVisitado[i] = 0; // Inicia no térreo
        }
    }

    @Override
    public void atualizar(int minutoSimulado) {
        contadorCiclos++;

        // Atualizar matriz de controle
        atualizarMapaDesignacoes();

        // Atualizar tempos de espera por andar
        atualizarTemposEspera();

        // Atualizar cada elevador
        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);

            // Atualizar o último andar visitado se o elevador parou em um andar
            if (!elevador.isEmMovimento() && elevador.getAndarAtual() != ultimoAndarVisitado[elevador.getId()]) {
                ultimoAndarVisitado[elevador.getId()] = elevador.getAndarAtual();
            }

            elevador.atualizar(minutoSimulado);
        }

        // Aplicar a heurística adequada a cada ciclo
        if (contadorCiclos % 3 == 0) {
            aplicarHeuristicaAtual();
        }
    }

    /**
     * Define uma referência para o prédio, permitindo acesso aos andares
     */
    private Predio predio;

    public void definirPredio(Predio predio) {
        this.predio = predio;
    }

    /**
     * Atualiza a matriz que controla quais elevadores estão designados para cada andar/direção
     */
    private void atualizarMapaDesignacoes() {
        // Reiniciar o mapa
        for (int andar = 0; andar < numeroTotalAndares; andar++) {
            for (int dir = 0; dir < 3; dir++) {
                elevadorDesignadoPara[andar][dir] = -1;
            }
        }

        // Verificar cada elevador
        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);
            int andarDestino = elevador.getAndarDestino();

            // Se o elevador tem um destino definido e não chegou ainda
            if (andarDestino != -1 && andarDestino != elevador.getAndarAtual()) {
                int direcaoIdx = obterIndiceDirecao(elevador.getDirecao());
                elevadorDesignadoPara[andarDestino][direcaoIdx] = elevador.getId();
            }
        }
    }

    /**
     * Atualiza o registro de tempos de espera em cada andar
     */
    private void atualizarTemposEspera() {
        // Incrementar o tempo para andares com chamadas pendentes
        for (int i = 0; i < andaresPendentes.tamanho(); i++) {
            int andar = andaresPendentes.obter(i);

            // Verificar se há chamadas para subir
            if (andar < numeroTotalAndares - 1 &&
                    elevadorDesignadoPara[andar][obterIndiceDirecao(Direcao.SUBINDO)] == -1) {
                temposEsperaAndares[andar][0]++; // Incrementa tempo de espera para subir
            }

            // Verificar se há chamadas para descer
            if (andar > 0 &&
                    elevadorDesignadoPara[andar][obterIndiceDirecao(Direcao.DESCENDO)] == -1) {
                temposEsperaAndares[andar][1]++; // Incrementa tempo de espera para descer
            }
        }
    }

    /**
     * Converte uma direção para seu índice na matriz de controle
     */
    private int obterIndiceDirecao(Direcao direcao) {
        switch (direcao) {
            case SUBINDO: return 0;
            case DESCENDO: return 1;
            default: return 2; // PARADO
        }
    }

    /**
     * Verifica se já existe um elevador designado para atender um andar em uma direção específica
     */
    private boolean existeElevadorParaAndarDirecao(int andar, Direcao direcao) {
        return elevadorDesignadoPara[andar][obterIndiceDirecao(direcao)] != -1;
    }

    /**
     * Registra que um elevador foi designado para atender um andar em uma direção específica
     */
    private void registrarElevadorParaAndarDirecao(int andar, Direcao direcao, int idElevador) {
        elevadorDesignadoPara[andar][obterIndiceDirecao(direcao)] = idElevador;
    }

    /**
     * Processa as chamadas de um andar, registrando para atendimento posterior
     */
    public void processarChamadas(Andar andar) {
        PainelChamadas painelChamadas = andar.getPainelChamadas();
        int numeroAndar = andar.getNumero();

        // Se não há chamada neste andar, não faz nada
        if (!painelChamadas.temChamada(Direcao.PARADO) && !andar.temPessoasEsperando()) {
            return;
        }

        // Verifica se este andar já foi adicionado à lista de pendentes
        boolean jaEstaNaLista = false;
        for (int i = 0; i < andaresPendentes.tamanho(); i++) {
            if (andaresPendentes.obter(i) == numeroAndar) {
                jaEstaNaLista = true;
                break;
            }
        }

        // Se não está na lista de pendentes, adiciona
        if (!jaEstaNaLista) {
            andaresPendentes.adicionar(numeroAndar);
            LOGGER.log(Level.INFO, "Andar {0} adicionado à lista de pendentes", numeroAndar);

            // Resetar o tempo de espera para este andar
            temposEsperaAndares[numeroAndar][0] = 0;
            temposEsperaAndares[numeroAndar][1] = 0;
        }
    }

    /**
     * Aplica a heurística configurada atualmente
     */
    private void aplicarHeuristicaAtual() {
        switch (modeloHeuristica) {
            case SEM_HEURISTICA:
                distribuirElevadoresSemHeuristica();
                break;
            case OTIMIZACAO_TEMPO_ESPERA:
                distribuirElevadoresOtimizandoTempoEspera();
                break;
            case OTIMIZACAO_CONSUMO_ENERGIA:
                distribuirElevadoresOtimizandoEnergia();
                break;
            default:
                distribuirElevadoresSemHeuristica();
                break;
        }
    }

    /**
     * Implementação da estratégia sem heurística - ordem de chegada
     */
    private void distribuirElevadoresSemHeuristica() {
        // Se não há andares pendentes, não faz nada
        if (andaresPendentes.tamanho() == 0) {
            return;
        }

        // Lista de elevadores disponíveis
        Lista<Elevador> elevadoresDisponiveis = obterElevadoresDisponiveis();

        // Se não há elevadores disponíveis, não faz nada
        if (elevadoresDisponiveis.tamanho() == 0) {
            return;
        }

        // Agora distribui os elevadores disponíveis entre os andares pendentes
        int elevadorAtual = 0;
        Lista<Integer> andaresPendentesTemp = copiarListaAndaresPendentes();

        // Limpa a lista original para reconstruir
        andaresPendentes = new Lista<>();

        // Distribui elevadores para andares, por ordem de chegada
        while (elevadorAtual < elevadoresDisponiveis.tamanho() && andaresPendentesTemp.tamanho() > 0) {
            // Obtém o próximo andar da lista (o mais antigo que chegou)
            int andarAtual = andaresPendentesTemp.obter(0);
            andaresPendentesTemp.remover(0);

            // Obtém o elevador atual
            Elevador elevador = elevadoresDisponiveis.obter(elevadorAtual);

            // Se o elevador já está no andar, pula para o próximo andar
            if (elevador.getAndarAtual() == andarAtual) {
                // Coloca o andar de volta na lista de pendentes
                andaresPendentes.adicionar(andarAtual);
                continue;
            }

            // Designa o elevador para este andar
            boolean sucesso = elevador.definirDestinoExterno(andarAtual);

            if (sucesso) {
                // Registra a designação na matriz de controle
                registrarElevadorParaAndarDirecao(andarAtual, Direcao.PARADO, elevador.getId());
                LOGGER.log(Level.INFO, "Elevador {0} designado para andar {1} (sem heurística)",
                        new Object[]{elevador.getId(), andarAtual});
            } else {
                // Se não conseguiu definir o destino, coloca o andar de volta na lista
                andaresPendentes.adicionar(andarAtual);
            }

            // Passa para o próximo elevador
            elevadorAtual++;
        }

        // Se sobrou algum andar pendente, adiciona de volta à lista
        for (int i = 0; i < andaresPendentesTemp.tamanho(); i++) {
            andaresPendentes.adicionar(andaresPendentesTemp.obter(i));
        }
    }

    /**
     * Implementação da heurística para minimizar tempo de espera
     * Prioriza andares com maior tempo de espera e pessoas esperando há mais tempo
     */
    private void distribuirElevadoresOtimizandoTempoEspera() {
        if (andaresPendentes.tamanho() == 0) {
            return;
        }

        Lista<Elevador> elevadoresDisponiveis = obterElevadoresDisponiveis();

        if (elevadoresDisponiveis.tamanho() == 0) {
            return;
        }

        // Ordenar andares por tempo de espera (maior para menor)
        Lista<Integer> andaresPorPrioridade = new Lista<>();
        Lista<Integer> tempAndaresPendentes = copiarListaAndaresPendentes();

        // Enquanto houver andares pendentes para ordenar
        while (tempAndaresPendentes.tamanho() > 0) {
            int andarMaiorEspera = -1;
            int maiorTempoEspera = -1;

            // Encontrar o andar com maior tempo de espera
            for (int i = 0; i < tempAndaresPendentes.tamanho(); i++) {
                int andar = tempAndaresPendentes.obter(i);
                int tempoEsperaTotal = temposEsperaAndares[andar][0] + temposEsperaAndares[andar][1];

                if (tempoEsperaTotal > maiorTempoEspera) {
                    maiorTempoEspera = tempoEsperaTotal;
                    andarMaiorEspera = andar;
                }
            }

            // Adicionar à lista ordenada e remover da temporária
            if (andarMaiorEspera != -1) {
                andaresPorPrioridade.adicionar(andarMaiorEspera);
                for (int i = 0; i < tempAndaresPendentes.tamanho(); i++) {
                    if (tempAndaresPendentes.obter(i) == andarMaiorEspera) {
                        tempAndaresPendentes.remover(i);
                        break;
                    }
                }
            } else {
                break; // Não deveria acontecer, mas previne loop infinito
            }
        }

        // Limpa a lista de andares pendentes para reconstrução
        andaresPendentes = new Lista<>();

        // Distribui elevadores para os andares priorizados
        for (int i = 0; i < andaresPorPrioridade.tamanho() && i < elevadoresDisponiveis.tamanho(); i++) {
            int andar = andaresPorPrioridade.obter(i);
            Elevador elevador = elevadoresDisponiveis.obter(i);

            // Verifica se o elevador já está no andar
            if (elevador.getAndarAtual() == andar) {
                andaresPendentes.adicionar(andar);
                continue;
            }

            // Tenta designar o elevador para o andar
            boolean sucesso = elevador.definirDestinoExterno(andar);

            if (sucesso) {
                registrarElevadorParaAndarDirecao(andar, Direcao.PARADO, elevador.getId());
                LOGGER.log(Level.INFO, "Elevador {0} designado para andar {1} (otimizando tempo de espera - {2}min)",
                        new Object[]{elevador.getId(), andar, temposEsperaAndares[andar][0] + temposEsperaAndares[andar][1]});

                // Resetar o tempo de espera para este andar
                temposEsperaAndares[andar][0] = 0;
                temposEsperaAndares[andar][1] = 0;
            } else {
                andaresPendentes.adicionar(andar);
            }
        }

        // Adicionar andares que não foram atendidos de volta à lista
        for (int i = elevadoresDisponiveis.tamanho(); i < andaresPorPrioridade.tamanho(); i++) {
            andaresPendentes.adicionar(andaresPorPrioridade.obter(i));
        }
    }

    /**
     * Implementação da heurística para minimizar consumo de energia
     * Prioriza deslocamentos mais curtos e evita movimentação desnecessária
     */
    private void distribuirElevadoresOtimizandoEnergia() {
        if (andaresPendentes.tamanho() == 0) {
            return;
        }

        Lista<Elevador> elevadoresDisponiveis = obterElevadoresDisponiveis();

        if (elevadoresDisponiveis.tamanho() == 0) {
            return;
        }

        // Primeiro, filtramos os andares pendentes que já têm pessoas esperando
        Lista<Integer> andaresCriticos = new Lista<>();

        for (int i = 0; i < andaresPendentes.tamanho(); i++) {
            int andar = andaresPendentes.obter(i);
            Andar objAndar = obterAndarPorNumero(andar);

            if (objAndar != null && objAndar.getNumPessoasEsperando() > 0) {
                andaresCriticos.adicionar(andar);
            }
        }

        // Matriz que representa a pontuação energética para cada par elevador-andar
        int[][] pontuacoesEnergeticas = new int[elevadoresDisponiveis.tamanho()][andaresPendentes.tamanho()];

        // Calcular pontuação energética (menor é melhor)
        for (int e = 0; e < elevadoresDisponiveis.tamanho(); e++) {
            Elevador elevador = elevadoresDisponiveis.obter(e);
            int andarAtualElevador = elevador.getAndarAtual();

            for (int a = 0; a < andaresPendentes.tamanho(); a++) {
                int andarDestino = andaresPendentes.obter(a);

                // Calcular custo energético baseado na distância
                int distancia = Math.abs(andarAtualElevador - andarDestino);

                // Penalidade para grandes deslocamentos
                int pontuacao = distancia * 10;

                // Verificar se o andar é crítico (pessoas esperando)
                boolean andarEhCritico = false;
                for (int c = 0; c < andaresCriticos.tamanho(); c++) {
                    if (andaresCriticos.obter(c) == andarDestino) {
                        andarEhCritico = true;
                        break;
                    }
                }

                // Se o andar for crítico, reduzir a pontuação para priorizá-lo
                if (andarEhCritico) {
                    pontuacao -= 50;
                }

                // Se o elevador estiver vazio, penalizar ainda mais os deslocamentos longos
                if (elevador.getNumPassageiros() == 0) {
                    pontuacao += distancia * 5;
                }

                // Se o elevador estiver subindo e o andar estiver acima, ou
                // se estiver descendo e o andar estiver abaixo, reduzir pontuação
                if ((elevador.getDirecao() == Direcao.SUBINDO && andarDestino > andarAtualElevador) ||
                        (elevador.getDirecao() == Direcao.DESCENDO && andarDestino < andarAtualElevador)) {
                    pontuacao -= 30;
                }

                pontuacoesEnergeticas[e][a] = pontuacao;
            }
        }

        // Lista de andares que foram atribuídos a elevadores
        Lista<Integer> andaresAtendidos = new Lista<>();

        // Lista de elevadores que já foram atribuídos
        Lista<Integer> elevadoresAtribuidos = new Lista<>();

        // Enquanto houver elevadores e andares a serem designados
        while (elevadoresAtribuidos.tamanho() < elevadoresDisponiveis.tamanho() &&
                andaresAtendidos.tamanho() < andaresPendentes.tamanho()) {

            // Encontrar o par elevador-andar com menor pontuação (mais eficiente energeticamente)
            int melhorElevador = -1;
            int melhorAndar = -1;
            int melhorPontuacao = Integer.MAX_VALUE;

            for (int e = 0; e < elevadoresDisponiveis.tamanho(); e++) {
                // Verificar se o elevador já foi atribuído
                boolean elevadorAtribuido = false;
                for (int i = 0; i < elevadoresAtribuidos.tamanho(); i++) {
                    if (elevadoresAtribuidos.obter(i) == e) {
                        elevadorAtribuido = true;
                        break;
                    }
                }

                if (elevadorAtribuido) continue;

                for (int a = 0; a < andaresPendentes.tamanho(); a++) {
                    // Verificar se o andar já foi atendido
                    boolean andarAtendido = false;
                    for (int i = 0; i < andaresAtendidos.tamanho(); i++) {
                        if (andaresAtendidos.obter(i) == a) {
                            andarAtendido = true;
                            break;
                        }
                    }

                    if (andarAtendido) continue;

                    // Verificar se este par tem a pontuação mais baixa
                    if (pontuacoesEnergeticas[e][a] < melhorPontuacao) {
                        melhorPontuacao = pontuacoesEnergeticas[e][a];
                        melhorElevador = e;
                        melhorAndar = a;
                    }
                }
            }

            // Se encontrou um par válido
            if (melhorElevador != -1 && melhorAndar != -1) {
                Elevador elevador = elevadoresDisponiveis.obter(melhorElevador);
                int andarDestino = andaresPendentes.obter(melhorAndar);

                // Verifica se o elevador já está no andar
                if (elevador.getAndarAtual() == andarDestino) {
                    // Marca como atendidos e continua
                    andaresAtendidos.adicionar(melhorAndar);
                    elevadoresAtribuidos.adicionar(melhorElevador);
                    continue;
                }

                // Tenta designar o elevador para o andar
                boolean sucesso = elevador.definirDestinoExterno(andarDestino);

                if (sucesso) {
                    registrarElevadorParaAndarDirecao(andarDestino, Direcao.PARADO, elevador.getId());
                    LOGGER.log(Level.INFO, "Elevador {0} designado para andar {1} (otimizando energia - pontuação: {2})",
                            new Object[]{elevador.getId(), andarDestino, melhorPontuacao});
                }

                // Marcar como atendidos
                andaresAtendidos.adicionar(melhorAndar);
                elevadoresAtribuidos.adicionar(melhorElevador);
            } else {
                break; // Não há mais pares válidos
            }
        }

        // Reconstruir a lista de andares pendentes
        Lista<Integer> novosAndaresPendentes = new Lista<>();

        for (int i = 0; i < andaresPendentes.tamanho(); i++) {
            boolean foiAtendido = false;

            for (int j = 0; j < andaresAtendidos.tamanho(); j++) {
                if (andaresAtendidos.obter(j) == i) {
                    foiAtendido = true;
                    break;
                }
            }

            if (!foiAtendido) {
                novosAndaresPendentes.adicionar(andaresPendentes.obter(i));
            }
        }

        andaresPendentes = novosAndaresPendentes;
    }

    /**
     * Obtém a lista de elevadores disponíveis para atendimento
     */
    private Lista<Elevador> obterElevadoresDisponiveis() {
        Lista<Elevador> elevadoresDisponiveis = new Lista<>();

        // Verifica quais elevadores estão disponíveis
        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);

            // Um elevador está disponível se não está em movimento e não tem destino
            if (!elevador.isEmMovimento() && elevador.getAndarDestino() == -1) {
                elevadoresDisponiveis.adicionar(elevador);
            }
        }

        return elevadoresDisponiveis;
    }

    /**
     * Cria uma cópia da lista de andares pendentes
     */
    private Lista<Integer> copiarListaAndaresPendentes() {
        Lista<Integer> copia = new Lista<>();
        for (int i = 0; i < andaresPendentes.tamanho(); i++) {
            copia.adicionar(andaresPendentes.obter(i));
        }
        return copia;
    }

    /**
     * Obtém o objeto Andar a partir do número do andar
     */
    private Andar obterAndarPorNumero(int numeroAndar) {
        if (predio == null) {
            return null;
        }
        return predio.getAndar(numeroAndar);
    }

    /**
     * Escolhe o melhor elevador para atender uma chamada específica
     * Método mantido para compatibilidade, mas não é mais usado
     */
    private Elevador escolherMelhorElevadorPara(int andar, Direcao direcao) {
        Elevador melhorElevador = null;
        int melhorPontuacao = Integer.MIN_VALUE;

        for (int i = 0; i < elevadores.tamanho(); i++) {
            Elevador elevador = elevadores.obter(i);

            if (elevador.podeAtenderChamada(andar, direcao)) {
                int pontuacao = calcularPontuacaoElevador(elevador, andar, direcao);

                if (pontuacao > melhorPontuacao) {
                    melhorPontuacao = pontuacao;
                    melhorElevador = elevador;
                }
            }
        }

        return melhorElevador;
    }

    /**
     * Calcula uma pontuação que indica o quão adequado um elevador é para atender uma chamada
     * Método mantido para compatibilidade, mas não é mais usado diretamente
     */
    private int calcularPontuacaoElevador(Elevador elevador, int andar, Direcao direcao) {
        int pontuacao = 0;

        // Distância (menor é melhor)
        int distancia = Math.abs(elevador.getAndarAtual() - andar);
        pontuacao -= distancia * 10;

        // Direção compatível (mesma direção é melhor)
        if (elevador.getDirecao() == direcao) {
            pontuacao += 30;
        }

        // Capacidade disponível (mais espaço é melhor)
        int espacoDisponivel = elevador.getCapacidadeMaxima() - elevador.getNumPassageiros();
        pontuacao += espacoDisponivel * 5;

        // Se elevador está parado (melhor do que em movimento)
        if (elevador.getDirecao() == Direcao.PARADO) {
            pontuacao += 20;
        }

        // Aplicar lógica de heurística conforme o modelo configurado
        switch (modeloHeuristica) {
            case OTIMIZACAO_TEMPO_ESPERA:
                // Priorizar elevadores que já estão movendo na direção certa
                if (elevador.getDirecao() == direcao) {
                    pontuacao += 50;
                }

                // Priorizar elevadores que estão próximos ao andar chamado
                if (distancia <= 2) {
                    pontuacao += 40;
                }
                break;

            case OTIMIZACAO_CONSUMO_ENERGIA:
                // Priorizar elevadores que estão mais próximos
                pontuacao -= (distancia * 15); // Aumentar o peso da distância

                // Priorizar elevadores que já têm passageiros
                if (elevador.getNumPassageiros() > 0) {
                    pontuacao += 30;
                }
                break;

            case SEM_HEURISTICA:
            default:
                // Usar lógica padrão sem ajustes adicionais
                break;
        }

        return pontuacao;
    }

    /**
     * Transfere pessoas de um andar para um elevador
     */
    public void transferirPessoasParaElevador(Andar andar, Elevador elevador) {
        if (!andar.temPessoasEsperando()) {
            return;
        }

        int numeroAndar = andar.getNumero();
        Direcao direcaoElevador = elevador.getDirecao();

        Lista<Pessoa> pessoasRemovidas = new Lista<>();

        // Lista de pessoas no andar
        Lista<Pessoa> pessoasEsperando = andar.getPessoasEsperando();

        // Lógica baseada na heurística selecionada
        switch (modeloHeuristica) {
            case OTIMIZACAO_TEMPO_ESPERA:
                // Pessoas com maior tempo de espera embarcam primeiro
                transferirPessoasPorTempoEspera(andar, elevador, pessoasEsperando, pessoasRemovidas, numeroAndar, direcaoElevador);
                break;

            case OTIMIZACAO_CONSUMO_ENERGIA:
                // Prioriza encher o elevador com pessoas indo na mesma direção
                transferirPessoasPorDirecao(andar, elevador, pessoasEsperando, pessoasRemovidas, numeroAndar, direcaoElevador);
                break;

            case SEM_HEURISTICA:
            default:
                // Transferência padrão, sem priorização especial
                transferirPessoasPadrao(andar, elevador, pessoasEsperando, pessoasRemovidas, numeroAndar, direcaoElevador);
                break;
        }

        // Remove as pessoas da fila
        for (int i = 0; i < pessoasRemovidas.tamanho(); i++) {
            Pessoa pessoaARemover = pessoasRemovidas.obter(i);

            // Primeiro encontramos a pessoa na fila
            boolean encontrada = false;
            Lista<Pessoa> pessoasNoAndar = andar.getPessoasEsperando();
            for (int j = 0; j < pessoasNoAndar.tamanho(); j++) {
                Pessoa pessoaFila = pessoasNoAndar.obter(j);
                if (pessoaFila.getId() == pessoaARemover.getId()) {
                    encontrada = true;
                    break;
                }
            }

            if (encontrada) {
                andar.removerPessoaDaFila();
            }
        }

        // Resetar a chamada se não houver mais pessoas esperando
        if (!andar.temPessoasEsperando()) {
            andar.getPainelChamadas().resetarChamada(Direcao.PARADO);

            // Verifica se este andar está na lista de pendentes e remove
            for (int i = 0; i < andaresPendentes.tamanho(); i++) {
                if (andaresPendentes.obter(i) == numeroAndar) {
                    andaresPendentes.remover(i);
                    break;
                }
            }
        }
    }

    /**
     * Transfere pessoas priorizando aquelas com maior tempo de espera
     */
    private void transferirPessoasPorTempoEspera(Andar andar, Elevador elevador,
                                                 Lista<Pessoa> pessoasEsperando,
                                                 Lista<Pessoa> pessoasRemovidas,
                                                 int numeroAndar, Direcao direcaoElevador) {
        // Primeiro, identificar pessoas com prioridade especial (idosos, cadeirantes)
        for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
            Pessoa pessoa = pessoasEsperando.obter(i);
            Direcao direcaoPessoa = Direcao.obterDirecaoPara(numeroAndar, pessoa.getAndarDestino());

            // Verificar se pode embarcar
            boolean podeEmbarcar = podePessoaEmbarcar(numeroAndar, direcaoElevador, direcaoPessoa);

            // Verifica se tem prioridade (idoso ou cadeirante)
            boolean temPrioridade = pessoa.isCadeirante() || pessoa.isIdoso();

            if (podeEmbarcar && temPrioridade && elevador.getNumPassageiros() < elevador.getCapacidadeMaxima()) {
                if (elevador.embarcarPessoa(pessoa)) {
                    pessoasRemovidas.adicionar(pessoa);
                    if (elevador.getNumPassageiros() >= elevador.getCapacidadeMaxima()) {
                        return; // Elevador lotado
                    }
                }
            }
        }

        // Depois, ordenar outras pessoas por tempo de espera
        while (pessoasEsperando.tamanho() > 0 && elevador.getNumPassageiros() < elevador.getCapacidadeMaxima()) {
            Pessoa pessoaMaiorEspera = null;
            int maiorTempoEspera = -1;

            // Encontrar pessoa com maior tempo de espera
            for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
                Pessoa pessoa = pessoasEsperando.obter(i);
                Direcao direcaoPessoa = Direcao.obterDirecaoPara(numeroAndar, pessoa.getAndarDestino());

                boolean podeEmbarcar = podePessoaEmbarcar(numeroAndar, direcaoElevador, direcaoPessoa);
                boolean jaRemovida = estaNaLista(pessoasRemovidas, pessoa.getId());

                if (podeEmbarcar && !jaRemovida && pessoa.getTempoEspera() > maiorTempoEspera) {
                    maiorTempoEspera = pessoa.getTempoEspera();
                    pessoaMaiorEspera = pessoa;
                }
            }

            // Se encontrou alguém para embarcar
            if (pessoaMaiorEspera != null) {
                if (elevador.embarcarPessoa(pessoaMaiorEspera)) {
                    pessoasRemovidas.adicionar(pessoaMaiorEspera);
                }
            } else {
                break; // Ninguém mais pode embarcar
            }
        }
    }

    /**
     * Transfere pessoas priorizando aquelas que vão na mesma direção
     */
    private void transferirPessoasPorDirecao(Andar andar, Elevador elevador,
                                             Lista<Pessoa> pessoasEsperando,
                                             Lista<Pessoa> pessoasRemovidas,
                                             int numeroAndar, Direcao direcaoElevador) {
        // Primeiro, verificar se o elevador já tem passageiros
        if (elevador.getNumPassageiros() > 0) {
            // Determinar a direção predominante dos passageiros atuais
            Direcao direcaoPredominante = determinarDirecaoPredominante(elevador);

            // Priorizar pessoas indo na mesma direção que a maioria
            for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
                Pessoa pessoa = pessoasEsperando.obter(i);
                Direcao direcaoPessoa = Direcao.obterDirecaoPara(numeroAndar, pessoa.getAndarDestino());

                boolean podeEmbarcar = podePessoaEmbarcar(numeroAndar, direcaoElevador, direcaoPessoa);
                boolean mesmaDir = direcaoPessoa == direcaoPredominante;

                if (podeEmbarcar && mesmaDir && elevador.getNumPassageiros() < elevador.getCapacidadeMaxima()) {
                    if (elevador.embarcarPessoa(pessoa)) {
                        pessoasRemovidas.adicionar(pessoa);
                    }
                }
            }

            // Depois, considerar outras pessoas se ainda houver espaço
            for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
                Pessoa pessoa = pessoasEsperando.obter(i);
                Direcao direcaoPessoa = Direcao.obterDirecaoPara(numeroAndar, pessoa.getAndarDestino());

                boolean podeEmbarcar = podePessoaEmbarcar(numeroAndar, direcaoElevador, direcaoPessoa);
                boolean jaRemovida = estaNaLista(pessoasRemovidas, pessoa.getId());

                if (podeEmbarcar && !jaRemovida && elevador.getNumPassageiros() < elevador.getCapacidadeMaxima()) {
                    if (elevador.embarcarPessoa(pessoa)) {
                        pessoasRemovidas.adicionar(pessoa);
                    }
                }
            }
        } else {
            // Se o elevador está vazio, tentar agrupar pessoas por destino
            // Isto minimiza paradas, economizando energia

            // Mapear pessoas por andar de destino
            int[] pessoasPorAndar = new int[numeroTotalAndares];

            for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
                Pessoa pessoa = pessoasEsperando.obter(i);
                int destino = pessoa.getAndarDestino();
                pessoasPorAndar[destino]++;
            }

            // Encontrar andar com mais pessoas
            int andarComMaisPessoas = -1;
            int maxPessoas = 0;

            for (int i = 0; i < numeroTotalAndares; i++) {
                if (pessoasPorAndar[i] > maxPessoas) {
                    maxPessoas = pessoasPorAndar[i];
                    andarComMaisPessoas = i;
                }
            }

            // Priorizar pessoas indo para o andar mais popular
            if (andarComMaisPessoas != -1) {
                for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
                    Pessoa pessoa = pessoasEsperando.obter(i);
                    Direcao direcaoPessoa = Direcao.obterDirecaoPara(numeroAndar, pessoa.getAndarDestino());

                    boolean podeEmbarcar = podePessoaEmbarcar(numeroAndar, direcaoElevador, direcaoPessoa);
                    boolean destinoPopular = pessoa.getAndarDestino() == andarComMaisPessoas;

                    if (podeEmbarcar && destinoPopular && elevador.getNumPassageiros() < elevador.getCapacidadeMaxima()) {
                        if (elevador.embarcarPessoa(pessoa)) {
                            pessoasRemovidas.adicionar(pessoa);
                        }
                    }
                }

                // Se ainda houver espaço, embarcar outras pessoas
                for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
                    Pessoa pessoa = pessoasEsperando.obter(i);
                    Direcao direcaoPessoa = Direcao.obterDirecaoPara(numeroAndar, pessoa.getAndarDestino());

                    boolean podeEmbarcar = podePessoaEmbarcar(numeroAndar, direcaoElevador, direcaoPessoa);
                    boolean jaRemovida = estaNaLista(pessoasRemovidas, pessoa.getId());

                    if (podeEmbarcar && !jaRemovida && elevador.getNumPassageiros() < elevador.getCapacidadeMaxima()) {
                        if (elevador.embarcarPessoa(pessoa)) {
                            pessoasRemovidas.adicionar(pessoa);
                        }
                    }
                }
            } else {
                // Abordagem padrão se não houver um andar predominante
                transferirPessoasPadrao(andar, elevador, pessoasEsperando, pessoasRemovidas, numeroAndar, direcaoElevador);
            }
        }
    }

    /**
     * Transferência padrão de pessoas, sem priorização especial
     */
    private void transferirPessoasPadrao(Andar andar, Elevador elevador,
                                         Lista<Pessoa> pessoasEsperando,
                                         Lista<Pessoa> pessoasRemovidas,
                                         int numeroAndar, Direcao direcaoElevador) {
        for (int i = 0; i < pessoasEsperando.tamanho(); i++) {
            Pessoa pessoa = pessoasEsperando.obter(i);
            Direcao direcaoPessoa = Direcao.obterDirecaoPara(numeroAndar, pessoa.getAndarDestino());

            // Verifica se a pessoa pode embarcar baseado nas regras de direção
            boolean podeEmbarcar = podePessoaEmbarcar(numeroAndar, direcaoElevador, direcaoPessoa);

            if (podeEmbarcar && elevador.getNumPassageiros() < elevador.getCapacidadeMaxima()) {
                // Tenta embarcar a pessoa
                if (elevador.embarcarPessoa(pessoa)) {
                    pessoasRemovidas.adicionar(pessoa);
                }
            }
        }
    }

    /**
     * Verifica se uma pessoa pode embarcar no elevador com base no andar e na direção
     */
    private boolean podePessoaEmbarcar(int numeroAndar, Direcao direcaoElevador, Direcao direcaoPessoa) {
        // Regra especial para o térreo (andar 0)
        if (numeroAndar == 0) {
            // No térreo, qualquer pessoa pode embarcar se o elevador estiver parado ou subindo
            return (direcaoElevador == Direcao.PARADO || direcaoElevador == Direcao.SUBINDO);
        }
        // Regra especial para o último andar
        else if (numeroAndar == (numeroTotalAndares - 1)) {
            // No último andar, qualquer pessoa pode embarcar se o elevador estiver parado ou descendo
            return (direcaoElevador == Direcao.PARADO || direcaoElevador == Direcao.DESCENDO);
        }
        // Regra para andares intermediários
        else {
            // Nos demais andares, siga a regra normal de direção
            return (direcaoElevador == direcaoPessoa || direcaoElevador == Direcao.PARADO);
        }
    }

    /**
     * Determina a direção predominante dos passageiros em um elevador
     */
    private Direcao determinarDirecaoPredominante(Elevador elevador) {
        int contSubindo = 0;
        int contDescendo = 0;

        Lista<Pessoa> passageiros = elevador.getPassageiros();
        int andarAtual = elevador.getAndarAtual();

        for (int i = 0; i < passageiros.tamanho(); i++) {
            Pessoa passageiro = passageiros.obter(i);
            int destino = passageiro.getAndarDestino();

            if (destino > andarAtual) {
                contSubindo++;
            } else if (destino < andarAtual) {
                contDescendo++;
            }
        }

        if (contSubindo > contDescendo) {
            return Direcao.SUBINDO;
        } else if (contDescendo > contSubindo) {
            return Direcao.DESCENDO;
        } else {
            return elevador.getDirecao(); // Manter direção atual se empate
        }
    }

    /**
     * Verifica se uma pessoa está em uma lista (por ID)
     */
    private boolean estaNaLista(Lista<Pessoa> lista, int idPessoa) {
        for (int i = 0; i < lista.tamanho(); i++) {
            if (lista.obter(i).getId() == idPessoa) {
                return true;
            }
        }
        return false;
    }

    // Getters
    public Lista<Elevador> getElevadores() {
        return elevadores;
    }

    public ModeloHeuristica getModeloHeuristica() {
        return modeloHeuristica;
    }

    public int getTempoMaximoEspera() {
        return tempoMaximoEspera;
    }

    public int getConsumoEnergiaTotal() {
        int consumoTotal = 0;

        for (int i = 0; i < elevadores.tamanho(); i++) {
            consumoTotal += elevadores.obter(i).getConsumoEnergiaTotal();
        }

        return consumoTotal;
    }
}