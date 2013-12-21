package cx;

public enum Operator {
	NOT("!"), INC_PRE("--"), DEC_PRE("--"), INC_POST("++"), DEC_POST("--"), MUL("*"), DIV("/"), MOD("%"), ADD("+"), SUB(
			"-"), EQ("=="), NE("!="), GE(">="), GT(">"), LE("<="), LT("<"), OR("||"), AND("&&"), BIT_OR("|"), BIT_AND(
			"&"), BIT_XOR("^"), BIT_LEFT("<<"), BIT_RIGHT(">>"), NEGATE("-"), COMPLEMENT("~"), BIT_RIGHTU(">>>"), ABSOLUTE(
			"+");

	final String str;

	Operator(String str) {
		this.str = str;
	}

	public String toString() {
		return str;
	}
}
