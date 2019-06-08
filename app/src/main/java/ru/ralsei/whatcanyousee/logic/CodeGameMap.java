package ru.ralsei.whatcanyousee.logic;

/**
 * Class representing an abstract code game map.
 */
public abstract class CodeGameMap {
    /**
     * Image in the center of the screen with hints the solution.
     */
    private int imageId;

    /**
     * Correct code to open the door.
     */
    private String correctCode;

    protected CodeGameMap() {
    }

    protected void setImageId(int imageId) {
        this.imageId = imageId;
    }

    boolean checkCode(String code) {
        return correctCode.equals(code);
    }

    protected void setCorrectCode(String correctCode) {
        this.correctCode = correctCode;
    }

    int getImageId() {
        return imageId;
    }
}
