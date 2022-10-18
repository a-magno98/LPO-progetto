package lab09_04_08.parser.ast;

import lab10_04_29.visitors.Visitor;

public class SingleExp extends Single<Exp> implements ExpSeq {

	public SingleExp(Exp single) {
		super(single);
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitSingleExp(single);
	}
}
