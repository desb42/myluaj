package org.luaj.vm2.lib;

// FOOTNOTE: UstringLibrary_APPROXIMATION
public class Str_char_class_mgr__unicode extends Str_char_class_mgr {
	@Override public boolean Match_class(int cp, int cls) {
		final int cls_lower = cls < 97 ? cls + 32 : cls;
		boolean res;
		int char_type;
		switch (cls_lower) { 
			case CLASS_ALPHA: // "\\p{L}"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L5635-L5639 
				char_type = Character.getType(cp);
				switch (char_type) {
					case Character.UPPERCASE_LETTER:
					case Character.LOWERCASE_LETTER:
					case Character.TITLECASE_LETTER:
					case Character.MODIFIER_LETTER:
					case Character.OTHER_LETTER:
						res = true;
						break;
					default:
						res = false;
						break;
				}
				break;
			case CLASS_CTRL: // "\\p{Cc}"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L5620
				char_type = Character.getType(cp);
				res = char_type == Character.CONTROL;
				break;
			case CLASS_DIGIT: // "\\p{Nd}"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L5614
				char_type = Character.getType(cp);
				switch (char_type) {
					case Character.DECIMAL_DIGIT_NUMBER:
						res = true;
						break;
					case Character.LETTER_NUMBER: // NOTE: LETTER_NUMBER / OTHER_NUMBER is not p{Nd}; FOOTNOTE:Superscript_is_not_a_DIGIT; ISSUE#:617; DATE:2019-11-24
					case Character.OTHER_NUMBER:
					default:
						res = false;
						break;
				}
				break;
			case CLASS_LOWER: // "\\p{Ll}"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L5607
				char_type = Character.getType(cp);
				res = char_type == Character.LOWERCASE_LETTER;
				break;
			case CLASS_PUNCT: // "\\p{P}"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L5653-L5659
				char_type = Character.getType(cp);
				switch (char_type) {
					case Character.DASH_PUNCTUATION:
					case Character.START_PUNCTUATION:
					case Character.END_PUNCTUATION:
					case Character.CONNECTOR_PUNCTUATION:
					case Character.OTHER_PUNCTUATION:
					case Character.INITIAL_QUOTE_PUNCTUATION:
					case Character.FINAL_QUOTE_PUNCTUATION:
						res = true;
						break;
					default:
						res = false;
						break;
				}
				break;
			case CLASS_SPACE: // "\\s"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L2436-L2438; https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/aa318070b27849f1fe00d14684b2a40f7b29bf79/jdk/src/share/classes/java/util/regex/UnicodeProp.java#L72-L75
				if ((cp >= 0x9 && cp <= 0xd) || cp == 0x85) {
					res = true;
				}
				else {
					char_type = Character.getType(cp);
					switch (char_type) {
						case Character.SPACE_SEPARATOR:
						case Character.LINE_SEPARATOR:
						case Character.PARAGRAPH_SEPARATOR:
						// case Character.CONNECTOR_PUNCTUATION:   // do not include CONNECTOR_PUNCTUATION b/c it includes "_"; ISSUE#:582 DATE:2019-09-28
							res = true;
							break;
						default:
							res = false; 
							break;
					}
				}
				break;
			case CLASS_UPPER: // "\\p{Lu}"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L5606
				char_type = Character.getType(cp);
				res = char_type == Character.UPPERCASE_LETTER;
				break;
			case CLASS_WORD: // "\\w"; REF:https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/regex/Pattern.java#L2459; https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/aa318070b27849f1fe00d14684b2a40f7b29bf79/jdk/src/share/classes/java/util/regex/UnicodeProp.java#L187-L194
				if (Character.isAlphabetic(cp)
					|| (cp == 0x200C || cp == 0x200D)) {
					res = true;
				}
				else {
					char_type = Character.getType(cp);
					switch (char_type) {
						case Character.NON_SPACING_MARK:
						case Character.ENCLOSING_MARK:
						case Character.COMBINING_SPACING_MARK:
						case Character.DECIMAL_DIGIT_NUMBER:
						case Character.CONNECTOR_PUNCTUATION:
						case Character.LETTER_NUMBER: // expand word to include LETTER_NUMBER / OTHER_NUMBER since Word should equal Letter + Number; ISSUE#:582 DATE:2019-09-28
						case Character.OTHER_NUMBER:
							res = true;
							break;
						default:
							res = false;
							break;
					}
				}
				break;
			case CLASS_HEX: // "[^0-9A-Fa-f０-９Ａ-Ｆａ-ｆ]"
				res = 
					(  (cp >=    48 && cp <=    57) // 0-9 
					|| (cp >=    65 && cp <=    70) // A-F
					|| (cp >=    97 && cp <=   102) // a-f
					|| (cp >= 65296 && cp <= 65305) // ０-９
					|| (cp >= 65313 && cp <= 65318) // Ａ-Ｆ
					|| (cp >= 65345 && cp <= 65350) // ａ-ｆ
					);
				break;
			case CLASS_NULL: // "\\x00" 
				res = cp == 0; 
				break;
			case CLASS_PRINT: // printable characters (MASK_ALPHA | MASK_DIGIT | MASK_PUNCT)
				char_type = Character.getType(cp);
				switch (char_type) {
					case Character.UPPERCASE_LETTER:
					case Character.LOWERCASE_LETTER:
					case Character.TITLECASE_LETTER:
					case Character.MODIFIER_LETTER:
					case Character.OTHER_LETTER:

					case Character.DECIMAL_DIGIT_NUMBER:

					case Character.DASH_PUNCTUATION:
					case Character.START_PUNCTUATION:
					case Character.END_PUNCTUATION:
					case Character.CONNECTOR_PUNCTUATION:
					case Character.OTHER_PUNCTUATION:
					case Character.INITIAL_QUOTE_PUNCTUATION:
					case Character.FINAL_QUOTE_PUNCTUATION:
						res = true;
						break;
					case Character.LETTER_NUMBER: // NOTE: LETTER_NUMBER / OTHER_NUMBER is not p{Nd}; FOOTNOTE:Superscript_is_not_a_DIGIT; ISSUE#:617; DATE:2019-11-24
					case Character.OTHER_NUMBER:
					default:
						res = false;
						break;
				}
				break;
			default: // escaped; EX: "%b" -> "b"
				return cls == cp;
		}
		return (cls_lower == cls) ? res : !res;
	}
	@Override public Str_char_matching_class Get_Match_class(int cls) {
		final int cls_lower = cls < 97 ? cls + 32 : cls;
		switch (cls_lower) {
			case CLASS_ALPHA:
				if (cls_lower == cls)
					return new Match_class_ALPHA_lower_unicode();
				else
					return new Match_class_ALPHA_upper_unicode();
			case CLASS_DIGIT:
				if (cls_lower == cls)
					return new Match_class_DIGIT_lower_unicode();
				else
					return new Match_class_DIGIT_upper_unicode();
			case CLASS_LOWER:
				if (cls_lower == cls)
					return new Match_class_LOWER_lower_unicode();
				else
					return new Match_class_LOWER_upper_unicode();
			case CLASS_UPPER:
				if (cls_lower == cls)
					return new Match_class_UPPER_lower_unicode();
				else
					return new Match_class_UPPER_upper_unicode();
			case CLASS_CTRL :
				if (cls_lower == cls)
					return new Match_class_CTRL_lower_unicode();
				else
					return new Match_class_CTRL_upper_unicode();
			case CLASS_PUNCT:
				if (cls_lower == cls)
					return new Match_class_PUNCT_lower_unicode();
				else
					return new Match_class_PUNCT_upper_unicode();
			case CLASS_SPACE:
				if (cls_lower == cls)
					return new Match_class_SPACE_lower_unicode();
				else
					return new Match_class_SPACE_upper_unicode();
			case CLASS_WORD :
				if (cls_lower == cls)
					return new Match_class_WORD_lower_unicode();
				else
					return new Match_class_WORD_upper_unicode();
			case CLASS_HEX  :
				if (cls_lower == cls)
					return new Match_class_HEX_lower_unicode();
				else
					return new Match_class_HEX_upper_unicode();
			case CLASS_NULL :
				if (cls_lower == cls)
					return new Match_class_NULL_lower_unicode();
				else
					return new Match_class_NULL_upper_unicode();
			case CLASS_PRINT:
				if (cls_lower == cls)
					return new Match_class_PRINT_lower_unicode();
				else
					return new Match_class_PRINT_upper_unicode();
			default:
				return new Match_class_the_rest(cls);
		}
	}
/*
== UstringLibrary_APPROXIMATION ==
* This approximates the MediaWiki UstringLibrary.php section below
* Most of the mapping from PHP to Java is based on https://www.regular-expressions.info/unicode.html
* Note that Java has different definitions of what different Unicode categories. Particularly:
  * CLASS_SPACE: should not have CONNECTOR_PUNCTUATION
  * CLASS_DIGIT: should not have LETTER_NUMBER, OTHER_NUMBER
  * CLASS_ALPHA: should have CASED_LETTER?

== Superscript_is_not_a_DIGIT ==
* %d is defined as "\p{Nd}"
** "'d' => '\p{Nd}',": https://github.com/wikimedia/mediawiki-extensions-Scribunto/blob/master/includes/engines/LuaCommon/UstringLibrary.php
* \p{Nd} is defined as "Decimal number"
** "Nd Decimal number": https://www.php.net/manual/en/regexp.reference.unicode.php
* Nd is defined as ASCII 0-9 plus 0-9 in other languages such as Arabic-Indic, Nko, Devanagari
** https://www.fileformat.info/info/unicode/category/Nd/list.htm
* Superscript 1 (¹) is defined as Other Number
** https://www.compart.com/en/unicode/U+00B9

=== PHP test code ===
<pre>
$pat = '/(\d\d\d+)+.* /';
$str = '1796¹ abc';
preg_match_all($pat, $str, $matches);
var_dump($matches);
// should output "1796" not "1796¹"
</pre>

=== MW test code ===
* https://en.wikipedia.org/w/index.php?title=Module:Sandbox/Gnosygnu&action=edit
<pre>
-- test ¹ is not a DIGIT
=mw.ustring.gsub("1796¹", '^%s*([%d][%d][%.%d]+).*$', '%1')
1796	1

-- test ٠ is a DIGIT (ARABIC-INDIC DIGIT ZERO)
=mw.ustring.gsub("1796٠", '^%s*([%d][%d][%.%d]+).*$', '%1')
1796٠	1

-- test ٠ is not a LETTER
=mw.ustring.gsub("17a٠", '^%s*([%d][%d][%.%a]+).*$', '%1')
17a	1
</pre>

=== Misc links ===
* PHP source for PCRE: https://github.com/php/php-src/blob/15cdc6d709fd479dfacf9a3a998f64e8dd562e17/ext/pcre/pcre2lib/pcre2_dfa_match.c

=== /extensions/Scribunto/engines/LuaCommon/UstringLibrary.php ===
// If you change these, also change lualib/ustring/make-tables.php
// (and run it to regenerate charsets.lua)
'a' => '\p{L}',
'c' => '\p{Cc}',
'd' => '\p{Nd}',
'l' => '\p{Ll}',
'p' => '\p{P}',
's' => '\p{Xps}',
'u' => '\p{Lu}',
'w' => '[\p{L}\p{Nd}]',
'x' => '[0-9A-Fa-f０-９Ａ-Ｆａ-ｆ]',
'z' => '\0',
*/
}
class Match_class_ALPHA_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.UPPERCASE_LETTER:
			case Character.LOWERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:
				return true;
			default:
				return false;
		}
	}
}
class Match_class_ALPHA_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.UPPERCASE_LETTER:
			case Character.LOWERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:
				return false;
			default:
				return true;
		}
	}
}
class Match_class_DIGIT_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.DECIMAL_DIGIT_NUMBER:
				return true;
			case Character.LETTER_NUMBER: // NOTE: LETTER_NUMBER / OTHER_NUMBER is not p{Nd}; FOOTNOTE:Superscript_is_not_a_DIGIT; ISSUE#:617; DATE:2019-11-24
			case Character.OTHER_NUMBER:
			default:
				return false;
		}
	}
}
class Match_class_DIGIT_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.DECIMAL_DIGIT_NUMBER:
				return false;
			case Character.LETTER_NUMBER: // NOTE: LETTER_NUMBER / OTHER_NUMBER is not p{Nd}; FOOTNOTE:Superscript_is_not_a_DIGIT; ISSUE#:617; DATE:2019-11-24
			case Character.OTHER_NUMBER:
			default:
				return true;
		}
	}
}
class Match_class_LOWER_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return (char_type == Character.LOWERCASE_LETTER);
	}
}
class Match_class_LOWER_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return (char_type != Character.LOWERCASE_LETTER);
	}
}
class Match_class_UPPER_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return (char_type == Character.UPPERCASE_LETTER);
	}
}
class Match_class_UPPER_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return (char_type != Character.UPPERCASE_LETTER);
	}
}
class Match_class_CTRL_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return (char_type == Character.CONTROL);
	}
}
class Match_class_CTRL_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return (char_type != Character.CONTROL);
	}
}
class Match_class_PUNCT_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.DASH_PUNCTUATION:
			case Character.START_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
				return true;
			default:
				return false;
		}
	}
}
class Match_class_PUNCT_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.DASH_PUNCTUATION:
			case Character.START_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
				return false;
			default:
				return true;
		}
	}
}
class Match_class_SPACE_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		if ((cp >= 0x9 && cp <= 0xd) || cp == 0x85) {
			return true;
		}
		else {
			int char_type = Character.getType(cp);
			switch (char_type) {
				case Character.SPACE_SEPARATOR:
				case Character.LINE_SEPARATOR:
				case Character.PARAGRAPH_SEPARATOR:
				// case Character.CONNECTOR_PUNCTUATION:   // do not include CONNECTOR_PUNCTUATION b/c it includes "_"; ISSUE#:582 DATE:2019-09-28
					return true;
				default:
					return false; 
			}
		}
	}
}
class Match_class_SPACE_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		if ((cp >= 0x9 && cp <= 0xd) || cp == 0x85) {
			return false;
		}
		else {
			int char_type = Character.getType(cp);
			switch (char_type) {
				case Character.SPACE_SEPARATOR:
				case Character.LINE_SEPARATOR:
				case Character.PARAGRAPH_SEPARATOR:
				// case Character.CONNECTOR_PUNCTUATION:   // do not include CONNECTOR_PUNCTUATION b/c it includes "_"; ISSUE#:582 DATE:2019-09-28
					return false;
				default:
					return true; 
			}
		}
	}
}
class Match_class_WORD_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		if (Character.isAlphabetic(cp)
			|| (cp == 0x200C || cp == 0x200D)) {
			return true;
		}
		else {
			int char_type = Character.getType(cp);
			switch (char_type) {
				case Character.NON_SPACING_MARK:
				case Character.ENCLOSING_MARK:
				case Character.COMBINING_SPACING_MARK:
				case Character.DECIMAL_DIGIT_NUMBER:
				case Character.CONNECTOR_PUNCTUATION:
				case Character.LETTER_NUMBER: // expand word to include LETTER_NUMBER / OTHER_NUMBER since Word should equal Letter + Number; ISSUE#:582 DATE:2019-09-28
				case Character.OTHER_NUMBER:
					return true;
				default:
					return false;
			}
		}
	}
}
class Match_class_WORD_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		if (Character.isAlphabetic(cp)
			|| (cp == 0x200C || cp == 0x200D)) {
			return false;
		}
		else {
			int char_type = Character.getType(cp);
			switch (char_type) {
				case Character.NON_SPACING_MARK:
				case Character.ENCLOSING_MARK:
				case Character.COMBINING_SPACING_MARK:
				case Character.DECIMAL_DIGIT_NUMBER:
				case Character.CONNECTOR_PUNCTUATION:
				case Character.LETTER_NUMBER: // expand word to include LETTER_NUMBER / OTHER_NUMBER since Word should equal Letter + Number; ISSUE#:582 DATE:2019-09-28
				case Character.OTHER_NUMBER:
					return false;
				default:
					return true;
			}
		}
	}
}
class Match_class_HEX_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return 
			(  (cp >=    48 && cp <=    57) // 0-9 
			|| (cp >=    65 && cp <=    70) // A-F
			|| (cp >=    97 && cp <=   102) // a-f
			|| (cp >= 65296 && cp <= 65305) // ０-９
			|| (cp >= 65313 && cp <= 65318) // Ａ-Ｆ
			|| (cp >= 65345 && cp <= 65350) // ａ-ｆ
			);
	}
}
class Match_class_HEX_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		return 
			!(  (cp >=    48 && cp <=    57) // 0-9 
			|| (cp >=    65 && cp <=    70) // A-F
			|| (cp >=    97 && cp <=   102) // a-f
			|| (cp >= 65296 && cp <= 65305) // ０-９
			|| (cp >= 65313 && cp <= 65318) // Ａ-Ｆ
			|| (cp >= 65345 && cp <= 65350) // ａ-ｆ
			);
	}
}
class Match_class_NULL_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		return (cp == 0);
	}
}
class Match_class_NULL_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		return (cp != 0);
	}
}
class Match_class_PRINT_lower_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.UPPERCASE_LETTER:
			case Character.LOWERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:

			case Character.DECIMAL_DIGIT_NUMBER:

			case Character.DASH_PUNCTUATION:
			case Character.START_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
				return true;
			case Character.LETTER_NUMBER: // NOTE: LETTER_NUMBER / OTHER_NUMBER is not p{Nd}; FOOTNOTE:Superscript_is_not_a_DIGIT; ISSUE#:617; DATE:2019-11-24
			case Character.OTHER_NUMBER:
			default:
				return false;
		}
	}
}
class Match_class_PRINT_upper_unicode extends Str_char_matching_class {
	@Override public boolean Match(int cp) {
		int char_type = Character.getType(cp);
		switch (char_type) {
			case Character.UPPERCASE_LETTER:
			case Character.LOWERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:

			case Character.DECIMAL_DIGIT_NUMBER:

			case Character.DASH_PUNCTUATION:
			case Character.START_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
				return false;
			case Character.LETTER_NUMBER: // NOTE: LETTER_NUMBER / OTHER_NUMBER is not p{Nd}; FOOTNOTE:Superscript_is_not_a_DIGIT; ISSUE#:617; DATE:2019-11-24
			case Character.OTHER_NUMBER:
			default:
				return true;
		}
	}
}
