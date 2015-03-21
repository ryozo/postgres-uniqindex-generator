package net.equj65.indexgenerator.analyzer;

/**
 * SQL文法上意味のある括弧情報を保持します。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlParenthesesInfo {
	
	/** 開始括弧の位置です。 */
	private int startParenthesesIndex;
	
	/** 終了括弧の一致です。 */
	private int endParenthesesIndex;
	
	public SqlParenthesesInfo(int startParenthesesIndex, int endParenthesesIndex) {
		this.startParenthesesIndex = startParenthesesIndex;
		this.endParenthesesIndex = endParenthesesIndex;
	}

	/**
	 * 括弧の開始位置を返します。
	 * @return 開始位置
	 */
	public int getStartParenthesesIndex() {
		return startParenthesesIndex;
	}

	/**
	 * 括弧の終了位置を返します。
	 * @return 終了位置
	 */
	public int getEndParenthesesIndex() {
		return endParenthesesIndex;
	}
}
