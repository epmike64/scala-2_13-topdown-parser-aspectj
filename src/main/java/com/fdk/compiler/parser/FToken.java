package com.fdk.compiler.parser;
import java.util.HashMap;
import java.util.Map;

public class FToken {

	final FTokenKind kind;
	final int pos;
	final int endPos;

	public FToken(FTokenKind kind, int pos, int endPos) {
		this.kind = kind;
		this.pos = pos;
		this.endPos = endPos;
	}

	public enum FTokenTag {
		DEFAULT,
		NAMED,
		STRING,
		NUMERIC
	}

	public enum FTokenKind {
		ABSTRACT("abstract", FTokenTag.DEFAULT),
		CASE("case", FTokenTag.DEFAULT),
		CATCH("catch", FTokenTag.DEFAULT),
		CLASS("class", FTokenTag.DEFAULT),
		DEF("def", FTokenTag.DEFAULT),
		DO("do", FTokenTag.DEFAULT),
		ELSE("else", FTokenTag.DEFAULT),
		EXTENDS("extends", FTokenTag.DEFAULT),
		FALSE("false", FTokenTag.NAMED),
		FINAL("final", FTokenTag.DEFAULT),
		FINALLY("finally", FTokenTag.DEFAULT),
		FOR("for", FTokenTag.DEFAULT),
		FORSOME("forSome", FTokenTag.DEFAULT),
		IF("if", FTokenTag.DEFAULT),
		IMPLICIT("implicit", FTokenTag.DEFAULT),
		IMPORT("import", FTokenTag.DEFAULT),
		LAZY("lazy", FTokenTag.DEFAULT),
		MACRO("macro", FTokenTag.DEFAULT),
		MATCH("match", FTokenTag.DEFAULT),
		NEW("new", FTokenTag.DEFAULT),
		NULL("null", FTokenTag.NAMED),
		OBJECT("object", FTokenTag.DEFAULT),
		OVERRIDE("override", FTokenTag.DEFAULT),
		PACKAGE("package", FTokenTag.DEFAULT),
		PRIVATE("private", FTokenTag.DEFAULT),
		PROTECTED("protected", FTokenTag.DEFAULT),
		RETURN("return", FTokenTag.DEFAULT),
		SEALED("sealed", FTokenTag.DEFAULT),
		SUPER("super", FTokenTag.DEFAULT),
		THIS("this", FTokenTag.DEFAULT),
		THROW("throw", FTokenTag.DEFAULT),
		TRAIT("trait", FTokenTag.DEFAULT),
		TRY("try", FTokenTag.DEFAULT),
		TRUE("true", FTokenTag.NAMED),
		TYPE("type", FTokenTag.DEFAULT),
		VAL("val", FTokenTag.DEFAULT),
		VAR("var", FTokenTag.DEFAULT),
		WHILE("while", FTokenTag.DEFAULT),
		WITH("with", FTokenTag.DEFAULT),
		YIELD("yield", FTokenTag.DEFAULT),

		LONGLITERAL(null, FTokenTag.NUMERIC),
		FLOATLITERAL(null, FTokenTag.NUMERIC),
		INTLITERAL(null, FTokenTag.NUMERIC),
		DOUBLELITERAL(null, FTokenTag.NUMERIC),
		CHARLITERAL(null, FTokenTag.NUMERIC),
		STRINGLITERAL(null, FTokenTag.STRING),
		BOOLEANLITERAL(null, FTokenTag.NAMED),

		ID(null, FTokenTag.NAMED),

		EOF(null, FTokenTag.DEFAULT),
		ERROR(null, FTokenTag.DEFAULT),

		PUBLIC("public", FTokenTag.DEFAULT),
		SYNCHRONIZED("synchronized", FTokenTag.DEFAULT),

		THROWS("throws", FTokenTag.DEFAULT),
		TRANSIENT("transient", FTokenTag.DEFAULT),
		VOLATILE("volatile", FTokenTag.DEFAULT),

		AT("@", FTokenTag.DEFAULT),
		FAT_ARROW("=>", FTokenTag.DEFAULT),
		LEFT_ARROW("<-", FTokenTag.DEFAULT),
		RIGHT_ARROW("->", FTokenTag.DEFAULT),
		UNDERSCORE("_", FTokenTag.NAMED),
		LCURL("{", FTokenTag.DEFAULT),
		RCURL("}", FTokenTag.DEFAULT),


		LPAREN("(", FTokenTag.DEFAULT),
		RPAREN(")", FTokenTag.DEFAULT),
		LBRACKET("[", FTokenTag.DEFAULT),
		RBRACKET("]", FTokenTag.DEFAULT),
		SEMI(";", FTokenTag.DEFAULT),
		COMMA(",", FTokenTag.DEFAULT),
		DOT(".", FTokenTag.DEFAULT),
		ELLIPSIS("...", FTokenTag.DEFAULT),
		EQ("=", FTokenTag.DEFAULT),
		GT(">", FTokenTag.DEFAULT),
		LT("<", FTokenTag.DEFAULT),
		BANG("!", FTokenTag.DEFAULT),
		TILDE("~", FTokenTag.DEFAULT),
		QUES("?", FTokenTag.DEFAULT),
		EQEQ("==", FTokenTag.DEFAULT),
		LTEQ("<=", FTokenTag.DEFAULT),
		GTEQ(">=", FTokenTag.DEFAULT),
		BANGEQ("!=", FTokenTag.DEFAULT),
		AMPAMP("&&", FTokenTag.DEFAULT),
		AMP("&", FTokenTag.DEFAULT),
		PIPEPIPE("||", FTokenTag.DEFAULT),
		PIPE("|", FTokenTag.DEFAULT),
		PLUSPLUS("++", FTokenTag.DEFAULT),
		PLUS("+", FTokenTag.DEFAULT),
		SUBSUB("--", FTokenTag.DEFAULT),
		SUB("-", FTokenTag.DEFAULT),
		STAR("*", FTokenTag.DEFAULT),
		SLASH("/", FTokenTag.DEFAULT),
		CARET("^", FTokenTag.DEFAULT),
		PERCENT("%", FTokenTag.DEFAULT),
		LTLT("<<", FTokenTag.DEFAULT),
		GTGT(">>", FTokenTag.DEFAULT),
		GTGTGT(">>>", FTokenTag.DEFAULT),
		PLUSEQ("+=", FTokenTag.DEFAULT),
		SUBEQ("-=", FTokenTag.DEFAULT),
		STAREQ("*=", FTokenTag.DEFAULT),
		SLASHEQ("/=", FTokenTag.DEFAULT),
		AMPEQ("&=", FTokenTag.DEFAULT),
		BAREQ("|=", FTokenTag.DEFAULT),
		CARETEQ("^=", FTokenTag.DEFAULT),
		PERCENTEQ("%=", FTokenTag.DEFAULT),
		UPPER_BOUND("<:", FTokenTag.DEFAULT),
		LOWER_BOUND(">:", FTokenTag.DEFAULT),
		GTGTEQ(">>=", FTokenTag.DEFAULT),
		GTGTGTEQ(">>>=", FTokenTag.DEFAULT),
		COLONCOLON("::", FTokenTag.DEFAULT),
		COLON(":", FTokenTag.DEFAULT),
		POUND("#", FTokenTag.DEFAULT);

		private final String name;
		private final FTokenTag tag;

		FTokenKind(String name, FTokenTag tag) {
			this.name = name;
			this.tag = tag;
		}

		public String getName() {
			return name;
		}

		public FTokenTag getTag() {
			return tag;
		}
	}
	private static final Map<String, FTokenKind> map = new HashMap();
	static {
		for (FTokenKind kind : FTokenKind.values()) {
			map.put(kind.name, kind);
		}
	}

	public static FTokenKind lookupKind(String name) {
		FTokenKind tk =  map.get(name);
		if(tk == null) {
			return FTokenKind.ID;
		}
		return tk;
	}

	public String name() {
		throw new UnsupportedOperationException();
	}

	public String stringVal() {
		throw new UnsupportedOperationException();
	}

	public int radix() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return String.format("FToken %s(%d, %d)", kind.toString(), pos, endPos);
	}


	public static class NamedToken extends FToken {
		private final String name;

		public NamedToken(FTokenKind kind, int pos, int endPos, String name) {
			super(kind, pos, endPos);
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String toString() {
			return String.format("NamedToken %s(%d, %d, value='%s')", super.kind.toString(), super.pos, super.endPos, name);
		}
	}

	public static class StringToken extends FToken {
		private final String stringVal;

		public StringToken(FTokenKind kind, int pos, int endPos, String stringVal) {
			super(kind, pos, endPos);
			this.stringVal = stringVal;
		}

		@Override
		public String stringVal() {
			return stringVal;
		}

		@Override
		public String toString() {
			return String.format("StringToken %s(%d, %d, value='%s')", super.kind.toString(), super.pos, super.endPos, stringVal);
		}
	}

	public static class NumericToken extends StringToken {
		private final int radix;

		public NumericToken(FTokenKind kind, int pos, int endPos, String stringVal, int radix) {
			super(kind, pos, endPos, stringVal);
			this.radix = radix;
		}

		@Override
		public int radix() {
			return radix;
		}

		@Override
		public String toString() {
			return String.format("NumericToken %s(%d, %d, value=['%s', radix=%d])", super.kind.name(), super.pos, super.endPos, stringVal(), radix);
		}
	}
}

