package uidxgenerator.exception;

/**
 * 当PJのベースとなる例外クラスです。
 * @author W.Ryozo
 * @version 1.0
 */
public class BaseRuntimeException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public BaseRuntimeException(String msg) {
		super(msg);
	}
	
	public BaseRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
