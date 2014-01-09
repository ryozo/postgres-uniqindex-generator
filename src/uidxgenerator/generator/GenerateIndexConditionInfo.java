package uidxgenerator.generator;

/**
 * 作成対象のUNIQUE制約の制約条件を保持するDTOです。
 * TODO 実装（必要か？）
 * @author W.Ryozo
 * @version 1.0 新規作成
 */
public class GenerateIndexConditionInfo<T> {
	
	private String indexConditionField;
	
	private T indexConditionValue;
	
	public GenerateIndexConditionInfo(String indexConditionField, T indexConditionValue) {
		this.indexConditionField = null;
	}
}
