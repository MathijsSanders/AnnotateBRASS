package com.sanger.annotateBRASS;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import com.sanger.intervalTree.*;

import htsjdk.samtools.*;

public class filterSlaveBrass implements Callable<svInformation>{
	private File bam_file = null;
	private svInformation info = null;
	private int search_width = -1;
	private int difference_score = 0;
	private int diffThreshold = 10;
	public filterSlaveBrass(File bam_file, svInformation info, int search_width, int difference_score) {
		this.bam_file = bam_file;
		this.info = info;
		this.search_width = search_width*2;
		this.difference_score = difference_score;
	}
	
	@Override
	public svInformation call() throws Exception {
		List<SAMRecord> leftReads = new ArrayList<SAMRecord>(100);
		List<SAMRecord> rightReads = new ArrayList<SAMRecord>(100);
		SAMRecordIterator it = null;
		String xa = null;
		Integer xaCountLeft = 0;
		Integer xaCountRight = 0;
		Integer subscoreLeft = 0;
		Integer subscoreRight = 0;
		Integer brassTotalLeft = 0;
		Integer brassTotalRight = 0;
		HashMap<String, SAMRecord> pairedMap = new HashMap<String, SAMRecord>(1000,0.9999f);
		SamReader inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).samRecordFactory(DefaultSAMRecordFactory.getInstance()).open(bam_file);
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
						if(currentRecord.getReferenceName().equals(info.getChromLeft())) {
							xa = currentRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountLeft++;
							if((currentRecord.getIntegerAttribute("XS") >= currentRecord.getIntegerAttribute("AS")) || (Math.abs(currentRecord.getIntegerAttribute("XS") - currentRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreLeft++;
							xa = pairedRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountRight++;
							if((pairedRecord.getIntegerAttribute("XS") >= pairedRecord.getIntegerAttribute("AS")) || (Math.abs(pairedRecord.getIntegerAttribute("XS") - pairedRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreRight++;
						} else {
							xa = currentRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountRight++;
							if((currentRecord.getIntegerAttribute("XS") >= currentRecord.getIntegerAttribute("AS")) || (Math.abs(currentRecord.getIntegerAttribute("XS") - currentRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreRight++;
							xa = pairedRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountLeft++;
							if((pairedRecord.getIntegerAttribute("XS") >= pairedRecord.getIntegerAttribute("AS")) || (Math.abs(pairedRecord.getIntegerAttribute("XS") - pairedRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreLeft++;
						}
						leftReads.add(currentRecord.getReferenceName().equals(info.getChromLeft()) ? currentRecord : pairedRecord);
						rightReads.add(currentRecord.getReferenceName().equals(info.getChromLeft()) ? pairedRecord : currentRecord);
						brassTotalLeft++;
						brassTotalRight++;
					}
				}
				else {
					if(fitsStructuralVariant(currentRecord, pairedRecord, info, search_width)) {
						if(isLeft(currentRecord, info)) {
							xa = currentRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountLeft++;
							if((currentRecord.getIntegerAttribute("XS") >= currentRecord.getIntegerAttribute("AS")) || (Math.abs(currentRecord.getIntegerAttribute("XS") - currentRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreLeft++;
							xa = pairedRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountRight++;
							if((pairedRecord.getIntegerAttribute("XS") >= pairedRecord.getIntegerAttribute("AS")) || (Math.abs(pairedRecord.getIntegerAttribute("XS") - pairedRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreRight++;
						} else {
							xa = currentRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountRight++;
							if((currentRecord.getIntegerAttribute("XS") >= currentRecord.getIntegerAttribute("AS")) || (Math.abs(currentRecord.getIntegerAttribute("XS") - currentRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreRight++;
							xa = pairedRecord.getStringAttribute("XA");
							if(xa != null && !xa.equals(""))
								xaCountLeft++;
							if((pairedRecord.getIntegerAttribute("XS") >= pairedRecord.getIntegerAttribute("AS")) || (Math.abs(pairedRecord.getIntegerAttribute("XS") - pairedRecord.getIntegerAttribute("AS")) < difference_score))
								subscoreLeft++;
						}
						leftReads.add(isLeft(currentRecord,info) ? currentRecord : pairedRecord);
						rightReads.add(isLeft(currentRecord,info) ? pairedRecord : currentRecord);
						brassTotalLeft++;
						brassTotalRight++;
					}
				}
			}
		}
		it.close();
		inputSam.close();
		return info.setAdditionalStatistics(brassTotalLeft, brassTotalRight, countUnique(leftReads), countUnique(rightReads), calcVariance(leftReads), calcVariance(rightReads), xaCountLeft, xaCountRight, subscoreLeft, subscoreRight, mismatchedSupplementaryAlignment(leftReads, info, search_width), mismatchedSupplementaryAlignment(rightReads, info, search_width), incongruentSupplementaryAlignment(leftReads), incongruentSupplementaryAlignment(rightReads));
	}
	private boolean isLeft(SAMRecord current, svInformation info) {
		return (!current.getReadNegativeStrandFlag()) ? (current.getAlignmentStart() <= info.getStartLeft() && (info.getStartLeft() - current.getAlignmentEnd()) < search_width) : (current.getAlignmentEnd() >= info.getStartLeft() && (current.getAlignmentStart() - info.getStartLeft()) < search_width);
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
	private Boolean incongruentSupplementaryAlignment(List<SAMRecord> reads) {
		IntervalST<SAMRecord> posIntervalTree = new IntervalST<SAMRecord>();
		IntervalST<SAMRecord> negIntervalTree = new IntervalST<SAMRecord>();
		Boolean incongruent = false;
		String sa = null;
		Iterator<Interval1D> intervalIterator = null;
		SAMRecord intersectRead = null;
		Interval1D searchInterval = null;
		for(SAMRecord read : reads) {
			if(!read.getReadNegativeStrandFlag())
				posIntervalTree.put(new Interval1D(read.getAlignmentStart(), read.getAlignmentEnd()), read);
			else
				negIntervalTree.put(new Interval1D(read.getAlignmentStart(), read.getAlignmentEnd()), read);
		}
		for(SAMRecord read : reads) {
			sa = read.getStringAttribute("SA");
			if(sa != null && !sa.equals("") && correctPosition(read)) {
				searchInterval = getClippingCoordinates(read);
				intervalIterator = ((!read.getReadNegativeStrandFlag()) ? posIntervalTree.searchAll(searchInterval) : negIntervalTree.searchAll(searchInterval)).iterator();
				while(intervalIterator.hasNext()) {
					intersectRead = (!read.getReadNegativeStrandFlag()) ? posIntervalTree.get(intervalIterator.next()) : negIntervalTree.get(intervalIterator.next());
					incongruent = incongruent | ((!read.getReadNegativeStrandFlag()) ? (intersectRead.getAlignmentEnd() - searchInterval.low) > diffThreshold : (searchInterval.high - intersectRead.getAlignmentStart()) > 10);
				}
			}
		}
		return incongruent;
	}
	private Interval1D getClippingCoordinates(SAMRecord read) {
		Interval1D coords = null;
		List<CigarElement> elements = read.getCigar().getCigarElements();
		int beginPos = 0;
		int endPos = 0;
		if(!read.getReadNegativeStrandFlag()) {
			for(CigarElement element : elements) {
				switch(element.getOperator()) {
					case M:
					case D:
					case N:
					case S:
					case X:
						beginPos = endPos;
						endPos += element.getLength();
						break;
					default: break;
				}
			}
			coords = new Interval1D(read.getAlignmentStart() + beginPos, read.getAlignmentStart() + endPos);
		} else {
			beginPos = elements.get(0).getLength();
			coords = new Interval1D(read.getAlignmentStart() - beginPos, read.getAlignmentStart());
		}
		return coords;
	}
	private Boolean mismatchedSupplementaryAlignment(List<SAMRecord> reads, svInformation info, int search_width) {
		String sa = null;
		boolean mismatch = false;
		List<Boolean> mismatchOutcomes = null;
		String[] tokens = null;
		for(SAMRecord currentRead : reads) {
			sa = currentRead.getStringAttribute("SA");
			if(sa != null && !sa.equals("") && correctPosition(currentRead)) {
				mismatchOutcomes = new ArrayList<Boolean>(10);
				tokens = sa.split(";");
				for(String supplementaryAlignment : tokens) {
					if(!supplementaryAlignment.equals(""))
						mismatchOutcomes.add(mismatch(currentRead, supplementaryAlignment.split(","), info, search_width));
				}
				mismatch = mismatch | mismatchOutcomes.stream().allMatch(t -> t==true);
			}
		}
		return mismatch;
	}
	private boolean correctPosition(SAMRecord read) {
		List<CigarElement> elements = read.getCigar().getCigarElements();
		return ((read.getReadNegativeStrandFlag()) ? elements.get(0).getOperator().isClipping() : elements.get(elements.size()-1).getOperator().isClipping());
	}
	private Boolean mismatch(SAMRecord read, String[] infoTokens, svInformation info, int search_width) {
		boolean leftMatch = false;
		boolean mismatch = false;
		Integer pos = null;
		try {
			pos = Integer.parseInt(infoTokens[1]);
			if(read.getReferenceName().equals(info.getChromLeft()))
				leftMatch = (!read.getReadNegativeStrandFlag()) ? (read.getAlignmentStart() <= info.getStartLeft() && Math.abs(info.getStartLeft() - read.getAlignmentEnd()) < search_width) : (read.getAlignmentEnd() >= info.getStartLeft() && Math.abs(read.getAlignmentStart() - info.getStartLeft()) < search_width) ;
			if(leftMatch)
				mismatch = (!infoTokens[0].equals(info.getChromRight())) ? true : !(Math.abs(info.getStartRight() - pos) < search_width);
			else
				mismatch = (!infoTokens[0].equals(info.getChromLeft())) ? true : !(Math.abs(info.getStartLeft() - pos) < search_width);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Conversion of supplementary alignment position failed");
		}
		return mismatch;
	}
}
