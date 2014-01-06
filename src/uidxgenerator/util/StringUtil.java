package uidxgenerator.util;

import java.util.List;

/**
 * String関連のUtilクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class StringUtil {
	
	/**
	 * 引数の文字列がnullまたは空文字であることをチェックします。
	 * @param target チェック対象の文字列
	 * @return チェック結果
	 */
	public static boolean isNullOrEmpty(String target) {
		return target == null || target.length() == 0;
	}
	
	/**
	 * 空白文字(半角、全角スペース、タブ、改行、復帰）をTrimします。
	 * @param target trim対象の文字列
	 * @return Trim後文字列
	 */
	public static String trimWithSpaceString(String target) {
		// TODO 正規表現のマッチを追加。
		return target.trim();
	}
	
	/**
	 * 引数に受け取ったString配列をdelimiterでjoinします。
	 * @param tokens join対象の文字列
	 * @param delimiter 連結用文字列
	 * @return join結果
	 */
	public static String join(List<String> tokens, String delimiter) {
		if (tokens == null) {
			return "";
		}
		if (delimiter == null) {
			delimiter = "";
		}
		
		StringBuilder resultBuilder = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			resultBuilder.append(tokens.get(i));
			if (i < tokens.size() - 1) {
				resultBuilder.append(delimiter);
			}
		}
		
		return resultBuilder.toString();
	}

}
