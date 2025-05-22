package com.alvaro.simulador.tads;

import java.io.Serializable;

public class Lista<T> implements Serializable{
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

    public Lista() {
        this.inicio = null;
        this.fim = null;
        this.tamanho = 0;
    }

    public void adicionar(T elemento) {
        No<T> novoNo = new No<>(elemento);
        
        if (vazia()) {
            inicio = novoNo;
            fim = novoNo;
        } else {
            fim.proximo = novoNo;
            fim = novoNo;
        }
        
        tamanho++;
    }

    public void adicionar(T elemento, int posicao) {
        if (posicao < 0 || posicao > tamanho) {
            throw new IndexOutOfBoundsException("Posição inválida");
        }
        
        if (posicao == tamanho) {
            adicionar(elemento);
            return;
        }
        
        No<T> novoNo = new No<>(elemento);
        
        if (posicao == 0) {
            novoNo.proximo = inicio;
            inicio = novoNo;
        } else {
            No<T> anterior = buscarNo(posicao - 1);
            novoNo.proximo = anterior.proximo;
            anterior.proximo = novoNo;
        }
        
        tamanho++;
    }

    public T remover(int posicao) {
        if (vazia() || posicao < 0 || posicao >= tamanho) {
            throw new IndexOutOfBoundsException("Posição inválida ou lista vazia");
        }
        
        T elementoRemovido;
        
        if (posicao == 0) {
            elementoRemovido = inicio.dado;
            inicio = inicio.proximo;
            
            if (inicio == null) {
                fim = null;
            }
        } else {
            No<T> anterior = buscarNo(posicao - 1);
            No<T> atual = anterior.proximo;
            elementoRemovido = atual.dado;
            anterior.proximo = atual.proximo;
            
            if (atual == fim) {
                fim = anterior;
            }
        }
        
        tamanho--;
        return elementoRemovido;
    }

    public boolean remover(T elemento) {
        if (vazia()) {
            return false;
        }
        
        if (inicio.dado.equals(elemento)) {
            inicio = inicio.proximo;
            tamanho--;
            
            if (inicio == null) {
                fim = null;
            }
            
            return true;
        }
        
        No<T> atual = inicio;
        while (atual.proximo != null) {
            if (atual.proximo.dado.equals(elemento)) {
                if (atual.proximo == fim) {
                    fim = atual;
                }
                
                atual.proximo = atual.proximo.proximo;
                tamanho--;
                return true;
            }
            atual = atual.proximo;
        }
        
        return false;
    }

    public T obter(int posicao) {
        if (posicao < 0 || posicao >= tamanho) {
            throw new IndexOutOfBoundsException("Posição inválida");
        }
        
        return buscarNo(posicao).dado;
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

    private No<T> buscarNo(int posicao) {
        No<T> atual = inicio;
        for (int i = 0; i < posicao; i++) {
            atual = atual.proximo;
        }
        return atual;
    }
}