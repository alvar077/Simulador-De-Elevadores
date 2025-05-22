package com.alvaro.simulador.tads;

import java.io.Serializable;

public class Fila<T> implements Serializable {
    private No<T> inicio;
    private No<T> fim;
    private int tamanho;

    private static class No<T> {
        private T dado;
        private No<T> proximo;

        public No(T dado) {
            this.dado = dado;
            this.proximo = null;
        }
    }

    public Fila() {
        this.inicio = null;
        this.fim = null;
        this.tamanho = 0;
    }

    public void enfileirar(T elemento) {
        No<T> novoNo = new No<>(elemento);
        
        if (vazia()) {
            inicio = novoNo;
        } else {
            fim.proximo = novoNo;
        }
        
        fim = novoNo;
        tamanho++;
    }

    public T desenfileirar() {
        if (vazia()) {
            throw new IllegalStateException("A fila está vazia");
        }
        
        T elementoRemovido = inicio.dado;
        inicio = inicio.proximo;
        tamanho--;
        
        if (inicio == null) {
            fim = null;
        }
        
        return elementoRemovido;
    }

    public T primeiro() {
        if (vazia()) {
            throw new IllegalStateException("A fila está vazia");
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
        fim = null;
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