package cx.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import cx.exception.ParserException;
import cx.exception.ScriptException;

public class SourceFile {
	public static char[] loadScript(String paramString) throws ScriptException {
		char[] paramArrayOfChar;
		try {
			File localFile = new File(paramString);
			long fileSize = localFile.length();
			if (fileSize >= Integer.MAX_VALUE) {
				throw new ParserException("file too big!");
			}
			paramArrayOfChar = new char[(int) fileSize];
			FileReader localFileReader = new FileReader(localFile);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 1024 * 16);
			localBufferedReader.read(paramArrayOfChar);
			localFileReader.close();
		} catch (FileNotFoundException localFileNotFoundException) {
			throw new ParserException(localFileNotFoundException.getMessage());
		} catch (IOException localIOException) {
			throw new ParserException(localIOException.getMessage());
		} catch (Exception localException) {
			throw new ParserException(localException.getMessage());
		}
		return paramArrayOfChar;
	}
}