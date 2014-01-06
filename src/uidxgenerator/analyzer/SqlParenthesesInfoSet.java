package uidxgenerator.analyzer;

import java.util.List;

/**
 * SQL文中の複数の括弧情報を保持するクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlParenthesesInfoSet {
	
	private List<SqlParenthesesInfo> sqlParenthesesInfoList;
	
	public SqlParenthesesInfoSet(List<SqlParenthesesInfo> sqlParenthesesInfoList) {
		if (sqlParenthesesInfoList == null) {
			throw new NullPointerException("sqlParenthesesInfoList is null");
		}
		this.sqlParenthesesInfoList = sqlParenthesesInfoList;
	}
	
	public List<SqlParenthesesInfo> getSqlParenthesesInfoList() {
		return sqlParenthesesInfoList;
	}
	
	/**
	 * 引数の文字列位置が当オブジェクトの管理するすべての括弧内に一つでも含まれているか確認します。
	 * @param targetIndex チェック対象の文字列位置
	 * @return チェック結果
	 */
	public boolean isEnclosed(int targetIndex) {
		boolean result = false;
		for (SqlParenthesesInfo info : sqlParenthesesInfoList) {
			result = result || info.isEnclosed(targetIndex);
		}
		return result;
	}
	
	/**
	 * 最も最初に始まる括弧の情報を取得します。
	 * FIXME: 本当は括弧の構造をTree構造で管理して、こういったメソッドは利用しない方針としたい。
	 * @return 最も最初に始まる括弧の情報
	 */
	public SqlParenthesesInfo getFirstStartParenthesesInfo() {
		SqlParenthesesInfo result = null;
		for (SqlParenthesesInfo info : sqlParenthesesInfoList) {
			if (result == null) {
				result = info;
			}
			if (info.getStartParenthesesIndex() < result.getStartParenthesesIndex()) {
				result = info;
			}
		}
		return result;
	}

}
