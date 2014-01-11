package uidxgenerator.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import uidxgenerator.parser.SQLStateManager;
import uidxgenerator.util.StringUtils;
import static uidxgenerator.constants.SqlConstants.*;

/**
 * TODO javadoc
 * @author W.Ryozo
 *
 */
public class SqlParenthesesAnalyzer {
	/** 開始括弧 */
	private static final Character START = '(';
	/** 終了括弧 */
	private static final Character END = ')';
	
	/**
	 * TODO javadoc
	 * @param target
	 * @return
	 */
	public static SqlParenthesesInfoSet analyze(String target) {
		if (StringUtils.isNullOrEmpty(target)) {
			throw new IllegalArgumentException("target is null or empty");
		}
		
		List<SqlParenthesesInfo> sqlParenthesesInfoList = new ArrayList<>();
		SQLStateManager manager = new SQLStateManager();
		int currentCursor = 0;
		Stack<Integer> stack = new Stack<>();
		
		
		for (int i = 0; i < target.length(); i++) {
			Character chr = target.charAt(i);
			if (START.equals(chr) || END.equals(chr)) {
				// 該当の括弧が有効なSQL構文であるかチェック
				manager.appendInSameRow(target.substring(currentCursor, i));
				currentCursor = i;
				if (manager.isEffective()) {
					if (START.equals(chr)) {
						stack.push(i);
					} else {
						if (!stack.empty()) {
							SqlParenthesesInfo info = new SqlParenthesesInfo(stack.pop(), i);
							sqlParenthesesInfoList.add(info);
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
		
		SqlParenthesesInfoSet resultInfo = new SqlParenthesesInfoSet(sqlParenthesesInfoList);
		return resultInfo;
	}
}
