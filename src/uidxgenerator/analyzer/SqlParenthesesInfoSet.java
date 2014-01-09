package uidxgenerator.analyzer;

import java.io.Serializable;
import java.util.List;

/**
 * SQL文中の複数の括弧情報を保持するクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlParenthesesInfoSet implements Serializable {
	
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	// TODO ここはSetで表現するべきでは？
	// TODO SQLの順序に関する処理はUtilクラス側で何とかする。
	private List<SqlParenthesesInfo> sqlParenthesesInfoList;
	
	/**
	 * SQLの括弧情報を利用してインスタンスを作成します。
	 * @param sqlParenthesesInfoList SQL括弧情報の一覧
	 */
	public SqlParenthesesInfoSet(List<SqlParenthesesInfo> sqlParenthesesInfoList) {
		if (sqlParenthesesInfoList == null) {
			throw new NullPointerException("sqlParenthesesInfoList is null");
		}
		this.sqlParenthesesInfoList = sqlParenthesesInfoList;
	}
	
	/**
	 * 当クラスが保持するSQL括弧情報の一覧を取得します。
	 * @return SQL括弧情報の一覧
	 */
	public List<SqlParenthesesInfo> getSqlParenthesesInfoList() {
		return sqlParenthesesInfoList;
	}
}
