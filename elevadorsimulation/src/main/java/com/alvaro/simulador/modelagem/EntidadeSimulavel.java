package com.alvaro.simulador.modelagem;

import java.io.Serializable;

public abstract class EntidadeSimulavel implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract void atualizar(int minutoSimulado);
}
