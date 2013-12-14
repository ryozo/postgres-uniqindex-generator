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

}
