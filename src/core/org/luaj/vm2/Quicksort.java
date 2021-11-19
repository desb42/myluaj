/* Java program for Quicksort */
package org.luaj.vm2;
public class Quicksort 
{
	private LuaValue[] orig_array;
	LuaValue cmpfunc;
	private Metatable m_metatable;

	Quicksort(Metatable m_metatable, LuaValue[] array, int size, LuaValue comparator) {
		orig_array = array;
		this.cmpfunc = comparator;
		this.m_metatable = m_metatable;
	}

	private void auxsort (int lower, int upper) {
		while (lower < upper) {	/* for tail recursion */
			int i, j;
			/* sort elements a[lower], a[(lower+upper)/2] and a[upper] */
			if (compare(upper, lower))	/* a[upper] < a[lower]? */
				swap(lower, upper);	/* swap a[lower] - a[upper] */
			if (upper-lower == 1) break;	/* only 2 elements */
			i = (lower+upper)/2;
			if (compare_b(i))	/* a[i]<a[lower]? */
				swap(i, lower);
			else {
				if (compare(upper, i))	/* a[upper]<a[i]? */
					swap(i, upper);
			}
			if (upper-lower == 2) break;	/* only 3 elements */
			swap(i, upper); // pivot
			/* a[lower] <= P == a[upper-1] <= a[upper], only need to sort from lower+1 to upper-2 */
			i = lower-1; j = upper;
			for (;;) {	/* invariant: a[lower..i] <= P <= a[j..upper] */
				compare(0, upper); // condition 'b' as Pivot
				/* repeat ++i until a[i] >= P */
				while (compare_b(++i) && i <= upper) {
				}
				compare(upper, 0); // condition 'a' as Pivot
				/* repeat --j until a[j] <= P */
				while (compare_a(--j) && j > lower) {
				}
				// check if pointers cross
				if (j<=i) {
					break;
				}
				swap(i, j);
			}
			swap(upper, i);	/* swap pivot (a[upper]) with a[i] */
			/* a[lower..i-1] <= a[i] == P <= a[i+1..upper] */
			/* adjust so that smaller half is in [j..i] and larger one in [lower..upper] */
			if (i-lower < upper-i) {
				j=lower; i=i-1; lower=i+2;
			}
			else {
				j=i+1; i=upper; upper=j-2;
			}
			auxsort(j, i);	/* call recursively the smaller one */
		} /* repeat the routine for the larger one */
	}


	private LuaValue a, b;
	private boolean compare_a(int j) {
		if (m_metatable == null) {
			b = orig_array[j];
		} else {
			b = m_metatable.arrayget(orig_array, j);
		}
		return cmp(a, b);
	}

	private boolean compare_b(int i) {
		if (m_metatable == null) {
			a = orig_array[i];
		} else {
			a = m_metatable.arrayget(orig_array, i);
		}
		return cmp(a, b);
	}

	private boolean compare(int i, int j) {
		if (m_metatable == null) {
			a = orig_array[i];
			b = orig_array[j];
		} else {
			a = m_metatable.arrayget(orig_array, i);
			b = m_metatable.arrayget(orig_array, j);
		}
		return cmp(a, b);
	}

	private boolean cmp(LuaValue a, LuaValue b) {
		if ( a == null || b == null )
			return false;
		if ( ! cmpfunc.isnil() ) {
			return cmpfunc.call(a,b).toboolean();
		} else {
			return a.lt_b(b);
		}
	}

	private void swap(int i, int j) {
		LuaValue a = orig_array[i];
		orig_array[i] = orig_array[j];
		orig_array[j] = a;
	}

	public void sort(int size) 
	{
		auxsort(0, size-1);
	}
}
