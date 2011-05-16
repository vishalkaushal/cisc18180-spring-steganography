package com.example.Steganography;

public class TwoReturn {

	public Object first;
	public Object second;
	
	public TwoReturn(Object a, Object b){
		first = a;
		second = b;
	}
	
	public static String clipEnd(String input){
		String output = "";
		for(int i = input.length() - 1; i > 0; i -= 1){
			if (input.charAt(i) == '/'){
				output = input.substring(0, i + 1);
			}
		}
		return (output + "encoded");
	}
	
}
