package com.sanger.annotateBRASS;

import com.beust.jcommander.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommaFileValidator implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		List<File> files = Arrays.stream(value.split(",")).map(f -> new File(f)).collect(Collectors.toList());
		for(File f : files) {
			if(!f.exists() || f.isDirectory())
				throw new ParameterException("Parameter " + name + " does not exist or is a directory. Found " + value);
		}
	}
}