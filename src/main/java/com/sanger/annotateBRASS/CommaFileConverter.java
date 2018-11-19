package com.sanger.annotateBRASS;

import com.beust.jcommander.IStringConverter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommaFileConverter implements IStringConverter<File[]> {
	@Override
	public File[] convert(String value) {
		return Arrays.stream(value.split(",")).map(f -> new File(f)).toArray(File[]::new);
	}
}
