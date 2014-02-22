package uidxgenerator.constants;

import java.math.BigDecimal;

import static uidxgenerator.constants.PostgresConditionLiteral.*;

/**
 * PostgreSQLのテーブルとマッピングされるJavaデータ型情報を保持する列挙子です。<br />
 * 
 * @author W.Ryozo
 * @version 1.0
 */
public enum PostgresDataType {
	
	/** 少数形式 */
	DECIMAL(BigDecimal.class, ELEMENTARY),
	/** 数値形式(2Byte) */
	INT2(Short.class, ELEMENTARY),
	/** 数値形式(4Byte) */
	INT4(Integer.class, ELEMENTARY),
	/** 数値形式(8Byte) */
	INT8(Long.class, ELEMENTARY),
	/** 浮動小数点形式(4Byte) */
	FLOAT4(Float.class, ELEMENTARY),
	/** 浮動小数点形式(8Byte) */
	FLOAT8(Double.class, ELEMENTARY),
	/** 文字列形式 */
	TEXT(String.class, STRING),
	/** 真偽値 */
	BOOLEAN(Boolean.class, ELEMENTARY),
	/** Null値 */
	NULL(null, ISNULL),
	;
	
	private PostgresConditionLiteral literal;
	private Class<?> dataType;
	/**
	 * データ型、およびリテラル形式を設定し、インスタンスを作成します。<br />
	 * データ型は、インスタンスの表すPostgreSQLデータ型をJavaデータ型にマッピングした際に利用するClassを表します。<br />
	 * また、リテラルは当インスタンスの表すデータ型に対して、SQL上の条件等を指定する場合に利用するリテラル表記を設定します。
	 * 
	 * @param dataType 該当データ型のJavaデータ型
	 * @param literal 該当データ型リテラル
	 */
	private PostgresDataType(Class<?> dataType, PostgresConditionLiteral literal) {
		this.dataType = dataType;
		this.literal = literal;
	}
	
	/**
	 * 引数に指定されたオブジェクトがPostgreSQL上でサポートされたJavaデータ型であるかチェックします。<br />
	 * 当メソッドがFalseを返却する場合、指定されたインスタンスのJavaデータ型はPostgreSQLにマッピングすることはできません。
	 * 
	 * @param clazz チェック対象のクラス
	 * @return チェック結果
	 */
	public static boolean isSupport(Object obj) {
		if (obj == null) {
			return true;
		}
		Class<?> clazz = obj.getClass();
		for (PostgresDataType type : values()) {
			if (type.dataType.equals(clazz)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 引数に受け取ったオブジェクトのクラスをSQLの絞り込み条件で使用する場合のリテラルを取得します。
	 * 
	 * @param clazz リテラル取得対象のクラス
	 * @return リテラル
	 */
	public static PostgresConditionLiteral getLiteral(Object obj) {
		if (obj == null) {
			return NULL.literal;
		}
		Class<?> clazz = obj.getClass();
		for (PostgresDataType type : values()) {
			if (type.dataType.equals(clazz)) {
				return type.literal;
			}
		}
		throw new IllegalArgumentException("Target class is not Supported");
	}
}
