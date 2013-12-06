package uidxgenerator.main;

import uidxgenerator.generator.UniqueIndexGenerator;

// TODO çÌèú
public class Main {
	
	public static void main(String[] args) throws Exception {
		UniqueIndexGenerator generator = new UniqueIndexGenerator();
		String sql = generator.generate("/Users/pray-for-freedom/Documents/eclipse-workspaces/ermaster/er-master-test/tables.sql", "Shift-JIS", "testBool", Boolean.FALSE);
		System.out.println(sql);
	}

}
