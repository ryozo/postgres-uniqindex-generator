package net.equj65.indexgenerator.builder;

import net.equj65.indexgenerator.domain.SqlCommand;

/**
 * CreateIndex文のBuilderクラスの振る舞いを定義するインタフェースです。
 * @author W.Ryozo
 * @version 1.0
 */
public interface ICreateIndexBuilder {

	/**
	 * Create Index文を付与する対象となるTable名を指定します。
	 * @param tableName 対象テーブル名
	 */
	void setTableName(String tableName);
	
	/**
	 * Indexを付与するフィールド名を指定します。
	 * @param fields　Index付与対象のフィールド名
	 */
	void setIndexFields(String... fields);
	
	/**
	 * Index名称を設定します。
	 * @param name Indexの名称
	 */
	void setIndexName(String name);
	
	/**
	 * Create Index文に対し、Indexが有効となる条件を追加します。<br />
	 * 当SQLに対して付与された条件はCreate Index文内のWhere句内に出力されます。
	 * @param field 条件を設定するフィールド名
	 * @param value 条件値
	 */
	void addIndexCondition(String field, Object value);
	
	/**
	 * これまで設定された各種情報を保持したCreateIndex文を表す{@link SqlCommand}を作成します。
	 * @return CreateIndex文を表す{@link SqlCommand}
	 */
	SqlCommand build();
	
}
