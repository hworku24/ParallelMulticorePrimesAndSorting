// A class for testing the correctness and performance of your
// implementation of the prime finding task. 
// ***DO NOT MODIFY THIS FILE***

public class PrimeTester {
    public static final int WARMUP_ITERATIONS = 5;
    public static final int TEST_ITERATIONS = 10;
    
    public static void main (String[] args) {
		System.out.println("Computing primes up to " + Primes.MAX_VALUE);
		
		int[] knownPrimes = new int[Primes.N_PRIMES];
		int[] testPrimes = new int[Primes.N_PRIMES];

		long baseLineStart = System.nanoTime();

		// find known primes using the baseline procedure
		Primes.baselinePrimes(knownPrimes);

		long elapsedBL = (System.nanoTime() - baseLineStart) / 1_000_000;
		System.out.println("Baseline elapsed time: " + elapsedBL + "ms");

		// run warmup before timing
		System.out.println("Warming up Team: " + ParallelPrimes.TEAM_NAME);

		for (int i = 0; i < WARMUP_ITERATIONS; i++) {
			ParallelPrimes.optimizedPrimes(testPrimes);
		}

		System.out.println("Finished warming up");

		// run main iterations
		long start = System.nanoTime();
		ParallelPrimes.optimizedPrimes(testPrimes);
		long elapsedMS = (System.nanoTime() - start) / 1_000_000;
		System.out.println("Optimized: elapsed time: " + elapsedMS + "ms");
		
		for (int i = 0; i < TEST_ITERATIONS; i++) {
			ParallelPrimes.optimizedPrimes(testPrimes);
		}

		// check correctness

		System.out.println("Verifying correctnes");
		
		for (int i = 0; i < knownPrimes.length; i++) {
			if (knownPrimes[i] != testPrimes[i]) {
			System.out.println("correctness test failed\n" +
					"i = " + i + "\n" +
					"knownPrimes[i] = " + knownPrimes[i] + "\n" +
					"testPrimes[i] = " + testPrimes[i]);
			return;
			}
		}

		System.out.println("correctness test passed");
    }
}
