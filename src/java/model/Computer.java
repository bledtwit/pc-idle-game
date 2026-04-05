package model;

import java.util.EnumMap;
import java.util.Map;

public class Computer {
    private final Map<ComponentType, PCComponent> parts = new EnumMap<>(ComponentType.class);
    private final int id;
    private static int counter = 1;

    public Computer() {
        this.id = counter++;
    }

    /** Вернёт true, если все 7 слотов заполнены */
    public boolean isComplete() {
        return parts.size() == ComponentType.values().length;
    }

    public void addPart(PCComponent component) {
        parts.put(component.type, component);
    }

    public boolean hasPart(ComponentType type) {
        return parts.containsKey(type);
    }

    /** Суммарный доход в монетах/сек */
    public int getCoinsPerSec() {
        if (!isComplete()) return 0;
        return parts.values().stream().mapToInt(p -> p.coinsPerSec).sum();
    }

    /** Средний тир деталей → определяет потребление энергии */
    public int getPowerCost() {
        if (!isComplete()) return 0;
        double avgTier = parts.values().stream().mapToInt(p -> p.tier).average().orElse(1);
        return (int)(10 * avgTier); // монет/сек за электричество
    }

    /** Чистый доход с учётом электричества */
    public int getNetIncome() {
        return getCoinsPerSec() - getPowerCost();
    }

    public int getId() { return id; }

    public Map<ComponentType, PCComponent> getParts() {
        return Map.copyOf(parts);
    }

    @Override
    public String toString() {
        return "ПК #" + id + " | +" + getCoinsPerSec() + " к/с | -" + getPowerCost()
                + " эл. | Чистый: +" + getNetIncome() + " к/с";
    }
}