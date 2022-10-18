package lab10_04_29.environments;

import lab09_04_08.parser.ast.Ident;

public interface Environment<T> {

	/* adds a new nested scope */

	void enterScope();

	/* removes the most nested scope */

	void exitScope();

	/*
	 * lookups the value associated with id starting from the most nested scope;
	 * throws an EnvironmentException if id could not be found in any scope
	 */

	T lookup(Ident id);

	/*
	 * updates the most nested scope by associating id with info; id is allowed
	 * to be already defined, id and info must be non-null
	 */

	T dec(Ident id, T info);

	/*
	 * updates the most nested scope which defines id by associating id with
	 * info; throws an EnvironmentException if id could not be found in any
	 * scope; id and info must be non-null
	 */

	T update(Ident id, T info);

}
