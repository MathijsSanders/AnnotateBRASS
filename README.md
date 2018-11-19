# Annotate BRASS

This repository contains the code for further annotation of BRASS output for filtering purposes. 

## How do I run it?

BRASS has been run on your sample of interest and matched control for determining somatic structural variants (SV). The output results are stored in a compressed BED file (gzip). This file in combination with the original BAM files for sample of interest and matched controls, the BAM files produced by BRASS (containing read-pairs supporting the SVs) and the BAM files for potential additional control samples can be provided to this package for further annotation.  

### The recommended way

The pre-compiled JAR file is included with the repository, but in case the package needs to be recompiled, please run:

```bash
mvn package clean
```

The following command adds additional statistics to the BRASS output for filtering purposes:

```bash
java -Xms5G -Xmx10G -jar AnnotateBRASS.jar --input-bam-file input_bam_file --input-brass-bam-file input_brass_bam_file --brass-bed-file gzip_bed_file --output-bed-file output_bed_file --master-control-bam brass_control_bam --shared-individual-controls brass_bam_controls --width-extract width --difference-alignment-scores difference_scores --discordant-distance distance --discordant-distance-search width_search --threads number_of_threads --help --version
```

- --input-bam-file*: BAM file of sample of interest produced by BWA-mem.
- --input-brass-bam-file*: BAM file produced by BRASS for the sample of interest.
- --brass-bed-file*: Compressed BED file describing all detected SVs.
- --output-bed-file*: Output BED file to store fully annotated SVs.
- --master-control-bam: BAM file of the matched control produced by BWA-mem.
- --shared-individual-controls: Comma-separated BAM file locations of additional controls. Optimal when BAM files produced by BRASS are used.
- --width-extract: Window size in which read-pairs supporting the SV are being sought (breakpoint +- width-extract). (Default 250bp).
- --difference-alignment-scores: Threshold for which current and alternative alignment scores are considered similar. (Default 20).
- --discordant-distance: Inferred insert size threshold to determine discordant read-pairs. (Default 1000bp).
- --discordant-distance-search: Window size around breakpoint to search for discordant read-pairs. (Default 5000bp).
- --threads: Number of threads for annotating the SVs. (Default 1).
- --help: Help information.
- --version: Current version number.
- \* Required.

*Dependencies*
- Maven version 3+ (For compiling only).
- Java JDK 1.8+
