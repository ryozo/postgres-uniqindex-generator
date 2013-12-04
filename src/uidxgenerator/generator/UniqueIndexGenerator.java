package uidxgenerator.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import uidxgenerator.domain.CreateTableSqlCommand;
import uidxgenerator.domain.EntireSQL;
import uidxgenerator.domain.SqlCommand;
import uidxgenerator.parser.SQLParser;

/**
 * UniqueIndexのGeneratorです。
 * @author W.Ryozo
 * @version 1.0
 */
public class UniqueIndexGenerator {
	
	// TODO javadoc
	public String generate(String sqlFilePath, 
			String fileEncoding, 
			String indexConditionField, 
			Boolean indexConditionValue) throws Exception {
		if (sqlFilePath == null) {
			throw new IllegalArgumentException("対象SQLファイルのパスが指定されていませんです");
		}
		File file = new File(sqlFilePath);
		if (!file.isFile()) {
			throw new IllegalArgumentException("対象SQLファイルが存在しません");
		}

		// TODO 引数のValidate
		String sql = readSqlFile(file, fileEncoding);		
		SQLParser sqlParser = new SQLParser();
		EntireSQL entireSql = sqlParser.parse(sql);
		
		List<SqlCommand> sqlCommandList = entireSql.getSqlCommandList();
		for (SqlCommand command : sqlCommandList) {
			if (command instanceof CreateTableSqlCommand) {
				
			}
		}

		return null;
	}
	
	/**
	 * TODO 詳細に記述。
	 * @param sqlPath UniqueIndex付与対象とするSQLファイルパス
	 * @param fileEncoding SQLFileのエンコーディング
	 * @param indexConditionFields UniqueIndexの条件
	 * @return 作成したSQLファイルのパス
	 */
	// TODO throws Exceptionを修正
	public String generate(String sqlFilePath, String fileEncoding, String... indexConditionFields) throws Exception {
		// Validate
		if (sqlFilePath == null) {
			throw new IllegalArgumentException("対象SQLファイルのパスが指定されていませんです");
		}
		File file = new File(sqlFilePath);
		if (!file.isFile()) {
			throw new IllegalArgumentException("対象SQLファイルが存在しません");
		}
		
		if (indexConditionFields == null 
				|| indexConditionFields.length == 0) {
			// TODO ファイル変更不要
		}
		
		String sql = readSqlFile(file, fileEncoding);
		SQLParser sqlParser = new SQLParser();
		EntireSQL entireSql = sqlParser.parse(sql);
		
		
		return null;
	}
	
	/**
	 * SQLファイルを読み込みます。
	 * @param sqlFile 読み込み対象のSQLファイル
	 * @param encoding SQLファイルの文字コード
	 * @return 読み込んだSQLファイル（改行コード含む）
	 */
	private String readSqlFile(File sqlFile, String encoding) throws IOException {
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(sqlFile), encoding));
			builder = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}
			
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}
		
		return builder.toString();
	}
}
