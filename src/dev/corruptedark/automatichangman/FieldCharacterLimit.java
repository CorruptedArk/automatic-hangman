/*
 * Automatic Hangman is a program that functions both as a simple playable game of hangman and an AI that plays hangman.
 *     Copyright (C) 2019  Noah Stanford <noahstandingford@gmail.com>
 *
 *     Automatic Hangman is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Automatic Hangman is distributed in the hope that it will be fun, interesting, and educational,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.corruptedark.automatichangman;


import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class FieldCharacterLimit extends DocumentFilter {

    private int limit;

    FieldCharacterLimit(int limit)
    {
        super();
        this.limit = limit;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {

        if(string != null && (fb.getDocument().getLength() + string.length() <= limit) && string.chars().allMatch(Character::isLetter))
        {
            super.insertString(fb, offset, string.toLowerCase(), attr);
        }

    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text != null && (fb.getDocument().getLength() + text.length() - length <= limit) && text.chars().allMatch(Character::isLetter))
        {
            super.replace(fb, offset, length, text.toLowerCase(), attrs);
        }
    }
}
