package lab09_04_08.parser.ast;

import lab10_04_29.visitors.Visitor;

public class MoreExp extends More<Exp, ExpSeq> implements ExpSeq {

	public MoreExp(Exp first, ExpSeq rest) {
		super(first, rest);
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitMoreExp(first,rest);
	}

}