package uidxgenerator.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import uidxgenerator.analyzer.SqlParenthesesAnalyzer;
import uidxgenerator.analyzer.SqlParenthesesInfo;
import uidxgenerator.analyzer.SqlParenthesesInfoSet;
import uidxgenerator.parser.SQLStateManager;
import uidxgenerator.util.StringUtil;
import static uidxgenerator.constants.SqlConstants.*;

/**
 * CreateTable文を表すDomainです。
 * @author W.Ryozo
 * @version 1.0
 */
public class CreateTableSqlCommand extends SqlCommand {

	private static final long serialVersionUID = 1L;
	
	/** 作成対象のテーブル名 */
	private String createTableName;
	
	/**
	 * Unique制約付与対象の項目一覧。<br />
	 * UNIQUE対象とするカラム名の一覧を保持するSetで構成される。<br />
	 * 単一UNIQUEの場合はSetは1要素のみ持つが、複合UNIQUEの場合、
	 * SetはUNIQUEキーを構成するすべてのカラム名を保持する。
	 */
	private List<Set<String>> uniqueKeyList;
	
	public CreateTableSqlCommand(String command, String tableName, List<Set<String>> uniqueKeyList) {
		super(command);
		this.createTableName = tableName;
		this.uniqueKeyList = uniqueKeyList;
	}
	
	/**
	 * 当CreateTableが作成するTABLE名称を返却します。
	 * @return TABLE名称
	 */
	public String getCreateTableName() {
		return createTableName;
	}
	
	/**
	 * 当CreateTableが保持するUNIQUE制約項目の一覧を返却します。
	 * @return UNIQUEとなる項目一覧
	 */
	public List<Set<String>> getUniqueKeyList() {
		return uniqueKeyList;
	}
	
	/**
	 * TODO javadoc
	 */
	public void removeUniqueConstraints() {
		//テーブルの項目定義部を取得
		SqlParenthesesInfoSet parenthesesInfoSet = SqlParenthesesAnalyzer.analyze(this.getSqlCommand());
		SqlParenthesesInfo parenthesesInfo = parenthesesInfoSet.getFirstStartParenthesesInfo();
		if (parenthesesInfo == null) {
			// TODO Exception修正
			throw new RuntimeException("SQL構文が不正です。");
		}
		
		String beforeDeclareFieldsSection = this.getSqlCommand().substring(0, parenthesesInfo.getStartParenthesesIndex() + 1);
		String declareFieldsSection = this.getSqlCommand().substring(parenthesesInfo.getStartParenthesesIndex() + 1, 
				parenthesesInfo.getEndParenthesesIndex());
		String afterDeclareFieldsSection = this.getSqlCommand().substring(parenthesesInfo.getEndParenthesesIndex());
		
		// CreateTable文のフィールド定義部を個々のフィールド別に分割し、配列に格納する。
		List<String> declareFieldArray = new ArrayList<>();
		SqlParenthesesInfoSet decFieldsParenthesesSet = SqlParenthesesAnalyzer.analyze(declareFieldsSection);
		SQLStateManager manager = new SQLStateManager();

		StringBuilder declareFieldBuilder = new StringBuilder();
		int fromIndex = 0;
		while (fromIndex <= declareFieldsSection.length()) {
			int delimiterIndex = declareFieldsSection.indexOf(DECLARE_FIELD_DELIMITER, fromIndex);
			String candidateField = null;
			if (0 <= delimiterIndex) {
				candidateField = declareFieldsSection.substring(fromIndex, delimiterIndex);
				declareFieldBuilder.append(candidateField);
				// TODO この一行の正当性を検証する
				manager.append(candidateField);
				if (manager.isEffective() 
						&& !decFieldsParenthesesSet.isEnclosed(delimiterIndex)) {
					// SQL文法上有効なフィールド定義区切り文字であり、かつ、括弧で包まれていない（フィールド定義の区切り文字として利用されている）
					declareFieldArray.add(declareFieldBuilder.toString());
					declareFieldBuilder = new StringBuilder();
				} else {
					// SQL文法とは無関係の区切り文字である場合、区切り文字を補完
					declareFieldBuilder.append(DECLARE_FIELD_DELIMITER);
				}
			} else {
				// Delimiterが見つからない場合（最終要素に達した場合）
				candidateField = declareFieldsSection.substring(fromIndex);
				declareFieldBuilder.append(candidateField);
				declareFieldArray.add(declareFieldBuilder.toString());
				declareFieldBuilder = new StringBuilder();
			}

			fromIndex = fromIndex + candidateField.length() + DECLARE_FIELD_DELIMITER.length();
		}
		
		ListIterator<String> decFieldsIte = declareFieldArray.listIterator();
		decFieldsLoop: while (decFieldsIte.hasNext()) {
			// 該当のフィールド定義部がUNIQUEであるか判定
			String decField = decFieldsIte.next();
			StringBuilder noUniqueDecFieldBuilder = new StringBuilder();
			manager = new SQLStateManager();
			int uniqueIndex = decField.toUpperCase().indexOf(UNIQUE);
			if (uniqueIndex < 0) {
				continue decFieldsLoop;
			}
			
			String beforeUniqueString = decField.substring(0, uniqueIndex);
			manager.append(beforeUniqueString);
			if (manager.isEffective() && isSqlUniqueKeyword(decField, uniqueIndex)) {
				// SQL文法上有効なUNIQUEキーワードである。
				if (isComplexUniqueConstraint(decField, uniqueIndex)) {
					// 複合Unique制約の場合、定義部自体を削除する。
					decFieldsIte.remove();
					continue decFieldsLoop;
				} else {
					// 単項目Unique制約の場合、UNIQUEキーワードを読み飛ばす
					String afterUniqueString = decField.substring(uniqueIndex + UNIQUE.length());
					// 単項目Uniqueキーワードの前文字がスペースである場合、スペース1文字も併せて削除する。
					int lastIndexOfSpace = beforeUniqueString.lastIndexOf(" ");
					if (lastIndexOfSpace == beforeUniqueString.length() - 1) {
						beforeUniqueString = beforeUniqueString.substring(0, beforeUniqueString.length() - 1);
					}
					noUniqueDecFieldBuilder.append(beforeUniqueString).append(afterUniqueString);
					decFieldsIte.set(noUniqueDecFieldBuilder.toString());
				}
			}
		}
		
		// CreateTable文を復元する。
		StringBuilder noUniqueCreateTableBuilder = new StringBuilder();
		noUniqueCreateTableBuilder.append(beforeDeclareFieldsSection)
								  .append(StringUtil.join(declareFieldArray, DECLARE_FIELD_DELIMITER))
								  .append(afterDeclareFieldsSection);

		this.command = noUniqueCreateTableBuilder.toString();
	}
	
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
	private boolean isSqlUniqueKeyword(String declareFieldString, int uniqueIndex) {
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
	private boolean isComplexUniqueConstraint(String declareFieldLine, int uniqueIndex) {
		String afterUniqueString = declareFieldLine.substring(uniqueIndex + UNIQUE.length()).trim();
		if (0 < afterUniqueString.length()) {
			Character leftParentheses = (char)40;
			return leftParentheses.equals(afterUniqueString.charAt(0));
		}
		return false;
	}
}
