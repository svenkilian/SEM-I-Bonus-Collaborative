
import java.util.HashSet;

import Jama.Matrix;

/**
 * @author Sven Koepke, Daniel Heinz, Marius Laemmlin
 * @version 1.0
 */
public class Sub {

	// Feld der rekurrenten Zustaende
	static Integer[] recurrentClass;
	static HashSet<Integer> set = new HashSet<Integer>();

	/**
	 * Berechnet den erwarteten Gewinn pro Zeitstufe in Abhaengigkeit vom
	 * Anfangszustand ind
	 * 
	 * @param ind
	 *            Anfangszustand als Integer-Array
	 * @return erwarteter Gewinn pro Zeitstufe
	 */
	public static double createExpectedRevenue(int[] ind) {
		double[] equ_distr = { 0 }; // Default-Wert der Gleichgewichtsverteilung

		double expectedAverageRevenue = 0;
		recurrentClass = createRecurrentClassArray(ind);

		equ_distr = new double[recurrentClass.length];
		for (int i = 0; i < recurrentClass.length; i++) {
			equ_distr[i] = solveLGS(createRecurrentMatrix(), createBVector())[i];
		}

		for (int i = 0; i < recurrentClass.length; i++) {
			expectedAverageRevenue += equ_distr[i] * Main.revenue(Main.decompose(recurrentClass[i]));
		}
		return expectedAverageRevenue;
	}

	/**
	 * Erstellt die Matrix des LGS fuer die rekurrenten Zustaende
	 * 
	 * @return Matrix des LGS fuer die rekurrenten Zustaende
	 */
	public static double[][] createRecurrentMatrix() {
		double[][] matrix = new double[recurrentClass.length + 1][recurrentClass.length];

		for (int i = 0; i < recurrentClass.length; i++) {
			for (int j = 0; j < recurrentClass.length; j++) {
				if (i == j) {
					matrix[i][j] = 1 - Main.p(recurrentClass[j], recurrentClass[i]);
				} else {
					matrix[i][j] = -Main.p(recurrentClass[j], recurrentClass[i]);
				}
			}
		}
		for (int i = 0; i < recurrentClass.length; i++) {
			matrix[recurrentClass.length][i] = 1;
		}
		return matrix;
	}

	/**
	 * Erstellt die rechte Seite des LGS
	 * 
	 * @return Rechte Seite des LGS
	 */
	public static double[][] createBVector() {
		double[][] b_vector = new double[recurrentClass.length + 1][1];
		for (int i = 0; i < recurrentClass.length; i++) {
			b_vector[i][0] = 0;
		}

		b_vector[recurrentClass.length][0] = 1;

		return b_vector;
	}

	/**
	 * Loest das LGS abhaengig von der Matrix und dem B-Vektor
	 * 
	 * @param matrix
	 *            LGS-Matrix in Gaussscher Normalform
	 * @param b_vector
	 *            Rechte Seite des Gleichungssystems
	 */
	public static double[] solveLGS(double[][] matrix, double[][] b_vector) {
		Matrix m, b_vec, x_vec;
		m = new Matrix(matrix);
		b_vec = new Matrix(b_vector);
		x_vec = m.solve(b_vec);
		double[] solution_vector = new double[recurrentClass.length];
		for (int i = 0; i < matrix.length - 1; i++) {
			solution_vector[i] = x_vec.getArray()[i][0];
		}
		return solution_vector;
	}

	/**
	 * Zustaende der rekurrenten Klasse der Markov-Kette mit ungeraden
	 * Fuellstaenden im zweiten Kraftwerk
	 * 
	 * @return Zustands-Array
	 */
	// public static int[] loadOdd() {
	//
	// int[] recurrentOdd = { Main.compose(0, 1, 1), Main.compose(0, 3, 2),
	// Main.compose(1, 1, 1),
	// Main.compose(1, 3, 2), Main.compose(2, 1, 1), Main.compose(2, 3, 2),
	// Main.compose(3, 1, 1),
	// Main.compose(3, 3, 2), Main.compose(4, 1, 1), Main.compose(4, 3, 2),
	// Main.compose(5, 1, 1),
	// Main.compose(5, 3, 2) };
	// return recurrentOdd;
	//
	// }

	/**
	 * Zustaende der rekurrenten Klasse der Markov-Kette mit geraden
	 * Fuellstaenden im zweiten Kraftwerk
	 * 
	 * @return Zustands-Array
	 */
	// public static int[] loadEven() {
	// int[] recurrentEven = { Main.compose(0, 2, 1), Main.compose(0, 4, 2),
	// Main.compose(1, 2, 1),
	// Main.compose(1, 4, 2), Main.compose(2, 2, 1), Main.compose(2, 4, 2),
	// Main.compose(3, 2, 1),
	// Main.compose(3, 4, 2), Main.compose(4, 2, 1), Main.compose(4, 4, 2),
	// Main.compose(5, 2, 1),
	// Main.compose(5, 4, 2) };
	// return recurrentEven;
	// }

	/**
	 * Integer-Array mit rekurrenten Zustaenden wird mithilfe der Methode
	 * simulate() erstellt
	 *
	 * 
	 * @param ind
	 *            Anfangszustand als Integer-Array
	 * @return Integer-Array mit rekurrenten Zuständen
	 */
	public static Integer[] createRecurrentClassArray(int[] ind) {
		set = simulate(ind[0], ind[1], ind[2], Main.NO_OF_SIMS);
		Integer[] recurrent = set.toArray(new Integer[set.size()]);
		return recurrent;
	}

	/**
	 * Methode simuliert den Prozess n mal ausgehend vom Uebergebenen
	 * Anfangszustand und gibt die Menge rekurrenter Zustände als HashSet zurück
	 * Als Rekurrenter Zustand wird ein Zustand betrachtet, der bei der
	 * Simulation eine relative Auftrittshäufigkeit von > 0,1 % hat
	 * 
	 * @param i1
	 *            Anfangsfuellstand in Kraftwerk 1
	 * @param i2
	 *            Anfangsfuellstand in Kraftwerk 2
	 * @param i3
	 *            Anfangsfuellstand in Kraftwerk 3
	 * @param n
	 *            Anzahl der simulierten Perioden
	 * @return HashSet mit rekurrenten Zuständen
	 */

	public static HashSet<Integer> simulate(int i1, int i2, int i3, int n) {
		HashSet<Integer> set_0 = new HashSet<Integer>();
		int[] state = { i1, i2, i3 };
		int[] count = new int[Main.states];
		for (int i = 0; i < Main.states; i++) {
			count[i] = 0;
		}
		double rand = 0;
		int flow = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < Main.states; j++) {
				if (Main.compose(state) == j) {
					count[j] += 1;
				}
			}
			rand = (int) (Math.random() * 100) + 1;

			if (rand <= 20) {
				flow = 0;
			} else if ((rand <= 45) && (rand > 20)) {
				flow = 1;
			} else if ((rand <= 75) && (rand > 45)) {
				flow = 2;
			} else if (rand > 75) {
				flow = 3;
			}
			state = Main.g(state, flow);

		}
		// System.out.println("\nRelative Haeufigkeiten der Simluation von " + n
		// + " Perioden ausgehend vom Anfangszustand (" + i1 + ", " + i2 + ", "
		// + i3 + "): ");
		for (int i = 0; i < Main.states; i++) {
			if (count[i] > Main.NO_OF_SIMS / 1000) {
				// System.out.println("Count(" + Main.decompose(i)[0] + ", " +
				// Main.decompose(i)[1] + ", "
				// + Main.decompose(i)[2] + ") = " + count[i] / ((double) n));
				set_0.add(i);

			}
		}
		return set_0;
	}

}
