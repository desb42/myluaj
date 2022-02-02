package org.luaj.vm2.lib;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.StringLib.MatchState;

import gplx.objects.strings.char_sources.Char_source_;

class Str_find_mgr__lua extends Str_find_mgr {
	private LuaValue[] captures_ary;
	private int captures_idx;

	public Str_find_mgr__lua(LuaString src, LuaString pat, int src_bgn, boolean plain, boolean find) {
 		super(src, pat, src_bgn, plain, find);
	}
	@Override public Str_char_class_mgr Char_class_mgr() {return char_class_mgr;} private final Str_char_class_mgr char_class_mgr = new Str_char_class_mgr__ascii();
	@Override protected void reset() {
		this.captures_ary = null;
		this.captures_idx = 0;
	}
	@Override public void Captures__init(int levels) {		
		this.captures_ary = new LuaValue[levels];		
	}
	@Override public LuaValue Captures__make__none() {
		return LuaValue.NONE;
	}
	@Override protected Varargs Captures__make__many() {
		return captures_ary == null ? LuaValue.NONE : LuaValue.varargsOf(captures_ary);
	}
	@Override protected LuaValue Capture__make__string(boolean register_capture, int bgn, int end) {
		// NOTE:cannot use Substring b/c Java will "fix" malformed bytes which will break things like "æ".Substring(0, 1); ISSUE#:504; DATE:2019-07-22
		// LuaValue rv = LuaString.valueOf(src.Substring(bgn, end));
		LuaString src_as_lstr = (LuaString)src;
		LuaString rv = LuaString.valueOfCopy(src_as_lstr.m_bytes, src_as_lstr.m_offset + bgn, end - bgn); // NOTE:must account for m_offset; ISSUE#:520; DATE:2019-07-25
		if (register_capture)
			captures_ary[captures_idx++] = rv;
		return rv;		
	}
	@Override protected LuaValue Capture__position(boolean register_capture, int val) {
		LuaValue rv = LuaValue.valueOf(val);
		if (register_capture)
			captures_ary[captures_idx++] = rv;
		return rv;		
	}	
	@Override protected void Result__make__bgn_end(int bgn, int end) {}
	@Override protected Varargs Result__make__plain(int bgn, int end) {
		return LuaValue.varargsOf(LuaValue.valueOf(bgn), LuaValue.valueOf(end));
	}
	@Override protected Varargs Result__make__find(int bgn, int end) {
		Varargs capt = (captures_ary == null) ? LuaValue.NONE : LuaValue.varargsOf(captures_ary);
		return LuaValue.varargsOf(LuaValue.valueOf(bgn), LuaValue.valueOf(end), capt);
	}
	@Override protected Varargs Result__make__match() {
		return captures_ary == null ? LuaValue.NONE : LuaValue.varargsOf(captures_ary);
	}
	@Override protected Varargs Result__make__nil() {
            return LuaValue.NIL;
/*
            if (this.find)
            else {
                String lpat = this.pat.toString();
                // if contains capture groups
                if ((lpat.contains("(") && lpat.contains(")")) || lpat.contains("{}")) // special behaviour?! 20211113
                    return LuaValue.NIL;
		return LuaValue.EMPTYSTRING;
            }
*/
	}
	public static Varargs Run(Varargs args, boolean find) {
		LuaString src = args.checkstring(1);
		LuaString pat = args.checkstring(2);

		// check for a specific match ^%s*(.-)%s*$ (this is a trim whitespace)
		if (pat.length() == 12 && pat.m_bytes[0] == '^' && pat.m_bytes[1] == '%' && pat.m_bytes[2] == 's' && pat.m_bytes[3] == '*'
		    && pat.m_bytes[4] == '(' && pat.m_bytes[5] == '.' && pat.m_bytes[6] == '-' && pat.m_bytes[7] == ')' && pat.m_bytes[8] == '%'
		    && pat.m_bytes[9] == 's' && pat.m_bytes[10] == '*' && pat.m_bytes[11] == '$') {
			return StringLib.trim(src);
		}

		Str_find_mgr__lua mgr = new Str_find_mgr__lua(src, pat, args.optint(3, 1), args.arg(4).toboolean(), find);
		return mgr.Process(true);
	}
}
