package org.controlsfx.glyphfont;

/**
 * Represents a named character.
 * This interface is usually implemented by a Enum
 * which holds all characters of a specific font.
 */
public interface INamedCharacter {
    /**
     * Gets the name of this character
     * @return
     */
    String name();

    /**
     * Gets the character value
     * @return
     */
    char getChar();
}
