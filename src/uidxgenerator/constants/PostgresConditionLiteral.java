package uidxgenerator.constants;

/**
 * PostgreSQLを利用した場合のSQLの絞り込み条件の指定に利用するリテラルを表す列挙子です。
 * @author W.Ryozo
 * @version 1.0
 */
public enum PostgresConditionLiteral {
	/** 文字列リテラルを表します */
	STRING {
		/** {@inheritDoc} */
		@Override
		public String toLiteralNotation(Object target) {
			return SqlConstants.STRING_LITERAL + target + SqlConstants.STRING_LITERAL;
		}
	},
	/** 直接文言を指定するリテラルを表します */
	ELEMENTARY {
		/** {@inheritDoc} */
		@Override
		public String toLiteralNotation(Object target) {
			return target.toString();
		}
	},
	/** NULLを表すリテラル */
	ISNULL {
		/** {@inheritDoc} */
		@Override
		public String toLiteralNotation(Object target) {
			return "IS NULL";
		}
	}
	;
	
	/**
	 * 引数に指定された文字列をリテラル表記に変換します<br />
	 * 変換方法は個々のリテラル別に定義します。
	 * @param target 変換対象の文字列
	 * @return リテラル表記の文字列
	 */
	public abstract String toLiteralNotation(Object target);
	
	/**
	 * 引数の文字列がIsNull演算子出力対象であるか判定する。<br />
	 * PostgreSQL用の判定を行うため、引数がNullであるか否かによる判定を行う。<br />
	 * PostgreSQLは条件として指定された空文字を認識するため、SQL上で[WHERE HOGE = ""]を指定可能である。<br />
	 * そのため、対象文字列が空文字である場合、IsNull出力対象とはしない。
	 * @return
	 */
	private boolean isNullTarget(String target) {
		return target == null;
	}
}
