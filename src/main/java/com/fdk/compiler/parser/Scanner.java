/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.fdk.compiler.parser;


import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;


public class Scanner implements IFLexer {

	private FToken token;

	private List<FToken> savedTokens = new ArrayList<>();

	private FTokenizer tokenizer;


	public Scanner(FTokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public FToken token() {
		return token(0);
	}

	public FToken token(int lookahead) {
		if (lookahead == 0) {
			return token;
		} else {
			ensureLookahead(lookahead);
			return savedTokens.get(lookahead - 1);
		}
	}

	//where
	private void ensureLookahead(int lookahead) {
		for (int i = savedTokens.size(); i < lookahead; i++) {
			savedTokens.add(tokenizer.readToken());
		}
	}


	public FToken nextToken() {
		if (!savedTokens.isEmpty()) {
			token = savedTokens.remove(0);
		} else {
			token = tokenizer.readToken();
		}
		return token;
	}

	@Override
	public FToken skip(int n) {
		assert n >= 0;
		for (int i = 0; i < n; i++) {
			token = nextToken();
		}
		return token;
	}

	@Override
	public FToken slide(FToken.FTokenKind kind) {
		while (token.kind == kind) {
			token = nextToken();
		}
		return token;
	}

	@Override
	public FToken lookAhead(int n) {
		if (n== 0) {
			return token;
		} else {
			ensureLookahead(n);
			return savedTokens.get(n - 1);
		}
	}

	public int errPos() {
		return tokenizer.errPos();
	}

	public void errPos(int pos) {
		tokenizer.errPos(pos);
	}
}
