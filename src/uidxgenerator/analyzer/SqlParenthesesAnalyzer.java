package uidxgenerator.analyzer;

import static uidxgenerator.constants.SqlConstants.*;
import uidxgenerator.parser.SQLStateManager;

public class SqlParenthesesAnalyzer {
	/** 開始括弧 */
	private static final Character START = '(';
	/** 終了括弧 */
	private static final Character END = ')';
	/** 開始括弧のIndex */
	private int startParenthesesIndex = -1;
	/** 終了括弧のIndex */
	private int endParenthesesIndex = -1;
	/** 分析済み？ */
	private boolean analyzed = false;
	
	/**
	 * TODO javadoc
	 * @param target
	 * @param traceStartParenthesesIndex
	 */
	public void analyze(String target) {
		if (target == null
				|| target.length() == 0) {
			throw new IllegalArgumentException("target is null or empty");
		}
		
		SQLStateManager manager = new SQLStateManager();
		int currentCursor = 0;
		int stackCount = 0;
		boolean isFirstParenthesesAppeared = false;
		
		for (int i = 0; i < target.length(); i++) {
			Character chr = target.charAt(i);
			if (START.equals(chr) || END.equals(chr)) {
				// 該当の括弧が有効なSQL構文であるかチェック
				manager.appendInSameRow(target.substring(currentCursor, i));
				currentCursor = i;
				if (manager.isEffective()) {
					if (START.equals(chr)) {
						if (!isFirstParenthesesAppeared) {
							isFirstParenthesesAppeared = true;
							startParenthesesIndex = i;
						}
						stackCount++;
					} else {
						stackCount--;
						if (stackCount == 0) {
							endParenthesesIndex = i;
							break;
						}
					}
				}
			}
			if (CR.equals(chr)) {
				manager.appendWithNewLine(target.substring(currentCursor, i));
				currentCursor = i;
				if (i < target.length() - 1) {
					Character nextChar = target.charAt(i + 1);
					if (LF.equals(nextChar)) {
						// 読み飛ばし
						i++;
					}
				}
			} else if (LF.equals(chr)) {
				manager.appendWithNewLine(target.substring(currentCursor, i));
				currentCursor = i;
			}
		}
		
		analyzed = true;
	}
	
	/**
	 * TODO javadoc
	 * @return
	 */
	public int getStartParenthesesIndex() {
		if (!analyzed) {
			throw new IllegalArgumentException(""); // TODO 見直し
		}
		
		return startParenthesesIndex;
	}
	
	/**
	 * TODO javadoc
	 * @return
	 */
	public int getEndParenthesesIndex() {
		if (!analyzed) {
			throw new IllegalArgumentException(""); // TODO 見直し
		}
		
		return endParenthesesIndex;
	}
}
