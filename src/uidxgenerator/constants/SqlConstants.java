package uidxgenerator.constants;

/**
 * SQL関連の定数を定義します。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlConstants {
	
	/** Create Table文の接頭辞 */
	public static final String CREATE_TABLE_PREFIX = "CREATE TABLE ";
	
	/** SQL文の区切り文字　*/
	public static final String SQL_DELIMITER = ";";
	
	/** 複数行コメントの開始 */
	public static final String MULTILINE_COMMENT_PREFIX = "/*";
	
	/** 文字列リテラルの開始/終端文字 */
	public static final String STRING_LITERAL = "'";
	
	/** 複数行コメントの終了 */
	public static final String MULTILINE_COMMENT_SUFFIX = "*/";
	
	/** 単一行コメントの開始 */
	public static final String SINGLELINE_COMMENT_PREFIX = "--";
	
	/** UNIQUEキーワード */
	public static final String UNIQUE = "UNIQUE";
	
	/** フィールド定義部のSeparator */
	public static final String DECLARE_FIELD_SEPARATOR = ",";
	
	/** 改行コード:CR */
	public static final Character CR = (char)13;
	
	/** 改行コード:LF */
	public static final Character LF = (char)10;
	
	/** 
	 * 改行コード<br />
	 * 本来は利用する環境毎の改行コード（line.separatorシステムプロパティ）を使用するべきだが、
	 * 当Generatorが対象としているER-Masterプラグインが出力するDDLの改行コードは、環境とは関係なくCRLFである。<br />
	 * よって当ツールで取り扱うSQLファイル上の改行コードはCRLF固定とする。<br />
	 * 出力するSQLファイルの改行コードを変更したい場合、当定数を変更すること。
	 */
	public static final String LINE_SEPARATOR = new String(new char[]{CR, LF});
}
