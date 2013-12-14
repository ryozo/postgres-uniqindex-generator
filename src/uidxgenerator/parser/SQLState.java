package uidxgenerator.parser;

import static uidxgenerator.constants.SqlConstants.MULTILINE_COMMENT_PREFIX;
import static uidxgenerator.constants.SqlConstants.MULTILINE_COMMENT_SUFFIX;
import static uidxgenerator.constants.SqlConstants.SINGLELINE_COMMENT_PREFIX;
import static uidxgenerator.constants.SqlConstants.STRING_LITERAL;

/**
 * SQLの状態を管理する.
 * @author W.Ryozo
 * @version 1.0
 */
public class SQLState {
	/** SQLの現在位置がコメント文中であることを表すフラグ */
	private boolean inCommnet = false;
	
	/** SQLの現在位置が文字列リテラル中であることを表す */
	private boolean inStringLiteral = false;
	
	/**
	 * 状態を更新する。
	 * @param decisionTargetSql
	 */
	void updateState(String decisionTargetSql) {
		// TODO 現在の処理は1行単位で当メソッドが呼び出される前提となっている。（1行コメントの開始/終了に関する情報が次メソッドの呼び出しに引き継がれない）行毎の呼び出し依存にならないようにする。
		// キーワードの有無を確認する。
		if (decisionTargetSql == null
				|| decisionTargetSql.trim().length() == 0) {
			return;
		}
		
		// 対象キーワードの有無を判定する。
		if (inCommnet) {
			// コメント文中である場合、コメントの終端を探す
			int endCommentIndex = decisionTargetSql.indexOf(MULTILINE_COMMENT_SUFFIX);
			if (endCommentIndex != -1) {
				inCommnet = false;
				// 後続の文字列を引き続き評価する。
				this.updateState(decisionTargetSql.substring(endCommentIndex + MULTILINE_COMMENT_SUFFIX.length()));
			}
		} else if (inStringLiteral) {
			// コメント文であり、かつリテラルであるといった表記は不可である。
			// TODO エスケープ文字列に対応？
			// String文字列定義である場合、Stringリテラルの終端を探す。
			int endStringLiteralIndex = decisionTargetSql.indexOf(STRING_LITERAL);
			if (endStringLiteralIndex != -1) {
				inStringLiteral = false;
				// 後続の文字を引き続き評価する。
				this.updateState(decisionTargetSql.substring(endStringLiteralIndex + STRING_LITERAL.length()));
			}
		} else {
			// コメント文中でも文字列リテラル内でもない場合
			int startSingleCommentIndex = decisionTargetSql.indexOf(SINGLELINE_COMMENT_PREFIX);
			int startMultiCommentIndex = decisionTargetSql.indexOf(MULTILINE_COMMENT_PREFIX);
			int startStringLiteralIndex = decisionTargetSql.indexOf(STRING_LITERAL);
			int minimumIndex = getMinimumOfPositive(startSingleCommentIndex, startMultiCommentIndex, startStringLiteralIndex);
			if (minimumIndex < 0) {
				// コメント文もリテラルも何も定義されていない。
				// ステータス更新せず処理終了。
				return ;
			} else if (startSingleCommentIndex == minimumIndex) {
				// 直近が1行コメントの場合
				// この行の残りの行はすべてコメントである。
				// 状態を更新せず、処理を終了する
				return ;
			} else if (startMultiCommentIndex == minimumIndex) {
				// 直近が複数行コメントの開始であった場合
				// 同一行内にコメントの終端があるかチェック
				int endMultiCommentIndex = decisionTargetSql.indexOf(MULTILINE_COMMENT_SUFFIX, startMultiCommentIndex + MULTILINE_COMMENT_PREFIX.length());
				if (endMultiCommentIndex != -1) {
					// コメントの終端が存在する場合
					// コメントまたは文字列リテラルの終了以後を切り取って判定を引き続き実施 同一行内でコメントまたは文字列リテラルは終了しているから、ステータス更新不要。
					this.updateState(decisionTargetSql.substring(endMultiCommentIndex + MULTILINE_COMMENT_SUFFIX.length()));
				} else {
					// コメントまたは文字列リテラルの終端が存在しない場合
					// 該当行内でコメントまたは文字列リテラル終了していないため、ステータスを更新する。
					inCommnet = true;
					return ;
				}
			} else if (startStringLiteralIndex == minimumIndex) {
				// 直近が文字列リテラルの開始である場合
				// 同一行内に文字列リテラルの終端があるかチェック
				int endStringLiteralIndex = decisionTargetSql.indexOf(STRING_LITERAL, startStringLiteralIndex + STRING_LITERAL.length());
				if (endStringLiteralIndex != -1) {
					// 文字列リテラルの終了が存在する場合
					// 文字列リテラルの終端以後を切り取って判定を引き続き実施
					// 同一行内で文字列リテラルは終了しているからステータス更新不要
					this.updateState(decisionTargetSql.substring(endStringLiteralIndex + STRING_LITERAL.length()));
				} else {
					// 文字列リテラルの終了が存在しない場合
					// 文字列リテラルは複数行にわたって記述されているため、ステータスを更新する。
					inStringLiteral = true;
					return ;
				}
			}
		}
	}

	private int getMinimumOfPositive(int... targets) {
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

	/**
	 * TODO javadoc
	 * @return
	 */
	boolean isEffective() {
		// コメントの中でなく、且つ文字列リテラルの中でもない。
		return !inCommnet && !inStringLiteral;
	}
}
