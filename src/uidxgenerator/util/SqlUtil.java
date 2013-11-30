package uidxgenerator.util;

import uidxgenerator.constants.SqlType;

/**
 * SQL関連の処理を提供するUtilクラスです。
 * @author W.Ryozo
 *
 */
public class SqlUtil {

	/** Create Table文の接頭辞 */
	private static final String CREATE_TABLE_PREFIX = "CREATE TABLE";
	
	/**
	 * SQL文の種別を判定します。
	 * @param sqlCommand 判定対象のSQL文
	 * @return 判定結果
	 */
	public static SqlType decisionSqlType(String sqlCommand) {
		// TODO 修正 そもそもSQLの種別はどこまで必要なのか？
		if (StringUtil.trimWithSpaceString(sqlCommand).startsWith(CREATE_TABLE_PREFIX)) {
			return SqlType.CREATETABLE;
		}

		return SqlType.OTHER;
	}
}
