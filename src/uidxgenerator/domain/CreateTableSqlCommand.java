package uidxgenerator.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import uidxgenerator.analyzer.SqlParenthesesAnalyzer;
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
		SqlParenthesesAnalyzer analyzer = new SqlParenthesesAnalyzer();
		analyzer.analyze(this.getSqlCommand());
		int startParenses = analyzer.getStartParenthesesIndex();
		int endParenses = analyzer.getEndParenthesesIndex();
		if (startParenses < 0 || endParenses < 0) {
			// TODO Exception修正
			throw new RuntimeException("SQL構文が不正です。");
		}
		
		String beforeDeclareFieldsSection = this.getSqlCommand().substring(0, startParenses);
		String declareFieldsSection = this.getSqlCommand().substring(startParenses + 1, endParenses);
		String afterDeclareFieldsSection = this.getSqlCommand().substring(endParenses);
		
		// CreateTable文のフィールド定義部を個々のフィールド別に分割し、配列に格納する。
		List<String> declareFieldArray = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(declareFieldsSection))) {
			SQLStateManager manager = new SQLStateManager();
			StringBuilder decFieldBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				int delimiterIndex = line.indexOf(DECLARE_FIELD_DELIMITER);
				int fromIndex = 0;
				if (delimiterIndex >= 0) {
					while (delimiterIndex >= 0) {
						String decFieldCandidate = line.substring(fromIndex, delimiterIndex);
						decFieldBuilder.append(decFieldCandidate);
						manager.appendInSameRow(decFieldCandidate);
						
						if (manager.isEffective()) {
							declareFieldArray.add(decFieldBuilder.toString());
							decFieldBuilder = new StringBuilder();
						}
						
						fromIndex = delimiterIndex + 1;
						delimiterIndex = line.indexOf(DECLARE_FIELD_DELIMITER, delimiterIndex + 1);
					}					
				} else {
					decFieldBuilder.append(line);
				}

				decFieldBuilder.append(LINE_SEPARATOR);
				manager.appendWithNewLine("");
			}
			
		} catch (IOException ioe) {
			// 処理しない。StringReaderのため、IOExceptionは発生しない。
		}
		
		ListIterator<String> decFieldsIte = declareFieldArray.listIterator();
		decFieldsLoop: while (decFieldsIte.hasNext()) {
			// 該当のフィールド定義部がUNIQUEであるか判定
			String decField = decFieldsIte.next();
			
			String line;
			try (BufferedReader reader = new BufferedReader(new StringReader(decField))) {
				StringBuilder noUniqueDecFieldBuilder = new StringBuilder();
				SQLStateManager manager = new SQLStateManager();
				decFieldLineLoop: while ((line = reader.readLine()) != null) {
					int uniqueIndex = line.toUpperCase().indexOf(UNIQUE);
					if (uniqueIndex < 0) {
						manager.appendWithNewLine(line);
						noUniqueDecFieldBuilder.append(line).append(LINE_SEPARATOR);
						continue decFieldLineLoop;
					}
					// 現在評価する行中にUNIQEコマンドが存在する場合
					String beforeUniqueString = line.substring(0, uniqueIndex);
					manager.appendInSameRow(beforeUniqueString);
					if (manager.isEffective() && isSqlUniqueKeyword(line, uniqueIndex)) {
						// SQL文法上有効なUNIQUEである。
						if (isComplexUniqueConstraint(line, uniqueIndex)) {
							// 複合Uniuqe定義部は定義部自体を削除する。
							decFieldsIte.remove();
							continue decFieldsLoop;
						} else {
							// 単項目Uniqueの場合はUNIQUEキーワードを読み飛ばす。
							String afterUniqueString = line.substring(uniqueIndex + UNIQUE.length());
							noUniqueDecFieldBuilder.append(beforeUniqueString).append(afterUniqueString).append(LINE_SEPARATOR);
						}
					} else {
						manager.appendWithNewLine(line.substring(uniqueIndex));
						noUniqueDecFieldBuilder.append(line).append(LINE_SEPARATOR);
					}
				}
				
				decFieldsIte.set(noUniqueDecFieldBuilder.toString());
			} catch (IOException ioe) {
				// 何もしない。StringReaderのため、IOExceptionは発生しない。
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
	 * TODO javadoc
	 * TODO 後方文字列の確認
	 * @param declareFieldLine
	 * @param uniqueIndex
	 * @return
	 */
	private boolean isSqlUniqueKeyword(String declareFieldLine, int uniqueIndex) {
		return uniqueIndex == 0
				|| " ".equals(String.valueOf(declareFieldLine.charAt(uniqueIndex - 1)));
	}
	
	/**
	 * TODO javadoc
	 * 複合UNIQUEの複合フィールド定義部が同一行内に入っている前提となっていることに注意する。
	 * @param declareFieldLine
	 * @param uniqueIndex
	 * @return
	 */
	private boolean isComplexUniqueConstraint(String declareFieldLine, int uniqueIndex) {
		String afterUniqueString = declareFieldLine.substring(uniqueIndex + UNIQUE.length());
		return afterUniqueString.trim().equals("(");
	}
}
