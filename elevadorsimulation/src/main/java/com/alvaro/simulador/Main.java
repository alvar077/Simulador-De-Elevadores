package com.alvaro.simulador;

import java.io.Serializable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.alvaro.simulador.graficos.ConfiguracaoSimuladorGUI;

public class Main implements Serializable {

    public static void main(String[] args) {
        System.out.println("=== SIMULADOR DE ELEVADORES INTELIGENTES ===");
        System.out.println("Iniciando configuração...");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Não foi possível definir o Look and Feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new ConfiguracaoSimuladorGUI();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erro ao iniciar a interface de configuração: " + e.getMessage());
            }
        });
    }
}
