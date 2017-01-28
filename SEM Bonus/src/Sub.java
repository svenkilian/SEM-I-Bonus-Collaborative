
// Author: Sven Köpke
import Jama.Matrix;

public class Sub {
	public static int[] recurrentOdd = { Main.compose(0, 1, 1), Main.compose(0, 3, 2), Main.compose(1, 1, 1),
			Main.compose(1, 3, 2), Main.compose(2, 1, 1), Main.compose(2, 3, 2), Main.compose(3, 1, 1),
			Main.compose(3, 3, 2), Main.compose(4, 1, 1), Main.compose(4, 3, 2), Main.compose(5, 1, 1),
			Main.compose(5, 3, 2) }; // Zustände
	// der
	// rekurrenten
	// Klasse
	// der
	// Markov-Kette
	// mit
	// ungeraden
	// Füllständen
	// im
	// 2.
	// Kraftwerk
	public static int[] recurrentEven = { Main.compose(0, 2, 1), Main.compose(0, 4, 2), Main.compose(1, 2, 1),
			Main.compose(1, 4, 2), Main.compose(2, 2, 1), Main.compose(2, 4, 2), Main.compose(3, 2, 1),
			Main.compose(3, 4, 2), Main.compose(4, 2, 1), Main.compose(4, 4, 2), Main.compose(5, 2, 1),
			Main.compose(5, 4, 2) }; // Zustände
	// der
	// rekurrenten
	// Klasse
	// der
	// Markov-Kette
	// mit
	// geraden
	// Füllständen
	// im
	// 2.
	// Kraftwerk

	public static void Main_2() {

		solveLGS(createRecurrentOddMatrix(), createOddBVector());
		solveLGS(createRecurrentEvenMatrix(), createEvenBVector());

		int i_Odd_1 = 1;
		int i_Odd_2 = 1;
		int i_Odd_3 = 1;

		int i_Even_1 = 10;
		int i_Even_2 = 10;
		int i_Even_3 = 8;

		System.out.println("\nErwateter Gewinn pro Zeitstufe ausgehend von Zustand (" + i_Odd_1 + ", " + i_Odd_2 + ", "
				+ i_Odd_3 + "): " + createExpectedRevenue(Main.decompose(Main.compose(i_Odd_1, i_Odd_2, i_Odd_3)))
				+ "\n");

		System.out.println("\nErwateter Gewinn pro Zeitstufe ausgehend von Zustand (" + i_Even_1 + ", " + i_Even_2
				+ ", " + i_Even_3 + "): "
				+ createExpectedRevenue(Main.decompose(Main.compose(i_Even_1, i_Even_2, i_Even_3))));

	}

	/**
	 * Berechnet den erwarteten Gewinn pro Zeitstufe in Abhängigkeit vom
	 * Anfangszustand ind
	 * 
	 * @param ind
	 *            Anfangszustand als Array
	 * @return erwarteter Gewinn pro Zeitstufe
	 */
	public static double createExpectedRevenue(int[] ind) {
		double[] equ_distr = new double[recurrentEven.length];
		int[] recurrent = new int[recurrentEven.length];
		double expectedAverageRevenue = 0;
		if (ind[1] % 2 != 0) {
			System.out.println(
					"Gleichgewichtsverteilung ausgehend von einem Zustand mit ungeradem Füllstand in Kraftwerk 2: ");
			for (int i = 0; i < recurrentEven.length; i++) {
				equ_distr[i] = solveLGS(createRecurrentOddMatrix(), createOddBVector())[i];

				System.out
						.println("Pi(" + Main.decompose(recurrentOdd[i])[0] + ", " + Main.decompose(recurrentOdd[i])[1]
								+ ", " + Main.decompose(recurrentOdd[i])[2] + ") = " + equ_distr[i]);

			}
			recurrent = recurrentOdd;
		}
		if (ind[1] % 2 == 0) {
			System.out.println(
					"Gleichgewichtsverteilung ausgehend von einem Zustand mit geradem Füllstand in Kraftwerk 2: ");
			for (int i = 0; i < recurrentEven.length; i++) {
				equ_distr[i] = solveLGS(createRecurrentEvenMatrix(), createEvenBVector())[i];

				System.out.println(
						"Pi(" + Main.decompose(recurrentEven[i])[0] + ", " + Main.decompose(recurrentEven[i])[1] + ", "
								+ Main.decompose(recurrentEven[i])[2] + ") = " + equ_distr[i]);

			}
			recurrent = recurrentEven;
		}

		for (int i = 0; i < 6; i++) {
			expectedAverageRevenue += equ_distr[i] * Main.revenue(Main.decompose(recurrent[i]));
		}
		return expectedAverageRevenue;
	}

	/**
	 * @return Matrix des LGS für die rekurrenten Zustände mit ungeradem
	 *         Füllstand in Kraftwerk 2
	 */
	public static double[][] createRecurrentOddMatrix() {
		double[][] matrix = new double[recurrentOdd.length + 1][recurrentOdd.length];

		for (int i = 0; i < recurrentOdd.length; i++) {
			for (int j = 0; j < recurrentOdd.length; j++) {
				if (i == j) {
					matrix[i][j] = 1 - Main.pVal(recurrentOdd[j], recurrentOdd[i]);
					// System.out.print(Main.pVal(recurrentOdd[i],
					// recurrentOdd[j]) + " ");

				} else {
					matrix[i][j] = -(Main.pVal(recurrentOdd[j], recurrentOdd[i])); // p(j,i)
					// System.out.print(Main.pVal(recurrentOdd[i],
					// recurrentOdd[j]) + " ");

				}
			}
			// System.out.println();
		}
		for (int i = 0; i < recurrentOdd.length; i++) {
			matrix[recurrentOdd.length][i] = 1;
		}

		return matrix;
	}

	/**
	 * @return Matrix des LGS für die rekurrenten Zustände mit geradem Füllstand
	 *         in Kraftwerk 2
	 */
	public static double[][] createRecurrentEvenMatrix() {
		double[][] matrix = new double[recurrentEven.length + 1][recurrentEven.length];

		for (int i = 0; i < recurrentEven.length; i++) {
			for (int j = 0; j < recurrentEven.length; j++) {
				if (i == j) {
					matrix[i][j] = 1 - Main.pVal(recurrentEven[j], recurrentEven[i]);
				} else {
					matrix[i][j] = -Main.pVal(recurrentEven[j], recurrentEven[i]);
				}
			}
		}
		for (int i = 0; i < recurrentEven.length; i++) {
			matrix[recurrentEven.length][i] = 1;
		}
		return matrix;
	}

	/**
	 * @return Rechte Seite des LGS für die Zustände mit ungeraden Füllständen
	 *         im 2. Kraftwerk
	 */
	public static double[][] createOddBVector() {
		double[][] b_vector = new double[recurrentOdd.length + 1][1];
		for (int i = 0; i < recurrentOdd.length; i++) {
			b_vector[i][0] = 0;
		}

		b_vector[recurrentOdd.length][0] = 1;

		return b_vector;
	}

	/**
	 * @param nOfVariables
	 * @return Rechte Seite des LGS für die Zustände mit geraden Füllständen im
	 *         2. Kraftwerk
	 */
	public static double[][] createEvenBVector() {
		double[][] b_vector = new double[recurrentEven.length + 1][1];
		for (int i = 0; i < recurrentEven.length; i++) {
			b_vector[i][0] = 0;
		}

		b_vector[recurrentEven.length][0] = 1;

		return b_vector;
	}

	/**
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
		double[] solution_vector = new double[recurrentEven.length];
		// for (int i = 0; i < matrix.length; i++) {
		// for (int j = 0; j < matrix.length - 1; j++) {
		// System.out.print(matrix[i][j] + " ");
		// }
		// System.out.println();
		// }
		for (int i = 0; i < matrix.length - 1; i++) {
			// System.out.println(
			// "Pi(" + Main.decompose(recurrentOdd[i])[0] + ", " +
			// Main.decompose(recurrentOdd[i])[1] + ", "
			// + Main.decompose(recurrentOdd[i])[2] + ") = " +
			// x_vec.getArray()[i][0]);
			solution_vector[i] = x_vec.getArray()[i][0];
		}
		return solution_vector;
	}

	/**
	 * @param matrix
	 *            LGS-Matrix in Gaussscher Normalform
	 * @param b_vector
	 *            Rechte Seite des Gleichungssystems
	 */

}
