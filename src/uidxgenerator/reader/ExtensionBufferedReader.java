package uidxgenerator.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * BufferedReaderの拡張クラスです。<br />
 * readLineメソッドは元々存在した文字コードを含んで取得します。
 * @author W.Ryozo
 * @version 1.0
 */
public class ExtensionBufferedReader extends BufferedReader {
	
	public ExtensionBufferedReader(Reader in) {
		super(in);
	}
	
	public ExtensionBufferedReader(Reader in, int sz) {
		super(in, sz);
	}
	
	/**
	 * 改行コードを含めて形で1行を取得します。
	 */
	@Override
	public String readLine() throws IOException {
		return super.readLine();
	}

}
