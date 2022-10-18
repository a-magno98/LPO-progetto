package lab11_05_06.visitors.evaluation;

import static java.util.Objects.requireNonNull;

public class PairValue implements Value {

	private final Value fstVal;
	private final Value sndVal;

	public PairValue(Value fstVal, Value sndVal) {
		this.fstVal = requireNonNull(fstVal);
		this.sndVal = requireNonNull(sndVal);
	}

	public Value getFstVal() {
		return fstVal;
	}

	public Value getSndVal() {
		return sndVal;
	}

	@Override
	public PairValue asPair() {
		return this;
	}

	@Override
	public String toString() {
		return "[" + fstVal + ", " + sndVal + "]";
	}

	@Override
	public int hashCode() {
		return 31 * fstVal.hashCode() + sndVal.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PairValue))
			return false;
		PairValue op = (PairValue) obj;
		return fstVal.equals(op.fstVal) && sndVal.equals(op.sndVal);
	}
}
