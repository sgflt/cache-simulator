package cz.zcu.kiv.cacheSimulator.shared;

/**
 * sablona pro praci se ctverici soudrznych hodnot
 * @author Pavel Bzoch
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Quartet<A, B, C, D> {

	private A first;

	private B second;

	private C third;

	private D fourth;

	public Quartet(A first, B second, C third, D fourth) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	public String toString() {
		return "first " + first + ", second " + second + ", third " + third
				+ ", fourth " + fourth;
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

	public D getFourth() {
		return fourth;
	}

	public void setFourth(D fourth) {
		this.fourth = fourth;
	}

}
