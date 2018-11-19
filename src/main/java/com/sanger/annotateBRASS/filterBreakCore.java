package com.sanger.annotateBRASS;

import java.io.*;
import java.util.*;
import java.util.zip.*;


public class filterBreakCore {
	private List<svInformation> svList = null;
	public filterBreakCore(File bam_file, File brass_bam_file, File brass_bed_file, String output_bed_file, int search_width, int threads, int difference_score, int discordant_distance, int discordant_distance_search, File master_control_bam, File[] shared_individual_controls) {
		retrieveResults structuralVariants = retrieveStructuralVariants(brass_bed_file);
		try {
			svList = (new filterMaster(bam_file, structuralVariants.getStructuralVariants(), search_width, threads, difference_score, discordant_distance, discordant_distance_search)).startAnalysis();
			svList = (new filterMasterBrass(brass_bam_file, svList, search_width, threads, difference_score)).startAnalysis();
			if(master_control_bam != null)
				svList = (new filterMasterControl(master_control_bam, svList, search_width, threads)).startAnalysis();
			if(shared_individual_controls != null)
				svList = (new filterMasterShared(shared_individual_controls, svList, search_width, threads)).startAnalysis();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Failed to retrieve structural variant information from the BAM file.");
			System.exit(-5);
		}
		Collections.sort(svList, Comparator.comparing(svInformation::getChromLeft).thenComparing(svInformation::getStartLeft));
		writeResults(structuralVariants.getHeader(), svList, output_bed_file, (master_control_bam != null), (shared_individual_controls != null));
	}
	private retrieveResults retrieveStructuralVariants(File brass_bed_file) {
		List<String> header = new ArrayList<String>(1000);
		List<svInformation> svList = new ArrayList<svInformation>(1000);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(brass_bed_file))));
			String line = null;
			String[] tokens = null;
			while((line = br.readLine()) != null) {
				if(line.startsWith("#")) {
					header.add(line);
					continue;
				}
				tokens = line.split("\t");
				svList.add(new svInformation(line,tokens[0], Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), tokens[3], Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]), tokens[14].split(",")));
			}
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Error reading BRASS BED file.");
			System.exit(-1);
		}
		return new retrieveResults(String.join("\n", header), svList);
	}
	private void writeResults(String header, List<svInformation> svList, String output_bed_file, boolean control, boolean shared) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(output_bed_file));
			StringBuffer buffer = new StringBuffer();
			int counter = 0;
			output.write(String.join("\t", header, "Total left reads", "Total right reads", "Unique left reads","Unique right reads","Variance start left reads","Variance start right reads", "Different chrom left", "Different chrom right","XA count left", "XA count right","Subscore left","Subscore right", "Percentage discordant distance left", "Percentage discordant distance right", "BRASS total left", "BRASS total right","BRASS Unique Left","BRASS Unique Right","BRASS Variance Left","BRASS Variance Right","BRASS XA count left","BRASS XA count right","BRASS subscore left","BRASS subscore right", ((control) ? "Is present in master control\tSupporting pairs in master control" : null), ((shared) ? "Is present in shared samples\tTotal shared samples\tFound in shared samples" : null), "Mismatched Supplementary Alignment BRASS left", "Mismatched Supplementary Alignment BRASS right", "Incongruent Supplementary Alignment BRASS left", "Incongruent Supplementary Alignment BRASS right"));
			output.flush();
			for(svInformation info : svList) {
				buffer.append("\n" + String.join("\t", info.getLine(), Integer.toString(info.getLeftReads()), Integer.toString(info.getRightReads()), Integer.toString(info.getUniqueLeftReads()), Integer.toString(info.getUniqueRightReads()), info.getVarLeft().toString(), info.getVarRight().toString(), Integer.toString(info.getDiffChromLeft()), Integer.toString(info.getDiffChromRight()), Integer.toString(info.getXaSaLeft()), Integer.toString(info.getXaSaRight()), Integer.toString(info.getSubScoreLeft()), Integer.toString(info.getSubScoreRight()), info.getDiscordantPercentageLeft().toString(), info.getDiscordantPercentageRight().toString(), info.getBrassTotalLeft().toString(), info.getBrassTotalRight().toString(), info.getBrassUniqueLeft().toString(), info.getBrassUniqueRight().toString(), info.getBrassVarianceLeft().toString(), info.getBrassVarianceRight().toString(), info.getXaBrassLeft().toString(), info.getXaBrassRight().toString(), info.getSubscoreBrassLeft().toString(), info.getSubscoreBrassRight().toString(), ((control) ? String.join("\t", info.getControlPresent().toString(),info.getSupportingPairs().toString()) : null), ((shared) ? String.join("\t", info.getSharedPresent().toString(), info.getTotalShared().toString(), info.getFoundShared().toString()) : null), info.getMismatchedSupplementaryAlignmentLeft().toString(), info.getMismatchedSupplementaryAlignmentRight().toString(), info.getIncongruentSupplementaryAlignmentLeft().toString(), info.getIncongruentSupplementaryAlignmentRight().toString()));
				counter++;
				if(counter > 100) {
					output.write(buffer.toString());
					output.flush();
					counter = 0;
					buffer = new StringBuffer();
				}
			}
			if(counter > 0) {
				output.write(buffer.toString());
				output.flush();
			}
			output.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Could not write the results to the output BED file.");
			System.exit(-6);
		}
	}
}
class retrieveResults {
	private String header = null;
	private List<svInformation> svList = new ArrayList<svInformation>(1000);
	public retrieveResults(String header, List<svInformation> svList) {
		this.header = header;
		this.svList = svList;
	}
	public String getHeader() {
		return header;
	}
	public List<svInformation> getStructuralVariants() {
		return svList;
	}
}