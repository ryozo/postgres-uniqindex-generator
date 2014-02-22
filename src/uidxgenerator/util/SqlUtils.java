package uidxgenerator.util;

import static uidxgenerator.constants.SqlConstants.*;

import java.util.ArrayList;
import java.util.List;

import uidxgenerator.analyzer.SqlParenthesesAnalyzer;
import uidxgenerator.analyzer.SqlParenthesesInfo;
import uidxgenerator.analyzer.SqlParenthesesInfoSet;
import uidxgenerator.parser.SQLStateManager;

/**
 * SQLに関する操作をとりまとめたのUtilクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlUtils {
	
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
		// TODO 第一引数の文字列長に対する入力チェックが実施できるのでは。
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
	 * SQL文中にコメントが含まれている場合、コメント文の削除は行わない。
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
		SqlParenthesesInfo fieldDefinitionPharentehses = SqlParenthesesUtils.getFirstStartParenthesesInfo(parenthesesInfoSet);
		
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
						&& !SqlParenthesesUtils.isEnclosed(decFieldsParenthesesSet,delimiterIndex)) {
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
	 * TODO 削除？
	 * 引数のSqlがCreateTable文であるか判定する。
	 * @param sql 判定対象のSQL文
	 * @return 判定結果
	 */
	public static boolean isCreateTableSql(String sql) {
		SQLStateManager manager = new SQLStateManager();
		int fromIndex = 0;
		while (fromIndex < sql.length()) {
			int createTableIndex = sql.toUpperCase().indexOf(CREATE_TABLE_PREFIX, fromIndex);
			if (createTableIndex < 0) {
				break;
			}
			String candidateCreateTable = sql.substring(fromIndex, createTableIndex);
			manager.append(candidateCreateTable);
			if (manager.isEffective()) {
				return true;
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
	 * SQL文中に含まれるコメントを取り除く。<br />
	 * 単一行コメント（"--"から改行文字まで）および複数行コメント("¥/*"から"*¥/"まで)を削除する。<br />
	 * なお、"--"や"¥/*"といったコメントを表す文言がStringリテラル内で利用されている場合、これをコメントとは判定せず、
	 * 文言の削除は行わない。
	 * <pre>
	 *   [example1]．
	 *   create table hoge (
	 *     field1 serial primary key -- comment
	 *   );
	 *   ↓
	 *   create table hoge (
	 *     field1 serial primary key 
	 *   );
	 *   
	 *   [example2]．
	 *   create table hoge ( 
	 *     field1 serial ¥/* comment *¥/ primary key
	 *   );
	 *   ↓
	 *   create table hoge ( 
	 *     field1 serial  primary key
	 *   );
	 *   ※ コメントが複数行に跨がっていても結果は同様。コメント文中のみ削除される。
	 *     改行コードはコメント文中内の改行コードのみ削除される。
	 *     ¥/* - *¥/外の改行コードはそのまま出力される。
	 *   
	 *   [ecample3].
	 *   create table hoge ( 
	 *     field1 text default('--') unique
	 *   );
	 *   ↓
	 *   create table hoge ( 
	 *     field1 text default('--') unique
	 *   );
	 *   ※ default句内で利用されている'--'はコメント開始文字列であるが、
	 *     Stringリテラル内（''内）で利用されているため、コメントとは判定されない。
	 *     このコメント判定はDBMSのコメント判定条件と同様である。
	 * </pre>
	 * @param sql 対象のSQL文
	 * @return コメントが存在しないSQL文
	 */
	public static String removeComment(String sql) {
		StringBuilder noCommentSqlBuilder = new StringBuilder();
		SQLStateManager manager = new SQLStateManager();
		// 単一行コメント文中であることを表すフラグ
		boolean isInnerSingleLineCommentFlg = false;
		// 複数行コメント文中であることを表すフラグ
		boolean isInnerMultiLineCommentFlg = false;
		
		int fromIndex = 0;
		while (fromIndex <= sql.length()) {
			// 現在判定対象としている文字列を取得
			String currentDecisionStr = sql.substring(fromIndex);
			if (isInnerSingleLineCommentFlg) {
				String lineSeparateStr = null;
				// ファイル内の改行コードは統一されている前提。
				int lineSeparatorIndex = currentDecisionStr.indexOf(CR);
				if (0 <= lineSeparatorIndex) {
					lineSeparateStr = String.valueOf(CR);
					if (lineSeparatorIndex < currentDecisionStr.length() - 1) {
						Character nextCrStr = currentDecisionStr.charAt(lineSeparatorIndex + 1);
						if (LF.equals(nextCrStr)) {
							lineSeparateStr = lineSeparateStr.concat(String.valueOf(LF));
						}
					}
				} else {
					lineSeparatorIndex = currentDecisionStr.indexOf(LF);
					if (0 <= lineSeparatorIndex) {
						lineSeparateStr = String.valueOf(LF);
					}
				}
				
				if (0 <= lineSeparatorIndex) {
					// 単一行コメント文中である場合、改行文字は例外無く単一行コメントの終了を意味するため
					// SqlStateManagerによる有効文字判定は行わない。
					isInnerSingleLineCommentFlg = false;
					manager.appendWithNewLine(currentDecisionStr.substring(0, lineSeparatorIndex));
					fromIndex = fromIndex + lineSeparatorIndex + lineSeparateStr.length();
					noCommentSqlBuilder.append(lineSeparateStr);

				} else {
					// コメントの終端文字が存在しない場合、以降の行はすべて単一コメントであるため、処理終了
					break;
				}
				
			} else if (isInnerMultiLineCommentFlg) {
				// 複数行コメントの終了文字を探し、該当文字がSQL文法用有効であるかチェックする。
				int mlCommentSuffixIndex = currentDecisionStr.indexOf(MULTILINE_COMMENT_SUFFIX);
				if (0 <= mlCommentSuffixIndex) {
					// 複数行コメント文中である場合、SUFFIX文字は例外無く複数行コメントの終了を意味するため
					// SqlStateManagerによる有効文字判定は行わない。
					isInnerMultiLineCommentFlg = false;
					manager.append(currentDecisionStr.substring(0, mlCommentSuffixIndex + MULTILINE_COMMENT_SUFFIX.length()));
					fromIndex = fromIndex + mlCommentSuffixIndex + MULTILINE_COMMENT_SUFFIX.length();
					continue;
				} else {
					// コメントの終端文字が存在しない場合、今後コメント以外のSQL文は存在しないため、処理終了
					break;
				}
			} else {
				// 現在コメント文中ではない
				int startSingleCommentIndex = currentDecisionStr.indexOf(SINGLELINE_COMMENT_PREFIX);
				int startMultiCommentIndex = currentDecisionStr.indexOf(MULTILINE_COMMENT_PREFIX);
				int minimumIndex = NumberUtils.getMinimumOfPositive(startSingleCommentIndex, startMultiCommentIndex);
				if (minimumIndex < 0) {
					// 以降の文字はすべてコメント文を含まないSQL文である。内容をBuilderにAppendし処理終了。
					noCommentSqlBuilder.append(currentDecisionStr);
					break;
				} else {
					String foundString = null;
					if (minimumIndex == startSingleCommentIndex) {
						foundString = SINGLELINE_COMMENT_PREFIX;
					} else {
						foundString = MULTILINE_COMMENT_PREFIX;
					}
					
					String beforeCommentStr = currentDecisionStr.substring(0, minimumIndex);
					noCommentSqlBuilder.append(beforeCommentStr);
					manager.append(beforeCommentStr);
					if (manager.isEffective()) {
						// 発見したコメント開始文字列はSQL文法上有意である。（コメント文の開始として有効である）
						isInnerSingleLineCommentFlg = minimumIndex == startSingleCommentIndex;
						isInnerMultiLineCommentFlg = minimumIndex == startMultiCommentIndex;
					} else {
						// コメント文字列がSQL文歩上コメントの開始として認識されない場合、該当の文字はSQL文であるため、BuilderにAppendする。
						noCommentSqlBuilder.append(foundString);
					}
					fromIndex = fromIndex + beforeCommentStr.length() + foundString.length();
					manager.append(foundString);
				}
			}
		}
		
		return noCommentSqlBuilder.toString();
	}
}
