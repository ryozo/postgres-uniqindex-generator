package uidxgenerator.parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uidxgenerator.constants.SqlConstants.*;
import uidxgenerator.analyzer.SqlParenthesesAnalyzer;
import uidxgenerator.analyzer.SqlParenthesesInfo;
import uidxgenerator.analyzer.SqlParenthesesInfoSet;
import uidxgenerator.domain.CreateTableSqlCommand;
import uidxgenerator.domain.EntireSQL;
import uidxgenerator.domain.SqlCommand;
import uidxgenerator.util.SqlParenthesesUtils;
import uidxgenerator.util.SqlUtils;
import uidxgenerator.util.StringUtils;

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
		if (StringUtils.isNullOrEmpty(targetSqlCommands)) {
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
		if (StringUtils.isNullOrEmpty(sql)) {
			throw new IllegalArgumentException("SQL is null or empty");
		}
		
		String noCommentSql = SqlUtils.removeComment(sql);
		boolean isCreateTableSql = noCommentSql.trim().startsWith(CREATE_TABLE_PREFIX);

		SqlCommand sqlCommand = null;
		if (isCreateTableSql) {
			// Table名の取得
			String tableName = getTableName(noCommentSql);
			
			// CreateTable文内に含まれるUniqueキー情報を解析
			List<Set<String>> uniqueKeyList = new ArrayList<>();
			
			List<String> fieldDefinitionList = SqlUtils.decompositionFieldDefinitionPart(sql);
			for (String fieldDefinition : fieldDefinitionList) {
				SQLStateManager manager = new SQLStateManager();
				int uniqueIndex = fieldDefinition.toUpperCase().indexOf(UNIQUE);
				if (uniqueIndex < 0) {
					continue;
				}
				String beforeUniqueString = fieldDefinition.substring(0, uniqueIndex);
				manager.append(beforeUniqueString);
				if (manager.isEffective() && SqlUtils.isSqlUniqueKeyword(fieldDefinition, uniqueIndex)) {
					// SQL文法上有効なUNIQUEキーワードである。
					Set<String> keySet = new LinkedHashSet<>();
					if (SqlUtils.isComplexUniqueConstraint(fieldDefinition, uniqueIndex)) {
						// 複合UNIQUE - 括弧内のフィールドすべてを対象とする。
						SqlParenthesesInfoSet parenthesesInfoSet = SqlParenthesesAnalyzer.analyze(fieldDefinition);
						SqlParenthesesInfo uniqueFieldsParentheses = SqlParenthesesUtils.getFirstStartParenthesesInfo(parenthesesInfoSet);
						String innerParentheses = SqlParenthesesUtils.getInsideParenthesesString(fieldDefinition, uniqueFieldsParentheses);
						String[] uniqueFields = SqlUtils.removeComment(innerParentheses).split(DECLARE_FIELD_DELIMITER);
						
						for (String uniqueField : uniqueFields) {
							keySet.add(uniqueField.trim());
						}
					} else {
						// 単項目UNIQE フィールド定義部の先頭単語がUNIQUEキー名である
						keySet.add(fieldDefinition.trim().split(" ")[0]);
					}
					uniqueKeyList.add(keySet);
				}
			}
			
			sqlCommand = new CreateTableSqlCommand(sql, tableName, uniqueKeyList);
			
		} else {
			sqlCommand = new SqlCommand(sql);
		}

		return sqlCommand;
		
		
		// TODO ここから下は削除予定

//		BufferedReader reader = null;
//		StringBuilder noCommentSqlBuilder = new StringBuilder();
//
//		try {
//			// 1SQLを1行ずつ分析し、コメントなしのSQL文を構成する。
//			reader = new BufferedReader(new StringReader(sql));
//
//			// 現在参照する行がコメント中であるか否かを表すフラグ
//			boolean innerCommentLineFLg = false;
//			String line;
//			while ((line = reader.readLine()) != null) {
//				line = line.trim();
//				if (innerCommentLineFLg) {
//					// コメント中である場合、該当行内のコメント終了タグを探す。
//					if (line.indexOf(MULTILINE_COMMENT_SUFFIX) != -1) {
//						// コメント部分を読み飛ばし、以降の文字列をAppend
//						// TODO 定数参照
//						noCommentSqlBuilder.append(line.substring(line
//								.indexOf("*/") + 2));
//						innerCommentLineFLg = false;
//					}
//					continue;
//				}
//				if (line.indexOf(SINGLELINE_COMMENT_PREFIX) != -1) {
//					// コメント開始するまでの文字列をBuilderにコピー
//					noCommentSqlBuilder.append(line.substring(0,
//							line.indexOf("--")));
//					continue;
//				}
//				if (line.indexOf(MULTILINE_COMMENT_PREFIX) != -1) {
//					// コメント開始するまでの文字列をBuilderにコピー
//					noCommentSqlBuilder.append(line.substring(0,
//							line.indexOf(MULTILINE_COMMENT_PREFIX)));
//					String commentedString = line.substring(line
//							.indexOf(MULTILINE_COMMENT_PREFIX) + 2);
//					if (commentedString.indexOf(MULTILINE_COMMENT_SUFFIX) != -1) {
//						// 同行中でSQLコメントが完了している
//						noCommentSqlBuilder
//								.append(commentedString.substring(commentedString
//										.indexOf(MULTILINE_COMMENT_SUFFIX) + 2));
//					} else {
//						innerCommentLineFLg = true;
//					}
//					continue;
//				}
//
//				noCommentSqlBuilder.append(line);
//				noCommentSqlBuilder.append(" ");
//			}
//
//		} catch (IOException ioe) {
//			// 何もしない。StringBuilderだからIOException発生しない。
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (Exception e) {
//					// 捨てる StringReaderだから。
//				}
//			}
//		}
//
//		String noCommentSql = noCommentSqlBuilder.toString().trim();
//		List<Set<String>> uniqueKeyList = new ArrayList<Set<String>>();
//
//		if (noCommentSql.toUpperCase().startsWith(CREATE_TABLE_PREFIX)) {
//			// Table名を取得する(CreateTableの開始から次のスペースまでがテーブル名である。
//			int fromIndex = noCommentSql.indexOf(CREATE_TABLE_PREFIX)
//					+ CREATE_TABLE_PREFIX.length();
//			int toIndex = noCommentSql.indexOf(" ",
//					CREATE_TABLE_PREFIX.length());
//			String tableName = noCommentSql.substring(fromIndex, toIndex);
//
//			String fieldDefinition = noCommentSql.substring(noCommentSql
//					.indexOf("(") + 1);
//			// Field毎に分離する。ただし、この時点では末尾にField以外の情報を含んでいる状態。
//			String[] fields = fieldDefinition.split(",");
//			for (int i = 0; i < fields.length; i++) {
//				// TODO フィールドの合間にスペースが複数個続いた場合の動作を検証
//				String[] fieldItems = fields[i].trim().split(" ");
//				// 単項目UNIQEのチェック
//				// 2項目目以降にUNIQUEキーワードがあるかチェック
//				// 1キーワード目はチェックしないからforのStartは添え字1要素目から。
//				for (int j = 1; j < fieldItems.length; j++) {
//					if (UNIQUE.equalsIgnoreCase(fieldItems[j].trim())) {
//						// 単項目UNIQUE発見
//						// UNIQUEキー情報を（1項目目）を保存
//						Set<String> columnSet = new HashSet<String>();
//						columnSet.add(fieldItems[0]);
//						uniqueKeyList.add(columnSet);
//					}
//				}
//
//				// 複合項目UNIQUEのチェック
//				// 1キーワード目がUNIQUEであり、かつ2単語目の接頭辞が括弧の開始である場合、複合UNIQUEである
//				if (UNIQUE.equalsIgnoreCase(fieldItems[0])
//						&& fieldItems[1].startsWith("(")) {
//					// 複合UNIQUE定義発見 括弧の開始から終わりまで取得し、フィールド一覧を取得
//					// 次の閉じ括弧を発見するまで後続のFieldを連結(split(",")実施のため、UNIQUEキーフィールドが分割されている。
//					StringBuilder uniqueFieldsBuilder = new StringBuilder();
//					uniqueFieldsBuilder.append(fieldItems[1]);
//					if (fieldItems[1].indexOf(")") == -1) {
//						// 該当フィールド上でUNIQUEキー定義が完了していない場合
//						// 終了括弧が現れるまで、以降のfieldを読み込む。
//						int j = i + 1;
//						for (; j < fields.length; j++) {
//							if (fields[j].indexOf(")") != -1) {
//								// 閉じ括弧が見つかったから該当Fieldで複合UNIQUE定義が終わり。
//								uniqueFieldsBuilder.append(",").append(
//										fields[j].substring(0,
//												fields[j].indexOf(")") + 1));
//								break;
//							}
//							uniqueFieldsBuilder.append(",").append(fields[j]);
//						}
//						// 複合UNIQUEの定義部は以降読み飛ばす。
//						i = j;
//					}
//
//					String uniqueFieldDeclare = uniqueFieldsBuilder.toString();
//					uniqueFieldDeclare = uniqueFieldDeclare.substring(
//							uniqueFieldDeclare.indexOf("(") + 1,
//							uniqueFieldDeclare.indexOf(")"));
//					String[] uniqueFields = uniqueFieldDeclare.split(",");
//					Set<String> columnSet = new LinkedHashSet<String>();
//					for (String uniqueField : uniqueFields) {
//						columnSet.add(uniqueField.trim());
//					}
//					uniqueKeyList.add(columnSet);
//				}
//			}
//
//			// SqlCommandを作成
//			sqlCommand = new CreateTableSqlCommand(sql, tableName, uniqueKeyList);
//		} else {
//			sqlCommand = new SqlCommand(sql);
//		}
//
//		return sqlCommand;
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
		List<String> sqlCommandList = new ArrayList<>();
		SQLStateManager manager = new SQLStateManager();
		StringBuilder sqlBuilder = new StringBuilder();
		int fromIndex = 0;
		while (fromIndex <= targetSqlCommands.length()) {
			int delimiterIndex = targetSqlCommands.indexOf(SQL_DELIMITER, fromIndex);
			String sqlCandidate = null;
			if (0 <= delimiterIndex) {
				sqlCandidate = targetSqlCommands.substring(fromIndex, delimiterIndex);
				sqlBuilder.append(sqlCandidate);
				manager.append(sqlCandidate);
				if (manager.isEffective()) {
					sqlBuilder.append(SQL_DELIMITER);
					sqlCommandList.add(sqlBuilder.toString());
					sqlBuilder = new StringBuilder();
				} else {
					// SQL文法とは無関係の区切り文字である場合、区切り文字を補完
					sqlBuilder.append(SQL_DELIMITER);
				}
			} else {
				// Delimiterが見つからない場合（最終に達した場合）
				sqlCandidate = targetSqlCommands.substring(fromIndex);
				sqlBuilder.append(sqlCandidate);
				sqlCommandList.add(sqlBuilder.toString());
				sqlBuilder = new StringBuilder();
			}
			
			fromIndex = fromIndex + sqlCandidate.length() + SQL_DELIMITER.length();
		}

		return sqlCommandList;
	}
	
	/**
	 * 引数のCreate Table文の作成テーブル名を取得します。<br />
	 * SQLキーワード[Create Table テーブル名]前にSQLコメント文が存在しない前提とします。
	 * @param createTableSql 対象とするCreateTable文
	 * @return Table名
	 */
	private String getTableName(String createTableSql) {
		String behindOfCreateTableStr = createTableSql.substring(
				createTableSql.toUpperCase().indexOf(CREATE_TABLE_PREFIX) + CREATE_TABLE_PREFIX.length()).trim();
		Pattern pattern = Pattern.compile("[\\s(]");
		Matcher matcher = pattern.matcher(behindOfCreateTableStr);
		if (matcher.find()) {
			return behindOfCreateTableStr.substring(0, matcher.start());
		}
		
		throw new IllegalArgumentException("SQL文法に誤りがあります");
	}
}
