import logic.GameEngine;
import model.GameState;
import ui.GameWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // ИСПРАВЛЕНО: 5000 стартовых монет — хватит сразу купить базовый комплект
        GameState  state  = new GameState(50000);
        GameEngine engine = new GameEngine(state);
        engine.start();

        SwingUtilities.invokeLater(() -> new GameWindow(engine, state));
    }
}