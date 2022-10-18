package lab10_04_29.environments;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lab09_04_08.parser.ast.Ident;

import static java.util.Objects.requireNonNull;

public class GenEnvironment<T> implements Environment<T> {

	private LinkedList<Map<Ident, T>> scopeChain = new LinkedList<>();

	/*
	 * enter a new nested scope; private method shared by enterScope() and the
	 * constructor GenEnvironment()
	 */
	private void addEmptyScope() {
		scopeChain.addFirst(new HashMap<>());
	}

	/* create an environment with just one empty scope */
	public GenEnvironment() {
		addEmptyScope();
	}

	@Override
	public void enterScope() {
		addEmptyScope();
	}

	@Override
	public void exitScope() {
		scopeChain.removeFirst();
	}

	/*
	 * looks up id starting from the most nested scope; throws EnvironmentException
	 * if id could not be found in any scope
	 */

	protected Map<Ident, T> resolve(Ident id) {
		for (Map<Ident, T> scope : scopeChain)
			if (scope.containsKey(id))
				return scope;
		throw new EnvironmentException("Undeclared variable " + id.getName());
	}

	@Override
	public T lookup(Ident id) {
		return resolve(id).get(id);
	}

	/*
	 * updates map to associate id with info; id and info must be non-null
	 */

	private static <T> T updateScope(Map<Ident, T> map, Ident id, T info) {
		return map.put(requireNonNull(id), requireNonNull(info));
	}

	/*
	 * adds id and its info to the most nested scope, works also if id is already
	 * there
	 */

	@Override
	public T dec(Ident id, T info) {
		Map<Ident, T> scope = scopeChain.getFirst();
		return updateScope(scope, id, info);
	}

	/*
	 * updates the info of the most enclosed id, throws an exception if no id can be
	 * found in the scope chain. Only used for the dynamic semantics
	 */
	
	@Override
	public T update(Ident id, T info) {
		Map<Ident, T> scope = resolve(id);
		return updateScope(scope, id, info);
	}

}
