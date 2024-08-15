package com.fdk.compiler;

import com.fdk.compiler.parser.FParser;
import com.fdk.compiler.parser.FTokenizer;
import com.fdk.compiler.parser.Scanner;
import com.fdk.compiler.parser.UnicodeReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Paths;

public class App {
	static char[] getChars(String s) {
		BufferedReader reader = null;

		try {
			URL resource = App.class.getResource("../../../parsingtest/Point.scala");
			File f = Paths.get(resource.toURI()).toFile();
			reader = new BufferedReader(new FileReader(f));
			char[] chars = new char[(int) f.length()];
			reader.read(chars);
			return chars;
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) {

		char[] input = getChars("src/main/resources/parsingtest/Point.scala");
		if(input == null) {
			System.out.println("Error reading input file. Exiting...");
			return;
		}
		Scanner scanner = new Scanner(new FTokenizer(new UnicodeReader(input)));
		FParser parser = new FParser(scanner);
		parser.p_compilationUnit();
		System.out.println("Parsing done. Exiting...");
	}
}
