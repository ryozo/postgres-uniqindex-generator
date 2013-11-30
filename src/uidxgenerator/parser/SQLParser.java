package uidxgenerator.parser;

import java.util.ArrayList;
import java.util.List;

import uidxgenerator.entity.EntireSQL;
import uidxgenerator.entity.SqlCommand;
import uidxgenerator.util.StringUtil;

/**
 * SQL文のParserです。<br />
 * @author W.Ryozo
 * @version 1.0
 */
public class SQLParser {
	
	/** SQL文の区切り文字　*/
	private static final String SQL_COMMAND_DELIMITER = ";";
	
	/**
	 * SQL文を読み込み、{@link EntireSQL}オブジェクトに変換します。
	 * @param targetSqlCommands SQL文
	 * @return 引数のSQLを解析したEntireSQL
	 */
	public EntireSQL parse(String targetSqlCommands) {
		EntireSQL entireSQL = new EntireSQL();
		if (StringUtil.isNullOrEmpty(targetSqlCommands)) {
			return entireSQL;
		}
		
		List<String> sqlCommandList = splitSqlCommands(targetSqlCommands);
		for (String sql : sqlCommandList) {
			entireSQL.addSqlCommand(new SqlCommand(sql));
		}
		
		return entireSQL;

	}
	
	/**
	 * 引数に受け取ったSQLコマンドをSQL区切り文字で分割し、List形式で返却します。<br />
	 * 個々のSQL分は末尾にSQL区切り文字を含んだ状態で分割されます。<br />
	 * 当メソッドはSQL区切り文字がSQL文のコメント文内で利用されている場合、正常に動作しません。
	 * @param targetSqlCommands 変換対象のSQL文
	 * @return 分割後のSQL文
	 */
	private List<String> splitSqlCommands(String targetSqlCommands) {
		// TODO コメント対応
		List<String> sqlCommandList = new ArrayList<String>();
		String[] splitSqlCommands = targetSqlCommands.split(SQL_COMMAND_DELIMITER);
		for (String command : splitSqlCommands) {
			sqlCommandList.add(command.concat(SQL_COMMAND_DELIMITER));
		}
		
		return sqlCommandList;
		
//		int ch =0;
//		while (ch >= targetSqlCommands.length()) {
//			int index = targetSqlCommands.indexOf(SQL_COMMAND_DELIMITER, ch) + 1;
//			sqlCommandList.add(targetSqlCommands.substring(ch, index));
//			ch += index - ch;
//		}
//		return sqlCommandList;
	}
}
