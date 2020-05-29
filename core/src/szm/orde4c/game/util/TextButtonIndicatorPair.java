package szm.orde4c.game.util;

public class TextButtonIndicatorPair {
    private String text;
    private int[] buttonIndicatorIds;

    public TextButtonIndicatorPair(String text, int[] buttonIndicatorIds) {
        this.text = text;
        this.buttonIndicatorIds = buttonIndicatorIds;
    }

    public String getText() {
        return text;
    }

    public int[] getButtonIndicatorIds() {
        return buttonIndicatorIds;
    }
}
