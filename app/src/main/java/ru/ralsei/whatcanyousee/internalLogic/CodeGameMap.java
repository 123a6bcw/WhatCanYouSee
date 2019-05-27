package ru.ralsei.whatcanyousee.internalLogic;

/**
 * TODO
 */
public class CodeGameMap {
    /**
     * TODO
     */
    private int imageId;

    /**
     * TODO
     */
    private String correctCode;

    public CodeGameMap() {
    }

    protected void setImageId(int imageId) {
        this.imageId = imageId;
    }


    boolean checkCode(String code) {
        return correctCode.equals(code);
    }

    public void setCorrectCode(String correctCode) {
        this.correctCode = correctCode;
    }

    public int getImageId() {
        return imageId;
    }
}
