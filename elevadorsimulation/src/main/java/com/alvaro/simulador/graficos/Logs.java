package com.alvaro.simulador.graficos;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.Serializable;

public class Logs extends Handler implements Serializable {
    private JTextArea areaLog;
    private SimpleDateFormat timeFormat;
    private int maxLogLines;

    public Logs(JTextArea areaLog) {
        this.areaLog = areaLog;
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");
        this.maxLogLines = 1000; // Limitar o número de linhas para evitar problemas de memória
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            SwingUtilities.invokeLater(() -> {
                String time = timeFormat.format(new Date(record.getMillis()));
                String message = String.format("[%s] %s%n", time, record.getMessage());

                // Adicionar a mensagem à área de texto
                areaLog.append(message);

                // Limitar o número de linhas
                limitLogLines();

                // Rolar para a última linha
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
            });
        }
    }

    private void limitLogLines() {
        String text = areaLog.getText();
        String[] lines = text.split("\n");

        if (lines.length > maxLogLines) {
            StringBuilder newText = new StringBuilder();
            for (int i = lines.length - maxLogLines; i < lines.length; i++) {
                newText.append(lines[i]).append("\n");
            }
            areaLog.setText(newText.toString());
        }
    }

    @Override
    public void flush() {
        // Não é necessário implementar para este caso
    }

    @Override
    public void close() throws SecurityException {
        // Não é necessário implementar para este caso
    }
}