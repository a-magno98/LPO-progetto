package lab09_04_08.parser.ast;
import lab10_04_29.visitors.Visitor;

public interface AST {
	<T> T accept(Visitor<T> visitor);

}
