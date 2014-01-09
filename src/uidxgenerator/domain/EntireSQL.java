package uidxgenerator.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uidxgenerator.builder.CreateIndexBuilderFactory;
import uidxgenerator.builder.ICreateIndexBuilder;
import uidxgenerator.builder.impl.PostgresCreateIndexBuilder;
import uidxgenerator.constants.DBMS;

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
	public void addConditionToAllUniqueConstraint(DBMS targetDBMS, Map<String, Object> conditionMap) {
		List<SqlCommand> addSqlCommandList = new ArrayList<>();
		for (SqlCommand sqlCommand : sqlCommandList) {
			if (sqlCommand instanceof CreateTableSqlCommand) {
				// TODO リファクタリング このif文は消せないか？
				// 個々のCreateTable文のUniqueIndex制約を削除
				CreateTableSqlCommand createTableSql = (CreateTableSqlCommand) sqlCommand;
				createTableSql.removeUniqueConstraints();

				List<Set<String>> uniqueKeyList = createTableSql.getUniqueKeyList();
				for (Set<String> uniqueKeySet : uniqueKeyList) {
					ICreateIndexBuilder indexBuilder = CreateIndexBuilderFactory.createBuilder(targetDBMS);
					indexBuilder.setTableName(createTableSql.getCreateTableName());
					indexBuilder.setIndexFields(uniqueKeySet.toArray(new String[]{}));
					if (conditionMap != null) {
						for (String key : conditionMap.keySet()) {
							indexBuilder.addIndexCondition(key, conditionMap.get(key));
						}
					}

					// 作成したCreateIndex文をSQLに追加
					addSqlCommandList.add(indexBuilder.build());
				}
			}
		}
		
		for (SqlCommand addSql : addSqlCommandList) {
			addSqlCommand(addSql);
		}
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
