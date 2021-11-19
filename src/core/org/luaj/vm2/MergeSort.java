/* Java program for Merge Sort */
package org.luaj.vm2;
public class MergeSort 
{
	private LuaValue[] local_array;
	private LuaValue[] orig_array;
	LuaValue cmpfunc;
	private Metatable m_metatable;

	MergeSort(Metatable m_metatable, LuaValue[] array, int size, LuaValue comparator) {
		local_array = new LuaValue[size];
		orig_array = array;
		this.cmpfunc = comparator;
		this.m_metatable = m_metatable;
	}
	
	// Merges two subarrays of arr[]. 
	// First subarray is arr[l..m] 
	// Second subarray is arr[m+1..r] 
	void merge(int l, int m, int r) 
	{ 
		// Find sizes of two subarrays to be merged 
		int n1 = m - l + 1; 
		int n2 = r - m; 

		/* Create temp arrays */
		//int L[] = new int [n1]; 
		//int R[] = new int [n2]; 
		int L = 0;
		int R = n1;

		/*Copy data to temp arrays*/
		for (int i=0; i<n1; ++i) 
			local_array[L + i] = orig_array[l + i]; 
		for (int j=0; j<n2; ++j) 
			local_array[R + j] = orig_array[m + 1+ j]; 


		/* Merge the temp arrays */

		// Initial indexes of first and second subarrays 
		int i = 0, j = 0; 

		// Initial index of merged subarry array 
		int k = l; 
		while (i < n1 && j < n2) 
		{ 
			if (!compare(L + i, R + j)) //  a >= b
			{ 
				orig_array[k] = local_array[R + j]; 
				j++; 
			} 
			else
			{ 
				orig_array[k] = local_array[L + i]; 
				i++; 
			} 
			k++; 
		} 

		/* Copy remaining elements of L[] if any */
		while (i < n1) 
		{ 
			orig_array[k] = local_array[L + i]; 
			i++; 
			k++; 
		} 

		/* Copy remaining elements of R[] if any */
		while (j < n2) 
		{ 
			orig_array[k] = local_array[R + j]; 
			j++; 
			k++; 
		} 
	} 

	private boolean compare(int i, int j) {
		LuaValue a, b;
		if (m_metatable == null) {
			a = local_array[i];
			b = local_array[j];
		} else {
			a = m_metatable.arrayget(local_array, i);
			b = m_metatable.arrayget(local_array, j);
		}
		if ( a == null || b == null )
			return false;
		if ( ! cmpfunc.isnil() ) {
			return cmpfunc.call(a,b).toboolean();
		} else {
			return a.lt_b(b);
		}
	}
	
	// Main function that sorts arr[l..r] using 
	// merge() 
	public void sort(int l, int r) 
	{ 
		if (l < r) 
		{ 
			// Find the middle point 
			int m = (l+r)/2; 

			// Sort first and second halves 
			sort(l, m); 
			sort(m+1, r); 

			// Merge the sorted halves 
			merge(l, m, r); 
		} 
	} 

	/* A utility function to print array of size n */
	static void printArray(int arr[]) 
	{ 
		int n = arr.length; 
		for (int i=0; i<n; ++i) 
			System.out.print(arr[i] + " "); 
		System.out.println(); 
	} 
} 
