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
