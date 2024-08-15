package com.fdk.compiler.parser;

import com.fdk.compiler.util.ArrayUtils;
import java.nio.CharBuffer;
import java.util.Arrays;
import static com.fdk.compiler.util.LayoutCharacters.EOI;

public class UnicodeReader {

    protected char[] buf;
    protected int bp;
    protected final int buflen;

    /** The current character.
     */
    protected char ch;

    /** The buffer index of the last converted unicode character
     */
    protected int unicodeConversionBp = -1;


    /** A character buffer for saved chars.
     */
    protected char[] sbuf = new char[128];
    protected int sp;



    public static char[] toArray(CharBuffer buffer) {
        if (buffer.hasArray())
            return ((CharBuffer)buffer.compact().flip()).array();
        else
            return buffer.toString().toCharArray();
    }

    public UnicodeReader(char[] input) {
//        log = sf.log;
        int inputLength = input.length;
        if (input.length > 0 && Character.isWhitespace(input[input.length - 1])) {
            inputLength--;
        } else {
            input = Arrays.copyOf(input, inputLength + 1);
        }

        buf = input;
        buflen = inputLength;
        buf[buflen] = EOI;
        bp = -1;
        scanChar();
    }

    /** Read next character.
     */
    protected void scanChar() {
        if (bp < buflen) {
            ch = buf[++bp];
          //  System.out.println("ScanChar: " + ch);
            if (ch == '\\') {
                convertUnicode();
            }
        }
    }

    /** Read next character in comment, skipping over double '\' characters.
     */
    protected void scanCommentChar() {
        scanChar();
        if (ch == '\\') {
            if (peekChar() == '\\' && !isUnicode()) {
                skipChar();
            } else {
                convertUnicode();
            }
        }
    }

    /** Append a character to sbuf.
     */
    protected void putChar(char ch, boolean scan) {
        sbuf = ArrayUtils.ensureCapacity(sbuf, sp);
        sbuf[sp++] = ch;
        String s = new String(sbuf, 0, sp);
       // System.out.println("PutChar: " + s);
        if (scan)
            scanChar();
    }

    protected void putChar(char ch) {
        putChar(ch, false);
    }

    protected void putChar(boolean scan) {
        putChar(ch, scan);
    }

    String name() {
        return chars();
    }

    String chars() {
        return new String(sbuf, 0, sp);
    }

    /** Convert unicode escape; bp points to initial '\' character
     *  (Spec 3.3).
     */
    protected void convertUnicode() {
        if (ch == '\\' && unicodeConversionBp != bp) {
            bp++; ch = buf[bp];
            if (ch == 'u') {
                do {
                    bp++; ch = buf[bp];
                } while (ch == 'u');
                int limit = bp + 3;
                if (limit < buflen) {
                    int d = digit(bp, 16);
                    int code = d;
                    while (bp < limit && d >= 0) {
                        bp++; ch = buf[bp];
                        d = digit(bp, 16);
                        code = (code << 4) + d;
                    }
                    if (d >= 0) {
                        ch = (char)code;
                        unicodeConversionBp = bp;
                        return;
                    }
                }
//                log.error(bp, "illegal.unicode.esc");
            } else {
                bp--;
                ch = '\\';
            }
        }
    }

    /** Are surrogates supported?
     */
    final static boolean surrogatesSupported = surrogatesSupported();
    private static boolean surrogatesSupported() {
        try {
            Character.isHighSurrogate('a');
            return true;
        } catch (NoSuchMethodError ex) {
            return false;
        }
    }

    /** Scan surrogate pairs.  If 'ch' is a high surrogate and
     *  the next character is a low surrogate, returns the code point
     *  constructed from these surrogates. Otherwise, returns -1.
     *  This method will not consume any of the characters.
     */
    protected int peekSurrogates() {
        if (surrogatesSupported && Character.isHighSurrogate(ch)) {
            char high = ch;
            int prevBP = bp;

            scanChar();

            char low = ch;

            ch = high;
            bp = prevBP;

            if (Character.isLowSurrogate(low)) {
                return Character.toCodePoint(high, low);
            }
        }

        return -1;
    }

    /** Convert an ASCII digit from its base (8, 10, or 16)
     *  to its value.
     */
    protected int digit(int pos, int base) {
        char c = ch;
        if ('0' <= c && c <= '9')
            return Character.digit(c, base); //a fast common case
        int codePoint = peekSurrogates();
        int result = codePoint >= 0 ? Character.digit(codePoint, base) : Character.digit(c, base);
        if (result >= 0 && c > 0x7f) {
//            log.error(pos + 1, "illegal.nonascii.digit");
            if (codePoint >= 0)
                scanChar();
            ch = "0123456789abcdef".charAt(result);
        }
        return result;
    }

    protected boolean isUnicode() {
        return unicodeConversionBp == bp;
    }

    protected void skipChar() {
        bp++;
    }

    protected char peekChar() {
        return buf[bp + 1];
    }

    /**
     * Returns a copy of the input buffer, up to its inputLength.
     * Unicode escape sequences are not translated.
     */
    public char[] getRawCharacters() {
        char[] chars = new char[buflen];
        System.arraycopy(buf, 0, chars, 0, buflen);
        return chars;
    }

    /**
     * Returns a copy of a character array subset of the input buffer.
     * The returned array begins at the {@code beginIndex} and
     * extends to the character at index {@code endIndex - 1}.
     * Thus the length of the substring is {@code endIndex-beginIndex}.
     * This behavior is like
     * {@code String.substring(beginIndex, endIndex)}.
     * Unicode escape sequences are not translated.
     *
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex the ending index, exclusive.
     * @throws ArrayIndexOutOfBoundsException if either offset is outside of the
     *         array bounds
     */
    public char[] getRawCharacters(int beginIndex, int endIndex) {
        int length = endIndex - beginIndex;
        char[] chars = new char[length];
        System.arraycopy(buf, beginIndex, chars, 0, length);
        return chars;
    }
}
