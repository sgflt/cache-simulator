package cz.zcu.kiv.cacheSimulator.dataAccess;
import java.util.Random;

/**
 * trida pro generovani nahodnych cisel s ZIPF distribuci 
 * @author Pavel Bzoch
 *
 */
public class ZIPFRandom {

	/**
	 * Static first time flag
	 */
	private boolean first = true;

	/**
	 * Normalization constant
	 */
	private double c = 0;

	/**
	 * random generator
	 */
	private final Random rd;

	/**
	 * horni mez pro generovani cisel
	 */
	private final int n;

	/**
	 * nastaveni yipf generatoru
	 */
	private final double alpha;

	/**
	 * konstruktor - inicializace promennych
	 * @param n
	 * @param alpha
	 */
	public ZIPFRandom(final int n, final double alpha) {
		this.n = n;
		this.alpha = alpha;
		this.rd = new Random(0);
	}

	/**
	 * metoda, ktera vraci dalsi nahodnou hodnotu
	 * @return nahodna hodnota
	 */
	public int zipfNext() {

		double z; // Uniform random number (0 < z < 1)
		double sum_prob; // Sum of probabilities
		double zipf_value = 0; // Computed exponential value to be returned
		int i; // Loop counter

		// Compute normalization constant on first call only
		if (this.first == true) {
			for (i = 1; i <= this.n; i++) {
				this.c = this.c + (1.0 / Math.pow((double) i, this.alpha));
			}
			this.c = 1.0 / this.c;
			this.first = false;
		}

		// Pull a uniform random number (0 < z < 1)
		do {
			z = this.rd.nextDouble();
		} while ((z == 0) || (z == 1));

		// Map z to the value
		sum_prob = 0;
		for (i = 1; i <= this.n; i++) {
			sum_prob = sum_prob + this.c / Math.pow((double) i, this.alpha);
			if (sum_prob >= z) {
				zipf_value = i;
				break;
			}
		}

		// Assert that zipf_value is between 1 and N
		assert ((zipf_value >= 1) && (zipf_value <= this.n));

		return ((int) zipf_value) - 1;
	}
}
