/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2.lib;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.luaj.vm2.Buffer;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Subclass of {@link LibFunction} which implements the standard lua {@code os} library.
 * <p>
 * It is a usable base with simplified stub functions
 * for library functions that cannot be implemented uniformly 
 * on Jse and Jme.   
 * <p>
 * This can be installed as-is on either platform, or extended 
 * and refined to be used in a complete Jse implementation.
 * <p>
 * Because the nature of the {@code os} library is to encapsulate 
 * os-specific features, the behavior of these functions varies considerably 
 * from their counterparts in the C platform.  
 * <p>
 * The following functions have limited implementations of features 
 * that are not supported well on Jme:
 * <ul>
 * <li>{@code execute()}</li>
 * <li>{@code remove()}</li>
 * <li>{@code rename()}</li>
 * <li>{@code tmpname()}</li>
 * </ul>
 * <p>
 * Typically, this library is included as part of a call to either 
 * {@link JsePlatform#standardGlobals()} or {@link JmePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("os").get("time").call() );
 * } </pre>
 * In this example the platform-specific {@link JseOsLib} library will be loaded, which will include
 * the base functionality provided by this class.
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new OsLib());
 * System.out.println( globals.get("os").get("time").call() );
 * } </pre>
 * <p>
  * @see LibFunction
 * @see JseOsLib
 * @see JsePlatform
 * @see JmePlatform
 * @see <a href="http://www.lua.org/manual/5.1/manual.html#5.8">http://www.lua.org/manual/5.1/manual.html#5.8</a>
 */
public class OsLib extends TwoArgFunction {
	public static String TMP_PREFIX    = ".luaj";
	public static String TMP_SUFFIX    = "tmp";

	private static final int CLOCK     = 0;
	private static final int DATE      = 1;
	private static final int DIFFTIME  = 2;
	private static final int EXECUTE   = 3;
	private static final int EXIT      = 4;
	private static final int GETENV    = 5;
	private static final int REMOVE    = 6;
	private static final int RENAME    = 7;
	private static final int SETLOCALE = 8;
	private static final int TIME      = 9;
	private static final int TMPNAME   = 10;

	private static final String[] NAMES = {
		"clock",
		"date",
		"difftime",
		"execute",
		"exit",
		"getenv",
		"remove",
		"rename",
		"setlocale",
		"time",
		"tmpname",
	};
	
	private static final long t0 = System.currentTimeMillis();
	private static long tmpnames = t0;

	protected Globals globals;
	
	/** 
	 * Create and OsLib instance.   
	 */
	public OsLib() {
	}
	
	public LuaValue call(LuaValue modname, LuaValue env) {
		globals = env.checkglobals();
		LuaTable os = new LuaTable();
		for (int i = 0; i < NAMES.length; ++i)
			os.set(NAMES[i], new OsLibFunc(i, NAMES[i]));
		env.set("os", os);
		env.get("package").get("loaded").set("os", os);
		return os;
	}

	class OsLibFunc extends VarArgFunction {
		public OsLibFunc(int opcode, String name) {
			this.opcode = opcode;
			this.name = name;
		}
		public Varargs invoke(Varargs args) {
			try {
				switch ( opcode ) {
				case CLOCK:
					return valueOf(clock());
				case DATE: {
					String s = args.optjstring(1, "%c");
					double t = args.isnumber(2)? args.todouble(2): time(null);
					boolean utc = false;
					if (s.startsWith("!")) {
						utc = true;
						s = s.substring(1);
					}
					if (s.equals("*t")) {
						Calendar d = Calendar.getInstance();
						long time_in_ms = (long)(t*1000);
						if (utc) {
							java.util.TimeZone current_tz = d.getTimeZone();
							int offset_from_utc = current_tz.getOffset(time_in_ms);
							time_in_ms += -offset_from_utc;
						}
						d.setTime(new Date(time_in_ms));
						LuaTable tbl = LuaValue.tableOf();
						tbl.set("year", LuaValue.valueOf(d.get(Calendar.YEAR)));
						tbl.set("month", LuaValue.valueOf(d.get(Calendar.MONTH)+1));
						tbl.set("day", LuaValue.valueOf(d.get(Calendar.DAY_OF_MONTH)));
						tbl.set("hour", LuaValue.valueOf(d.get(Calendar.HOUR)));
						tbl.set("min", LuaValue.valueOf(d.get(Calendar.MINUTE)));
						tbl.set("sec", LuaValue.valueOf(d.get(Calendar.SECOND)));
						tbl.set("wday", LuaValue.valueOf(d.get(Calendar.DAY_OF_WEEK)));
						tbl.set("yday", LuaValue.valueOf(d.get(0x6))); // Day of year
						tbl.set("isdst", LuaValue.valueOf(isDaylightSavingsTime(d)));
						return tbl;
					}
					return valueOf( date(s, t==-1? time(null): t) );
				}
				case DIFFTIME:
					return valueOf(difftime(args.checkdouble(1),args.checkdouble(2)));
				case EXECUTE:
					return execute(args.optjstring(1, null));
				case EXIT:
					exit(args.optint(1, 0));
					return NONE;
				case GETENV: {
					final String val = getenv(args.checkjstring(1));
					return val!=null? valueOf(val): NIL;
				}
				case REMOVE:
					remove(args.checkjstring(1));
					return LuaValue.TRUE;
				case RENAME:
					rename(args.checkjstring(1), args.checkjstring(2));
					return LuaValue.TRUE;
				case SETLOCALE: {
					String s = setlocale(args.optjstring(1,null), args.optjstring(2, "all"));
					return s!=null? valueOf(s): NIL;
				}
				case TIME:
					return valueOf(time(args.opttable(1, null)));
				case TMPNAME:
					return valueOf(tmpname());
				}
				return NONE;
			} catch ( IOException e ) {
				return varargsOf(NIL, valueOf(e.getMessage()));
			}
		}
	}

	/**
	 * @return an approximation of the amount in seconds of CPU time used by 
	 * the program.  For luaj this simple returns the elapsed time since the 
	 * OsLib class was loaded.
	 */
	protected double clock() {
		return (System.currentTimeMillis()-t0) / 1000.;
	}

	/**
	 * Returns the number of seconds from time t1 to time t2. 
	 * In POSIX, Windows, and some other systems, this value is exactly t2-t1.
	 * @param t2
	 * @param t1
	 * @return diffeence in time values, in seconds
	 */
	protected double difftime(double t2, double t1) {
		return t2 - t1;
	}

	/**
	 * If the time argument is present, this is the time to be formatted 
	 * (see the os.time function for a description of this value). 
	 * Otherwise, date formats the current time.
	 * 
	 * Date returns the date as a string, 
	 * formatted according to the same rules as ANSII strftime, [but without
	 * support for %g, %G, or %V.] now added 20210815
	 * 
	 * When called without arguments, date returns a reasonable date and 
	 * time representation that depends on the host system and on the 
	 * current locale (that is, os.date() is equivalent to os.date("%c")).
	 *  
	 * @param format 
	 * @param time time since epoch, or -1 if not supplied
	 * @return a LString or a LTable containing date and time, 
	 * formatted according to the given string format.
	 */
	public String date(String format, double time) {
		Calendar d = Calendar.getInstance();
		d.setTime(new Date((long)(time*1000)));
		if (format.startsWith("!")) {
			time -= timeZoneOffset(d);
			d.setTime(new Date((long)(time*1000)));
			format = format.substring(1);
		}
		byte[] fmt = format.getBytes();
		final int n = fmt.length;
		Buffer result = new Buffer(n);
		byte c;
		for ( int i = 0; i < n; ) {
			switch ( c = fmt[i++ ] ) {
			case '\n':
				result.append( "\n" );
				break;
			default:
				result.append( c );
				break;
			case '%':	// http://www.gnu.org/software/libc/manual/html_node/Formatting-Calendar-Time.html
				if (i >= n) break;
				switch ( c = fmt[i++ ] ) {
				case 'Z': // The time zone abbreviation (empty if the time zone can't be determined).
				default:
					LuaValue.argerror(1, "invalid conversion specifier '%"+(char)c+"'");
					break;
				case '%':
					result.append( (byte)'%' );
					break;
				case 'a':
					result.append(WeekdayNameAbbrev[d.get(Calendar.DAY_OF_WEEK)-1]);
					break;
				case 'A':
					result.append(WeekdayName[d.get(Calendar.DAY_OF_WEEK)-1]);
					break;
				case 'b':
				case 'h': // The abbreviated month name according to the current locale. The action is the same as for %b. 
					result.append(MonthNameAbbrev[d.get(Calendar.MONTH)]);
					break;
				case 'B':
					result.append(MonthName[d.get(Calendar.MONTH)]);
					break;
				case 'c': 
					result.append(date("%a %b %d %H:%M:%S %Y", time));
					break;
				case 'C':	// The century of the year. This is equivalent to the greatest integer not greater than the year divided by 100. 
					int century = (int)d.get(Calendar.YEAR) / 100;
					result.append(Integer.toString(century));
					break;
				case 'd':
					result.append(String.valueOf(100+d.get(Calendar.DAY_OF_MONTH)).substring(1));
					break;
				case 'D':	// The date using the format %m/%d/%y. 
					result.append(date("%m/%d/%y", time));
					break;
				case 'e':	// The day of the month like with %d, but padded with blank (range 1 through 31).
					result.append(Pad_left_space_2(d.get(Calendar.DAY_OF_MONTH)));
					break;
				case 'F': 	// The date using the format %Y-%m-%d. This is the form specified in the ISO 8601 standard and is the preferred form for all uses.
					result.append(date("%Y-%m-%d", time));
					break;
				case 'H':
					result.append(String.valueOf(100+d.get(Calendar.HOUR_OF_DAY)).substring(1));
					break;
				case 'I':
					result.append(String.valueOf(100+(d.get(Calendar.HOUR_OF_DAY)%12)).substring(1));
					break;
				case 'j': { // day of year.
					Calendar y0 = beginningOfYear(d);
					int dayOfYear = (int) ((d.getTime().getTime() - y0.getTime().getTime()) / (24 * 3600L * 1000L));
					result.append(String.valueOf(1001+dayOfYear).substring(1));
					break;
				}
				case 'k': // The hour as a decimal number, using a 24-hour clock like %H, but padded with blank (range 0 through 23).
					result.append(Pad_left_space_2(d.get(Calendar.HOUR_OF_DAY)));
					break;
				case 'l': // The hour (12-hour clock) as a decimal number (range 1 to 12);
					result.append(Pad_left_space_2(d.get(Calendar.HOUR_OF_DAY) % 12));
					break;
				case 'm':
					result.append(String.valueOf(101+d.get(Calendar.MONTH)).substring(1));
					break;
				case 'M':
					result.append(String.valueOf(100+d.get(Calendar.MINUTE)).substring(1));
					break;
				case 'p':
					result.append(d.get(Calendar.HOUR_OF_DAY) < 12? "AM": "PM");
					break;
				case 'P': // Like %p but in lowercase: "am" or "pm" or a corresponding string for the current locale. (GNU)
					result.append(d.get(Calendar.HOUR_OF_DAY) < 12? "am": "pm");
					break;
				case 'r': // The time in a.m. or p.m. notation.  In the POSIX locale this is equivalent to %I:%M:%S %p.
					result.append(date("%I:%M:%S %p", time));
					break;
				case 'R': // The hour and minute in decimal numbers using the format %H:%M.
					result.append(date("%H:%M", time));
					break;
				case 's': // The number of seconds since the Epoch, 1970-01-01 00:00:00 +0000 (UTC). (TZ)
					result.append(Double.toString(time));
					break;
				case 'S':
					result.append(String.valueOf(100+d.get(Calendar.SECOND)).substring(1));
					break;
				case 't':	// A tab character. (SU)
					result.append((byte)9);
					break;
				case 'T': // The time in 24-hour notation (%H:%M:%S).  (SU)
					result.append(date("%H:%M:%S", time));
					break;
				case 'u': // %u The day of the week as a decimal, range 1 to 7, Monday being 1.  See also %w.  (SU)
					result.append(String.valueOf(d.get(Calendar.DAY_OF_WEEK)));
					break;
				case 'U':
					result.append(String.valueOf(weekNumber(d, 0)));
					break;
				case 'V': // The ISO 8601:1988 week number as a decimal number (range 01 through 53). ISO weeks start with Monday and end with Sunday...
					result.append(String.valueOf(d.get(Calendar.WEEK_OF_YEAR)));
					break;
				case 'w':	// The day of the week as a decimal number (range 0 through 6), Sunday being 0. 
					result.append(String.valueOf(d.get(Calendar.DAY_OF_WEEK)-1));		// was "+6)%7"; note Sunday=1 and Saturday=7
					break;
				case 'W': 
					result.append(String.valueOf(weekNumber(d, 1)));
					break;
				case 'x':
					result.append(date("%m/%d/%y", time));
					break;
				case 'X':
					result.append(date("%H:%M:%S", time));
					break;
				case 'g': // The year corresponding to the ISO week number, but without the century (range 00 through 99). This has the same format and value as %y, except that if the ISO week number (see %V) belongs to the previous or next year, that year is used instead.
				case 'y':
					result.append(String.valueOf(d.get(Calendar.YEAR)).substring(2));
					break;
				case 'G': // The year corresponding to the ISO week number. This has the same format and value as %Y, except that if the ISO week number (see %V) belongs to the previous or next year, that year is used instead.
				case 'Y':
					result.append(String.valueOf(d.get(Calendar.YEAR)));
					break;
				case 'z': {
					final int tzo = timeZoneOffset(d) / 60;
					final int a = Math.abs(tzo);
					final String h = String.valueOf(100 + a / 60).substring(1);
					final String m = String.valueOf(100 + a % 60).substring(1);
					result.append((tzo>=0? "+": "-") + h + m);
					break;
				}
				}
			}
		}
		return result.tojstring();
	}
	private static String Pad_left_space_2(int v) {
		String v_str = Integer.toString(v);
		if (v < 10) v_str = " " + v_str;
		return v_str;
	}
	
	private static final String[] WeekdayNameAbbrev = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
	private static final String[] WeekdayName = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
	private static final String[] MonthNameAbbrev = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private static final String[] MonthName = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

	private Calendar beginningOfYear(Calendar d) {
		Calendar y0 = Calendar.getInstance();
		y0.setTime(d.getTime());
		y0.set(Calendar.MONTH, 0);
		y0.set(Calendar.DAY_OF_MONTH, 1);
		y0.set(Calendar.HOUR_OF_DAY, 0);
		y0.set(Calendar.MINUTE, 0);
		y0.set(Calendar.SECOND, 0);
		y0.set(Calendar.MILLISECOND, 0);
		return y0;
	}
	
	private int weekNumber(Calendar d, int startDay) {
		Calendar y0 =  beginningOfYear(d);
		y0.set(Calendar.DAY_OF_MONTH, 1 + (startDay + 8 - y0.get(Calendar.DAY_OF_WEEK)) % 7);
		if (y0.after(d)) {
			y0.set(Calendar.YEAR, y0.get(Calendar.YEAR) - 1);
			y0.set(Calendar.DAY_OF_MONTH, 1 + (startDay + 8 - y0.get(Calendar.DAY_OF_WEEK)) % 7);
		}
		long dt = d.getTime().getTime() - y0.getTime().getTime();
		return 1 + (int) (dt / (7L * 24L * 3600L * 1000L));
	}
	
	private int timeZoneOffset(Calendar d) {
		int localStandarTimeMillis = ( 
				d.get(Calendar.HOUR_OF_DAY) * 3600 +
				d.get(Calendar.MINUTE) * 60 +
				d.get(Calendar.SECOND)) * 1000;
		return d.getTimeZone().getOffset(
				1,
				d.get(Calendar.YEAR),
				d.get(Calendar.MONTH),
				d.get(Calendar.DAY_OF_MONTH),
				d.get(Calendar.DAY_OF_WEEK), 
				localStandarTimeMillis) / 1000;
	}
	
	private boolean isDaylightSavingsTime(Calendar d) {
		return timeZoneOffset(d) != d.getTimeZone().getRawOffset() / 1000;		
	}
	
	/** 
	 * This function is equivalent to the C function system. 
	 * It passes command to be executed by an operating system shell. 
	 * It returns a status code, which is system-dependent. 
	 * If command is absent, then it returns nonzero if a shell 
	 * is available and zero otherwise.
	 * @param command command to pass to the system
	 */ 
	protected Varargs execute(String command) {
		return varargsOf(NIL, valueOf("exit"), ONE);
	}

	/**
	 * Calls the C function exit, with an optional code, to terminate the host program. 
	 * @param code
	 */
	protected void exit(int code) {
		System.exit(code);
	}

	/**
	 * Returns the value of the process environment variable varname, 
	 * or null if the variable is not defined. 
	 * @param varname
	 * @return String value, or null if not defined
	 */
	protected String getenv(String varname) {
		return System.getProperty(varname);
	}

	/**
	 * Deletes the file or directory with the given name. 
	 * Directories must be empty to be removed. 
	 * If this function fails, it throws and IOException
	 *  
	 * @param filename
	 * @throws IOException if it fails
	 */
	protected void remove(String filename) throws IOException {
		throw new IOException( "not implemented" );
	}

	/**
	 * Renames file or directory named oldname to newname. 
	 * If this function fails,it throws and IOException
	 *  
	 * @param oldname old file name
	 * @param newname new file name
	 * @throws IOException if it fails
	 */
	protected void rename(String oldname, String newname) throws IOException {
		throw new IOException( "not implemented" );
	}

	/**
	 * Sets the current locale of the program. locale is a string specifying 
	 * a locale; category is an optional string describing which category to change: 
	 * "all", "collate", "ctype", "monetary", "numeric", or "time"; the default category 
	 * is "all". 
	 * 
	 * If locale is the empty string, the current locale is set to an implementation-
	 * defined native locale. If locale is the string "C", the current locale is set 
	 * to the standard C locale.
	 * 
	 * When called with null as the first argument, this function only returns the 
	 * name of the current locale for the given category.
	 *  
	 * @param locale
	 * @param category
	 * @return the name of the new locale, or null if the request 
	 * cannot be honored.
	 */
	protected String setlocale(String locale, String category) {
		return "C";
	}

	/**
	 * Returns the current time when called without arguments, 
	 * or a time representing the date and time specified by the given table. 
	 * This table must have fields year, month, and day, 
	 * and may have fields hour, min, sec, and isdst 
	 * (for a description of these fields, see the os.date function).
	 * @param table
	 * @return long value for the time
	 */
	protected double time(LuaTable table) {
		java.util.Date d;
		if (table == null) {
			d = new java.util.Date();
		} else {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, table.get("year").checkint());
			c.set(Calendar.MONTH, table.get("month").checkint()-1);
			c.set(Calendar.DAY_OF_MONTH, table.get("day").checkint());
			c.set(Calendar.HOUR_OF_DAY, table.get("hour").optint(12));	// XOWA:LUAJ.BUG: was Calendar.HOUR which will be off by 12 hours depending on current time; REF:http://www.tutorialspoint.com/c_standard_library/c_function_mktime.htm DATE:2016-01-21 
			c.set(Calendar.MINUTE, table.get("min").optint(0));
			c.set(Calendar.SECOND, table.get("sec").optint(0));
			c.set(Calendar.MILLISECOND, 0);
			d = c.getTime();
		}
		return d.getTime() / 1000;
	}

	/**
	 * Returns a string with a file name that can be used for a temporary file. 
	 * The file must be explicitly opened before its use and explicitly removed 
	 * when no longer needed.
	 * 
	 * On some systems (POSIX), this function also creates a file with that name, 
	 * to avoid security risks. (Someone else might create the file with wrong 
	 * permissions in the time between getting the name and creating the file.) 
	 * You still have to open the file to use it and to remove it (even if you 
	 * do not use it). 
	 * 
	 * @return String filename to use
	 */
	protected String tmpname() {
		synchronized ( OsLib.class ) {
			return TMP_PREFIX+(tmpnames++)+TMP_SUFFIX;
		}
	}
}
