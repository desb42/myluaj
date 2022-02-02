package org.luaj.vm2.lib;

public abstract class Str_char_class_mgr {
	public abstract boolean Match_class(int codepoint, int character_class);
		
	public static final int
	  CLASS_ALPHA   = (int)'a'  // %a: represents all letters.
	, CLASS_CTRL    = (int)'c'  // %c: represents all control characters.
	, CLASS_DIGIT   = (int)'d'  // %d: represents all digits.
	, CLASS_PRINT   = (int)'g'  // %g: represents all printable characters except space.
	, CLASS_LOWER   = (int)'l'  // %l: represents all lowercase letters.
	, CLASS_PUNCT   = (int)'p'  // %p: represents all punctuation characters.
	, CLASS_SPACE   = (int)'s'  // %s: represents all space characters.
	, CLASS_UPPER   = (int)'u'  // %u: represents all uppercase letters.
	, CLASS_WORD    = (int)'w'  // %w: represents all alphanumeric characters.
	, CLASS_HEX     = (int)'x'  // %x: represents all hexadecimal digits.
	, CLASS_NULL    = (int)'z'  // %z: null character '\0' deprecated in 5.2
	;
}
