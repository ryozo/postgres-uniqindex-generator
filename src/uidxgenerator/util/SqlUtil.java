package uidxgenerator.util;

import static uidxgenerator.constants.SqlConstants.CR;
import static uidxgenerator.constants.SqlConstants.CREATE_TABLE_PREFIX;
import static uidxgenerator.constants.SqlConstants.DECLARE_FIELD_DELIMITER;
import static uidxgenerator.constants.SqlConstants.LF;
import static uidxgenerator.constants.SqlConstants.UNIQUE;

import java.util.ArrayList;
import java.util.List;

import uidxgenerator.analyzer.SqlParenthesesAnalyzer;
import uidxgenerator.analyzer.SqlParenthesesInfo;
import uidxgenerator.analyzer.SqlParenthesesInfoSet;
import uidxgenerator.parser.SQLStateManager;

/**
 * SQL関連のUtilクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlUtil {
	
	/**
	 * 引数に指定されたSQL文字列上のUNIQUEキーワードがSQL上の一意制約を表すUNIQUEキーワードであるかチェックする。<br />
	 * <pre>
	 * [UNIQUE制約と判定する条件]
	 * UNIQUE文言の前文字、後文字が以下の条件に合致すること。
	 * ※PostgreSQLでは"unique"を列名とすることはできないため、以下チェックでUNIQUEキーワードを判定可能。
	 * 
	 * 1．UNIQUE文言の前文字が下記いずれかの文字であること
	 * [空白文字][改行文字(CR or LF)][タブ][なし（文字列がUNIQUEキーワードで開始されている）]
	 * 
	 * 2．UNIQUE文言の後文字が下記いずれかの文字であること
	 * [空白文字][改行文字(CR or LF)][タブ][開始括弧"("][なし（文字列がUNIQUEキーワードで終了している）]
	 * ※ 複合UNIQUE制約の場合、UNIQUEキーワードの次文字として開始括弧が利用される場合があるため、開始括弧を許容する。
	 * </pre>
	 * @param declareFieldString チェック対象のSQL文字列
	 * @param uniqueIndex チェック対象UNIQUEキーワードの開始位置（Index)
	 * @return チェック結果
	 */
	public static boolean isSqlUniqueKeyword(String declareFieldString, int uniqueIndex) {
		if (uniqueIndex < 0) {
			return false;
		}
		Character tab = (char)9;
		Character space = (char)32;
		Character leftParentheses = (char)40;
		boolean isBeforeCharMatch = false;
		if (0 == uniqueIndex) {
			isBeforeCharMatch = true;
		} else {
			Character beforeUniqueChar = declareFieldString.charAt(uniqueIndex - 1);
			isBeforeCharMatch = CR.equals(beforeUniqueChar)
					|| LF.equals(beforeUniqueChar)
					|| tab.equals(beforeUniqueChar)
					|| space.equals(beforeUniqueChar);
		}
		
		boolean isAfterCharMatch = false;
		if (uniqueIndex + UNIQUE.length() == declareFieldString.length()) {
			isAfterCharMatch = true;
		} else {
			Character afterUniqueChar = declareFieldString.charAt(uniqueIndex + UNIQUE.length());
			isAfterCharMatch = CR.equals(afterUniqueChar)
					|| LF.equals(afterUniqueChar)
					|| tab.equals(afterUniqueChar)
					|| space.equals(afterUniqueChar)
					|| leftParentheses.equals(afterUniqueChar);
		}
		return isBeforeCharMatch && isAfterCharMatch;
	}
	
	/**
	 * 引数に指定されたSQL文字列上のUNIQUEキーワードが複合UNIQUE制約であるかチェックする。<br />
	 * UNIQUEキーワードの次文字列が括弧である場合、複合UNIQUE制約であると判定する。<br />。
	 * ただし、UNIQUEキーワード直後の空白文字（スペース、タブ、改行、復帰）は無視し、判定を行う。
	 * @param declareFieldLine チェック対象のSQL文字列
	 * @param uniqueIndex チェック対象UNIQUEキーワードの開始位置
	 * @return チェック結果
	 */
	public static boolean isComplexUniqueConstraint(String declareFieldLine, int uniqueIndex) {
		String afterUniqueString = declareFieldLine.substring(uniqueIndex + UNIQUE.length()).trim();
		if (0 < afterUniqueString.length()) {
			Character leftParentheses = (char)40;
			return leftParentheses.equals(afterUniqueString.charAt(0));
		}
		return false;
	}
	
	/**
	 * CreateTable文のフィールド定義部を個々のフィールド文字列別に分割する。<br />
	 * <pre>
	 * [例]
	 *   [入力]
	 *     Create table hoge (
	 *       field1 serial primary key,
	 *       field2 text not null,
	 *       field3 varchar(10),
	 *       UNIQUE (field2, field3)
	 *     ) WHTH fuga;
	 *     
	 *   [出力](以下の要素を持つList)
	 *     [field1 serial primary key]
	 *     [field2 text not null]
	 *     [field3 varchar(10)]
	 *     [UNIQUE (field2, field3)]
	 * </pre>
	 * @param createTableSql
	 * @return
	 */
	public static List<String> decompositionFieldDefinitionPart(String createTableSql) {
		// TODO create table文の判定を追加
		SqlParenthesesInfoSet parenthesesInfoSet = SqlParenthesesAnalyzer.analyze(createTableSql);
		SqlParenthesesInfo fieldDefinitionPharentehses = SqlParenthesesUtil.getFirstStartParenthesesInfo(parenthesesInfoSet);
		
		String fieldDefinitionSection = createTableSql.substring(fieldDefinitionPharentehses.getStartParenthesesIndex() + 1, 
				fieldDefinitionPharentehses.getEndParenthesesIndex());
		
		List<String> declareFieldArray = new ArrayList<>();
		SqlParenthesesInfoSet decFieldsParenthesesSet = SqlParenthesesAnalyzer.analyze(fieldDefinitionSection);
		SQLStateManager manager = new SQLStateManager();

		StringBuilder declareFieldBuilder = new StringBuilder();
		int fromIndex = 0;
		while (fromIndex <= fieldDefinitionSection.length()) {
			int delimiterIndex = fieldDefinitionSection.indexOf(DECLARE_FIELD_DELIMITER, fromIndex);
			String candidateField = null;
			if (0 <= delimiterIndex) {
				candidateField = fieldDefinitionSection.substring(fromIndex, delimiterIndex);
				declareFieldBuilder.append(candidateField);
				// TODO この一行の正当性を検証する
				manager.append(candidateField);
				if (manager.isEffective() 
						&& !SqlParenthesesUtil.isEnclosed(decFieldsParenthesesSet,delimiterIndex)) {
					// SQL文法上有効なフィールド定義区切り文字であり、かつ、括弧で包まれていない（フィールド定義の区切り文字として利用されている）
					declareFieldArray.add(declareFieldBuilder.toString());
					declareFieldBuilder = new StringBuilder();
				} else {
					// SQL文法とは無関係の区切り文字である場合、区切り文字を補完
					declareFieldBuilder.append(DECLARE_FIELD_DELIMITER);
				}
			} else {
				// Delimiterが見つからない場合（最終要素に達した場合）
				candidateField = fieldDefinitionSection.substring(fromIndex);
				declareFieldBuilder.append(candidateField);
				declareFieldArray.add(declareFieldBuilder.toString());
				declareFieldBuilder = new StringBuilder();
			}

			fromIndex = fromIndex + candidateField.length() + DECLARE_FIELD_DELIMITER.length();
		}
		
		return declareFieldArray;
	}
	
	/**
	 * 引数のSqlがCreateTable文であるか判定する。
	 * @param sql 判定対象のSQL文
	 * @return 判定結果
	 */
	public static boolean isCreateTableSql(String sql) {
		SQLStateManager manager = new SQLStateManager();
		int fromIndex = 0;
		while (fromIndex < sql.length()) {
			int createTableIndex = sql.toUpperCase().indexOf(CREATE_TABLE_PREFIX);
			String candidateCreateTable = null;
			if (0 <= createTableIndex) {
				candidateCreateTable = sql.substring(fromIndex, createTableIndex);
				manager.append(candidateCreateTable);
				if (manager.isEffective()) {
					// CreateTableキーワードを発見
					return true;
				}
			}
			fromIndex = fromIndex + candidateCreateTable.length() + CREATE_TABLE_PREFIX.length();
		}
		
		return false;
	}

	/**
	 * SQL文をDelimiterで分割する。<br />
	 * SQL文中のDelimiterがSQL文としての意味を成さない場合（コメント文中や文字列リテラル内の場合）は分割対象としない。<br />
	 * <pre>
	 * [例]
	 *  [input]
	 *   以下のCreateTable文のフィールド定義部を、区切り文字「,」（カンマ）で分割する場合
	 *   (field1 serial primary key,field2 text ¥/*comment , comment*¥/ unique, field3 text DEFAULT('hoge,hoge'))
	 *  [output]
	 *   以下の3要素を保持する配列を返却
	 *   [(field1 serial primary key]
	 *   [field2 text ¥/*comment , comment*¥/ unique]  ※ コメント文中のカンマは分割対象の文字列として扱われない。
	 *   [field3 text DEFAULT('hoge,hoge')]            ※ 文字列リテラル内のカンマは分割対象の文字列として扱われない。
	 * </pre>
	 * 
	 * @param sql 分割対象のSQL
	 * @param delimiter 分割文字
	 * @return 分割後の個々の要素を保持するList
	 */
	public static List<String> split(String sql, String delimiter) {
		return null;
	}
	
	/**
	 * SQL文中に含まれるコメントを取り除く。
	 * @param sql 対象のSQL文
	 * @return コメントが存在しないSQL文
	 */
	public static String removeComment(String sql) {
		// TODO 実装
		return null;
	}
}
