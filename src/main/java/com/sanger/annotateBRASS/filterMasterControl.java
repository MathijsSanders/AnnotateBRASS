package com.sanger.annotateBRASS;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class filterMasterControl {
	
	private File master_control_bam = null;
	private List<svInformation> structuralVariants = null;
	private int search_width = 0;
	private int threads = 1;
	
	public filterMasterControl(File master_control_bam, List<svInformation> structuralVariants, int search_width, int threads) {
		this.master_control_bam = master_control_bam;
		this.structuralVariants = structuralVariants;
		this.search_width = search_width;
		this.threads = threads;
	}
	public List<svInformation> startAnalysis() throws InterruptedException {
		ExecutorService exec = Executors.newFixedThreadPool(threads);
		List<Future<svInformation>> futures = new ArrayList<Future<svInformation>>(structuralVariants.size());
		structuralVariants.forEach(i -> futures.add(exec.submit(new filterSlaveControl(master_control_bam, i, search_width))));
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
		return svList;
	}
}