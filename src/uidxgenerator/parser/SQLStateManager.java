package uidxgenerator.parser;

import static uidxgenerator.constants.SqlConstants.*;
import uidxgenerator.util.StringUtil;

/**
 * SQLの状態を管理する.
 * TODO javadoc
 * @author W.Ryozo
 * @version 1.0
 */
public class SQLStateManager {

	/** 現在位置が複数行コメント中であることを表すフラグ */
	private boolean inMultiLineComment = false;
	
	/** 現在の行が単一行コメント中であることを表すフラグ */
	private boolean inSingleLineComment = false;
	
	/**
	 * 現在の行が文字列リテラル中であることを表すフラグ<br />
	 * 文字列リテラルは複数行に渡って記述することが可能である。
	 */
	private boolean inStringLiteral = false;
	
	/**
	 * 引数に受け取ったSQL断片を元にSQL状態を更新する。<br />
	 * @param sqlFlagment SQL断片
	 */
	public void append(String sqlFlagment) {
		if (StringUtil.isNullOrEmpty(sqlFlagment)) {
			return;
		}
		int fromIndex = 0;
		int lineSeparatorIndex = sqlFlagment.indexOf(LF) < 0 ? sqlFlagment.indexOf(CR) : sqlFlagment.indexOf(LF);
		do {
			if (lineSeparatorIndex >= 0) {
				String line = sqlFlagment.substring(fromIndex, lineSeparatorIndex);
				appendWithNewLine(line);
				fromIndex = lineSeparatorIndex + 1;
			} else {
				appendInSameRow(sqlFlagment.substring(fromIndex));
				fromIndex = sqlFlagment.length();
			}
			lineSeparatorIndex = sqlFlagment.indexOf(LF, lineSeparatorIndex + 1);
			
		} while (fromIndex < sqlFlagment.length());
	}
	
	/**
	 * 引数に受け取ったSQL断片を行末と見なしてSQLの状態を更新する。
	 * @param sqlFlagment SQLの断片
	 */
	public void appendWithNewLine(String sqlFlagment) {
		updateState(sqlFlagment, true);
	}
	
	/**
	 * 引数に受け取ったSQLの断片を行中（SQL断片以後も同行内にSQLが続く）と見なしてSQL状態を更新する。
	 * @param sqlFlagment SQLの断片
	 */
	public void appendInSameRow(String sqlFlagment) {
		updateState(sqlFlagment, false);
	}
	
	/**
	 * 状態を更新する。
	 * @param sqlFlagment
	 */
	private void updateState(String sqlFlagment, boolean newLine) {
		// キーワードの有無を確認する。
		if (sqlFlagment == null
				|| sqlFlagment.trim().length() == 0) {
			return;
		}

		if (inSingleLineComment) {
			// 現在の読み込み位置が単一行コメント文中
			// 単一行コメントを完了できるのは改行文字のみ
			if (newLine) {
				// 改行指定のメソッド呼び出しが行われた場合
				inSingleLineComment = false;
			}
			return ;
		} else if (inMultiLineComment) {
			// コメント文中である場合、コメントの終端を探す
			int endCommentIndex = sqlFlagment.indexOf(MULTILINE_COMMENT_SUFFIX);
			if (endCommentIndex != -1) {
				inMultiLineComment = false;
				// 後続の文字列を引き続き評価する。
				this.updateState(sqlFlagment.substring(endCommentIndex + MULTILINE_COMMENT_SUFFIX.length()), newLine);
			}
		} else if (inStringLiteral) {
			// コメント文であり、かつリテラルであるといった表記は不可である。
			// TODO エスケープ？
			// String文字列定義である場合、Stringリテラルの終端を探す。
			int endStringLiteralIndex = sqlFlagment.indexOf(STRING_LITERAL);
			if (endStringLiteralIndex != -1) {
				inStringLiteral = false;
				// 後続の文字を引き続き評価する。
				this.updateState(sqlFlagment.substring(endStringLiteralIndex + STRING_LITERAL.length()), newLine);
			}
		} else {
			// コメント文中でも文字列リテラル内でもない場合
			int startSingleCommentIndex = sqlFlagment.indexOf(SINGLELINE_COMMENT_PREFIX);
			int startMultiCommentIndex = sqlFlagment.indexOf(MULTILINE_COMMENT_PREFIX);
			int startStringLiteralIndex = sqlFlagment.indexOf(STRING_LITERAL);
			int minimumIndex = getMinimumOfPositive(startSingleCommentIndex, startMultiCommentIndex, startStringLiteralIndex);
			if (minimumIndex < 0) {
				// コメント文もリテラルも何も定義されていない。
				// ステータス更新せず処理終了。
				return ;
			} else if (startSingleCommentIndex == minimumIndex) {
				// 直近が1行コメントの場合
				// この行の残りの行はすべてコメントである。
				// 改行に伴う呼び出しではない場合、状態を更新する。
				if (!newLine) {
					inSingleLineComment = true;
				}
				return ;
			} else if (startMultiCommentIndex == minimumIndex) {
				// 直近が複数行コメントの開始であった場合
				// 同一行内にコメントの終端があるかチェック
				int endMultiCommentIndex = sqlFlagment.indexOf(MULTILINE_COMMENT_SUFFIX, startMultiCommentIndex + MULTILINE_COMMENT_PREFIX.length());
				if (endMultiCommentIndex != -1) {
					// コメントの終端が存在する場合
					// コメントまたは文字列リテラルの終了以後を切り取って判定を引き続き実施 同一行内でコメントまたは文字列リテラルは終了しているから、ステータス更新不要。
					this.updateState(sqlFlagment.substring(endMultiCommentIndex + MULTILINE_COMMENT_SUFFIX.length()), newLine);
				} else {
					// コメントまたは文字列リテラルの終端が存在しない場合
					// 該当行内でコメントまたは文字列リテラル終了していないため、ステータスを更新する。
					inMultiLineComment = true;
					return ;
				}
			} else if (startStringLiteralIndex == minimumIndex) {
				// 直近が文字列リテラルの開始である場合
				// 同一行内に文字列リテラルの終端があるかチェック
				int endStringLiteralIndex = sqlFlagment.indexOf(STRING_LITERAL, startStringLiteralIndex + STRING_LITERAL.length());
				if (endStringLiteralIndex != -1) {
					// 文字列リテラルの終了が存在する場合
					// 文字列リテラルの終端以後を切り取って判定を引き続き実施
					// 同一行内で文字列リテラルは終了しているからステータス更新不要
					this.updateState(sqlFlagment.substring(endStringLiteralIndex + STRING_LITERAL.length()), newLine);
				} else {
					// 文字列リテラルの終了が存在しない場合
					// 文字列リテラルは複数行にわたって記述されているため、ステータスを更新する。
					inStringLiteral = true;
					return ;
				}
			}
		}
	}

	/**
	 * 引数のうち正の数で最小の値を返す。（0がある場合、0を返す）<br />
	 * 正の数が1つも無い場合（すべて負の数である場合）、最初の引数を返す。
	 * @param targets チェック対象の数値
	 * @return 判定結果
	 */
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
	 * SQLの状態を取得する。<br />
	 * 当メソッドがtrueを返却する場合、SQL上の現在のカーソル位置は有効（コメント内でなく、SQLリテラル内でもない）であり、
	 * 次に来る文字列はSQL文としての効力を発揮する。<br />
	 * falseを返却する場合、SQL上の現在のカーソル位置は無効（コメントもじしくはSQLリテラル内）である。
	 * 次に来る文字列はSQL予約後であったとしても、SQL文上効力を持たない。
	 * 
	 * @return 現在カーソル位置が有効であるか否か。
	 */
	public boolean isEffective() {
		// 単一／複数行コメント中ではなく、かつ文字列リテラル定義中でもない
		return !inSingleLineComment && !inMultiLineComment && !inStringLiteral;
	}
}
