package com.sanger.annotateBRASS;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import htsjdk.samtools.*;

public class filterSlaveControl implements Callable<svInformation>{
	private File master_control_bam = null;
	private svInformation info = null;
	private int search_width = -1;
	
	public filterSlaveControl(File master_control_bam, svInformation info, int search_width) {
		this.master_control_bam = master_control_bam;
		this.info = info;
		this.search_width = search_width * 2;
	}
	
	@Override
	public svInformation call() throws Exception {
		SAMRecordIterator it = null;
		Boolean controlPresent = false;
		Integer supportingPairs = 0;
		HashMap<String, SAMRecord> pairedMap = new HashMap<String, SAMRecord>(1000,0.9999f);
		SamReader inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).samRecordFactory(DefaultSAMRecordFactory.getInstance()).open(master_control_bam);
		if(!intersect(info, search_width)) {
			QueryInterval[] intervals = {new QueryInterval(inputSam.getFileHeader().getSequenceIndex(info.getChromLeft()), info.getStartLeft()-search_width, info.getEndLeft() + search_width), new QueryInterval(inputSam.getFileHeader().getSequenceIndex(info.getChromRight()), info.getStartRight()-search_width, info.getEndRight() + search_width)};
			it = inputSam.query(intervals, false);
		} else
			it = inputSam.query(info.getChromLeft(), info.getStartLeft() - search_width, info.getEndRight() + search_width, false);
		SAMRecord currentRecord = null;
		SAMRecord pairedRecord = null;
		while(it.hasNext()) {
			currentRecord = it.next();
			if(!pairedMap.containsKey(currentRecord.getReadName()))
				pairedMap.put(currentRecord.getReadName(),currentRecord);
			else if(!pairedMap.get(currentRecord.getReadName()).getPairedReadName().equals(currentRecord.getPairedReadName())) {
				pairedRecord = pairedMap.get(currentRecord.getReadName());
				pairedMap.remove(currentRecord.getReadName());
				if(!info.getChromLeft().equals(info.getChromRight())) {
					if(!currentRecord.getReferenceName().equals(pairedRecord.getReferenceName())) {
						controlPresent = true;
						supportingPairs++;
					}
				} else {
					if(fitsStructuralVariant(currentRecord, pairedRecord, info, search_width)) {
						controlPresent = true;
						supportingPairs++;
					}
				}
			}
		}
		it.close();
		inputSam.close();
		return info.setControlStatistics(controlPresent,supportingPairs);
	}
	private boolean intersect(svInformation info, int search) {
		if(!info.getChromLeft().equals(info.getChromRight()))
			return false;
		else if(info.getEndLeft() + search < info.getStartRight()-search)
			return false;
		return true;
	}
	private boolean fitsStructuralVariant(SAMRecord first, SAMRecord second, svInformation info, int search_width) {
		Boolean firstLeft = null;
		Boolean firstRight = null;
		Boolean secondLeft = null;
		Boolean secondRight = null;
		firstLeft = (!first.getReadNegativeStrandFlag()) ? (first.getAlignmentStart() <= info.getStartLeft() && Math.abs(info.getStartLeft() - first.getAlignmentEnd()) < search_width) : (first.getAlignmentEnd() >= info.getStartLeft() && Math.abs(first.getAlignmentStart() - info.getStartLeft()) < search_width);
		firstRight = (!first.getReadNegativeStrandFlag()) ? (first.getAlignmentStart() <= info.getStartRight() && Math.abs(info.getStartRight() - first.getAlignmentEnd()) < search_width) : (first.getAlignmentEnd() >= info.getStartRight() && Math.abs(first.getAlignmentStart() - info.getStartRight()) < search_width);
		secondLeft = (!second.getReadNegativeStrandFlag()) ? (second.getAlignmentStart() <= info.getStartLeft() && Math.abs(info.getStartLeft() - second.getAlignmentEnd()) < search_width) : (second.getAlignmentEnd() >= info.getStartLeft() && Math.abs(second.getAlignmentStart() - info.getStartLeft()) < search_width);
		secondRight = (!second.getReadNegativeStrandFlag()) ? (second.getAlignmentStart() <= info.getStartRight() && Math.abs(info.getStartRight() - second.getAlignmentEnd()) < search_width) : (second.getAlignmentEnd() >= info.getStartRight() && Math.abs(second.getAlignmentStart() - info.getStartRight()) < search_width);
		if((!firstLeft && !firstRight) || (!secondLeft && !secondRight))
			return false;
		else if((firstLeft && firstRight) || (secondLeft && secondRight))
			return false;
		else if((firstLeft == secondLeft) || (firstRight == secondRight))
			return false;
		return true;
	}
}