package uidxgenerator.builder;

import uidxgenerator.builder.impl.PostgresCreateIndexBuilder;
import uidxgenerator.constants.DBMS;

/**
 * {@link ICreateIndexBuilder}のファクトリクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class CreateIndexBuilderFactory {
	
	/**
	 * 指定されたDBMSに対応する{@link ICreateIndexBuilder}を作成します。
	 * @param dbms CreateIndex文作成対象のRDBMS名
	 * @return 該当DBMSと対応する{@link ICreateIndexBuilder}
	 */
	public static ICreateIndexBuilder createBuilder(DBMS dbms) {
		if (DBMS.POSTGRESQL == dbms) {
			return new PostgresCreateIndexBuilder();
		}
		// TODO 専用のExceptionを定義する。
		throw new RuntimeException();
	}
}
