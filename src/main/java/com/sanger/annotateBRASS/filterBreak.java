package com.sanger.annotateBRASS;

import java.io.*;
import java.util.*;
import com.beust.jcommander.*;
import com.beust.jcommander.validators.PositiveInteger;

public class filterBreak {
	private static String versionNumber = "0.1";
	@Parameter
	private List<String> parameters = new ArrayList<String>();
	
	@Parameter(names = "--input-bam-file", description = "Input BAM file.", required = true, converter = FileConverter.class, validateWith = FileValidator.class, order=0)
	public File input_bam_file = null;
	
	@Parameter(names = "--input-brass-bam-file", description = "Input BRASS BAM file.", required = true, converter = FileConverter.class, validateWith = FileValidator.class, order=1)
	public File input_brass_bam_file = null;
	
	@Parameter(names = "--brass-bed-file", description = "BRASS BED output file to filter.", required = true, converter = FileConverter.class, validateWith=FileValidator.class, order=2)
	public File brass_bed_file = null;
	
	@Parameter(names = "--output-bed-file", description = "Output BED file to store results.", required = true, order=3)
	public String output_bed_file = null;
	
	@Parameter(names = "--master-control-bam", description = "Master control BAM file used for BRASS filtering", converter = FileConverter.class, validateWith = FileValidator.class, order=4)
	public File master_control_bam = null;
	
	@Parameter(names = "--shared-individual-controls", description = "BRASS BAM file of samples isolated from the same individual (excluding sample-of-interest and master control.", converter = CommaFileConverter.class, validateWith = CommaFileValidator.class, order=5)
	public File[] shared_individual_controls = null;
	
	@Parameter(names = "--width-extract", description = "Window to extract reads (mutation_position +- width).", validateWith = PositiveInteger.class, order=6)
	public Integer extract_width = 250;
	
	@Parameter(names = "--difference-alignment-scores", description = "Threshold of difference between current and alternative scores.", validateWith = PositiveInteger.class, order=7)
	public Integer difference_score = 20;
	
	@Parameter(names = "--discordant-distance", description = "Distance threshold for discordant read-pair.", validateWith = PositiveInteger.class, order=8)
	public Integer discordant_distance = 1000;
	
	@Parameter(names = "--discordant-distance-search", description = "Window to search for read-pairs with discordant distances.", validateWith = PositiveInteger.class, order=9)
	public Integer discordant_distance_search = 5000;
	
	@Parameter(names = "--threads", description = "Number of threads.", validateWith = PositiveInteger.class, order=10)
	public Integer threads = 1;
	
	@Parameter(names = {"--help","-help"}, help = true, description = "Get usage information", order=11)
	private boolean help;
	
	@Parameter(names = {"--version","-version"}, description = "Get current version", order=12)
	private Boolean version = null;
	public static void main(String[] args) {
		filterBreak fb  = new filterBreak();
		JCommander jCommander = new JCommander(fb);
		jCommander.setProgramName("filterBreak.jar");
		JCommander.newBuilder().addObject(fb).build().parse(args);
		if(fb.version != null && fb.version) {
			System.out.println("Filter BRASS output for LCM experiments: " + versionNumber);
			System.exit(0);
		}
		else if(fb.help) {
			jCommander.usage();
			System.exit(0);
		} else {
			int nThreads = Runtime.getRuntime().availableProcessors();
			fb.shared_individual_controls = (fb.shared_individual_controls != null) ? exclude(fb.shared_individual_controls, fb.input_bam_file, fb.input_brass_bam_file, fb.master_control_bam) : null ;
			if(fb.threads > nThreads)
				System.out.println("Warning: Number of threads exceeds number of available cores");
			filterBreakCore fbc = new filterBreakCore(fb.input_bam_file,fb.input_brass_bam_file, fb.brass_bed_file,fb.output_bed_file,fb.extract_width,fb.threads, fb.difference_score, fb.discordant_distance, fb.discordant_distance_search, fb.master_control_bam, fb.shared_individual_controls);
		}
	}
	private static File[] exclude(File[] shared, File input_bam_file, File input_bam_brass_file, File master) {
		if(master == null)
			return shared;
		List<File> sharedExclude = new ArrayList<File>(100);
		for(File share : shared) {
			if(!share.equals(master) && !share.equals(input_bam_file) && !share.equals(input_bam_brass_file))
				sharedExclude.add(share);
		}
		return sharedExclude.stream().toArray(File[]::new);
	}
}
