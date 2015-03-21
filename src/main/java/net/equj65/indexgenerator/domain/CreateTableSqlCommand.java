package net.equj65.indexgenerator.domain;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.equj65.indexgenerator.analyzer.SqlParenthesesAnalyzer;
import net.equj65.indexgenerator.analyzer.SqlParenthesesInfo;
import net.equj65.indexgenerator.analyzer.SqlParenthesesInfoSet;
import net.equj65.indexgenerator.parser.SQLStateManager;
import net.equj65.indexgenerator.util.SqlParenthesesUtils;
import net.equj65.indexgenerator.util.SqlUtils;
import net.equj65.indexgenerator.util.StringUtils;
import static net.equj65.indexgenerator.constants.SqlConstants.*;

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
		SqlParenthesesInfo declareFieldParenthese = SqlParenthesesUtils.getFirstStartParenthesesInfo(parenthesesInfoSet);
		if (declareFieldParenthese == null) {
			// TODO Exception修正
			throw new RuntimeException("SQL構文が不正です。");
		}
		
		String beforeDeclareFieldsSection = this.getSqlCommand().substring(0, declareFieldParenthese.getStartParenthesesIndex() + 1);
		String afterDeclareFieldsSection = this.getSqlCommand().substring(declareFieldParenthese.getEndParenthesesIndex());
		
		// CreateTable文のフィールド定義部を個々のフィールド別に分割し、配列に格納する。
		List<String> declareFieldArray = SqlUtils.decompositionFieldDefinitionPart(getSqlCommand());
		
		ListIterator<String> decFieldsIte = declareFieldArray.listIterator();
		decFieldsLoop: while (decFieldsIte.hasNext()) {
			// 該当のフィールド定義部がUNIQUEであるか判定
			String decField = decFieldsIte.next();
			StringBuilder noUniqueDecFieldBuilder = new StringBuilder();
			SQLStateManager manager = new SQLStateManager();
			int uniqueIndex = decField.toUpperCase().indexOf(UNIQUE);
			if (uniqueIndex < 0) {
				continue decFieldsLoop;
			}
			
			String beforeUniqueString = decField.substring(0, uniqueIndex);
			manager.append(beforeUniqueString);
			if (manager.isEffective() && SqlUtils.isSqlUniqueKeyword(decField, uniqueIndex)) {
				// SQL文法上有効なUNIQUEキーワードである。
				if (SqlUtils.isComplexUniqueConstraint(decField, uniqueIndex)) {
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
								  .append(StringUtils.join(declareFieldArray, DECLARE_FIELD_DELIMITER))
								  .append(afterDeclareFieldsSection);

		this.command = noUniqueCreateTableBuilder.toString();
	}
}
