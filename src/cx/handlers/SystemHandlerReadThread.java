package cx.handlers;

import java.io.InputStream;
import java.io.InputStreamReader;

public class SystemHandlerReadThread extends Thread {
	protected final InputStream stream;
	public final StringBuffer buffer;

	public SystemHandlerReadThread(String name, InputStream inpStr) {
		this.stream = inpStr;
		buffer = new StringBuffer(8192);
		setName(name);
	}

	public void run() {
		try {
			InputStreamReader inpStrd = new InputStreamReader(stream, "UTF-8");
			int len = 0;
			char[] buf = new char[8192];
			while (-1 != (len = inpStrd.read(buf))) {
				if (len > 0) {
					buffer.append(buf, 0, len);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}