package uidxgenerator.entity;

import java.util.List;
import java.util.Set;

/**
 * CreateTable文を表すEntityです。
 * @author W.Ryozo
 * @version 1.0
 */
public class CreateTableSql extends SqlCommand {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Unique制約付与対象の項目一覧。<br />
	 * UNIQUE対象とするカラム名の一覧を保持するSetで構成される。<br />
	 * 単一UNIQUEの場合はSetは1要素のみ持つが、複合UNIQUEの場合、
	 * SetはUNIQUEキーを構成するすべてのカラム名を保持する。
	 */
	private List<Set<String>> uniqueKeyList;
	
	public CreateTableSql(String command, List<Set<String>> uniqueKeyList) {
		super(command);
		this.uniqueKeyList = uniqueKeyList;
	}
	
	/**
	 * 該当SQL文が保持するUnique制約を削除します。
	 */
	public void removeUniqueConstraint() {
		// 単項目Uniqueの削除
	}
}
