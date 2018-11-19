package com.sanger.annotateBRASS;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class filterMasterBrass {
	
	private File brass_bam_file = null;
	private List<svInformation> structuralVariants = null;
	private int search_width = 0;
	private int threads = 1;
	private int difference_score = 0;
	
	public filterMasterBrass(File brass_bam_file, List<svInformation> structuralVariants, int search_width, int threads, int difference_score) {
		this.brass_bam_file = brass_bam_file;
		this.structuralVariants = structuralVariants;
		this.search_width = search_width;
		this.threads = threads;
		this.difference_score = difference_score;
	}
	public List<svInformation> startAnalysis() throws InterruptedException {
		ExecutorService exec = Executors.newFixedThreadPool(threads);
		List<Future<svInformation>> futures = new ArrayList<Future<svInformation>>(structuralVariants.size());
		structuralVariants.forEach(i -> futures.add(exec.submit(new filterSlaveBrass(brass_bam_file, i, search_width, difference_score))));
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
