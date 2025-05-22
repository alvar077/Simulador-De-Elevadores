package com.alvaro.simulador.modelagem;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.alvaro.simulador.enums.Direcao;
import com.alvaro.simulador.enums.TipoPainel;
import com.alvaro.simulador.tads.FilaPrioridade;
import com.alvaro.simulador.tads.Lista;

public class Andar extends EntidadeSimulavel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(Andar.class.getName());

    private int numero;
    private PainelChamadas painelChamadas;
    private FilaPrioridade<Pessoa> filaPessoas;

    // Removemos a listaPessoas duplicada e utilizamos apenas a FilaPrioridade
    // Isso resolve problemas de sincronização entre estruturas

    public Andar(int numero, int numeroTotalAndares, TipoPainel tipoPainel) {
        this.numero = numero;
        this.painelChamadas = new PainelChamadas(numero, numeroTotalAndares, tipoPainel);
        this.filaPessoas = new FilaPrioridade<>();
    }

    public void adicionarPessoa(Pessoa pessoa) {
        boolean jaEstaFila = pessoaJaEstaFila(pessoa.getId());

        if (jaEstaFila) {
            LOGGER.log(Level.WARNING, "Pessoa {0} já está no andar {1}. Removendo antes de adicionar novamente.",
                    new Object[]{pessoa, numero});

            // Como não podemos remover diretamente da FilaPrioridade, vamos recriar
            FilaPrioridade<Pessoa> novaFila = new FilaPrioridade<>();
            Lista<Pessoa> listaAtual = filaPessoas.paraLista();

            for (int i = 0; i < listaAtual.tamanho(); i++) {
                Pessoa p = listaAtual.obter(i);
                if (p.getId() != pessoa.getId()) {
                    novaFila.enfileirar(p, p.calcularPrioridade());
                }
            }

            filaPessoas = novaFila;
        }

        // Adiciona a pessoa na fila com sua prioridade
        filaPessoas.enfileirar(pessoa, pessoa.calcularPrioridade());
        LOGGER.log(Level.INFO, "Pessoa {0} adicionada ao andar {1} (Destino: {2})",
                new Object[]{pessoa, numero, pessoa.getAndarDestino()});

        // Registra a chamada no painel automaticamente
        // Isso garantirá que o elevador seja chamado
        switch (painelChamadas.getTipoPainel()) {
            case UNICO_BOTAO:
                painelChamadas.registrarChamada(Direcao.PARADO, -1);
                break;

            case DOIS_BOTOES:
                Direcao direcao = Direcao.obterDirecaoPara(numero, pessoa.getAndarDestino());
                painelChamadas.registrarChamada(direcao, -1);
                break;

            case PAINEL_NUMERICO:
                painelChamadas.registrarChamada(null, pessoa.getAndarDestino());
                break;
        }
    }

    /**
     * Verifica se uma pessoa já está na fila (por ID)
     */
    private boolean pessoaJaEstaFila(int idPessoa) {
        Lista<Pessoa> pessoas = filaPessoas.paraLista();
        for (int i = 0; i < pessoas.tamanho(); i++) {
            if (pessoas.obter(i).getId() == idPessoa) {
                return true;
            }
        }
        return false;
    }

    public Pessoa removerPessoaDaFila() {
        if (filaPessoas.vazia()) {
            LOGGER.log(Level.WARNING, "Tentativa de remover pessoa de fila vazia no andar {0}", numero);
            return null;
        }

        Pessoa pessoaRemovida = filaPessoas.desenfileirar();
        LOGGER.log(Level.INFO, "Pessoa {0} removida da fila no andar {1}",
                new Object[]{pessoaRemovida, numero});

        // Se não houver mais pessoas na direção específica, resetar o botão correspondente
        if (!temPessoasEsperando()) {
            painelChamadas.resetarChamada(Direcao.PARADO);
            LOGGER.log(Level.INFO, "Não há mais pessoas esperando no andar {0}. Chamada resetada.", numero);
        } else {
            // Verifica se há pessoas indo para cima ou para baixo
            boolean temPessoasSubindo = false;
            boolean temPessoasDescendo = false;

            Lista<Pessoa> pessoas = filaPessoas.paraLista();
            for (int i = 0; i < pessoas.tamanho(); i++) {
                Pessoa pessoa = pessoas.obter(i);
                Direcao direcaoPessoa = Direcao.obterDirecaoPara(numero, pessoa.getAndarDestino());

                if (direcaoPessoa == Direcao.SUBINDO) {
                    temPessoasSubindo = true;
                } else if (direcaoPessoa == Direcao.DESCENDO) {
                    temPessoasDescendo = true;
                }
            }

            // Se não há mais pessoas subindo, resetar o botão de subida
            if (!temPessoasSubindo) {
                painelChamadas.resetarChamada(Direcao.SUBINDO);
                LOGGER.log(Level.INFO, "Não há mais pessoas subindo no andar {0}. Botão subida resetado.", numero);
            }

            // Se não há mais pessoas descendo, resetar o botão de descida
            if (!temPessoasDescendo) {
                painelChamadas.resetarChamada(Direcao.DESCENDO);
                LOGGER.log(Level.INFO, "Não há mais pessoas descendo no andar {0}. Botão descida resetado.", numero);
            }
        }

        return pessoaRemovida;
    }

    public boolean temPessoasEsperando() {
        return !filaPessoas.vazia();
    }

    @Override
    public void atualizar(int minutoSimulado) {
        // Converte a fila para lista para poder iterar sem remover
        Lista<Pessoa> listaPessoas = filaPessoas.paraLista();

        // Incrementa o tempo de espera de cada pessoa na fila
        for (int i = 0; i < listaPessoas.tamanho(); i++) {
            Pessoa pessoa = listaPessoas.obter(i);
            pessoa.incrementarTempoEspera();
        }
    }

    public int getNumero() {
        return numero;
    }

    public PainelChamadas getPainelChamadas() {
        return painelChamadas;
    }

    public int getNumPessoasEsperando() {
        return filaPessoas.tamanho();
    }

    public Lista<Pessoa> getPessoasEsperando() {
        return filaPessoas.paraLista();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Andar %d: ", numero));
        sb.append(painelChamadas.toString());
        sb.append(" Pessoas[");
        sb.append(filaPessoas.tamanho());
        sb.append("]");
        return sb.toString();
    }
}