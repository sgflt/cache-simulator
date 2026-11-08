package cz.zcu.kiv.cacheSimulator.shared;

/**
 * sablona pro praci s dvojici soudrznych hodnot
 * @author Pavel Bzoch
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Pair<A, B> {
	private A first;

	private B second;

	public Pair(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	public A getFirst() {
		return first;
	}

	public void setFirst(A first) {
		this.first = first;
	}

	public B getSecond() {
		return second;
	}

	public void setSecond(B second) {
		this.second = second;
	}
}