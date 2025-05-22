package com.alvaro.simulador.modelagem;

import java.io.Serializable;

import com.alvaro.simulador.enums.Direcao;
import com.alvaro.simulador.enums.TipoPainel;

public class PainelChamadas implements Serializable {
    private TipoPainel tipoPainel;
    private boolean chamadaSubida;
    private boolean chamadaDescida;
    private boolean[] chamadasPorAndar;
    private int numeroAndares;
    private int andarAtual;
    
    public PainelChamadas(int andarAtual, int numeroAndares, TipoPainel tipoPainel) {
        this.andarAtual = andarAtual;
        this.numeroAndares = numeroAndares;
        this.tipoPainel = tipoPainel;
        this.chamadaSubida = false;
        this.chamadaDescida = false;
        
        if (tipoPainel == TipoPainel.PAINEL_NUMERICO) {
            this.chamadasPorAndar = new boolean[numeroAndares];
        }
    }
    
    public boolean registrarChamada(Direcao direcao, int andarDestino) {
        System.out.println("Registrando chamada no andar " + andarAtual
                + " com direção " + direcao
                + " e destino " + andarDestino);

        switch (tipoPainel) {
            case UNICO_BOTAO:
                chamadaSubida = true;
                chamadaDescida = true;
                return true;

            case DOIS_BOTOES:
                // Regra especial para térreo - só pode subir
                if (andarAtual == 0) {
                    chamadaSubida = true;
                    return true;
                } // Regra especial para último andar - só pode descer
                else if (andarAtual == numeroAndares - 1) {
                    chamadaDescida = true;
                    return true;
                } // Regra normal para outros andares
                else if (direcao == Direcao.SUBINDO) {
                    if (andarAtual == numeroAndares - 1) {
                        return false;
                    }
                    chamadaSubida = true;
                    return true;
                } else if (direcao == Direcao.DESCENDO) {
                    if (andarAtual == 0) {
                        return false;
                    }
                    chamadaDescida = true;
                    return true;
                }
                return false;

            case PAINEL_NUMERICO:
                if (andarDestino < 0 || andarDestino >= numeroAndares || andarDestino == andarAtual) {
                    return false;
                }
                chamadasPorAndar[andarDestino] = true;
                if (andarDestino > andarAtual) {
                    chamadaSubida = true;
                } else {
                    chamadaDescida = true;
                }
                return true;

            default:
                return false;
        }
    }
    
    public void resetarChamada(Direcao direcao) {
        if (direcao == Direcao.SUBINDO) {
            chamadaSubida = false;
        } else if (direcao == Direcao.DESCENDO) {
            chamadaDescida = false;
        } else {
            chamadaSubida = false;
            chamadaDescida = false;
        }
        
        if (tipoPainel == TipoPainel.PAINEL_NUMERICO) {
            for (int i = 0; i < numeroAndares; i++) {
                if (direcao == Direcao.SUBINDO && i > andarAtual) {
                    chamadasPorAndar[i] = false;
                } else if (direcao == Direcao.DESCENDO && i < andarAtual) {
                    chamadasPorAndar[i] = false;
                } else if (direcao == Direcao.PARADO) {
                    chamadasPorAndar[i] = false;
                }
            }
        }
    }
    
    public boolean temChamada(Direcao direcao) {
        if (direcao == Direcao.SUBINDO) {
            return chamadaSubida;
        } else if (direcao == Direcao.DESCENDO) {
            return chamadaDescida;
        } else {
            // Para PARADO, verifica qualquer chamada
            return chamadaSubida || chamadaDescida;
        }
    }
    
    public boolean temChamadaParaAndar(int andar) {
        if (tipoPainel != TipoPainel.PAINEL_NUMERICO || andar < 0 || andar >= numeroAndares) {
            return false;
        }
        
        return chamadasPorAndar[andar];
    }
    
    public TipoPainel getTipoPainel() {
        return tipoPainel;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Painel[");
        
        switch (tipoPainel) {
            case UNICO_BOTAO:
                sb.append(temChamada(Direcao.PARADO) ? "●" : "○");
                break;
                
            case DOIS_BOTOES:
                sb.append(chamadaSubida ? "↑" : "·");
                sb.append(chamadaDescida ? "↓" : "·");
                break;
                
            case PAINEL_NUMERICO:
                boolean primeiro = true;
                for (int i = 0; i < numeroAndares; i++) {
                    if (chamadasPorAndar[i]) {
                        if (!primeiro) {
                            sb.append(",");
                        }
                        sb.append(i);
                        primeiro = false;
                    }
                }
                break;
        }
        
        sb.append("]");
        return sb.toString();
    }
}