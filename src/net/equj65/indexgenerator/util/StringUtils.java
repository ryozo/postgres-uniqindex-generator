package net.equj65.indexgenerator.util;

import java.util.List;

/**
 * 文字列関連の操作をとりまとめたUtilクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class StringUtils {
	
	/**
	 * 引数の文字列がnullまたは空文字であることをチェックします。
	 * @param target チェック対象の文字列
	 * @return チェック結果
	 */
	public static boolean isNullOrEmpty(String target) {
		return target == null || target.length() == 0;
	}
	
	/**
	 * {@link StringUtils#join(String[], String)}
	 * @param tokens
	 * @param delimiter
	 * @return
	 */
	public static String join(List<String> tokens, String delimiter) {
		return join(tokens.toArray(new String[0]), delimiter);
	}
	
	/**
	 * 引数に受け取ったString配列をdelimiterでjoinします。
	 * @param tokens join対象の文字列
	 * @param delimiter 連結用文字列
	 * @return join結果
	 */
	public static String join(String[] tokens, String delimiter) {
		if (tokens == null) {
			return "";
		}
		if (delimiter == null) {
			delimiter = "";
		}
		
		StringBuilder resultBuilder = new StringBuilder();
		if (tokens.length > 0) {
			resultBuilder.append(tokens[0]);
			for (int i = 1; i < tokens.length; i++) {
				resultBuilder.append(delimiter);
				resultBuilder.append(tokens[i]);
			}
		}
		
		return resultBuilder.toString();
	}

}
