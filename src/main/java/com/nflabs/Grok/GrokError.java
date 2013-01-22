package com.nflabs.Grok;

/*
 * Class of Grok errors
 */
//TODO: looger for grok
public class GrokError {
	
	public static final int GROK_OK = 0;
	public static final int GROK_ERROR_FILE_NOT_ACCESSIBLE = 1;
	public static final int GROK_ERROR_PATTERN_NOT_FOUND = 2;
	public static final int GROK_ERROR_UNEXPECTED_READ_SIZE = 3;
	public static final int GROK_ERROR_COMPILE_FAILED = 4;
	public static final int GROK_ERROR_UNINITIALIZED = 5;
	public static final int GROK_ERROR_NOMATCH = 6;
}
