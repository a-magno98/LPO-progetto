package lab09_04_08.parser.ast;

import static java.util.Objects.requireNonNull;

import lab10_04_29.visitors.Visitor;

public class SetLit implements Exp {
	private final ExpSeq exps;

	public SetLit(ExpSeq exps) {
		this.exps = requireNonNull(exps);
	}

	public ExpSeq getExps() {
		return exps;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + exps + ")";
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitSetLit(exps);
	}

}