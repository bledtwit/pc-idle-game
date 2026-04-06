package model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GameState {

    // ── Кошелёк ───────────────────────────────────────────────────
    private long coins;

    // ── Склад деталей ─────────────────────────────────────────────
    private final Map<ComponentType, Integer> inventory = new EnumMap<>(ComponentType.class);
    private final Map<ComponentType, Integer> tierInInv = new EnumMap<>(ComponentType.class);

    // ── Верстак ───────────────────────────────────────────────────
    private Computer workbench = new Computer();

    // ── Готовые ПК ────────────────────────────────────────────────
    private final List<Computer> runningPCs = new ArrayList<>();

    // ── Электричество ─────────────────────────────────────────────
    public  static final int  BILL_INTERVAL = 60;
    private int  ticksSinceBill = 0;
    private long lastBillAmount = 0;
    private boolean powerDebt   = false;

    // ── Флотирующие монеты ────────────────────────────────────────
    private long lastTickIncome = 0;

    // ── Конструктор ───────────────────────────────────────────────
    public GameState(long startCoins) {
        this.coins = startCoins;
        for (ComponentType t : ComponentType.values()) {
            inventory.put(t, 0);
            tierInInv.put(t, 1);
        }
    }

    // ── Монеты ────────────────────────────────────────────────────
    public long getCoins()        { return coins; }
    public void addCoins(long n)  { coins += n; }

    public boolean spendCoins(long n) {
        if (coins < n) return false;
        coins -= n;
        return true;
    }

    // ── Магазин ───────────────────────────────────────────────────
    public boolean buyComponent(ComponentType type, int tier) {
        int price = type.priceForTier(tier);
        if (!spendCoins(price)) return false;
        inventory.merge(type, 1, Integer::sum);
        tierInInv.put(type, tier);
        return true;
    }

    // ── Верстак ───────────────────────────────────────────────────
    public boolean installPart(ComponentType type) {
        if (inventory.getOrDefault(type, 0) == 0) return false;
        if (workbench.hasPart(type)) return false;
        int tier = tierInInv.getOrDefault(type, 1);
        PCComponent part = new PCComponent(type, tier);
        workbench.addPart(part);
        inventory.merge(type, -1, Integer::sum);
        return true;
    }

    public boolean finishAssembly() {
        if (!workbench.isComplete()) return false;
        runningPCs.add(workbench);
        workbench = new Computer();
        return true;
    }

    // ── ИСПРАВЛЕНО: продажа ПК ────────────────────────────────────
    public boolean sellPC(int index) {
        if (index < 0 || index >= runningPCs.size()) return false;
        Computer pc = runningPCs.remove(index);
        // возврат 30% стоимости деталей
        long refund = pc.getParts().values().stream()
                .mapToLong(p -> p.type.priceForTier(p.tier) * 30 / 100)
                .sum();
        coins += refund;
        return true;
    }

    // ── Тик ───────────────────────────────────────────────────────
    public void tick() {
        if (runningPCs.isEmpty()) {
            lastTickIncome = 0;
            return;
        }

        ticksSinceBill++;

        if (powerDebt) {
            lastTickIncome = 0;
            checkBill();
            return;
        }

        long income = runningPCs.stream().mapToLong(Computer::getNetIncome).sum();
        coins          += income;
        lastTickIncome  = income;

        checkBill();
    }

    public long consumeTickIncome() {
        long v = lastTickIncome;
        lastTickIncome = 0;
        return v;
    }

    private void checkBill() {
        if (ticksSinceBill >= BILL_INTERVAL && !runningPCs.isEmpty()) {
            ticksSinceBill = 0;
            issueBill();
        }
    }

    public boolean sellComponent(ComponentType type) {
        if (inventory.getOrDefault(type, 0) <= 0) return false;
        int tier = tierInInv.getOrDefault(type, 1);
        long refund = type.priceForTier(tier) / 2;
        inventory.merge(type, -1, Integer::sum);
        coins += refund;
        return true;
    }

    private void issueBill() {
        lastBillAmount = computeBillAmount(); // ИСПРАВЛЕНО: не рекурсия
        if (lastBillAmount == 0) return;

        if (coins >= lastBillAmount) {
            coins -= lastBillAmount;
            powerDebt = false;
        } else {
            powerDebt = true;
        }
    }

    public boolean payDebt() {
        if (!powerDebt) return false;
        if (coins < lastBillAmount) return false;
        coins -= lastBillAmount;
        powerDebt = false;
        return true;
    }

    // ── ИСПРАВЛЕНО: была бесконечная рекурсия calculateBill() → calculateBill() ──
    // Теперь реальный расчёт: сумма потребления всех ПК × интервал
    private long computeBillAmount() {
        return runningPCs.stream().mapToLong(Computer::getPowerCost).sum() * BILL_INTERVAL;
    }

    // Публичный метод для UI
    public long calculateBill() {
        return computeBillAmount();
    }

    // ── Геттеры ───────────────────────────────────────────────────
    public Map<ComponentType, Integer> getInventory()   { return Map.copyOf(inventory); }
    public Map<ComponentType, Integer> getTierInInv()   { return Map.copyOf(tierInInv); }
    public Computer                    getWorkbench()   { return workbench; }
    public List<Computer>              getRunningPCs()  { return List.copyOf(runningPCs); }

    public long    getTotalIncome()    { return runningPCs.stream().mapToLong(Computer::getNetIncome).sum(); }
    public long    getLastTickIncome() { return lastTickIncome; }
    public boolean isPowerDebt()       { return powerDebt; }
    public long    getLastBillAmount() { return lastBillAmount; }
    public int     getTicksSinceBill() { return ticksSinceBill; }
}