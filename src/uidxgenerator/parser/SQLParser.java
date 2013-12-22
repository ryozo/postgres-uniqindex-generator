package uidxgenerator.parser;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static uidxgenerator.constants.SqlConstants.CREATE_TABLE_PREFIX;
import static uidxgenerator.constants.SqlConstants.SQL_DELIMITER;
import static uidxgenerator.constants.SqlConstants.MULTILINE_COMMENT_PREFIX;
import static uidxgenerator.constants.SqlConstants.MULTILINE_COMMENT_SUFFIX;
import static uidxgenerator.constants.SqlConstants.SINGLELINE_COMMENT_PREFIX;
import static uidxgenerator.constants.SqlConstants.UNIQUE;
import static uidxgenerator.constants.SqlConstants.LINE_SEPARATOR;
import uidxgenerator.domain.CreateTableSqlCommand;
import uidxgenerator.domain.EntireSQL;
import uidxgenerator.domain.SqlCommand;
import uidxgenerator.util.StringUtil;

/**
 * SQL文のParserです。<br />
 * 
 * @author W.Ryozo
 * @version 1.0
 */
public class SQLParser {

	/**
	 * SQL文を読み込み、{@link EntireSQL}オブジェクトに変換します。
	 * 
	 * @param targetSqlCommands
	 *            SQL文
	 * @return 引数のSQLを解析したEntireSQL
	 */
	public EntireSQL parse(String targetSqlCommands) {
		EntireSQL entireSQL = new EntireSQL();
		if (StringUtil.isNullOrEmpty(targetSqlCommands)) {
			return entireSQL;
		}

		List<String> sqlCommandList = splitSqlCommands(targetSqlCommands);
		for (String sql : sqlCommandList) {
			entireSQL.addSqlCommand(buildSqlCommand(sql));
		}
		return entireSQL;

	}

	/**
	 * 引数に受け取ったSQLを元にSqlCommandを作成します。
	 * 
	 * @param sql
	 *            対象のSQL
	 * @return 作成したSQLCommand
	 */
	private SqlCommand buildSqlCommand(String sql) {
		// TODO 当ロジック内でのSqlStateManager利用を検討
		if (StringUtil.isNullOrEmpty(sql)) {
			throw new IllegalArgumentException("SQL is null or empty");
		}

		SqlCommand sqlCommand = null;

		BufferedReader reader = null;
		StringBuilder noCommentSqlBuilder = new StringBuilder();

		try {
			// 1SQLを1行ずつ分析し、コメントなしのSQL文を構成する。
			reader = new BufferedReader(new StringReader(sql));

			// 現在参照する行がコメント中であるか否かを表すフラグ
			boolean innerCommentLineFLg = false;
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (innerCommentLineFLg) {
					// コメント中である場合、該当行内のコメント終了タグを探す。
					if (line.indexOf(MULTILINE_COMMENT_SUFFIX) != -1) {
						// コメント部分を読み飛ばし、以降の文字列をAppend
						// TODO 定数参照
						noCommentSqlBuilder.append(line.substring(line
								.indexOf("*/") + 2));
						innerCommentLineFLg = false;
					}
					continue;
				}
				if (line.indexOf(SINGLELINE_COMMENT_PREFIX) != -1) {
					// コメント開始するまでの文字列をBuilderにコピー
					noCommentSqlBuilder.append(line.substring(0,
							line.indexOf("--")));
					continue;
				}
				if (line.indexOf(MULTILINE_COMMENT_PREFIX) != -1) {
					// コメント開始するまでの文字列をBuilderにコピー
					noCommentSqlBuilder.append(line.substring(0,
							line.indexOf(MULTILINE_COMMENT_PREFIX)));
					String commentedString = line.substring(line
							.indexOf(MULTILINE_COMMENT_PREFIX) + 2);
					if (commentedString.indexOf(MULTILINE_COMMENT_SUFFIX) != -1) {
						// 同行中でSQLコメントが完了している
						noCommentSqlBuilder
								.append(commentedString.substring(commentedString
										.indexOf(MULTILINE_COMMENT_SUFFIX) + 2));
					} else {
						innerCommentLineFLg = true;
					}
					continue;
				}

				noCommentSqlBuilder.append(line);
				noCommentSqlBuilder.append(" ");
			}

		} catch (IOException ioe) {
			// 何もしない。StringBuilderだからIOException発生しない。
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					// 捨てる StringReaderだから。
				}
			}
		}

		String noCommentSql = noCommentSqlBuilder.toString().trim();
		List<Set<String>> uniqueKeyList = new ArrayList<Set<String>>();

		if (noCommentSql.toUpperCase().startsWith(CREATE_TABLE_PREFIX)) {
			// Table名を取得する(CreateTableの開始から次のスペースまでがテーブル名である。
			int fromIndex = noCommentSql.indexOf(CREATE_TABLE_PREFIX)
					+ CREATE_TABLE_PREFIX.length();
			int toIndex = noCommentSql.indexOf(" ",
					CREATE_TABLE_PREFIX.length());
			String tableName = noCommentSql.substring(fromIndex, toIndex);

			String fieldDefinition = noCommentSql.substring(noCommentSql
					.indexOf("(") + 1);
			// Field毎に分離する。ただし、この時点では末尾にField以外の情報を含んでいる状態。
			String[] fields = fieldDefinition.split(",");
			for (int i = 0; i < fields.length; i++) {
				// TODO フィールドの合間にスペースが複数個続いた場合の動作を検証
				String[] fieldItems = fields[i].trim().split(" ");
				// 単項目UNIQEのチェック
				// 2項目目以降にUNIQUEキーワードがあるかチェック
				// 1キーワード目はチェックしないからforのStartは添え字1要素目から。
				for (int j = 1; j < fieldItems.length; j++) {
					if (UNIQUE.equalsIgnoreCase(fieldItems[j].trim())) {
						// 単項目UNIQUE発見
						// UNIQUEキー情報を（1項目目）を保存
						Set<String> columnSet = new HashSet<String>();
						columnSet.add(fieldItems[0]);
						uniqueKeyList.add(columnSet);
					}
				}

				// 複合項目UNIQUEのチェック
				// 1キーワード目がUNIQUEであり、かつ2単語目の接頭辞が括弧の開始である場合、複合UNIQUEである
				if (UNIQUE.equalsIgnoreCase(fieldItems[0])
						&& fieldItems[1].startsWith("(")) {
					// 複合UNIQUE定義発見 括弧の開始から終わりまで取得し、フィールド一覧を取得
					// 次の閉じ括弧を発見するまで後続のFieldを連結(split(",")実施のため、UNIQUEキーフィールドが分割されている。
					StringBuilder uniqueFieldsBuilder = new StringBuilder();
					uniqueFieldsBuilder.append(fieldItems[1]);
					if (fieldItems[1].indexOf(")") == -1) {
						// 該当フィールド上でUNIQUEキー定義が完了していない場合
						// 終了括弧が現れるまで、以降のfieldを読み込む。
						int j = i + 1;
						for (; j < fields.length; j++) {
							if (fields[j].indexOf(")") != -1) {
								// 閉じ括弧が見つかったから該当Fieldで複合UNIQUE定義が終わり。
								uniqueFieldsBuilder.append(",").append(
										fields[j].substring(0,
												fields[j].indexOf(")") + 1));
								break;
							}
							uniqueFieldsBuilder.append(",").append(fields[j]);
						}
						// 複合UNIQUEの定義部は以降読み飛ばす。
						i = j;
					}

					String uniqueFieldDeclare = uniqueFieldsBuilder.toString();
					uniqueFieldDeclare = uniqueFieldDeclare.substring(
							uniqueFieldDeclare.indexOf("(") + 1,
							uniqueFieldDeclare.indexOf(")"));
					String[] uniqueFields = uniqueFieldDeclare.split(",");
					Set<String> columnSet = new LinkedHashSet<String>();
					for (String uniqueField : uniqueFields) {
						columnSet.add(uniqueField.trim());
					}
					uniqueKeyList.add(columnSet);
				}
			}

			// SqlCommandを作成
			sqlCommand = new CreateTableSqlCommand(sql, tableName,
					uniqueKeyList);
		} else {
			sqlCommand = new SqlCommand(sql);
		}

		return sqlCommand;
	}

	/**
	 * 引数に受け取ったSQLコマンドをSQL区切り文字で分割し、List形式で返却します。<br />
	 * 個々のSQL分は末尾にSQL区切り文字を含んだ状態で分割されます。<br />
	 * SQL文を分離する際はSQLのDelimiterを利用しますが、以下のブロックに位置するDelimiterは認識されません。
	 * 
	 * <pre>
	 * 1．コメント文中のDelimiter（--ブロック内、もしくは\/* *\/ブロック内
	 * 2．シングルクォート、またはダブルクォート内のDelimiter（例えばInsert時の初期値として設定されたDelimiter
	 * </pre>
	 * 
	 * @param targetSqlCommands
	 *            変換対象のSQL文
	 * @return 分割後のSQL文
	 */
	private List<String> splitSqlCommands(String targetSqlCommands) {
		// TODO SQL本文の取得までSQLStateManagerに持たせる⇒SQLStateManagerがParserとなる。
		List<String> sqlCommandList = new ArrayList<>();
		SQLStateManager manager = new SQLStateManager();
		try (BufferedReader reader = new BufferedReader(new StringReader(
				targetSqlCommands))) {
			String currentLine;
			StringBuilder sqlBuilder = new StringBuilder();
			while ((currentLine = reader.readLine()) != null) {
				int delimiterIndex = currentLine.indexOf(SQL_DELIMITER);
				if (delimiterIndex >= 0) {
					String[] sqlBlocks = currentLine.split(SQL_DELIMITER);
					// TODO　改行コードとかパーサの考え方とかSQLの作り方とか全体的にリファクタリング
					for (int i = 0; i < sqlBlocks.length; i++) {
						String sqlBlock = sqlBlocks[i];
						sqlBuilder.append(sqlBlock);
						sqlBuilder.append(SQL_DELIMITER);
						if (i == sqlBlocks.length - 1) {
							sqlBuilder.append(LINE_SEPARATOR);
							manager.updateStateWithNewLine(sqlBlock);
						} else {
							manager.updateStateInSameRow(sqlBlock);
						}
						if (manager.isEffective()) {
							sqlCommandList.add(sqlBuilder.toString());
							sqlBuilder = new StringBuilder();
						}
					}
				} else {
					manager.updateStateWithNewLine(currentLine);
					sqlBuilder.append(currentLine);
					sqlBuilder.append(LINE_SEPARATOR);
				}
			}
		} catch (IOException e) {
			// 何もしない。(StringReaderだからClose時にIOExceptionは発生しない）
		}

		return sqlCommandList;
	}
}
