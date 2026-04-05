package logic;

import model.ComponentType;
import model.GameState;

import java.util.Timer;
import java.util.TimerTask;

public class GameEngine {

    private final GameState state;
    private Timer timer;

    public GameEngine(GameState state) {
        this.state = state;
    }

    /** Запускает игровой цикл (1 тик = 1 секунда) */
    public void start() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                state.tick();
            }
        }, 1000, 1000);
    }

    public void stop() {
        if (timer != null) timer.cancel();
    }

    // ── Действия игрока ───────────────────────────────────────

    public boolean buyPart(ComponentType type, int tier) {
        return state.buyComponent(type, tier);
    }
    public boolean sellPart(ComponentType type) {
        return state.sellComponent(type);
    }


    public boolean installPart(ComponentType type) {
        return state.installPart(type);
    }

    public boolean buildPC() {
        return state.finishAssembly();
    }
}