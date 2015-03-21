package net.equj65.indexgenerator.parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.equj65.indexgenerator.analyzer.SqlParenthesesAnalyzer;
import net.equj65.indexgenerator.analyzer.SqlParenthesesInfo;
import net.equj65.indexgenerator.analyzer.SqlParenthesesInfoSet;
import net.equj65.indexgenerator.domain.CreateTableSqlCommand;
import net.equj65.indexgenerator.domain.EntireSQL;
import net.equj65.indexgenerator.domain.SqlCommand;
import net.equj65.indexgenerator.util.SqlParenthesesUtils;
import net.equj65.indexgenerator.util.SqlUtils;
import net.equj65.indexgenerator.util.StringUtils;
import static net.equj65.indexgenerator.constants.SqlConstants.*;

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
						// 単項目UNIQE コメントを除いたフィールド定義部の先頭単語がUNIQUEキー名である
						keySet.add(SqlUtils.removeComment(fieldDefinition).trim().split(" ")[0]);
					}
					uniqueKeyList.add(keySet);
				}
			}
			
			sqlCommand = new CreateTableSqlCommand(sql, tableName, uniqueKeyList);
			
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
