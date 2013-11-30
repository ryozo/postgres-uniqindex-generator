package uidxgenerator.validator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * SQLファイル情報をValidateします。
 * @author W.Ryozo
 * @version 1.0
 */
public class FileValidator {
	
	/**
	 * TODO 記述
	 * @param filePath チェック対象ファイルのパス
	 */
	public static void validateFile(String filePath) throws IOException {
		if (filePath == null) {
			throw new NullPointerException("対象ファイルパスがNullです");
		}
		
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException("対象ファイルが存在しません");
		}
	}
}
