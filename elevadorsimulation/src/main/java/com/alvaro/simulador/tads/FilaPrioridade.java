package com.alvaro.simulador.tads;

import java.io.Serializable;

public class FilaPrioridade<T> implements Serializable{
    private No<T> inicio;
    private int tamanho;

    private static class No<T> {
        private T dado;
        private int prioridade;
        private No<T> proximo;

        public No(T dado, int prioridade) {
            this.dado = dado;
            this.prioridade = prioridade;
            this.proximo = null;
        }
    }

    public FilaPrioridade() {
        this.inicio = null;
        this.tamanho = 0;
    }

    public void enfileirar(T elemento, int prioridade) {
        No<T> novoNo = new No<>(elemento, prioridade);
        
        // Caso especial: fila vazia ou o novo nó tem prioridade maior que o início
        if (vazia() || prioridade > inicio.prioridade) {
            novoNo.proximo = inicio;
            inicio = novoNo;
        } else {
            // Encontrar a posição correta baseada na prioridade
            No<T> atual = inicio;
            
            while (atual.proximo != null && atual.proximo.prioridade >= prioridade) {
                atual = atual.proximo;
            }
            
            novoNo.proximo = atual.proximo;
            atual.proximo = novoNo;
        }
        
        tamanho++;
    }

    public T desenfileirar() {
        if (vazia()) {
            throw new IllegalStateException("A fila de prioridade está vazia");
        }
        
        T elementoRemovido = inicio.dado;
        inicio = inicio.proximo;
        tamanho--;
        
        return elementoRemovido;
    }

    public T primeiro() {
        if (vazia()) {
            throw new IllegalStateException("A fila de prioridade está vazia");
        }
        
        return inicio.dado;
    }

    public boolean vazia() {
        return tamanho == 0;
    }

    public int tamanho() {
        return tamanho;
    }

    public void limpar() {
        inicio = null;
        tamanho = 0;
    }
    
    public boolean contem(T elemento) {
        No<T> atual = inicio;
        while (atual != null) {
            if (atual.dado.equals(elemento)) {
                return true;
            }
            atual = atual.proximo;
        }
        return false;
    }
    
    public Lista<T> paraLista() {
        Lista<T> lista = new Lista<>();
        No<T> atual = inicio;
        
        while (atual != null) {
            lista.adicionar(atual.dado);
            atual = atual.proximo;
        }
        
        return lista;
    }
}