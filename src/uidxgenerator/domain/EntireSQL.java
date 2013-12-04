package uidxgenerator.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL全体を保持するDomainです。
 * @author W.Ryozo
 * @version 1.0
 */
public class EntireSQL implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** SQL文を構成するSQLのリスト */
	private List<SqlCommand> sqlCommandList = new ArrayList<SqlCommand>();
	
	/**
	 * SQL文を追加します。
	 * @param command
	 */
	public void addSqlCommand(SqlCommand command) {
		sqlCommandList.add(command);
	}
	
	/**
	 * SQL文を取得します。
	 * @return　SQL文の一覧
	 */
	public List<SqlCommand> getSqlCommandList() {
		return sqlCommandList;
	}
	
	/**
	 * 自身が保持するSQL文中の全UNIQUE制約に対して引数に指定されたUNIQUE条件を追加します。<br />
	 * TODO javadoc
	 * <pre>
	 * [SQLの変更内容]
	 *  1.自身が保持する全CreateTable文に定義されたUNIQUE制約定義を削除
	 *  2．1にて引数に指定された条件を設定したCreate Unique Index文を追加
	 * </pre>
	 * @param conditionMap UNIQUE制約に対して追加する条件（Key:カラム名、Value:条件値)
	 */
	public void addConditionToAllUniqueConstraint(Map<String, String> conditionMap) {
		
	}
	
	/**
	 * SQL文を文字列表現で返却します。
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (SqlCommand command : sqlCommandList) {
			builder.append(command.toString());
		}
		return builder.toString();
	}

}
