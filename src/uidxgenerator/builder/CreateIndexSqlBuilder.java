package uidxgenerator.builder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static uidxgenerator.constants.SqlConstants.CREATE_TABLE_PREFIX;
import static uidxgenerator.constants.SqlConstants.SQL_DELIMITER;
import uidxgenerator.domain.SqlCommand;
import uidxgenerator.util.StringUtil;

/**
 * CreateUniqueIndex文のBuilderクラスです。<br />
 * 
 * 
 * @author W.Ryozo
 * @version 1.0
 */
public class CreateIndexSqlBuilder {
	
	/** SQL文のベース */
	private static final String SQL_BASE = "CREATE UNIQUE INDEX {IDX_NAME} ON {TABLE_NAME} ({KEY_LIST})";
	private static final String SQL_WHERE = " WHERE ";
	private static final String SQL_CONDITION = "{FIELD_NAME} = {FIELD_VALUE}";
	private static final String SQL_AND = "AND";
	private static final String SQL_ISNULL = "IS NULL";
	/** 置換文字列 */
	private static final String REPLACE_STR_INDEX_NAME = "{IDX_NAME}";
	private static final String REPLACE_STR_TABLE_NAME = "{TABLE_NAME}";
	private static final String REPLACE_STR_KEY_LIST = "{KEY_LIST}";
	private static final String REPLACE_STR_FIELD_NAME = "{FIELD_NAME}";
	private static final String REPLACE_STR_FIELD_VALUE = "{FIELD_VALUE}";
	
	/** Index名称 */
	// TODO index名は指定もできるし、指定しなくてもいい（デフォルト値の設定）する。
	private String indexName;
	/** テーブル名称 */
	private String tableName;
	/** 一意キー項目 */
	private String[] keyList;
	/** 一意条件 */
	private Map<String, String> conditionMap = new LinkedHashMap<String, String>();
	
	/**
	 * インデックス名、テーブル名、インデックスフィールド名を利用してインスタンスを作成します。
	 * @param indexName 作成対象のインデックス名称
	 * @param tableName インデックス付与対象のテーブル名
	 * @param keyList インデックスを付与するカラム名（複合ユニークの場合、複数指定）
	 */
	public CreateIndexSqlBuilder(String indexName, String tableName, String... keyList) {
		if (StringUtil.isNullOrEmpty(indexName) 
				|| StringUtil.isNullOrEmpty(tableName)
				|| keyList == null
				|| keyList.length == 0) {
			throw new IllegalArgumentException("引数指定が不正です。引数を空とすることは許されません。");
		}
		this.indexName = indexName;
		this.tableName = tableName;
		this.keyList = keyList;
	}
	
	/**
	 * 当インデックスが有効となる条件を設定します。<br />
	 * 条件値がnullの場合、IsNull文を出力します。
	 * @param fieldName 条件とするフィールド名
	 * @param fieldValue 条件値
	 */
	public void setIndexCondition(String fieldName, String fieldValue) {
		if (StringUtil.isNullOrEmpty(fieldName)) {
			throw new IllegalArgumentException("UniqueIndexの条件フィールドがNullです");
		}
		if (fieldValue == null) {
			// PostgreSQL向けのSQLを発行する場合、この判定式にisEmptyを追加してはならない。
			// PostgreSQLは空文字を認識するため、SQL上で[WHERE KEY = ""]を指定可能である。
			// 空文字をISNULLに変換すると意味が変わってしまう。
			// （変換後SQLがOracleである場合、Oracleは空文字を認識できないため、空文字をISNULL置換すべきである。
			fieldValue = SQL_ISNULL;
		}
		conditionMap.put(fieldName, fieldValue);
	}
	
	/**
	 * 当インデックスが有効となる条件をBoolean値で指定します。<br />
	 * @param fieldName
	 * @param fieldValue
	 */
	public void setIndexCondition(String fieldName, boolean fieldValue) {
		if (StringUtil.isNullOrEmpty(fieldName)) {
			throw new IllegalArgumentException("UniqueIndexの条件フィールドがです");
		}
		conditionMap.put(fieldName, Boolean.toString(fieldValue));
	}

	/**
	 * 設定された内容に基づきCreateUniqueIndex文を作成します。
	 * @return CreateUniqueIndex文を保持するSqlCommand
	 */
	public SqlCommand build() {
		StringBuilder sqlBuilder = new StringBuilder();
		StringBuilder keyListBuilder =new StringBuilder();
		keyListBuilder.append(keyList[0]);
		if (keyList.length > 1) {
			for (int i = 1; i< keyList.length; i++) {
				keyListBuilder.append(", ");
				keyListBuilder.append(keyList[i]);
			}
		}
		sqlBuilder.append(
				SQL_BASE.replace(REPLACE_STR_INDEX_NAME, indexName)
				.replace(REPLACE_STR_TABLE_NAME, tableName)
				.replace(REPLACE_STR_KEY_LIST, keyListBuilder.toString()));
		
		if (!conditionMap.isEmpty()) {
			sqlBuilder.append(SQL_WHERE);
			StringBuilder conditionBuilder = new StringBuilder();
			Set<Entry<String, String>> entrySet = conditionMap.entrySet();
			Iterator<Entry<String, String>> iterator = entrySet.iterator();
			
			boolean isFirst = true;
			while (iterator.hasNext()) {
				Entry<String, String> conditionEntry = iterator.next();
				if (!isFirst) {
					conditionBuilder.append(SQL_AND);
					isFirst = false;
				}
				conditionBuilder.append(
						SQL_CONDITION.replace(REPLACE_STR_FIELD_NAME, conditionEntry.getKey())
								     .replace(REPLACE_STR_FIELD_VALUE, conditionEntry.getValue()));
			}
			sqlBuilder.append(conditionBuilder.toString());
		}
		
		sqlBuilder.append(SQL_DELIMITER);
		sqlBuilder.append(System.getProperty("line.separator"));
		
		SqlCommand command = new SqlCommand(sqlBuilder.toString());
		return command;
	}
}
