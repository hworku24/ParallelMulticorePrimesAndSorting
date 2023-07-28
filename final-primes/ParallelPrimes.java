import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ParallelPrimes {

    // replace this string with your team name
    public static final String TEAM_NAME = "Dottin-CarterChapmanWorku";
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    public static final int N_PRIMES = 105_097_565;
    public static final int ROOT_MAX = (int) Math.sqrt(MAX_VALUE);
    public static final int MAX_SMALL_PRIME = 1 << 20;
    public static final int nThreads = Runtime.getRuntime().availableProcessors();

    // Use the sieve of Eratosthenes to compute all prime numbers up
    // to max. The largest allowed value of max is MAX_SMALL_PRIME.
    public static int[] getSmallPrimesUpTo(int max) {

        // check that the value max is in bounds, and throw an
        // exception if not
        if (max > MAX_SMALL_PRIME) {
            throw new RuntimeException("The value " + max + "exceeds the maximum small prime value (" + MAX_SMALL_PRIME + ")");
        }

        // isPrime[i] will be true if and only if i is
        // prime. Initially set isPrime[i] to true for all i >= 2.
        boolean[] isComposite = new boolean[max];

        // Apply the sieve of Eratosthenes to find primes. The
        // procedure iterates over values i = 2, 3,.... If isPrime[i]
        // == true, then i is a prime. When a prime value i is found,
        // set isPrime[j] = false for all multiples j of i. The
        // procedure terminates once we've examined all values i up to
        // Math.sqrt(max).
        int rootMax = (int) Math.sqrt(max);
        for (int i = 2; i <= rootMax; i++) {
            if (!isComposite[i]) {
                for (int j = i * i; j < max; j += i) {
                    isComposite[j] = true;
                }
            }
        }

        // Count the number of primes we've found, and put them
        // sequentially in an appropriately sized array.
        // int count = trueCount(isComposite);
        int count = 0;
        for (int i = 2; i < max; i++) {
            if (!isComposite[i]) count++;
        }

        int[] primes = new int[count];
        int pIndex = 0;

        for (int i = 2; i < max; i++) {
            if (!isComposite[i]) {
                primes[pIndex] = i;
                pIndex++;
            }
        }

        return primes;
    }

    // Count the number of true values in an array of boolean values,
    // arr
    public static int trueCount(boolean[] arr) {
        int count = 0;
        int length = arr.length;
        for (int i = 0; i < length; i++) {
            if (arr[i])
            count++;
        }

        return count;
    }

    // Returns an array of all prime numbers up to ROOT_MAX
    public static int[] getSmallPrimes() {
        return getSmallPrimesUpTo(ROOT_MAX);
    }    


    // Compute a block of prime values between start and start +
    // isPrime.length. Specifically, after calling this method
    // isPrime[i] will be true if and only if start + i is a prime
    // number, assuming smallPrimes contains all prime numbers of to
    // sqrt(start + isPrime.length).
    private static void primeBlock(boolean[] isPrime, int[] smallPrimes, int start) {

        // initialize isPrime to be all true
        for (int i = 0; i < isPrime.length; i++) {
            isPrime[i] = true;
        }

        for (int p : smallPrimes) {
            
            // find the next number >= start that is a multiple of p
            int i = (start % p == 0) ? start : p * (1 + start / p);
            i -= start;

            while (i < isPrime.length) {
                isPrime[i] = false;
                i += p;
            }
        }
    }

    public static void optimizedPrimes(int[] primes) {
    
        // replace this with your optimized method
        // Primes.baselinePrimes(primes);
        // compute small prime values
        int[] smallPrimes = getSmallPrimes();
        int nPrimes = primes.length;

        // write small primes to primes
        int count = 0;
        int minSize = Math.min(nPrimes, smallPrimes.length);
        for (; count < minSize; count++) {
            primes[count] = smallPrimes[count];
        }

        // check if we've already filled primes, and return if so
        if (nPrimes == minSize) {
            return;
        }
    
        // Apply the sieve of Eratosthenes to find primes. This
        // procedure partitions the sieving task up into several
        // blocks, where each block isPrime stores boolean values
        // associated with ROOT_MAX consecutive numbers. Note that
        // partitioning the problem in this way is necessary because
        // we cannot create a boolean array of size MAX_VALUE.
        // boolean[] isPrime = new boolean[N_PRIMES];

        //The getSmallPrimes method took care of getting the prime numbers from 2 to ROOT_MAX, so we can take care of the prime numbers from ROOT_MAX to MAX_VALUE.
        //That's around 2,147,437,306 numbers, but we can parallelize it into chunks
        //The size of each chunk is basically how many numbers we have to sieve(MAX_VALUE - ROOT_MAX), divided by how many tasks we want it to be done in.
        //For now, the amount of tasks is arbitrary, we chose 10000, but it could easily be something else.
        //The +1 at the end is to handle the round off that occurs with Integer division
        int chunkSize = ((MAX_VALUE - ROOT_MAX)/10000)+1;

        //Each task will receive a copy of a boolean array the size of each chunk
        //We don't initialized all the indices to true to save time
        boolean[] isPrimesChunk = new boolean[chunkSize];

        //We make a thread pool of a fixed size
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        //Make an arraylist of Callable<boolean[]> objects
        ArrayList<Callable<boolean[]>> tasks = new ArrayList<Callable<boolean[]>>();

        //For 0 up to the total number of tasks 
        for (long i = 0; i < 10000; i += 1) {
            //Each task is an instance of our PrimeBlockTask class, which will basically do a modified version of the primeBlock method.
            //Each task will take chunk of the numbers from ROOT_MAX to MAX_VALUE to sieve
            tasks.add(new PrimeBlockTask(Arrays.copyOf(isPrimesChunk, chunkSize), smallPrimes, (int) (ROOT_MAX + i * chunkSize), (int)((ROOT_MAX + i * chunkSize) + chunkSize)));
        }
        
        try {
            //Submit all the tasks and wait for all of them to be completed via the invokeAll method, and put that into a list of Future<boolean<>>.
            //We opt for this approach because the order of the tasks corresponds to the numerical order of the primes that they sieve collectively.
            //For example, let's say task 1 sieves the numbers from 45000 to 64999 and task 1 sieves the numbers from 65000 to 84999.
            //Both tasks can be done in parallel and it doesn't really matter which one is completed, 
            //but we have to look at the result of task 1 before looking at the result of task 2 since we have to print the prime numbers in numerical order
            List<Future<boolean[]>> results = executorService.invokeAll(tasks);

            //For all the results of each task
            int resultsLength = results.size();
            int chunkLength = isPrimesChunk.length;
            for (int i = 0; i < resultsLength; i++) {

                //Get the chunk from the task, where the chunk is a boolean array that indicates which numbers in a specific range were prime.
                boolean[] chunk = results.get(i).get();

                //For each position, j, in that chunk
                for (int j = 0; j < chunkLength && count < nPrimes; j++) {
                    //If the position is marked as prime
                    if(!chunk[j]){
                        //go to the next position in the primes int array, and set the value at that index to the expression below
                        //the expression in question is pretty much taking position j in the chunck, and mapping it to its position in the range from ROOT_MAX to MAX_VALUE
                        primes[count++] = (int) (i * chunkSize) + j + ROOT_MAX;
                    }
                }
            }
        } catch (Exception e) {
            // System.out.println(e.getMessage());
        }
        
        //That's it. All we have to do now is shutdown and threadpool and wait for the service to terminate
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // System.out.println(e.getMessage());
        }
    }
}
