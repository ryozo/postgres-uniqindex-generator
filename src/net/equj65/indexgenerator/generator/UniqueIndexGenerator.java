package net.equj65.indexgenerator.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import net.equj65.indexgenerator.constants.DBMS;
import net.equj65.indexgenerator.domain.EntireSQL;
import net.equj65.indexgenerator.parser.SQLParser;

/**
 * UniqueIndexのGeneratorです。
 * TODO javadoc
 * @author W.Ryozo
 * @version 1.0
 */
public class UniqueIndexGenerator {
	
	/** 論理削除フラグのデフォルト名 */
	public static final String DEFAULT_DELFLAG_NAME = "is_deleted";
	
	/** デフォルトDBMS */
	public static final DBMS DEFAULT_DBMS = DBMS.POSTGRESQL;

	public void generate(File inputSqlFile, File outputSqlFile, String fileEncoding) {
		generate(inputSqlFile, outputSqlFile, fileEncoding, DEFAULT_DBMS, DEFAULT_DELFLAG_NAME, Boolean.FALSE);
	}
	
	public void generate(File inputSqlFile, File outputSqlFile, String fileEncoding, DBMS targetDBMS) {
		generate(inputSqlFile, outputSqlFile, fileEncoding, targetDBMS, DEFAULT_DELFLAG_NAME, Boolean.FALSE);
	}
	
	public void generate(File inputSqlFile, File outputSqlFile, String fileEncoding, String conditionField, Object conditionValue) {
		generate(inputSqlFile, outputSqlFile, fileEncoding, DEFAULT_DBMS, conditionField, conditionValue);
	}
	
	public void generate(File inputSqlFile, File outputSqlFile, String fileEncoding, Map<String, Object> conditionMap) {
		generate(inputSqlFile, outputSqlFile, fileEncoding, DEFAULT_DBMS, conditionMap);
	}
	
	public void generate(File inputSqlFile, File outputSqlFile, String fileEncoding, DBMS targetDBMS, String conditionField, Object conditionValue) {
		Map<String, Object> conditionMap = new HashMap<>();
		conditionMap.put(conditionField, conditionValue);
		generate(inputSqlFile, outputSqlFile, fileEncoding, targetDBMS, conditionMap);
	}
	
	// TODO javadoc
	// TODO Exceptionの取り扱いを修正
	public void generate(File inputSqlFile,
			File outputSqlFile,
			String fileEncoding,
			DBMS targetDBMS,
			Map<String, Object> conditionMap) {
		// arguments validate
		if (inputSqlFile == null) {
			throw new IllegalArgumentException("読み込み対象のSQLファイルが指定されていません。");
		}
		if (!inputSqlFile.isFile()) {
			throw new IllegalArgumentException("読み込み対象SQLファイルが存在しないかファイルではありません");
		}
		if (outputSqlFile == null) {
			throw new IllegalArgumentException("出力用のSQLファイルが指定されていません");
		}
		if (outputSqlFile.exists()) {
			throw new IllegalArgumentException("出力用のSQLファイルが既に存在します。存在しないファイル名を指定してください");
		}

		try {
		String sql = readSqlFile(inputSqlFile, fileEncoding);
		SQLParser sqlParser = new SQLParser();
		EntireSQL entireSql = sqlParser.parse(sql);
		// TODO ここでDBMSを渡さないようにする。
		entireSql.addConditionToAllUniqueConstraint(targetDBMS, conditionMap);
		
		writeSqlFile(outputSqlFile, entireSql.toString(), fileEncoding);
		} catch (Exception e) {
			// TODO 例外処理の実装
			e.printStackTrace();
		}
	}
	
	/**
	 * SQLファイル全体を読み込みます。
	 * @param sqlFile 読み込み対象のSQLファイル
	 * @param encoding SQLファイルの文字コード
	 * @return 読み込んだSQLファイル（改行コード含む）
	 */
	private String readSqlFile(File sqlFile, String encoding) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(sqlFile), encoding))){
			int c;
			while ((c = reader.read()) != -1) {
				// TODO サロゲート文字が含まれていた場合問題無いのか？
				builder.append((char) c);
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * ファイルを書き込みます。
	 * @param outputFile 出力先ファイルを表すFileオブジェクト
	 * @param writeString 出力対象の文字列
	 * @param encoding 出力先ファイルのエンコーディング
	 * @throws IOException
	 */
	private void writeSqlFile(File outputFile, String writeString, String encoding) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encoding))) {
			// TODO　削除
			System.out.println(writeString);
			writer.write(writeString);
		}
	}
}
