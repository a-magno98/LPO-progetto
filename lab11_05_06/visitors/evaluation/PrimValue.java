package lab11_05_06.visitors.evaluation;

import static java.util.Objects.requireNonNull;

public abstract class PrimValue<T> implements Value {
	protected T value;

	protected PrimValue(T value) {
		this.value = requireNonNull(value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}
}