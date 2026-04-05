import logic.GameEngine;
import model.GameState;
import ui.GameWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        GameState  state  = new GameState(2000);   // 2000 стартовых монет
        GameEngine engine = new GameEngine(state);
        engine.start();

        // Запускаем UI в потоке Swing
        SwingUtilities.invokeLater(() -> new GameWindow(engine, state));
    }
}