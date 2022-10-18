package lab09_04_08.parser.ast;

import static java.util.Objects.requireNonNull;

public class Single<T> {
	protected final T single;

	public Single(T single) {
		this.single = requireNonNull(single);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + single + ")";
	}
}
