package uidxgenerator.analyzer;

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
	
	/**
	 * 引数の文字列位置が括弧内に含まれていることを確認します。
	 * @param target チェック対象の文字の位置
	 * @return 括弧内に含まれている場合true、含まれていない場合false
	 */
	public boolean isEnclosed(int targetIndex) {
		return startParenthesesIndex < targetIndex && targetIndex < endParenthesesIndex;
	}

}
