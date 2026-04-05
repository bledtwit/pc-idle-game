package model;

public class PCComponent {
    public final ComponentType type;
    public final int tier;        // 1 = базовый, 2 = средний, 3 = топ
    public final int coinsPerSec; // вклад в доход собранного ПК

    public PCComponent(ComponentType type, int tier) {
        this.type = type;
        this.tier = tier;
        this.coinsPerSec = calculateIncome();
    }

    private int calculateIncome() {
        // каждая деталь вносит небольшой вклад; тир умножает
        int base = switch (type) {
            case CPU        -> 5;
            case GPU        -> 8;
            case MOTHERBOARD-> 3;
            case RAM        -> 4;
            case PSU        -> 2;
            case STORAGE    -> 2;
            case CASE       -> 1;
        };
        return base * tier;
    }

    @Override
    public String toString() {
        return type.displayName + " (Tier " + tier + ") — +" + coinsPerSec + " к/с";
    }
}