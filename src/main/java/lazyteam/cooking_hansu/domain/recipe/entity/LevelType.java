package lazyteam.cooking_hansu.domain.recipe.entity;

public enum LevelType {
    VERY_HIGH("매우 어려움"),
    HIGH("어려움"),
    MEDIUM("보통"),
    LOW("쉬움"),
    VERY_LOW("매우 쉬움");

    private final String label;

    LevelType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
