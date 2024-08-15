package com.fdk.compiler.parser;

public interface IFLexer {
	FToken nextToken();

	FToken skip(int n);

	FToken slide(FToken.FTokenKind kind) ;

	FToken lookAhead(int n);
}
