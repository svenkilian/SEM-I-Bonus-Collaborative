
// Author: Sven Koepke
import Jama.Matrix;

public class Sub {

	static int[] recurrent;
	
	public static void Main_2() {

		recurrent = loadOdd();
		solveLGS(createRecurrentMatrix(), createBVector());
		recurrent = loadEven();
		solveLGS(createRecurrentMatrix(), createBVector());

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
	 * Berechnet den erwarteten Gewinn pro Zeitstufe in Abhaengigkeit vom
	 * Anfangszustand ind
	 * 
	 * @param ind
	 *            Anfangszustand als Array
	 * @return erwarteter Gewinn pro Zeitstufe
	 */
	public static double createExpectedRevenue(int[] ind) {
		double[] equ_distr = new double[recurrent.length];
		double expectedAverageRevenue = 0;
		if (ind[1] % 2 != 0) {
			recurrent = loadOdd();
			System.out.println(
					"Gleichgewichtsverteilung ausgehend von einem Zustand mit ungeradem Fuellstand in Kraftwerk 2: ");
			for (int i = 0; i < recurrent.length; i++) {
				equ_distr[i] = solveLGS(createRecurrentMatrix(), createBVector())[i];

				System.out
						.println("Pi(" + Main.decompose(recurrent[i])[0] + ", " + Main.decompose(recurrent[i])[1]
								+ ", " + Main.decompose(recurrent[i])[2] + ") = " + equ_distr[i]);

			}
		}
		if (ind[1] % 2 == 0) {
			recurrent = loadEven();
			System.out.println(
					"Gleichgewichtsverteilung ausgehend von einem Zustand mit geradem Fuellstand in Kraftwerk 2: ");
			for (int i = 0; i < recurrent.length; i++) {
				equ_distr[i] = solveLGS(createRecurrentMatrix(), createBVector())[i];

				System.out.println(
						"Pi(" + Main.decompose(recurrent[i])[0] + ", " + Main.decompose(recurrent[i])[1] + ", "
								+ Main.decompose(recurrent[i])[2] + ") = " + equ_distr[i]);

			}
		}

		for (int i = 0; i < 6; i++) {
			expectedAverageRevenue += equ_distr[i] * Main.revenue(Main.decompose(recurrent[i]));
		}
		return expectedAverageRevenue;
	}


	/**
	 * Erstellt die Matrix des LGS fuer die rekurrenten Zustaende
	 * @return Matrix des LGS fuer die rekurrenten Zustaende
	 */
	public static double[][] createRecurrentMatrix() {
		double[][] matrix = new double[recurrent.length + 1][recurrent.length];

		for (int i = 0; i < recurrent.length; i++) {
			for (int j = 0; j < recurrent.length; j++) {
				if (i == j) {
					matrix[i][j] = 1 - Main.p(recurrent[j], recurrent[i]);
				} else {
					matrix[i][j] = -Main.p(recurrent[j], recurrent[i]);
				}
			}
		}
		for (int i = 0; i < recurrent.length; i++) {
			matrix[recurrent.length][i] = 1;
		}
		return matrix;
	}

	/**
	 * Erstellt die rechte Seite des LGS
	 * @return Rechte Seite des LGS
	 */
	public static double[][] createBVector() {
		double[][] b_vector = new double[recurrent.length + 1][1];
		for (int i = 0; i < recurrent.length; i++) {
			b_vector[i][0] = 0;
		}

		b_vector[recurrent.length][0] = 1;

		return b_vector;
	}

	/**
	 * Loest das LGS abhaenigig von der Matrix und dem B-Vektor
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
		double[] solution_vector = new double[recurrent.length];
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
	public static int[] loadOdd() {
		int[] recurrentOdd = { Main.compose(0, 1, 1), Main.compose(0, 3, 2), Main.compose(1, 1, 1),
				Main.compose(1, 3, 2), Main.compose(2, 1, 1), Main.compose(2, 3, 2), Main.compose(3, 1, 1),
				Main.compose(3, 3, 2), Main.compose(4, 1, 1), Main.compose(4, 3, 2), Main.compose(5, 1, 1),
				Main.compose(5, 3, 2) };
		return recurrentOdd;

	}

	/**
	 * Zustaende der rekurrenten Klasse der Markov-Kette mit geraden
	 * Fuellstaenden im zweiten Kraftwerk
	 * 
	 * @return Zustands-Array
	 */
	public static int[] loadEven() {
		int[] recurrentEven = { Main.compose(0, 2, 1), Main.compose(0, 4, 2), Main.compose(1, 2, 1),
				Main.compose(1, 4, 2), Main.compose(2, 2, 1), Main.compose(2, 4, 2), Main.compose(3, 2, 1),
				Main.compose(3, 4, 2), Main.compose(4, 2, 1), Main.compose(4, 4, 2), Main.compose(5, 2, 1),
				Main.compose(5, 4, 2) };
		return recurrentEven;
	}

}
