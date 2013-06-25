package cx.runtime;

import java.util.HashMap;
import java.util.Map;

class SymbolTable {
	Symbol top = null;

	Symbol addSymbol(String paramString, Object paramScriptObject) {
		if ((paramString == null) || (paramScriptObject == null)) {
			System.out.println("Cannot add symbol with null value");
			return null;
		}
		Symbol localSymbol = getSymbolInScope(paramString);
		if (localSymbol == null) {
			localSymbol = new Symbol(paramString, paramScriptObject);
			localSymbol.next = top;
			top = localSymbol;
			return top;
		}
		return null;
	}

	Symbol getSymbol(String paramString) {
		Symbol localSymbol;
		for (localSymbol = top; (localSymbol != null) && (!localSymbol.name.equals(paramString)); localSymbol = localSymbol.next)
			;
		return localSymbol;
	}

	Symbol getSymbolInScope(String paramString) {
		Symbol localSymbol = top;
		if ((localSymbol == null) || (localSymbol.getFrameEnd() == true)) return null;
		while ((localSymbol != null) && (!localSymbol.name.equals(paramString))) {
			if (localSymbol.getFrameEnd() == true) return null;
			localSymbol = localSymbol.next;
		}
		return localSymbol;
	}

	Map<String, Object> getSymbols() {
		Symbol localSymbol = top;
		Map<String, Object> localHashtable = new HashMap<String, Object>();
		while (localSymbol != null) {
			localHashtable.put(localSymbol.name, localSymbol.value);
			localSymbol = localSymbol.next;
		}
		return localHashtable;
	}

	void dumpSymbols() {
		Symbol localSymbol = top;
		StringBuffer localStringBuffer1 = new StringBuffer();
		while (localSymbol != null) {
			StringBuffer localStringBuffer2 = new StringBuffer();
			String str1 = localSymbol.value.getClass().getName();
			int i = str1.lastIndexOf(".");
			str1 = "   [" + str1.substring(i + 1) + "]";
			String str2 = localSymbol.getScopeEnd() ? " (Stack End)" : "";
			localStringBuffer2.append(" ").append(localSymbol.name).append("=").append(localSymbol.value.toString());
			if (localStringBuffer2.length() > 15) {
				localStringBuffer2.append(str1).append(str2).append("\r\n");
			} else {
				while (localStringBuffer2.length() < 15)
					localStringBuffer2.append(" ");
				localStringBuffer2.append(str1).append(str2).append("\r\n");
			}
			localStringBuffer1.insert(0, localStringBuffer2);
			localSymbol = localSymbol.next;
		}
		System.out.println(localStringBuffer1.toString());
	}
}
