package cz.zcu.kiv.cacheSimulator.shared;

/**
 * sablona pro praci s trojici soudrznych hodnot
 * @author Pavel Bzoch
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Triplet<A, B, C> {

	private A first;

	private B second;

	private C third;

	public Triplet(A first, B second, C third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public String toString() {
		return "first " + first + ", second " + second + ", third " + third;
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

	public C getThird() {
		return third;
	}

	public void setThird(C trird) {
		this.third = trird;
	}
}
