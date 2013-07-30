package cx.runtime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @NotThreadSafe
 */
public class ArrayPool<T> {
	final int maxsize;
	final int maxcount;
	final T[] empty;
	final Class<T> clazz;
	private final Map<Integer, List<T[]>> cache;

	@SuppressWarnings("unchecked")
	public ArrayPool(Class<T> clazz, int maxsize, int maxcount) {
		this.maxsize = maxsize;
		this.maxcount = maxcount;
		this.clazz = clazz;
		this.empty = (T[]) Array.newInstance(clazz, 0);
		this.cache = new HashMap<Integer, List<T[]>>(maxsize);
	}

	@SuppressWarnings("unchecked")
	public T[] pull(int size) {
		if (size <= 0) {
			return empty;
		}
		if (size < maxsize) {
			// check cache
			List<T[]> cached = cache.get(Integer.valueOf(size));
			int cachedsize;
			if (cached != null && (cachedsize = cached.size()) > 0) {
				T[] result = cached.remove(cachedsize - 1);
				return result;
			}
		}
		return (T[]) Array.newInstance(clazz, size);
	}

	public void push(T[] array) {
		int size = array.length;
		if (size > 0 && size < maxsize) {
			// check cache
			Integer ix = Integer.valueOf(size);
			List<T[]> cached = cache.get(ix);
			if (cached == null) {
				cached = new ArrayList<T[]>(maxcount);
				cache.put(ix, cached);
			}
			for (int i = 0; i < size; i++) {
				array[i] = null;
			}
			cached.add(array);
		}
	}
}
