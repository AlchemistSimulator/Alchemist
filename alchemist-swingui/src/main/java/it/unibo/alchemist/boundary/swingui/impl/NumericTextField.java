/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.swingui.impl;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.io.Serial;
import java.util.regex.Pattern;

/**
 */
@Deprecated
public final class NumericTextField extends JTextField {

    @Serial
    private static final long serialVersionUID = 1556539674522648542L;

    private static final class NumericDocument extends PlainDocument {

        @Serial
        private static final long serialVersionUID = 3063832505179925120L;

        // The regular expression to match input against (zero or more digits)
        private static final String REGEX = "[+-]?\\d+";
        private static final Pattern DIGITS = Pattern.compile(REGEX);

        @Override
        public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
            // Only insert the text if it matches the regular expression
            if (str != null && DIGITS.matcher(str).matches()) {
                super.insertString(offs, str, a);
            }
        }
    }

    @Override
    protected Document createDefaultModel() {
        return new NumericDocument();
    }
}
