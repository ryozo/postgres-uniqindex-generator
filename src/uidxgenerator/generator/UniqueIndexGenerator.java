package uidxgenerator.generator;

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

import uidxgenerator.domain.EntireSQL;
import uidxgenerator.parser.SQLParser;

/**
 * UniqueIndexのGeneratorです。
 * TODO javadoc
 * @author W.Ryozo
 * @version 1.0
 */
public class UniqueIndexGenerator {
	
	/** 論理削除フラグのデフォルト名 */
	public static final String DEFAULT_DELFLAG_NAME = "is_deleted";
	
	/**
	 * TODO javadoc
	 * TODO Exceptionを投げないようにする。
	 * @param inputSqlFile
	 * @param outputSqlFile
	 * @param fileEncoding
	 * @throws Exception
	 */
	public void generate(File inputSqlFile, File outputSqlFile, String fileEncoding) throws Exception {
		generate(inputSqlFile, outputSqlFile, fileEncoding, DEFAULT_DELFLAG_NAME, Boolean.FALSE);
	}
	
	// TODO javadoc
	// TODO Exceptionの取り扱いを修正
	public void generate(File inputSqlFile,
			File outputSqlFile,
			String fileEncoding,
			String indexConditionField,
			Boolean indexConditionValue) throws Exception {
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

		String sql = readSqlFile(inputSqlFile, fileEncoding);
		SQLParser sqlParser = new SQLParser();
		EntireSQL entireSql = sqlParser.parse(sql);

		Map<String, String> conditionMap = new HashMap<String, String>();
		// TODO 修正
		conditionMap.put(indexConditionField, indexConditionValue.toString());
		entireSql.addConditionToAllUniqueConstraint(conditionMap);
		
		writeSqlFile(outputSqlFile, entireSql.toString(), fileEncoding);
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
			System.out.println(writeString);
			writer.write(writeString);
		}
	}
}
