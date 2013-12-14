package uidxgenerator.util;

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

}
