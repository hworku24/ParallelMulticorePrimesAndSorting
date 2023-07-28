// importing necessary classes
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class Sorting {

    private static final int SEQUENTIAL_THRESHOLD = 1000; //  threshold value for when to switch to sequential sorting.

    public static class QuickSortTask extends RecursiveAction {
        private final float[] data;
        private final int low;
        private final int high;

        QuickSortTask(float[] data, int low, int high) {
            this.data = data;
            this.low = low;
            this.high = high;
        }

        @Override
        protected void compute() {
            if (high - low < SEQUENTIAL_THRESHOLD) {
                Arrays.sort(data, low, high + 1); // Switch to sequential sorting for small arrays
            } else {
                int pivotIndex = partition(data, low, high);
                QuickSortTask leftTask = new QuickSortTask(data, low, pivotIndex - 1);
                QuickSortTask rightTask = new QuickSortTask(data, pivotIndex + 1, high);
                leftTask.fork(); // Submit the left subtask to the pool asynchronously
                rightTask.compute(); // Sort the right subtask sequentially in the current thread
                leftTask.join(); // Wait for the left subtask to complete before returning
            }
        }
    }
    public static final String TEAM_NAME = "Dottin-CarterChapmanWorku";

    public static void baselineSort (float[] data) {
        Arrays.sort(data, 0, data.length);

    }


    public static void parallelSort(float[] data) {
        ForkJoinPool pool = new ForkJoinPool(); // Default pool size is the number of processors
        pool.invoke(new QuickSortTask(data, 0, data.length - 1));
        pool.shutdown();
    }

    private static void randomPivot(float[] arr, int low, int high){
        int pivot = low + ThreadLocalRandom.current().nextInt(high - low); // Use ThreadLocalRandom instead of Random for thread safety
        float temp = arr[pivot];
        arr[pivot] = arr[high];
        arr[high] = temp;
    }

    private static int partition(float[] arr, int low, int high){
        randomPivot(arr, low, high);
        float pivotValue = arr[high];
        int smallerIndex = low - 1;
        for (int i = low; i < high; i++) {
            if(arr[i] < pivotValue){
                smallerIndex++;
                float temp = arr[smallerIndex];
                arr[smallerIndex] = arr[i];
                arr
                        [ i ] = temp;
            }
        }
        int pivotIndex = smallerIndex + 1;
        float temp = arr[pivotIndex];
        arr[pivotIndex] = arr[high];
        arr[high] = temp;
        return pivotIndex;
    }
}
