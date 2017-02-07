package org.dancecoder.smailer;

public class TextConverter {
	
	// GSM 03.38 version 7.2.0
	public static final char[] GSM_7BIT_DEFAULT = new char[] {
		'@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å',
		'Δ', '\u005F', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', '\u001B', 'Æ', 'æ', 'ß', 'É', 
		' ', '!', '\"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', 
		'¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 
		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '\u00A7', 
		'¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 
		'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à' 
	};
	
	// T-REC T.50
	private static final char[] IRA_7BIT = new char[] { 
		'\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\u0008', '\u0009', '\n', '\u000B', '\u000C', '\r', '\u000E', '\u000F',
		'\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D', '\u001E', '\u001F', 
		' ', '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', 
		'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 
		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', 
		'`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 
		'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '\u007F' 
	};
	
	private static final char[] HEX_CHAR_TABLE = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};  
	
	public static String parse(String input, TextEncoding enc) throws IllegalArgumentException  
	{		
		switch(enc) {
			case gsm7BitDefaultHex:					
				return bytesToString(unpack(unhex(input)), GSM_7BIT_DEFAULT);
			case ira7bitHex:
				return bytesToString(unpack(unhex(input)), IRA_7BIT);
			case ucs2Hex:
				return bytesToUnicode(unhex(input));
			default: throw new IllegalArgumentException();
		}	
	}
	
	public static String convert(String input, TextEncoding enc) throws IllegalArgumentException  
	{
		switch(enc) {
			case gsm7BitDefaultHex:
				return hex(pack(stringToBytes(input, GSM_7BIT_DEFAULT)));
			case ira7bitHex:
				return hex(pack(stringToBytes(input, IRA_7BIT)));
			default: throw new IllegalArgumentException();
		}
	}
	
	public static String bytesToUnicode(byte[] input) {
    return bytesToUnicode(input, 0, input.length);
	}

  public static String bytesToUnicode(byte[] input, int from, int length) {
		char[] output = new char[length / 2];
		int j;
		for (int i = 0; i < output.length; i++) {
			j = i << 1;
			output[i] = (char)(input[from + j] << 8 | input[from + j + 1]);
		}
		return new String(output);
	}
	
	private static char[] stringToBytes(String input, char[] table) {
		int length = input.length();
		char[] result = new char[length];
		for (int i = 0; i < length; i++) {
			char c = input.charAt(i);
			for (int j = 0; j < table.length; j++) {
				if (table[j] == c) {
					result[i] = (char)j;
					break;
				}
			}
		}
		return result;
	}
	
	private static String bytesToString(int[] input, char[] table) {
		StringBuilder sb = new StringBuilder(input.length);
		for (int i = 0; i < input.length; i++) {
			sb.append(table[input[i] & 0xFF]);
		}
		return sb.toString();
	}
	
	public static String hex(char[] input) {
		char[] output = new char[input.length * 2];
		char c;
		int ii;
		for (int i = 0; i < input.length; i++) {
			c = input[i];
			ii = i << 1;
			output[ii] = HEX_CHAR_TABLE[(byte)c >> 4 & 15];
      output[ii + 1] = HEX_CHAR_TABLE[(byte)c & 15];			
		}
		return new String(output);
	}
	
	public static byte[] unhex(String input) {
		byte[] output = new byte[input.length() >> 1];
		int j;
		for (int i = 0; i < output.length; i++) {
			j = i << 1;
			output[i] = (byte)(unhexChar(input.charAt(j)) << 4 | unhexChar(input.charAt(j + 1)));
		}
		return output;
	}
	
	private static byte unhexChar(char c) {
		if (c > '/' && c < ':') {
			return (byte)(c - '0');
    } else if (c > '`' && c < 'g') {
			return (byte)(c - 'a' + 10);
		} else  if (c > '@' && c < 'G') {
			return (byte)(c - 'A' + 10);
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Octets to septets unpacking by GSM algorithm
	 */
  public static int[] unpack(byte[] input) {
    return unpack(input, 0, input.length);
  }
	public static int[] unpack(byte[] input, int first, int length) {
		int[] output = new int[length + length / 7];
		byte prev = 0;		
		for (int i = 0; i < length; i++) {
			output[i + i/7] = ((input[i + first] << i%7 | (prev >> 8-i%7) & ~(-1 << i%7) ) & 127);
			prev = input[i + first];
		}
		for (int i = 6; i < length; i+=7) {
			output[i + 1 + i/7] = ((input[i+first] >> 1) & 127);
		}
		return output;
	}
	
	/**
	 * Septets to octets packing by GSM algorithm
	 */
	private static char[] pack(char[] input) {
		char[] output = new char[input.length - input.length / 8];
		int i;
		for (i = 0; i < input.length-1; i++) {
			output[i-i/8] = (char)(input[i] >> i%8 | input[i+1] << 7 - i%8);
		}				
		output[i-i/8] = (char)(input[i] >> i%8);
		return output;
	}
	
}
