package net.tslat.smartbrainlib.object.backport;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * Helper class to backport from newer java versions
 */
public final class Collections {
	public static <E> List<E> list(E... elements) {
		return new ObjectArrayList<>(elements);
	}

	public static <E> List<E> immutableList(E... elements) {
		if (elements.length == 0)
			return java.util.Collections.emptyList();

		return ImmutableList.copyOf(elements);
	}
}
