package com.sanger.annotateBRASS;

public class svInformation {
	private String line = null;
	private String chromLeft = null;
	private Integer startLeft = null;
	private Integer endLeft = null;
	private String chromRight = null;
	private Integer startRight = null;
	private Integer endRight = null;
	private String[] reads = null;
	private int leftReads = 0;
	private int rightReads = 0;
	private int leftUniqueStart = 0;
	private int rightUniqueStart = 0;
	private double leftVar = 0;
	private double rightVar = 0;
	private int diffChromLeft = 0;
	private int diffChromRight = 0;
	private int xaLeft = 0;
	private int xaRight = 0;
	private int subscoreLeft = 0;
	private int subscoreRight = 0;
	private Double discordantPercentageLeft = 0.0;
	private Double discordantPercentageRight = 0.0;
	private int brassTotalLeft = 0;
	private int brassTotalRight = 0;
	private int brassUniqueLeft = 0;
	private int brassUniqueRight = 0;
	private double brassVarianceLeft = 0.0;
	private double brassVarianceRight = 0.0;
	private int xaBrassLeft = 0;
	private int xaBrassRight = 0;
	private int subscoreBrassLeft = 0;
	private int subscoreBrassRight = 0;
	private Boolean controlPresent = null;
	private Integer supportingPairs = null;
	private Boolean sharedFound = null;
	private int totalShared = 0;
	private int foundShared = 0;
	private Boolean mismatchedSupplementaryAlignmentLeft = null;
	private Boolean mismatchedSupplementaryAlignmentRight = null;
	private Boolean incongruentSupplementaryAlignmentLeft = null;
	private Boolean incongruentSupplementaryAlignmentRight = null;
	
	public svInformation (String line, String chromLeft, Integer startLeft, Integer endLeft, String chromRight, Integer startRight, Integer endRight, String[] reads) {
		this.line = line;
		this.chromLeft = chromLeft;
		this.startLeft = startLeft;
		this.endLeft = endLeft;
		this.chromRight = chromRight;
		this.startRight = startRight;
		this.endRight = endRight;
		this.reads = reads;
	}
	public svInformation setStatistics(int lCount, int rCount, int uniqueLeftCount, int uniqueRightCount, double leftVar, double rightVar, int diffChromLeft, int diffChromRight, int xaLeft, int xaRight, int subscoreLeft, int subscoreRight, Double discordantPercentageLeft, Double discordantPercentageRight) {
		leftReads = lCount;
		rightReads = rCount;
		leftUniqueStart = uniqueLeftCount;
		rightUniqueStart = uniqueRightCount;
		this.leftVar = leftVar;
		this.rightVar = rightVar;
		this.diffChromLeft = diffChromLeft;
		this.diffChromRight = diffChromRight;
		this.xaLeft = xaLeft;
		this.xaRight = xaRight;
		this.subscoreLeft = subscoreLeft;
		this.subscoreRight = subscoreRight;
		this.discordantPercentageLeft = discordantPercentageLeft;
		this.discordantPercentageRight = discordantPercentageRight;
		return this;
	}
	public svInformation setAdditionalStatistics(int brassTotalLeft, int brassTotalRight, int brassUniqueLeft, int brassUniqueRight, double brassVarianceLeft, double brassVarianceRight, int xaBrassLeft, int xaBrassRight, int subscoreBrassLeft, int subscoreBrassRight,Boolean mismatchedSupplementaryAlignmentLeft, Boolean mismatchedSupplementaryAlignmentRight, Boolean incongruentSupplementaryAlignmentLeft, Boolean incongruentSupplementaryAlignmentRight) {
		this.brassTotalLeft = brassTotalLeft;
		this.brassTotalRight = brassTotalRight;
		this.xaBrassLeft = xaBrassLeft;
		this.xaBrassRight = xaBrassRight;
		this.subscoreBrassLeft = subscoreBrassLeft;
		this.subscoreBrassRight = subscoreBrassRight;
		this.brassUniqueLeft = brassUniqueLeft;
		this.brassUniqueRight = brassUniqueRight;
		this.brassVarianceLeft = brassVarianceLeft;
		this.brassVarianceRight = brassVarianceRight;
		this.mismatchedSupplementaryAlignmentLeft = mismatchedSupplementaryAlignmentLeft;
		this.mismatchedSupplementaryAlignmentRight = mismatchedSupplementaryAlignmentRight;
		this.incongruentSupplementaryAlignmentLeft = incongruentSupplementaryAlignmentLeft;
		this.incongruentSupplementaryAlignmentRight = incongruentSupplementaryAlignmentRight;
		return this;
	}
	public svInformation setControlStatistics(Boolean controlPresent, Integer supportingPairs) {
		this.controlPresent = controlPresent;
		this.supportingPairs = supportingPairs;
		return this;
	}
	public svInformation setSharedStatistics(boolean sharedFound) {
		if(this.sharedFound == null)
			this.sharedFound = sharedFound;
		else
			this.sharedFound = this.sharedFound | sharedFound;
		if(sharedFound)
			foundShared++;
		totalShared++;
		return this;
	}
	public String getLine() {
		return line;
	}
	public String getChromLeft() {
		return chromLeft;
	}
	public Integer getStartLeft() {
		return startLeft;
	}
	public Integer getEndLeft() {
		return endLeft;
	}
	public String getChromRight() {
		return chromRight;
	}
	public Integer getStartRight() {
		return startRight;
	}
	public Integer getEndRight() {
		return endRight;
	}
	public String[] getReads() {
		return reads;
	}
	public int getLeftReads() {
		return leftReads;
	}
	public int getRightReads() {
		return rightReads;
	}
	public int getUniqueLeftReads() {
		return leftUniqueStart;
	}
	public int getUniqueRightReads() {
		return rightUniqueStart;
	}
	public Double getVarLeft() {
		return leftVar;
	}
	public Double getVarRight() {
		return rightVar;
	}
	public int getDiffChromLeft() {
		return diffChromLeft;
	}
	public int getDiffChromRight() {
		return diffChromRight;
	}
	public int getXaSaLeft() {
		return xaLeft;
	}
	public int getXaSaRight() {
		return xaRight;
	}
	public int getSubScoreLeft() {
		return subscoreLeft;
	}
	public int getSubScoreRight() {
		return subscoreRight;
	}
	public Double getDiscordantPercentageLeft() {
		return discordantPercentageLeft;
	}
	public Double getDiscordantPercentageRight() {
		return discordantPercentageRight;
	}
	public Integer getBrassTotalLeft() {
		return Integer.valueOf(brassTotalLeft);
	}
	public Integer getBrassTotalRight() {
		return Integer.valueOf(brassTotalRight);
	}
	public Integer getBrassUniqueLeft() {
		return Integer.valueOf(brassUniqueLeft);
	}
	public Integer getBrassUniqueRight() {
		return Integer.valueOf(brassUniqueRight);
	}
	public Double getBrassVarianceLeft() {
		return Double.valueOf(brassVarianceLeft);
	}
	public Double getBrassVarianceRight() {
		return Double.valueOf(brassVarianceRight);
	}
	public Integer getXaBrassLeft() {
		return Integer.valueOf(xaBrassLeft);
	}
	public Integer getXaBrassRight() {
		return Integer.valueOf(xaBrassRight);
	}
	public Integer getSubscoreBrassLeft() {
		return Integer.valueOf(subscoreBrassLeft);
	}
	public Integer getSubscoreBrassRight() {
		return Integer.valueOf(subscoreBrassRight);
	}
	public Boolean getControlPresent() {
		return controlPresent;
	}
	public Integer getSupportingPairs() {
		return supportingPairs;
	}
	public Boolean getSharedPresent() {
		return sharedFound;
	}
	public Integer getTotalShared() {
		return Integer.valueOf(totalShared);
	}
	public Integer getFoundShared() {
		return Integer.valueOf(foundShared);
	}
	public Boolean getMismatchedSupplementaryAlignmentLeft() {
		return mismatchedSupplementaryAlignmentLeft;
	}
	public Boolean getMismatchedSupplementaryAlignmentRight() {
		return mismatchedSupplementaryAlignmentRight;
	}
	public Boolean getIncongruentSupplementaryAlignmentLeft() {
		return incongruentSupplementaryAlignmentLeft;
	}
	public Boolean getIncongruentSupplementaryAlignmentRight() {
		return incongruentSupplementaryAlignmentRight;
	}
}
