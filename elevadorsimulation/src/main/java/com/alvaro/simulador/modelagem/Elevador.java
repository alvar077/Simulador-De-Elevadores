package com.alvaro.simulador.modelagem;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

import com.alvaro.simulador.enums.Direcao;
import com.alvaro.simulador.enums.ModeloHeuristica;
import com.alvaro.simulador.enums.TipoPainel;
import com.alvaro.simulador.tads.Lista;

public class Elevador extends EntidadeSimulavel implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private int andarAtual;
    private int andarDestino;
    private Direcao direcao;
    private PainelElevador painelInterno;
    private Lista<Pessoa> passageiros;
    private int capacidadeMaxima;
    private boolean emMovimento;
    private int tempoParaProximoAndar;
    private int tempoDeslocamentoPadrao;
    private int tempoDeslocamentoPico;
    private int consumoEnergiaPorDeslocamento;
    private int consumoEnergiaPorParada;
    private int consumoEnergiaTotal;
    private ModeloHeuristica modeloHeuristica;

    public Elevador(int id, int numeroTotalAndares, TipoPainel tipoPainel, int capacidadeMaxima, int tempoDeslocamentoPadrao, int tempoDeslocamentoPico, int consumoEnergiaPorDeslocamento, int consumoEnergiaPorParada, ModeloHeuristica modeloHeuristica) {
        this.id = id;
        this.andarAtual = 0;  // Inicia no térreo
        this.andarDestino = -1;
        this.direcao = Direcao.PARADO;
        this.painelInterno = new PainelElevador(numeroTotalAndares, tipoPainel);
        this.passageiros = new Lista<>();
        this.capacidadeMaxima = capacidadeMaxima;
        this.emMovimento = false;
        this.tempoParaProximoAndar = 0;
        this.tempoDeslocamentoPadrao = tempoDeslocamentoPadrao;
        this.tempoDeslocamentoPico = tempoDeslocamentoPico;
        this.consumoEnergiaPorDeslocamento = consumoEnergiaPorDeslocamento;
        this.consumoEnergiaPorParada = consumoEnergiaPorParada;
        this.consumoEnergiaTotal = 0;
        this.modeloHeuristica = modeloHeuristica;
    }

    @Override
    public void atualizar(int minutoSimulado) {
        // PARTE 1: MOVIMENTAÇÃO
        if (emMovimento) {
            // Decrementar o tempo para o próximo andar
            tempoParaProximoAndar--;

            // Quando o tempo de deslocamento terminar
            if (tempoParaProximoAndar <= 0) {
                // Atualizar a posição do elevador
                andarAtual += (direcao == Direcao.SUBINDO) ? 1 : -1;

                // Verificar se chegou ao destino
                if (andarAtual == andarDestino) {
                    // Parou no andar de destino
                    emMovimento = false;
                    pararNoAndar();
                } else {
                    // Continua em movimento para o próximo andar
                    // Reiniciar o temporizador para o próximo trecho
                    boolean horarioPico = false;
                    tempoParaProximoAndar = horarioPico ? tempoDeslocamentoPico : tempoDeslocamentoPadrao;
                    // Não altera emMovimento - continua em movimento
                }
            }
        }
        // PARTE 2: DECISÃO (apenas se não estiver em movimento)
        else {
            // Se tem um destino e não está no destino, começa a se mover
            if (andarDestino != -1 && andarAtual != andarDestino) {
                iniciarMovimento();
            }
            // Se não tem destino e não há botões pressionados no painel, procura novo destino
            else if (andarDestino == -1 && !painelInterno.temBotaoPressionado()) {
                determinarProximoDestino();
            }
        }
    }

    private void iniciarMovimento() {
        // Verifica se o destino é válido
        if (andarDestino < 0 || andarDestino == andarAtual) {
            return;
        }

        // Define a direção baseada no destino
        direcao = Direcao.obterDirecaoPara(andarAtual, andarDestino);
        if (direcao == Direcao.PARADO) {
            return;
        }

        // Define o tempo de deslocamento
        boolean horarioPico = false;
        tempoParaProximoAndar = horarioPico ? tempoDeslocamentoPico : tempoDeslocamentoPadrao;

        // Registra o consumo de energia
        consumoEnergiaTotal += consumoEnergiaPorDeslocamento;

        // Marca o elevador como em movimento
        emMovimento = true;
    }

    private void pararNoAndar() {
        // Registrar consumo de energia por parada
        consumoEnergiaTotal += consumoEnergiaPorParada;

        // Liberar passageiros que chegaram ao destino
        liberarPassageiros();

        // Resetar o botão deste andar no painel
        painelInterno.resetarBotao(andarAtual);

        // Determinar o próximo destino
        int proximoAndarInterno = painelInterno.proximoAndarSelecionado(andarAtual, direcao);

        if (proximoAndarInterno != -1) {
            // Ainda há andares para atender na mesma direção
            andarDestino = proximoAndarInterno;
        } else {
            // Verificar na direção oposta
            Direcao direcaoOposta = direcao.oposto();
            proximoAndarInterno = painelInterno.proximoAndarSelecionado(andarAtual, direcaoOposta);

            if (proximoAndarInterno != -1) {
                // Há andares na direção oposta
                direcao = direcaoOposta;
                andarDestino = proximoAndarInterno;
            } else {
                // Não há mais destinos - ficar parado
                direcao = Direcao.PARADO;
                andarDestino = -1;
            }
        }

        Logger.getLogger(Elevador.class.getName()).info(
                "Elevador " + id + " parou no andar " + andarAtual +
                        " com " + passageiros.tamanho() + " passageiros");
    }

    private void liberarPassageiros() {
        Lista<Pessoa> passageirosParaRemover = new Lista<>();

        // Identificar passageiros que chegaram ao destino
        for (int i = 0; i < passageiros.tamanho(); i++) {
            Pessoa passageiro = passageiros.obter(i);
            if (passageiro.getAndarDestino() == andarAtual) {
                passageirosParaRemover.adicionar(passageiro);
            }
        }

        // Remover os passageiros
        for (int i = 0; i < passageirosParaRemover.tamanho(); i++) {
            Pessoa passageiro = passageirosParaRemover.obter(i);
            passageiro.sairDoElevador();
            passageiros.remover(passageiro);
        }

        if (passageirosParaRemover.tamanho() > 0) {
            Logger.getLogger(Elevador.class.getName()).info(
                    "Elevador " + id + " liberou " + passageirosParaRemover.tamanho() +
                            " passageiros no andar " + andarAtual);
        }
    }

    private void determinarProximoDestino() {
        // Procurar destino no painel interno
        int proximoAndarInterno = painelInterno.proximoAndarSelecionado(andarAtual, direcao);

        if (proximoAndarInterno != -1) {
            andarDestino = proximoAndarInterno;
            direcao = Direcao.obterDirecaoPara(andarAtual, andarDestino);
        } else {
            // Tentar na direção oposta
            Direcao direcaoOposta = direcao.oposto();
            proximoAndarInterno = painelInterno.proximoAndarSelecionado(andarAtual, direcaoOposta);

            if (proximoAndarInterno != -1) {
                andarDestino = proximoAndarInterno;
                direcao = Direcao.obterDirecaoPara(andarAtual, andarDestino);
            } else {
                // Sem destinos - ficar parado
                direcao = Direcao.PARADO;
                andarDestino = -1;
            }
        }
    }

    public boolean embarcarPessoa(Pessoa pessoa) {
        // Verificar capacidade
        if (passageiros.tamanho() >= capacidadeMaxima) {
            return false;
        }

        // Adicionar pessoa e registrar entrada
        passageiros.adicionar(pessoa);
        pessoa.entrarNoElevador(id);

        // Registrar destino no painel
        painelInterno.pressionarBotao(pessoa.getAndarDestino());

        // Se elevador está parado sem destino, definir novo destino
        if (andarDestino == -1 && direcao == Direcao.PARADO) {
            andarDestino = pessoa.getAndarDestino();
            direcao = Direcao.obterDirecaoPara(andarAtual, andarDestino);
        }

        Logger.getLogger(Elevador.class.getName()).info(
                "Elevador " + id + " embarcou pessoa " + pessoa.getId() +
                        " no andar " + andarAtual + " com destino ao andar " + pessoa.getAndarDestino());

        return true;
    }

    public boolean podeAtenderChamada(int andarChamada, Direcao direcaoChamada) {
        // Elevador cheio não aceita mais passageiros
        if (passageiros.tamanho() >= capacidadeMaxima) {
            return false;
        }

        // Elevador em movimento não aceita novas chamadas
        if (emMovimento) {
            return false;
        }

        // Se já tem um destino diferente, não pode atender
        if (andarDestino != -1 && andarDestino != andarChamada) {
            return false;
        }

        // Estamos no mesmo andar da chamada
        if (andarAtual == andarChamada) {
            return true;
        }

        // Se estamos parados e sem destino, podemos atender
        if (andarDestino == -1) {
            return true;
        }

        // Se estamos indo na mesma direção da chamada
        if (direcao != Direcao.PARADO && direcao == direcaoChamada) {
            if (direcao == Direcao.SUBINDO && andarChamada > andarAtual) {
                return true;
            } else if (direcao == Direcao.DESCENDO && andarChamada < andarAtual) {
                return true;
            }
        }

        return false;
    }

    public boolean definirDestinoExterno(int andar) {
        if (andar < 0 || andar == andarAtual) {
            return false;
        }

        // Se não tiver destino no momento, aceita o novo destino
        if (andarDestino == -1) {
            andarDestino = andar;
            direcao = Direcao.obterDirecaoPara(andarAtual, andar);
            return true;
        }

        // Se estiver subindo e o novo destino estiver no caminho
        if (direcao == Direcao.SUBINDO && andar > andarAtual && andar < andarDestino) {
            andarDestino = andar;
            return true;
        }
        // Se estiver descendo e o novo destino estiver no caminho
        else if (direcao == Direcao.DESCENDO && andar < andarAtual && andar > andarDestino) {
            andarDestino = andar;
            return true;
        }

        return false;
    }

    // Getters e setters
    public int getId() {
        return id;
    }

    public int getAndarAtual() {
        return andarAtual;
    }

    public int getAndarDestino() {
        return andarDestino;
    }

    public Direcao getDirecao() {
        return direcao;
    }

    public boolean isEmMovimento() {
        return emMovimento;
    }

    public int getNumPassageiros() {
        return passageiros.tamanho();
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public int getConsumoEnergiaTotal() {
        return consumoEnergiaTotal;
    }

    public Lista<Pessoa> getPassageiros() {
        return passageiros;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Elevador %d: Andar %d %s ", id, andarAtual, direcao.toString()));

        if (andarDestino == -1) {
            sb.append("Destino=- ");
        } else {
            sb.append(String.format("Destino=%d ", andarDestino));
        }

        sb.append(String.format("Passageiros=%d/%d ", passageiros.tamanho(), capacidadeMaxima));
        sb.append(painelInterno.toString());
        return sb.toString();
    }
}