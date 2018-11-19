package com.sanger.annotateBRASS;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class filterMasterShared {
	
	private File[] shared_individual_controls = null;
	private List<svInformation> structuralVariants = null;
	private int search_width = 0;
	private int threads = 1;
	
	public filterMasterShared(File[] shared_individual_controls, List<svInformation> structuralVariants, int search_width, int threads) {
		this.shared_individual_controls = shared_individual_controls;
		this.structuralVariants = structuralVariants;
		this.search_width = search_width;
		this.threads = threads;
	}
	public List<svInformation> startAnalysis() throws InterruptedException {
		List<svInformation> masterList = structuralVariants;
		for(File currentSample : shared_individual_controls) {
			ExecutorService exec = Executors.newFixedThreadPool(threads);
			List<Future<svInformation>> futures = new ArrayList<Future<svInformation>>(structuralVariants.size());
			masterList.forEach(i -> futures.add(exec.submit(new filterSlaveShared(currentSample, i, search_width))));
			for(Future<svInformation> result : futures) {
			    try {
			    	result.get();
			    } catch(InterruptedException | ExecutionException e) {
			    	e.printStackTrace();
			    	System.out.println("Thread failed.");
			    	System.exit(-3);
			    }
			}
			exec.shutdown();
			System.gc();
			List<svInformation> svList = new ArrayList<svInformation>(futures.size());
			try {
				for(Future<svInformation> result : futures) {
					svList.add(result.get());
				}
			} catch(InterruptedException | ExecutionException e) {
				e.printStackTrace();
				System.out.println("Retrieving structural variant information failed.");
				System.exit(-4);
			}
			masterList = svList;
		}
		return masterList;
	}
}
