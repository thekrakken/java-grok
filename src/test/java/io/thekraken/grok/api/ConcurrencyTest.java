package io.thekraken.grok.api;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import oi.thekraken.grok.api.Match;

import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsNot.*;
import org.junit.Assert;
import org.junit.Test;

public class ConcurrencyTest {

	@Test
	public void test_001_concurrent_match() throws InterruptedException, ExecutionException {

		int threadCount = 2;
		Callable<Integer> task = new Callable<Integer>() {
			@Override
			public Integer call() {
				return Match.getInstance().hashCode();
			}
		};
		List<Callable<Integer>> tasks = Collections.nCopies(threadCount, task);
	    
		ExecutorService es = Executors.newFixedThreadPool(threadCount);
		List<Future<Integer>> futures = es.invokeAll(tasks);
		int hash1 = futures.get(0).get();
		int hash2 = futures.get(1).get();
		
		Assert.assertThat(hash1, not(equalTo(hash2)));
	}
	
	@Test
	public void test_002_match_within_instance() {
		Match m1 = Match.getInstance();
		Match m2 = Match.getInstance();
		assertEquals(m1, m2);
	}
	
}
