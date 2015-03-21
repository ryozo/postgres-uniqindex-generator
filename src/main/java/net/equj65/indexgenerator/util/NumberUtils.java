package net.equj65.indexgenerator.util;

/**
 * 数値に関する操作をとりまとめたUtilクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class NumberUtils {
	
	/**
	 * 引数のうち正の数で最小の値を返す。（0がある場合、0を返す）<br />
	 * 正の数が1つも無い場合（すべて負の数である場合）、最初の引数を返す。
	 * @param targets チェック対象の数値
	 * @return 判定結果
	 */
	public static int getMinimumOfPositive(int... targets) {
		if (targets == null || targets.length == 0) {
			throw new IllegalArgumentException("targets is null or empty");
		}
		boolean foundPositiveNum = false;
		int a = Integer.MAX_VALUE;
		for (int target : targets) {
			if (target < 0) {
				continue;
			}
			if (target == 0) {
				// 0が来たら最小値確定。
				return 0;
			}
			foundPositiveNum = true;
			if (target < a) {
				a = target;
			}
		}
		
		// 正の数が一つも無ければ最初の要素を返す。
		return foundPositiveNum ? a : targets[0];
	}

}
