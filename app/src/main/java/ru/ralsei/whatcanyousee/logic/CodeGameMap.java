package ru.ralsei.whatcanyousee.logic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class CodeGameMap {
    /**
     * Image in the center of the screen with hints to the solution.
     */
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PROTECTED)
    private int imageId;

    /**
     * Correct code to open the door that user should print in the text field.
     */
    @Setter(AccessLevel.PROTECTED)
    private String correctCode;

    protected CodeGameMap() {
    }

    boolean checkCode(String code) {
        return correctCode.equals(code);
    }
}
