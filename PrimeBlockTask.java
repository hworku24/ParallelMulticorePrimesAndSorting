import java.util.concurrent.Callable;

public class PrimeBlockTask implements Callable<boolean[]>{

    private boolean[] isPrimesChunk;
    private int[] smallPrimes;
    private int start;
    private int end;

    public PrimeBlockTask(boolean[] arr, int[] smallPrimes, int start, int end){
        this.isPrimesChunk = arr;
        this.smallPrimes = smallPrimes;
        this.start = start;
        this.end = end;
    }

    public boolean[] call(){

        for (int p : smallPrimes) {
			// find the next number >= start that is a multiple of p
            int i = (start % p == 0) ? start : p * (1 + start / p);
			i -= start;

            for (; i < end - start; i += p) {
                isPrimesChunk[i] = true;
            }
        }
        return isPrimesChunk;
    }
}
