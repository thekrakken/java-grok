package io.thekraken.grok.api;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

public class ConcurrencyTest {

    /**
     * We will test this by setting up two threads, asserting on the hash values
     * the instances generate for each thread
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void test_001_concurrent_match() throws InterruptedException, ExecutionException {

        // Setup callable method that reports the hashcode for the thread
        int threadCount = 2;
        Callable<Integer> task = new Callable<Integer>() {
            @Override
            public Integer call() {
                return Match.getInstance().hashCode();
            }
        };

        // Create n tasks to execute
        List<Callable<Integer>> tasks = Collections.nCopies(threadCount, task);

        // Execute the task for both tasks
        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        List<Future<Integer>> futures = es.invokeAll(tasks);
        int hash1 = futures.get(0).get();
        int hash2 = futures.get(1).get();

        // The two hashcodes must NOT be equal
        Assert.assertThat(hash1, not(equalTo(hash2)));
    }

    @Test
    public void test_002_match_within_instance() {
        // Verify that the instances are equal for the same thread
        Match m1 = Match.getInstance();
        Match m2 = Match.getInstance();
        assertEquals(m1, m2);
    }

}
