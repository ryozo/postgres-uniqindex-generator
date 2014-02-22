package net.equj65.indexgenerator.main;

import java.io.File;

import net.equj65.indexgenerator.generator.UniqueIndexGenerator;

// TODO 削除
public class Main {
	
	public static void main(String[] args) throws Exception {
		UniqueIndexGenerator generator = new UniqueIndexGenerator();
		File inputFile = new File("/Users/pray-for-freedom/Documents/eclipse-workspaces/ermaster/er-master-test/tables.sql");
		File outputFile = new File("/Users/pray-for-freedom/Documents/eclipse-workspaces/ermaster/er-master-test/tables_output.sql");
		outputFile.delete();
		generator.generate(inputFile, outputFile, "Shift-JIS");
	}

}
