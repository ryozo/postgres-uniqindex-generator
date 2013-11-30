package uidxgenerator.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL全体を保持するEntityです。
 * @author W.Ryozo
 * @version 1.0
 */
public class EntireSQL implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** SQL文を構成するSQLのリスト */
	private Map<Integer, SqlCommand> sqlCommandMap = new LinkedHashMap<Integer, SqlCommand>();
//	private List<SqlCommand> sqlCommandList = new ArrayList<SqlCommand>();
	
	/**
	 * SQL文を追加します。
	 * @param command
	 */
	public void addSqlCommand(SqlCommand command) {
		
	}
	
	/**
	 * SQL文を置き換えます。
	 * @param sqlId 置き換え対象のSqlCommandのID
	 * @param command 置き換え対象のSqlCommand
	 */
	public void replaceSqlCommand(Integer sqlId, SqlCommand command) {
		
	}
	
	/**
	 * SQL文を文字列表現で返却します。
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (SqlCommand command : sqlCommandMap.values()) {
			builder.append(command.toString());
		}
		return builder.toString();
	}

}
