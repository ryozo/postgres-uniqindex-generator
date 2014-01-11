package uidxgenerator.util;

import uidxgenerator.analyzer.SqlParenthesesInfo;
import uidxgenerator.analyzer.SqlParenthesesInfoSet;

/**
 * SQL上の括弧に関する操作をとりまとめたUtilクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlParenthesesUtils {

	/**
	 * 第二引数の文字列位置が第一引数の括弧内に含まれていることを確認します。
	 * @param parenthesesInfo チェック対象の括弧情報
	 * @param targetIndex チェック対象のIndex
	 * @return チェック結果
	 */
	public static boolean isEnclosed(SqlParenthesesInfo parenthesesInfo, int targetIndex) {
		return parenthesesInfo.getStartParenthesesIndex() < targetIndex 
				&& targetIndex < parenthesesInfo.getEndParenthesesIndex();
	}
	
	/**
	 * 第一引数の括弧群の一つ以上の括弧に第一引数の文字列位置が含まれていることを確認します。
	 * 文字列位置がどの括弧にも属さない場合、falseを返します。
	 * @param parenthesesInfoSet チェック対象の括弧集合
	 * @param targetIndex チェック対象のIndex
	 * @return チェック結果
	 */
	public static boolean isEnclosed(SqlParenthesesInfoSet parenthesesInfoSet, int targetIndex) {
		boolean result = false;
		for (SqlParenthesesInfo info : parenthesesInfoSet.getSqlParenthesesInfoList()) {
			result = result || isEnclosed(info, targetIndex);
		}
		return result;
	}
	
	/**
	 * 最も最初に始まる括弧の情報を取得します。
	 * FIXME: 本当は括弧の情報をTree情報で管理して、こういったメソッドは利用しない方針としたい
	 * @param parenthsesSet 最も最初に始まる括弧の情報
	 * @return
	 */
	public static SqlParenthesesInfo getFirstStartParenthesesInfo(SqlParenthesesInfoSet parenthsesSet) {
		SqlParenthesesInfo result = null;
		for (SqlParenthesesInfo info : parenthsesSet.getSqlParenthesesInfoList()) {
			if (result == null) {
				result = info;
			}
			if (info.getStartParenthesesIndex() < result.getStartParenthesesIndex()) {
				result = info;
			}
		}
		return result;
	}
	
	/**
	 * 引数に指定された括弧情報の内側文字列を抽出する。<br />
	 * 抽出文字列中に括弧文字列は含まれない。
	 * TODO javadoc
	 * @param target 
	 * @param parenthesesInfo 
	 */
	public static String getInsideParenthesesString(String target, SqlParenthesesInfo parenthesesInfo) {
		// TODO チェックの追加。
		return target.substring(parenthesesInfo.getStartParenthesesIndex() + 1, 
				parenthesesInfo.getEndParenthesesIndex());
	}
}
