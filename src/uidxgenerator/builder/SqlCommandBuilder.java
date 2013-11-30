package uidxgenerator.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uidxgenerator.entity.CreateTableSql;
import uidxgenerator.entity.SqlCommand;
import uidxgenerator.util.StringUtil;

/**
 * {@link SqlCommand}のBuilderクラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlCommandBuilder {
	
	/**
	 * @param sql
	 * @return
	 */
	public SqlCommand build(String sql) {
		if (StringUtil.isNullOrEmpty(sql)) {
			throw new IllegalArgumentException("SQL is null or empty");
		}
		
		SqlCommand sqlCommand = null;

		BufferedReader reader = null;
		StringBuilder noCommentSqlBuilder = new StringBuilder();

		try {
			// 1SQLを1行ずつ分析し、コメントなしのSQL文を構成する。
			reader = new BufferedReader(new StringReader(sql));
			
			// 現在参照する行がコメント中であるか否かを表すフラグ
			boolean innerCommentLineFLg = false;
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (innerCommentLineFLg) {
					// コメント中である場合、該当行内のコメント終了タグを探す。
					if (line.indexOf("*/") != -1) {
						// コメント部分を読み飛ばし、以降の文字列をAppend
						noCommentSqlBuilder.append(line.substring(line.indexOf("*/") + 2));
						innerCommentLineFLg = false;
					}
					continue;
				}
				if (line.indexOf("--") != -1) {
					// コメント開始するまでの文字列をBuilderにコピー
					noCommentSqlBuilder.append(line.substring(0, line.indexOf("--")));
					continue;
				}
				if (line.indexOf("/*") != -1) {
					// コメント開始するまでの文字列をBuilderにコピー
					noCommentSqlBuilder.append(line.substring(0, line.indexOf("/*")));
					String commentedString = line.substring(line.indexOf("/*") + 2);
					if (commentedString.indexOf("*/") != -1) {
						// 同行中でSQLコメントが完了している
						noCommentSqlBuilder.append(commentedString.substring(commentedString.indexOf("*/") + 2));
					} else {
						innerCommentLineFLg = true;
					}
					continue;
				}
				
				noCommentSqlBuilder.append(line);
			}
			
			System.out.println(noCommentSqlBuilder.toString());
			
		} catch (IOException ioe) {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					// 捨てる StringReaderだから。
				}
			}
		}

		String noCommentSql = noCommentSqlBuilder.toString();
		List<Set<String>> uniqueKeyList = new ArrayList<Set<String>>();

		if (noCommentSql.startsWith("CREATE TABLE")) {
			String fieldDefinition = noCommentSql.substring(noCommentSql.indexOf("(") + 1);
			// Field毎に分離する。ただし、この時点では末尾にField以外の情報を含んでいる状態。
			String[] fields = fieldDefinition.split(",");
			for (int i = 0; i < fields.length; i++) {
				String[] fieldItems = fields[i].split(" ");
				// 単項目UNIQEのチェック
				// 2項目目以降にUNIQUEキーワードがあるかチェック forのStartは添え字1要素目から。
				for (int j = 1; j < fieldItems.length; j++) {
					if ("UNIQUE".equalsIgnoreCase(fieldItems[j].trim())) {
						// 単項目UNIQUE発見
						Set<String> columnSet = new HashSet<String>();
						columnSet.add(fieldItems[0]);
						uniqueKeyList.add(columnSet);
					}
				}
				
				// 複合項目UNIQUEのチェック
				// 1キーワード目がUNIQUEであり、かつ2単語目の接頭辞が括弧の開始である場合複合UNIQUEである
				if ("UNIQUE".equalsIgnoreCase(fieldItems[0])
						&& fieldItems[1].startsWith("(")) {
					// 複合UNIQUE定義発見 括弧の開始から終わりまで取得し、フィールド一覧を取得
					// 次の閉じ括弧を発見するまで後続のFieldを連結(split(",")実施のため、UNIQUEキーフィールドが分割されている。
					StringBuilder uniqueFieldsBuilder = new StringBuilder();
					uniqueFieldsBuilder.append(fieldItems[1]);
					if (fieldItems[1].indexOf(")") == -1) { 
						// 該当フィールド上でUNIQUEキー定義が完了していない場合
						// 終了括弧が現れるまで、以降のfieldを読み込む。
						int j = i + 1;
						for (; j < fields.length; j++) {
							if (fields[j].indexOf(")") != -1) {
								// 閉じ括弧が見つかったから該当Fieldで複合UNIQUE定義が終わり。
								uniqueFieldsBuilder.append(",").append(fields[j].substring(0, fields[j].indexOf(")") + 1));
								break;
							}
							uniqueFieldsBuilder.append(",").append(fields[j]);
						}
						// 複合UNIQUEの定義部は以降読み飛ばす。
						i = j;
					}
					
					String uniqueFieldDeclare = uniqueFieldsBuilder.toString();
					uniqueFieldDeclare = uniqueFieldDeclare.substring(uniqueFieldDeclare.indexOf("(") + 1, uniqueFieldDeclare.indexOf(")"));
					String[] uniqueFields = uniqueFieldDeclare.split(",");
					Set<String> columnSet = new LinkedHashSet<String>();
					for (String uniqueField : uniqueFields) {
						columnSet.add(uniqueField.trim());
					}
					uniqueKeyList.add(columnSet);
				}
			}
			
			// SqlCommandを作成
			sqlCommand = new CreateTableSql(sql, uniqueKeyList);
		} else {
			sqlCommand = new SqlCommand(sql);
		}
		
		return sqlCommand;
	}
	
	/**
	 * 1行コメント行であるか判定する。
	 * @param line
	 * @return
	 */
	private boolean isSingleCommentLine(String line) {
		return line.trim().startsWith("--");
//		String trimmedLine = line.trim();
//		if (trimmedLine.startsWith("--")) {
//			return true;
//		}
//		if (trimmedLine.startsWith("/*")
//				&& trimmedLine.endsWith("*/")) {
//			return true;
//		}
//		
//		return false;
	}
}
