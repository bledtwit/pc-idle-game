package model;

public enum ComponentType {

    CPU        ("⚙️",  "Процессор",        "Core i5-12400",  300),
    MOTHERBOARD("🗃️",  "Материнская плата", "B660M Pro",      200),
    RAM        ("💾",  "Оперативная память","DDR5 16GB",       100),
    GPU        ("🎮",  "Видеокарта",        "RTX 3060",        500),
    PSU        ("🔋",  "Блок питания",      "650W Gold",       150),
    STORAGE    ("💿",  "Накопитель",        "SSD 512GB",       120),
    CASE       ("📦",  "Корпус",            "MidTower ATX",    80);

    public final String icon;
    public final String displayName;
    public final String model;       // подпись под названием
    public final int    price;

    ComponentType(String icon, String displayName, String model, int price) {
        this.icon        = icon;
        this.displayName = displayName;
        this.model       = model;
        this.price       = price;
    }

    /** Цена конкретного тира */
    public int priceForTier(int tier) {
        return price * tier;
    }

    /** Короткая строка для кнопки */
    public String shortName() {
        return switch (this) {
            case CPU         -> "CPU";
            case MOTHERBOARD -> "MB";
            case RAM         -> "RAM";
            case GPU         -> "GPU";
            case PSU         -> "PSU";
            case STORAGE     -> "SSD";
            case CASE        -> "CASE";
        };
    }
}