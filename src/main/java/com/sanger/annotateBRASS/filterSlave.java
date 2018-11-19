package com.sanger.annotateBRASS;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import htsjdk.samtools.*;

public class filterSlave implements Callable<svInformation>{
	private File bam_file = null;
	private svInformation info = null;
	private int search_width = -1;
	private int difference_score = 0;
	private int discordant_distance = 0;
	private int discordant_distance_search = 0;
	
	public filterSlave(File bam_file, svInformation info, int search_width, int difference_score, int discordant_distance, int discordant_distance_search) {
		this.bam_file = bam_file;
		this.info = info;
		this.search_width = search_width;
		this.difference_score = difference_score;
		this.discordant_distance = discordant_distance;
		this.discordant_distance_search = discordant_distance_search;
	}
	
	@Override
	public svInformation call() throws Exception {
		List<SAMRecord> firstBreak = new ArrayList<SAMRecord>(100);
		List<SAMRecord> secondBreak = new ArrayList<SAMRecord>(100);
		HashSet<String> diffMapLeft = new HashSet<String>(100);
		HashSet<String> diffMapRight = new HashSet<String>(100);
		SAMRecordIterator it = null;
		HashSet<String> search = new HashSet<String>(Arrays.asList(info.getReads()));
		String xa = null;
		Integer xaCountLeft = 0;
		Integer xaCountRight = 0;
		Integer subscoreLeft = 0;
		Integer subscoreRight = 0;
		Double discordantPercentageLeft = 0.0;
		Double discordantPercentageRight = 0.0;
		SamReader inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).samRecordFactory(DefaultSAMRecordFactory.getInstance()).open(bam_file);
		if(!intersect(info, search_width)) {
			QueryInterval[] intervals = {new QueryInterval(inputSam.getFileHeader().getSequenceIndex(info.getChromLeft()), info.getStartLeft()-search_width, info.getEndLeft() + search_width), new QueryInterval(inputSam.getFileHeader().getSequenceIndex(info.getChromRight()), info.getStartRight()-search_width, info.getEndRight() + search_width)};
			it = inputSam.query(intervals, false);
		} else
			it = inputSam.query(info.getChromLeft(), info.getStartLeft() - search_width, info.getEndRight() + search_width, false);
		SAMRecord currentRecord = null;
		while(it.hasNext()) {
			currentRecord = it.next();
			if(!currentRecord.getReferenceName().equals(currentRecord.getMateReferenceName())) {
				if(validateFirst(currentRecord, info))
					diffMapLeft.add(currentRecord.getMateReferenceName());
				else
					diffMapRight.add(currentRecord.getMateReferenceName());
			}
			if(search.contains(currentRecord.getReadName())) {
				xa = currentRecord.getStringAttribute("XA");
				if(xa != null && !xa.equals("")){
					if(validateFirst(currentRecord, info))
						xaCountLeft++;
					else
						xaCountRight++;
				}
				if((currentRecord.getIntegerAttribute("XS") >= currentRecord.getIntegerAttribute("AS")) || (Math.abs(currentRecord.getIntegerAttribute("XS") - currentRecord.getIntegerAttribute("AS")) < difference_score)) {
					if(validateFirst(currentRecord, info))
						subscoreLeft++;
					else
						subscoreRight++;
				}
				addVicinity(currentRecord, info, firstBreak, secondBreak);
			}
		}
		it.close();
		discordantPercentageLeft = getDiscordantPercentage(inputSam, info.getChromLeft(), info.getStartLeft(), info.getEndLeft(), discordant_distance, discordant_distance_search, search);
		discordantPercentageRight = getDiscordantPercentage(inputSam, info.getChromRight(), info.getStartRight(), info.getEndRight(), discordant_distance, discordant_distance_search, search);
		inputSam.close();
		return info.setStatistics(firstBreak.size(), secondBreak.size(), countUnique(firstBreak), countUnique(secondBreak), calcVariance(firstBreak), calcVariance(secondBreak), diffMapLeft.size(), diffMapRight.size(), xaCountLeft, xaCountRight, subscoreLeft, subscoreRight, discordantPercentageLeft, discordantPercentageRight);
	}
	private Double getDiscordantPercentage(SamReader inputSam, String chrom, int start, int end, int discordant_distance, int discordant_distance_search, HashSet<String> search) {
		SAMRecordIterator it = inputSam.query(chrom, start - discordant_distance_search, end + discordant_distance_search, false);
		SAMRecord current = null;
		HashSet<String> found = new HashSet<String>(10000, 0.9999f);
		Double discordant = 0.0;
		Double total = 0.0;
		while(it.hasNext()) {
			current = it.next();
			if(!found.contains(current.getReadName()) && !search.contains(current.getReadName())) {
				if(current.getInferredInsertSize() >= discordant_distance)
					discordant++;
				total++;
				found.add(current.getReadName());
			}
		}
		it.close();
		return discordant/total;
	}
	private boolean intersect(svInformation info, int search) {
		if(!info.getChromLeft().equals(info.getChromRight()))
			return false;
		else if(info.getEndLeft() + search < info.getStartRight()-search)
			return false;
		return true;
	}
	private boolean validateFirst(SAMRecord current, svInformation info) {
		if(!current.getContig().equals(info.getChromLeft()))
			return false;
		else if(!current.getContig().equals(info.getChromRight()))
			return true;
		else if(Math.abs(current.getAlignmentStart() - info.getStartLeft()) < Math.abs(current.getAlignmentStart()-info.getStartRight()))
			return true;
		return false;
	}
	private void addVicinity(SAMRecord current, svInformation info, List<SAMRecord> first, List<SAMRecord> second) {
		if(!current.getContig().equals(info.getChromLeft()))
			second.add(current);
		else if(!current.getContig().equals(info.getChromRight()))
			first.add(current);
		else if(Math.abs(current.getAlignmentStart() - info.getStartLeft()) < Math.abs(current.getAlignmentStart()-info.getStartRight()))
			first.add(current);
		else
			second.add(current);
	}
	private int countUnique(List<SAMRecord> reads) {
		HashSet<Integer> location = new HashSet<Integer>(1000,0.9999f);
		for(SAMRecord read : reads)
			location.add((!read.getReadNegativeStrandFlag()) ? read.getAlignmentStart() : read.getAlignmentEnd());
		return location.size();		
	}
	private double calcVariance(List<SAMRecord> reads) {
		if(reads.size() == 0)
			return Double.NaN;
		Double mean = reads.stream().mapToDouble(i -> ((!i.getReadNegativeStrandFlag()) ? i.getAlignmentStart() : i.getAlignmentEnd())).average().getAsDouble();
		Double size = Double.valueOf(reads.size())-1.0;
		Double diff = 0.0;
		for(SAMRecord current : reads)
			diff += Math.pow(((!current.getReadNegativeStrandFlag()) ? current.getAlignmentStart() : current.getAlignmentEnd()) - mean, 2.0);
		return Math.sqrt(diff/size);
	}
}
