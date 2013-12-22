package uidxgenerator.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

import uidxgenerator.parser.SQLStateManager;
import static uidxgenerator.constants.SqlConstants.UNIQUE;

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
		StringBuilder sqlBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new StringReader(this.getSqlCommand()))){
			SQLStateManager manager = new SQLStateManager();
			String line;
			while ((line = reader.readLine()) != null) {
				int uniqueIndex = line.toUpperCase().indexOf(UNIQUE);
				if (uniqueIndex == -1) {
					manager.updateStateWithNewLine(line);
					sqlBuilder.append(line).append(System.getProperty("line.separator"));
				} else {
					if (line.trim().toUpperCase().startsWith(UNIQUE)) {
						// 行頭にUniqueが存在する場合はその行自体無視する。
						continue;
					}
					sqlBuilder.append(line.replaceAll("(?i) UNIQUE", "")).append(System.getProperty("line.separator"));
				}
			}
		} catch (IOException ioe) {
			// 何もしない。StringReaderなのでIOExceptionは発生しない。
		}
		
		this.command = sqlBuilder.toString();
	}

}
