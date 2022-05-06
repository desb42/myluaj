package org.luaj.vm2.lib;

import org.luaj.vm2.Buffer;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;
import gplx.objects.strings.char_sources.*;

public class Match_state {
	private final Str_find_mgr find_mgr;
	private final Str_char_class_mgr char_class_mgr;
	private final Char_source src;
	private final Char_source pat;
	private final int src_len;
	private final int pat_len;
	private /*final*/ int[] capture_bgns;
	private /*final*/ int[] capture_lens;
	private int counter;
	private int maxcounter;
	private int level;
	private int stretch;
	private boolean text_first;
	private int first_char;
	private int first_pos;
	public int Stretch() { return stretch; }

	public Match_state(Str_find_mgr find_mgr) {
		this.find_mgr = find_mgr;
		this.char_class_mgr = find_mgr.Char_class_mgr();
		this.src = find_mgr.src;
		this.src_len = find_mgr.src_len;
		this.pat = find_mgr.pat;
		this.pat_len = find_mgr.pat_len;
                //this.pat_eps = new  byte[pat_len];
		this.level = 0;
		this.capture_bgns = new int[MAX_CAPTURES];
		this.capture_lens = new int[MAX_CAPTURES];
		this.counter = 0;
		this.maxcounter = src_len*100; //??? is that enough
		if (maxcounter < 150000) maxcounter = 1500000;
		this.first_pos = 0;
                //System.out.println(pat.Src() + " src_len:" + Integer.toString(src_len) + " s:" + src.Src());
/*
//                if (pat_len > 2 && pat.Get_data( 0 ) != '^' && pat.Get_data(pat_len -1) == '$') {
                if (src_len > 0 && src_len < 200) {
                System.out.println(removeUnicode(pat.Src()) + " src_len:" + Integer.toString(src_len) + " s:" + removeUnicode(src.Src()));
                }
                else
                System.out.println(removeUnicode(pat.Src()) + " src_len:" + Integer.toString(src_len));
                if (pat_len == 1 && pat.Get_data( 0 ) == '#') {//127) {// && pat_len == 1) {
                    int a=1;
                }
                if (src_len == 0) { //src_len == 32 && src.Get_data(0) == 'D') {
                    int a=1;
                }
                //^[a-zA-Z0-9]+$
                if (pat_len == 14 && pat.Get_data( 0 ) == '^' && pat.Get_data( 1 ) == '[' && pat.Get_data( 2 ) == 'a') {//pat.Get_data( 3 ) == 'a') { //pat.equals("^(%a%a%a?)%-(%a%a%a%a)%-(%a%a)%-(%d%d%d%d)$")) {
                    int a=1;
                }
//                }
*/
/*
                if (pat_len > 10 && pat.Get_data( 0 ) == '[' && pat.Get_data( 1 ) == '%' && pat.Get_data( 2 ) == '.') {//pat.Get_data( 3 ) == 'a') { //pat.equals("^(%a%a%a?)%-(%a%a%a%a)%-(%a%a)%-(%d%d%d%d)$")) {
                if (src_len > 0 && src_len < 200) {
                System.out.println(removeUnicode(pat.Src()) + " src_len:" + Integer.toString(src_len) + " s:" + removeUnicode(src.Src()));
                }
                else
                System.out.println(removeUnicode(pat.Src()) + " src_len:" + Integer.toString(src_len));
                    int a=1;
                }
*/
                if (pat_len > 0) {
			first_char = pat.Get_data( first_pos );
			if (first_char == '(') { // allow for '(', '()', '(()', '()('
				first_pos++;
				first_char = pat.Get_data( first_pos );
				if (first_char == ')') {
					first_pos++;
					first_char = pat.Get_data( first_pos );
					if (first_char == '(') {
						first_pos++;
						first_char = pat.Get_data( first_pos );
					}
				}
				else if (first_char == '(') {
					first_pos++;
					first_char = pat.Get_data( first_pos );
					if (first_char == ')') {
						first_pos++;
						first_char = pat.Get_data( first_pos );
					}
				}
			}
			switch (first_char) {
				case '%':
					int second_char = pat.Get_data( first_pos + 1 );
					//if (second_char == 'b' || second_char == 'f')
					//	break;
					if (second_char == 'b') {
						first_char = pat.Get_data( first_pos + 2 );
						text_first = true;
						break;
					}
					if (second_char == 'f') {
						first_char = 4;
						text_first = true;
						break;
					}
					if (first_pos + 2 < pat_len) {
						int third_char = pat.Get_data( first_pos + 2 );
						if (third_char != '?' && third_char != '*' && third_char != '-') {
							text_first = true;
						}
					}
					else {
						text_first = true;
					}
					switch (second_char) {
						case '(': case ')': case '.': case '"': // % checks %(%)%.%%%+%-%*%?%[%^%$%]
						case '%': case '+': case '-':
						case '*': case '?': case '[':
						case '^': case '$': case ']':
							first_char = second_char;
							break;
						default:
							first_char = 2;
							break;
					}
					break;
				case '$':
					if (first_pos + 1 == pat_len) { // is the `$' the last char in pat?
						first_char = 3;
						text_first = true;
					}
					else {
						second_char = pat.Get_data( first_pos + 1 );
						if (second_char != '?' && second_char != '*' && second_char != '-')
							text_first = true;
					}
					break;
				case '[':
					int ep = classend(first_pos);
					if (ep < pat_len) {
						second_char = pat.Get_data( ep );
						if (second_char != '?' && second_char != '*' && second_char != '-') {
							text_first = true;
							first_char = 1;
						}
					}
					else {
						text_first = true;
						first_char = 1;
					}
					break;
				case '^': case ']':
				case '(': case ')': case '.': case '*':
				case '?': case '+':
					break;
				case 1: case 2: case 3: // ignore trigger chars
					break;
				default:
					if (first_pos + 1 < pat_len) {
						second_char = pat.Get_data( first_pos + 1 );
						if (second_char != '?' && second_char != '*' && second_char != '-')
							text_first = true;
					}
					else
						text_first = true;
					break;
			}
		}
	}
	private static int[] firsts = new int[] { 1,1,1,1 };

	public void reset() {
		level = 0;
		counter = 0;
		find_mgr.reset();
	}

	private void add_s(Buffer lbuf, LuaString new_s, int str_off, int str_end) {
		int l = new_s.length();
		for (int i = 0; i < l; i++) {
			byte b = (byte)new_s.Get_data(i);
			if (b != StringLib.L_ESC) {
				lbuf.append((byte)b);
			} else {
				i++; // skip ESC
				if (i == l) {// handle ESC at EOS; ISSUE#:571; DATE:2019-09-08
					lbuf.append(StringLib.L_ESC_STRING);
					break;
				}
				b = (byte)new_s.Get_data(i);
				if (!Character.isDigit((char)b)) {
					lbuf.append(b);
				} else if (b == '0') {
					lbuf.append(src.Substring(str_off, str_end));
				} else {
					lbuf.append(push_onecapture(false, b - '1', str_off, str_end).strvalue());
				}
			}
		}
	}

	/*
	public void add_value_old(Buffer lbuf, int src_pos, int str_end, LuaValue repl) {
		switch (repl.type()) {
			case LuaValue.TNUMBER:
			case LuaValue.TSTRING:
				add_s(lbuf, repl.strvalue(), src_pos, str_end);
				return;	
			case LuaValue.TFUNCTION:
				Varargs n = push_captures_old(true, src_pos, str_end);
				repl = repl.invoke(n).arg1();
				break;
			case LuaValue.TTABLE:
				// Need to call push_onecapture here for the error checking
				repl = repl.get(push_onecapture_old(0, src_pos, str_end));
				break;

			default:
				LuaValue.error("bad argument: string/function/table expected");
				return;
		}

		if (!repl.toboolean()) { // nil or false? 
			repl = src.Src().substring(src_pos, str_end); // keep original text
		} else if (!repl.isstring()) {
			LuaValue.error("invalid replacement value (a " + repl.typename() + ")");
		}
		lbuf.append(repl.strvalue()); // add result to accumulator
	}
	*/
	public void add_value(Buffer lbuf, int src_pos, int str_end, LuaValue repl) {
		switch (repl.type()) {
			case LuaValue.TNUMBER:
			case LuaValue.TSTRING:
				add_s(lbuf, repl.strvalue(), src_pos, str_end);
				return;	
			case LuaValue.TFUNCTION:
				Varargs n = push_captures(true, src_pos, str_end);
				repl = repl.invoke(n).arg1();
				break;
			case LuaValue.TTABLE:
				// Need to call push_onecapture here for the error checking
				repl = repl.get(push_onecapture(false, 0, src_pos, str_end));
				break;

			default:
				LuaValue.error("bad argument: string/function/table expected");
				return;
		}

		if (!repl.toboolean()) { // nil or false?
			// TOMBSTONE: was LuaValue.valueOf(src.Substring(src_pos, str_end)), but this fails for multi-byte chars in LUA mode (not XOWA mode)
			repl = LuaValue.valueOf(src.SubstringAsBry(src_pos, str_end)); // keep original text
		} else if (!repl.isstring()) {
			LuaValue.error("invalid replacement value (a " + repl.typename() + ")");
		}
		lbuf.append(repl.strvalue()); // add result to accumulator
	}

	/*
	private LuaValue push_onecapture_old(int i, int src_pos, int end) {
		if (i >= this.level) {
			if (i == 0) {
				return src.Src().substring(src_pos, end);
			} else {
				throw new LuaError("invalid capture index");
			}
		} else {
			int l = capture_lens[i];
			if (l == CAP_UNFINISHED) {
				throw new LuaError("unfinished capture");
			}
			if (l == CAP_POSITION) {
				return LuaValue.valueOf(capture_bgns[i] + 1);
			} else {
				int begin = capture_bgns[i];
				return src.Src().substring(begin, begin + l);
			}
		}
	}
	*/
	private LuaValue push_onecapture(boolean register_capture, int i, int src_pos, int end) {
		if (i >= this.level) {
			if (i == 0) {
				return find_mgr.Capture__make__string(register_capture, src_pos, end);
			} else {
				throw new LuaError("invalid capture index");
			}
		} else {
			int capture_len = capture_lens[i];
			if (capture_len == CAP_UNFINISHED) {
				throw new LuaError("unfinished capture");
			}
			int capture_bgn = capture_bgns[i];
			if (capture_len == CAP_POSITION) {
				// assert register_capture is true; refactor code to remove register_capture from Capture__position after next enwiki build
//				if (!register_capture) {
//					throw new LuaError("LUAJ_XOWA:register capture should always be true");
//				}
				// NOTE: +1 to normalize capture to base1; ISSUE#:726; DATE:2020-05-17;
				// REF.LUA: https://www.lua.org/source/5.1/lstrlib.c.html
				//   if (l == CAP_POSITION)
				//     lua_pushinteger(ms->L, ms->capture[i].init - ms->src_init + 1);
				// REF.LUAJ: https://github.com/luaj/luaj/blob/master/src/core/org/luaj/vm2/lib/StringLib.java#L954
				return find_mgr.Capture__position(register_capture, capture_bgn + Str_find_mgr.Base_1);
			} else {
				return find_mgr.Capture__make__string(register_capture, capture_bgn, capture_bgn + capture_len);
			}
		}
	}
	
	/*
	public Varargs push_captures_old(boolean wholeMatch, int src_pos, int end) {
		int nlevels = (this.level == 0 && wholeMatch) ? 1 : this.level;
		switch (nlevels) {
			case 0: return LuaValue.NONE;
			case 1: return push_onecapture_old(0, src_pos, end);
		}
		LuaValue[] v = new LuaValue[nlevels];
		for (int i = 0; i < nlevels; ++i)
			v[i] = push_onecapture_old(i, src_pos, end);
		return LuaValue.varargsOf(v);
	}
	*/
	public Varargs push_captures(boolean wholeMatch, int src_pos, int end) {
		int nlevels = (this.level == 0 && wholeMatch) ? 1 : this.level;		
		if (nlevels == 0) {
			return find_mgr.Captures__make__none();
		}
		else {
			find_mgr.Captures__init(nlevels);
			for (int i = 0; i < nlevels; ++i)
				push_onecapture(true, i, src_pos, end);
		}
		return find_mgr.Captures__make__many();
	}


	private int check_capture(int l) {
		l -= '1'; // NOTE: '1' b/c Lua uses %1 to means captures[0]
		if (l < 0 || l >= level || this.capture_lens[l] == CAP_UNFINISHED) {
			LuaValue.error("invalid capture index");
		}
		return l;
	}

	private int capture_to_close() {
		int level = this.level;
		for (level--; level >= 0; level--)
			if (capture_lens[level] == CAP_UNFINISHED)
				return level;
		LuaValue.error("invalid pat capture");
		return 0;
	}

	private int classend(int pat_pos) {
		switch (pat.Get_data(pat_pos++)) {
			case StringLib.L_ESC:
				if (pat_pos == pat_len) {
					LuaValue.error("malformed pat (ends with %)");
				}
				return pat_pos + 1;	
			case '[':
                            
				if (pat.Get_data(pat_pos) == '^')
					pat_pos++;
				do {
					if (pat_pos == pat_len) {
						LuaValue.error("malformed pat (missing])");
					}
					if (pat.Get_data(pat_pos++) == StringLib.L_ESC && pat_pos != pat_len)
						pat_pos++;
				} while (pat.Get_data(pat_pos) != ']');
				return pat_pos + 1;	
			default:
				return pat_pos;	
		}
	}

	private boolean matchbracketclass(int cur, int pat_pos, int ep) {
		boolean sig = true;
		if (pat.Get_data(pat_pos + 1) == '^') {
			sig = false;
			pat_pos++;
		}
		while (++pat_pos < ep) {
			int pcode = pat.Get_data(pat_pos);
			if (pcode == StringLib.L_ESC) {
				pat_pos++;
				if (char_class_mgr.Match_class(cur, pat.Get_data(pat_pos)))
					return sig;
			}
			else if ((pat.Get_data(pat_pos + 1) == '-') && (pat_pos + 2 < ep)) {
				pat_pos += 2;
				if (pcode <= cur && cur <= pat.Get_data(pat_pos))
					return sig;
			}
			else if (pcode == cur) return sig;
		}
		return !sig;
	}
	private boolean matchbracketclass(int cur, int pat_pos, int ep, boolean sig) { // 20220227 'inverse' logic moved elsewhere
		while (++pat_pos < ep) {
			int pcode = pat.Get_data(pat_pos);
			if (pcode == StringLib.L_ESC) {
				pat_pos++;
				if (char_class_mgr.Match_class(cur, pat.Get_data(pat_pos)))
					return sig;
			}
			else if ((pat.Get_data(pat_pos + 1) == '-') && (pat_pos + 2 < ep)) {
				pat_pos += 2;
				if (pcode <= cur && cur <= pat.Get_data(pat_pos))
					return sig;
			}
			else if (pcode == cur) return sig;
		}
		return !sig;
	}

	private boolean singlematch(int cur, int pat_pos, int ep) {
		int pcode = pat.Get_data(pat_pos);
		switch (pcode) {
			case '.': return true;
			case StringLib.L_ESC: return char_class_mgr.Match_class(cur, pat.Get_data(pat_pos + 1));
			case '[': return matchbracketclass(cur, pat_pos, ep - 1);
			default: return pcode == cur;
		}
	}

	private int matchbalance(int src_pos, int pat_pos) {
		if (pat_pos == pat_len || pat_pos + 1 == pat_len) {
			LuaValue.error("unbalanced pat");
		}
		if (src_pos >= src.Len_in_data()) return NULL;	// XOWA: check bounds; EX:string_match('a', '^(.) ?%b()'); DATE:2014-08-13
		if (src.Get_data(src_pos) != pat.Get_data(pat_pos))
			return NULL;
		else {
			int balance_bgn = pat.Get_data(pat_pos);
			int balance_end = pat.Get_data(pat_pos + 1);
			int balance_count = 1;
			while (++src_pos < src_len) {
				if (src.Get_data(src_pos) == balance_end) {
					if (--balance_count == 0)
						return src_pos + 1;
				}
				else if (src.Get_data(src_pos) == balance_bgn)
					balance_count++;
			}
		}
		return NULL;
	}

	private int max_expand(int src_pos, int pat_pos, int ep, int i) {
		// i counts maximum expand for item
		int pcode = pat.Get_data(pat_pos);
		switch (pcode) {
			case '[':
				boolean sig = true;
				if (pat.Get_data(pat_pos + 1) == '^') {
					sig = false;
					pat_pos++;
				}
				while	( src_pos + i < src_len
					&&	matchbracketclass(src.Get_data(src_pos + i), pat_pos, ep - 1, sig))
					i++;
				break;
			case '.':
				i = src_len - src_pos;
				break;
			case StringLib.L_ESC:
				int pat_code = pat.Get_data(pat_pos + 1);
				Str_char_matching_class mc = char_class_mgr.Get_Match_class(pat_code);
				while	( src_pos + i < src_len
					&&	mc.Match(src.Get_data(src_pos + i)) ) //char_class_mgr.Match_class(src.Get_data(src_pos + i), pat_code))
					i++;
				break;
			default:
				while	( src_pos + i < src_len
					&&	pcode == src.Get_data(src_pos + i))
					i++;
				break;
		}

		// keeps trying to match with the maximum repetitions 
		int max_i = i;
                int stretch = this.stretch;
		while (i >= 0) {
			int res = i_match(src_pos + i, ep + 1);
			if (res != NULL) {
                            this.stretch = stretch;
				return res;
                        }
			i--; // else didn't match; reduce 1 repetition to try again
		}
		// can we skip max_i? as there have been no matches??????
		// [aeiou][^aeiou]*$ this.stretch = src_pos + max_i; //???????????????????????
		return NULL;
	}

	private int min_expand(int src_pos, int pat_pos, int ep) {
		int src_len = src.Len_in_data();	// XOWA: cache string length; DATE: 2014-08-13
		int pcode = pat.Get_data(pat_pos);
		boolean sig = true;
		Str_char_matching_class mc = null;
		switch (pcode) {
			case '[':
				if (pat.Get_data(pat_pos + 1) == '^') {
					sig = false;
					pat_pos++;
				}
				break;
			case StringLib.L_ESC:
				mc = char_class_mgr.Get_Match_class(pat.Get_data(pat_pos + 1));
				break;
		}
		for (;;) {
			int res = i_match(src_pos, ep + 1);
			if (res != NULL)
				return res;
			else if (src_pos < src_len) {
				boolean bv;
				switch (pcode) {
					case '[':
						bv = matchbracketclass(src.Get_data(src_pos), pat_pos, ep - 1, sig);
						break;
					case '.':
						bv = true;
						break;
					case StringLib.L_ESC:
						bv = mc.Match(src.Get_data(src_pos));
						break;
					default:
						bv = (pcode == src.Get_data(src_pos));
						break;
				}
				if (bv)
					src_pos++;
				else
					return NULL;
			}
			else
				return NULL;
		}
	}

	private int start_capture(int src_pos, int pat_pos, int what) {
		int res;
		int level = this.level;
		if (level >= MAX_CAPTURES) {
                        System.out.println(this.pat.Src());
			LuaValue.error("too many captures");
			int[] l_capture_bgns = new int[MAX_CAPTURES + 32];
			int[] l_capture_lens = new int[MAX_CAPTURES + 32];
			for (int i = 0; i < MAX_CAPTURES; i++) {
				l_capture_bgns[i] = this.capture_bgns[i];
				l_capture_lens[i] = this.capture_lens[i];
			}
			MAX_CAPTURES += 32;
			this.capture_bgns = l_capture_bgns;
			this.capture_lens = l_capture_lens;
		}
		capture_bgns[level] = src_pos;
		capture_lens[level] = what;
		this.level = level + 1;
		if ((res = i_match(src_pos, pat_pos)) == NULL) // match failed?
			this.level--; // undo capture
		return res;
	}

	private int end_capture(int src_pos, int pat_pos) {
		int l = capture_to_close();
		int res;
		capture_lens[l] = src_pos - capture_bgns[l]; // close capture
		if ((res = i_match(src_pos, pat_pos)) == NULL) // match failed?
			capture_lens[l] = CAP_UNFINISHED; // undo capture
		return res;
	}

	private int match_capture(int src_pos, int l) {
		l = check_capture(l);
		int len = capture_lens[l];
		
		if 	((src_len - src_pos) >= len
//			&& LuaString.equals(src, capture_bgns[l], src, src_pos, len))
			&& src.Eq(capture_bgns[l], src, src_pos, len)
			)
			return src_pos + len;
		else
			return NULL;
	}

	// Perform pat matching. If there is a match, returns offset into src
	// where match ends, otherwise returns -1.
	public int match(int src_pos, int pat_pos) {
		this.stretch = src_pos;
		if (pat_pos == 0 && text_first) {
			switch (first_char) {
				case 1: // [...] in first pos
					int ep = classend(first_pos) - 1;
					int local_pat_pos = first_pos;
					boolean sig = true;
					if (pat.Get_data(local_pat_pos + 1) == '^') {
						sig = false;
						local_pat_pos++;
					}
					while (src_pos < src_len) {
						if (matchbracketclass(src.Get_data(src_pos), local_pat_pos, ep, sig)) {
							break;
						}
						else
							src_pos++;
					}
					break;
				case 2: // %[a...] in first position
					int local_pat = pat.Get_data(first_pos + 1);
					Str_char_matching_class mc = char_class_mgr.Get_Match_class(local_pat);
					while (src_pos < src_len) {
						if ( mc.Match(src.Get_data(src_pos))) { //char_class_mgr.Match_class(src.Get_data(src_pos), local_pat)) {
							break;
						}
						else
							src_pos++;
					}
					break;
				case 3: // $ as the only character
					src_pos = src.Len_in_data();
					break;
				case 4: { // frontier
					local_pat_pos = first_pos + 2;
					if (pat.Get_data(local_pat_pos) != '[') {
						LuaValue.error("Missing [after %f in pat");
					}
					ep = classend(local_pat_pos);
					sig = true;
					if (pat.Get_data(local_pat_pos + 1) == '^') {
						sig = false;
						local_pat_pos++;
					}
					int previous = (src_pos == 0) ? -1 : src.Get_data(src_pos - 1);
					while (true) {
						int next = (src_pos == src.Len_in_data()) ? '\0' : src.Get_data(src_pos);
						if ( matchbracketclass(previous, local_pat_pos, ep - 1, sig)
						||  !matchbracketclass(next    , local_pat_pos, ep - 1, sig)) {
							if (++src_pos < src_len) {
								previous = next;
							}
							else {
								src_pos = src_len; //ugh
								break;
							}
						}
						else
							break;
					}
					break;
				}
				default:
					while (src_pos < src_len) {
						if (src.Get_data(src_pos) == first_char) {
							break;
						}
						else
							src_pos++;
					}
					break;
			}
			this.stretch = src_pos;
			//if (src_pos == src_len) {
			//	return NULL;
			//}
		}
		return i_match(src_pos, pat_pos);
	}
	private int i_match(int src_pos, int pat_pos) {
		while (true) {
			// Check if we are at the end of the pat - 
			// equivalent to the '\0' case in the C version, but our pat
			// string is not NUL-terminated.
			if (pat_pos == pat_len)
				return src_pos;
			int pat_chr = pat.Get_data(pat_pos);
			if (++counter > maxcounter) { //150000) { //
				throw new LuaError("catastrophic backtrack");
			}
			switch (pat_chr) {
				case '(': // start capture
					if (++pat_pos < pat_len && pat.Get_data(pat_pos) == ')') // position capture?
						return start_capture(src_pos, pat_pos + 1, CAP_POSITION);
					else
						return start_capture(src_pos, pat_pos, CAP_UNFINISHED);
				case ')': // end capture
					return end_capture(src_pos, pat_pos + 1);
				case StringLib.L_ESC:
					if (pat_pos + 1 == pat_len)
						LuaValue.error("malformed pat (ends with '%')");
					int c = pat.Get_data(pat_pos + 1);
					switch (c) {
						case 'b': // balanced string?
							src_pos = matchbalance(src_pos, pat_pos + 2);
							if (src_pos == NULL) return NULL;
							pat_pos += 4; // NOTE assumes <> are ASCII length %b<> 
							continue;
						case 'f': {// frontier?
							pat_pos += 2;
							if (pat.Get_data(pat_pos) != '[') {
								LuaValue.error("Missing [after %f in pat");
							}
							int ep = classend(pat_pos);
							boolean sig = true;
							if (pat.Get_data(pat_pos + 1) == '^') {
								sig = false;
								pat_pos++;
							}
							int previous = (src_pos == 0) ? -1 : src.Get_data(src_pos - 1);
							// NOTE: reinstated `next` variable declaration (must have been lost in refactoring); ISSUE#:732; DATE:2020-05-29
							// REF.LUA:https://www.lua.org/source/5.1/lstrlib.c.html
							// REF.LUAJ:https://github.com/luaj/luaj/blob/master/src/core/org/luaj/vm2/lib/StringLib.java
							int next = (src_pos == src.Len_in_data()) ? '\0' : src.Get_data(src_pos);
							// XOWA:
							// * DATE:2014-08-14: added bounds check of "src_pos < src.m_length"
							// * DATE:2016-01-28: changed "matchbracketclass" to "!matchbracketclass"; PAGE:en.w:A
							// * DATE:2020-05-29: removed "src_pos < src.Len_in_data() && "; ISSUE#:732
							if ( matchbracketclass(previous, pat_pos, ep - 1, sig)
							||  !matchbracketclass(next    , pat_pos, ep - 1, sig)) {
								return NULL;
							}
							pat_pos = ep;
							continue;
						}
						default: {
							if (Character.isDigit((char) c)) {
								src_pos = match_capture(src_pos, c);
								if (src_pos == NULL)
									return NULL;
								return i_match(src_pos, pat_pos + 2);
							}
						}
					}
					break;
				case '$':
					if (pat_pos + 1 == pat_len) // is the `$' the last char in pat?
						return (src_pos == src_len) ? src_pos : NULL; // check end of string
			}
			
			int ep = classend(pat_pos);
			boolean m = src_pos < src_len && singlematch(src.Get_data(src_pos), pat_pos, ep);
			pat_chr = (ep < pat_len) ? pat.Get_data(ep) : '\0';
			if (!m) {
				switch (pat_chr) {
					case '*': case '?': case '-':
						pat_pos = ep + 1;
						break;
					default:   /* '+' or no suffix */
						return NULL;  /* fail */
				}
			}
			else {  /* matched once */
				switch (pat_chr) {  /* handle optional suffix */
					case '?':  /* optional */
						int res;
						if (((res = i_match(src_pos + 1, ep + 1)) != NULL))
							return res;
						pat_pos = ep + 1;
						break;
					case '+':  /* 1 or more repetitions */
						src_pos++;  /* 1 match already done */
						return max_expand(src_pos, pat_pos, ep, 0);
					case '*':  /* 0 or more repetitions */
						return max_expand(src_pos, pat_pos, ep, 1);
					case '-':  /* 0 or more repetitions (minimum) */
						return min_expand(src_pos, pat_pos, ep);
					default:  /* no suffix */
						src_pos++;
						pat_pos = ep;
						break;
				}
			}
		}
	}

	private static final int NULL = -1;
	private static /*final*/ int MAX_CAPTURES = 64;
	private static final int CAP_UNFINISHED = -1;
	public static final int CAP_POSITION = -2;

public static String removeUnicode(String input){
    StringBuffer buffer = new StringBuffer(input.length());
    for (int i =0; i < input.length(); i++){
        int chr = (int)input.charAt(i);
        if ( chr > 256){
        buffer.append("\\u").append(Integer.toHexString(chr));
        } else {
            if ( chr == '\n'){
                buffer.append("\\n");
            } else if(chr == '\t'){
                buffer.append("\\t");
            } else if(chr == '\r'){
                buffer.append("\\r");
            } else if(chr == '\b'){
                buffer.append("\\b");
            } else if(chr == '\f'){
                buffer.append("\\f");
            } else if(chr == '\''){
                buffer.append("\\'");
            } else if(chr == '\"'){
                buffer.append("\\");
            } else if(chr == '\\'){
                buffer.append("\\\\");
            } else {
            	if (chr < 32 || chr > 126)
                buffer.append("\\x").append(Integer.toHexString(chr));
              else
                buffer.append((char)chr);
            }
        }
    }
    return buffer.toString();
}
	private Db_pattern compile(int pat_pos, int ep) {
		Db_pattern newpat = new Db_pattern(ep - pat_pos + 5);
		int pcode = pat.Get_data(pat_pos++);
		switch (pcode) {
			case '.':
				newpat.Add(Db_pattern.DOT);
				break;
			case StringLib.L_ESC:
				newpat.Add(Db_pattern.ESC);
				newpat.Add(pat.Get_data(pat_pos));
				break;
			case '[':
				matchbracketclass_compile(newpat, pat_pos, ep - 1);
				break;
			default:
				newpat.Add(Db_pattern.SINGLECHAR);
				newpat.Add(pcode);
		}
		return newpat;
	}
	private void matchbracketclass_compile(Db_pattern newpat, int pat_pos, int ep) {
		int pcode = pat.Get_data(pat_pos++);
		if (pcode == '^') {
			newpat.Add(Db_pattern.NEGMATCH);
			pcode = pat.Get_data(pat_pos++);
		}
		else
			newpat.Add(Db_pattern.POSMATCH);
		while (true) {
			if (pcode == StringLib.L_ESC) {
				newpat.Add(Db_pattern.ESC);
				newpat.Add(pat.Get_data(pat_pos++));
			}
			else if ((pat.Get_data(pat_pos) == '-') && (pat_pos + 1 < ep)) {
				newpat.Add(Db_pattern.RANGE);
				newpat.Add(pcode);
                                pat_pos++;
				newpat.Add(pat.Get_data(pat_pos++));
			}
			else {
				int charcount = 1;
				while (pat_pos + 1 < ep) {
					if ((pat.Get_data(pat_pos + 1) == '-') && (pat_pos + 2 < ep))
						break;
					charcount++;
					pat_pos++;
				}
				if (charcount == 1) {
					newpat.Add(Db_pattern.SINGLECHAR);
					newpat.Add(pcode);
				}
				else {
					newpat.Add(Db_pattern.CHAR);
					newpat.Add(charcount);
					pat_pos -= charcount;
					while (charcount-- > 0) {
						newpat.Add(pat.Get_data(pat_pos++));
					}
				}
			}
			if (pat_pos < ep)
				pcode = pat.Get_data(pat_pos++);
                        else
                            break;
		}
	}
	private boolean singlematch_run(int cur, Db_pattern pat) {
		int pat_pos = 0;
		int pcode = pat.pattern[pat_pos++];
		switch (pcode) {
			case Db_pattern.DOT:
				return true;
			case Db_pattern.ESC:
				return char_class_mgr.Match_class(cur, pat.pattern[pat_pos]);
			case Db_pattern.POSMATCH:
				return matchbracketclass_run(cur, pat, pat_pos, true);
			case Db_pattern.NEGMATCH:
				return matchbracketclass_run(cur, pat, pat_pos, false);
			case Db_pattern.SINGLECHAR:
				return pat.pattern[pat_pos] == cur;
		}
		// should never get here
		return false;
	}
	private boolean matchbracketclass_run(int cur, Db_pattern pat, int pat_pos, boolean sig) {
		int ep = pat.Len();
		while (pat_pos < ep) {
			int pcode = pat.pattern[pat_pos++];
			switch (pcode) {
				case Db_pattern.ESC:
					if (char_class_mgr.Match_class(cur, pat.pattern[pat_pos++]))
						return sig;
					break;
				case Db_pattern.RANGE:
					int bgn = pat.pattern[pat_pos++];
					int end = pat.pattern[pat_pos++];
					if (bgn <= cur && cur <= end)
						return sig;
					break;
				case Db_pattern.CHAR:
					int len = pat.pattern[pat_pos++];
					while (len-- > 0) {
						if (pat.pattern[pat_pos++] == cur)
							return sig;
					}
					break;
				case Db_pattern.SINGLECHAR:
					if (pat.pattern[pat_pos++] == cur)
						return sig;
					break;
			}
		}
		return !sig;
	}
}
class Db_pattern {
	public int[] pattern;
	private int size;
	public int Len() { return len; } private int len;
	public Db_pattern(int size) {
		pattern = new int[size];
		this.size = size;
		this.len = 0;
	}
	public void Add(int val) {
		pattern[len++] = val;
	}
	public static final int
	  POSMATCH = 1
	, NEGMATCH = 2
	, RANGE = 3
	, CHAR = 4
	, SINGLECHAR = 5
	, DOT = 6
	, ESC = 7
	;
}
