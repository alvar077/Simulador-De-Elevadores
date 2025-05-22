package com.alvaro.simulador.modelagem;

import java.io.Serializable;

import com.alvaro.simulador.enums.Direcao;
import com.alvaro.simulador.enums.TipoPainel;

public class PainelElevador implements Serializable{
    private boolean[] botoesAndares;
    private TipoPainel tipoPainelConfig;
    private int numeroAndares;
    
    public PainelElevador(int numeroAndares, TipoPainel tipoPainelConfig) {
        this.numeroAndares = numeroAndares;
        this.tipoPainelConfig = tipoPainelConfig;
        this.botoesAndares = new boolean[numeroAndares];
    }
    
    public boolean pressionarBotao(int andar) {
        if (andar < 0 || andar >= numeroAndares) {
            return false;
        }
        
        botoesAndares[andar] = true;
        return true;
    }

    public void imprimirEstadoBotoes() {
        System.out.println("Estado atual dos botões do painel:");
        for (int i = 0; i < numeroAndares; i++) {
            if (botoesAndares[i]) {
                System.out.println("  Andar " + i + ": PRESSIONADO");
            }
        }
    }
    
    public void resetarBotao(int andar) {
        if (andar >= 0 && andar < numeroAndares) {
            botoesAndares[andar] = false;
        }
    }

    public int getNumeroAndares() {
        return numeroAndares;
    }
    
    public boolean estaPressionado(int andar) {
        if (andar < 0 || andar >= numeroAndares) {
            return false;
        }
        
        return botoesAndares[andar];
    }
    
    public boolean temBotaoPressionado() {
        for (boolean botao : botoesAndares) {
            if (botao) {
                return true;
            }
        }
        return false;
    }
    
    public int proximoAndarSelecionado(int andarAtual, Direcao direcao) {
        System.out.println("Buscando próximo andar a partir do andar " + andarAtual
                + " na direção " + direcao);

        // Imprimir estado atual dos botões
        imprimirEstadoBotoes();

        if (direcao == Direcao.PARADO) {
            // Se estiver parado, verificamos em ambas as direções
            int andarProximoSubindo = proximoAndarSelecionado(andarAtual, Direcao.SUBINDO);
            int andarProximoDescendo = proximoAndarSelecionado(andarAtual, Direcao.DESCENDO);

            System.out.println("Próximo andar subindo: "
                    + (andarProximoSubindo == -1 ? "Nenhum" : andarProximoSubindo));
            System.out.println("Próximo andar descendo: "
                    + (andarProximoDescendo == -1 ? "Nenhum" : andarProximoDescendo));

            if (andarProximoSubindo == -1) {
                return andarProximoDescendo;
            } else if (andarProximoDescendo == -1) {
                return andarProximoSubindo;
            } else {
                // Se houver destinos em ambas as direções, escolhe o mais próximo
                int distanciaSubindo = Math.abs(andarProximoSubindo - andarAtual);
                int distanciaDescendo = Math.abs(andarProximoDescendo - andarAtual);

                int resultado = (distanciaSubindo <= distanciaDescendo) ? andarProximoSubindo : andarProximoDescendo;
                System.out.println("Escolhido: " + resultado);
                return resultado;
            }
        } else if (direcao == Direcao.SUBINDO) {
            // Busca andares acima
            for (int i = andarAtual + 1; i < numeroAndares; i++) {
                if (botoesAndares[i]) {
                    System.out.println("Encontrado andar acima: " + i);
                    return i;
                }
            }
            System.out.println("Nenhum andar acima encontrado");
        } else if (direcao == Direcao.DESCENDO) {
            // Busca andares abaixo
            for (int i = andarAtual - 1; i >= 0; i--) {
                if (botoesAndares[i]) {
                    System.out.println("Encontrado andar abaixo: " + i);
                    return i;
                }
            }
            System.out.println("Nenhum andar abaixo encontrado");
        }

        System.out.println("Nenhum próximo andar encontrado na direção " + direcao);
        return -1;
    }

    public void limparTodosBotoes() {
        for (int i = 0; i < numeroAndares; i++) {
            botoesAndares[i] = false;
        }
    }

    public TipoPainel getTipoPainelConfig() {
        return tipoPainelConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Painel[");
        
        boolean primeiro = true;
        for (int i = 0; i < numeroAndares; i++) {
            if (botoesAndares[i]) {
                if (!primeiro) {
                    sb.append(",");
                }
                sb.append(i);
                primeiro = false;
            }
        }
        
        sb.append("]");
        return sb.toString();
    }
}
