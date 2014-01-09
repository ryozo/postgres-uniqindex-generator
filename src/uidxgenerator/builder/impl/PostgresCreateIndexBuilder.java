package uidxgenerator.builder.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static uidxgenerator.constants.SqlConstants.*;
import uidxgenerator.builder.ICreateIndexBuilder;
import uidxgenerator.constants.PostgresConditionLiteral;
import uidxgenerator.constants.PostgresDataType;
import uidxgenerator.domain.SqlCommand;
import uidxgenerator.util.StringUtil;

/**
 * PostgreSQL用のCreateUniqueIndex文Builderです。
 * CreateUniqueIndex文のBuilderです。
 * 
 * @author W.Ryozo
 * @version 1.0
 */
public class PostgresCreateIndexBuilder implements ICreateIndexBuilder {
	
	/** SQL文のベース */
	private static final String SQL_BASE = "CREATE UNIQUE INDEX {IDX_NAME} ON {TABLE_NAME} ({KEY_LIST})";
	private static final String SQL_WHERE = " WHERE ";
	private static final String SQL_CONDITION = "{FIELD_NAME} = {FIELD_VALUE}";
	private static final String SQL_AND = "AND";
	/** 置換文字列 */
	private static final String REPLACE_STR_INDEX_NAME = "{IDX_NAME}";
	private static final String REPLACE_STR_TABLE_NAME = "{TABLE_NAME}";
	private static final String REPLACE_STR_KEY_LIST = "{KEY_LIST}";
	private static final String REPLACE_STR_FIELD_NAME = "{FIELD_NAME}";
	private static final String REPLACE_STR_FIELD_VALUE = "{FIELD_VALUE}";
	/** デフォルトのIndex名定義 */
	private static final String INDEX_NAME = "{TABLE_NAME}_{FIELD_LIST}_key";
	
	/** Index名称 */
	// TODO index名は指定もできるし、指定しなくてもいい（デフォルト値の設定）する。
	private String indexName;
	/** テーブル名称 */
	private String tableName;
	/** 一意キー項目 */
	private String[] keyList;
	/** 一意条件 */
	private Map<String, Object> conditionMap = new LinkedHashMap<>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTableName(String tableName) {
		if (StringUtil.isNullOrEmpty(tableName)) {
			throw new IllegalArgumentException("対象テーブル名が指定されていません。");
		}
		this.tableName = tableName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexFields(String... fields) {
		if (keyList == null || keyList.length == 0) {
			throw new IllegalArgumentException("インデックス付与対象のカラムが指定されていません。");
		}
		this.keyList = fields;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addIndexCondition(String fieldName, Object fieldValue) {
		if (StringUtil.isNullOrEmpty(fieldName)) {
			throw new IllegalArgumentException("UniqueIndexの条件フィールドがNullです");
		}
		
		if (!PostgresDataType.isSupport(fieldValue)) {
			throw new IllegalArgumentException("条件値がサポート外のデータ型です。 : " + fieldValue.getClass());
		}

		conditionMap.put(fieldName, fieldValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SqlCommand build() {
		if (tableName == null || keyList == null) {
			// TODO Exceptionを定義する。
			throw new RuntimeException();
		}
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
			Set<Entry<String, Object>> entrySet = conditionMap.entrySet();
			Iterator<Entry<String, Object>> iterator = entrySet.iterator();
			
			boolean isFirst = true;
			while (iterator.hasNext()) {
				Entry<String, Object> conditionEntry = iterator.next();
				if (!isFirst) {
					conditionBuilder.append(SQL_AND);
					isFirst = false;
				}
				PostgresConditionLiteral literal = PostgresDataType.getLiteral(conditionEntry.getValue());
				conditionBuilder.append(
						SQL_CONDITION.replace(REPLACE_STR_FIELD_NAME, conditionEntry.getKey())
								     .replace(REPLACE_STR_FIELD_VALUE, literal.toLiteralNotation(conditionEntry.getValue())));
			}
			sqlBuilder.append(conditionBuilder.toString());
		}
		
		sqlBuilder.append(SQL_DELIMITER);
		// ツール[ER-MASTER]はどの環境で出力したとしても改行コードをCRLFで出力する。
		// そのため、出力改行コードはCRLF固定とする。
		sqlBuilder.append(LINE_SEPARATOR);
		
		SqlCommand command = new SqlCommand(sqlBuilder.toString());
		return command;
	}
}
