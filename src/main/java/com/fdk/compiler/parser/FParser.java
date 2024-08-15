package com.fdk.compiler.parser;

import com.fdk.compiler.parser.FToken.FTokenKind;

import java.util.Arrays;
import static com.fdk.compiler.parser.FToken.FTokenKind.*;

public class FParser {

	private IFLexer lexer;
	private FToken token;

	public void assrt(boolean cond) {
		if (!cond) throw new AssertionError();
	}


	public FParser(IFLexer lexer) {
		this.lexer = lexer;
		next();
	}

	public void next() {
		System.out.println("ACCEPTED=[" + token + "]");
		token = lexer.nextToken();
		System.out.println("Next=[" + token + "]");
	}

	public void skip(int n) {
		if (n == 1) next();
		else if (n > 1) token = lexer.skip(n);
		else throw new IllegalArgumentException("n must be positive");
	}

	public int slide(FTokenKind kind) {
		int n = 0;
		while (token.kind == kind) {
			n += 1;
			next();
		}
		return n;
	}

	public void acceptCount(FTokenKind kind, int count) {
		for (int i = 0; i < count; i++) {
			if (token.kind == kind) next();
			else {
				setErrorEndPos(token.pos);
				reportSyntaxError(token.pos, "expected", kind);
			}
		}
	}

	public FToken lookAhead(int n) {
		if (n == 0) return token;
		else if (n > 0) return lexer.lookAhead(n);
		else throw new IllegalArgumentException("n must be positive");
	}

	public void accept(FTokenKind kind) {
		if (token.kind == kind) next();
		else {
			throw new IllegalArgumentException("Expected token " + kind + ", but got " + token.kind);
		}
	}

	public void acceptOneOf(FTokenKind... kinds) {
		if (Arrays.asList(kinds).contains(token.kind)) next();
		else {
			setErrorEndPos(token.pos);
			reportSyntaxError(token.pos, "expected", kinds);
		}
	}

	public boolean isTokenLaOneOf(int n, FTokenKind... kinds) {
		return Arrays.asList(kinds).contains(lookAhead(n).kind);
	}

	public boolean isTokenPrefix(FTokenKind... prefix) {
		for (int i = 0; i < prefix.length; i++) {
			FToken t = lookAhead(i);
			if (t.kind != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean isToken(FTokenKind kind) {
		return token.kind == kind;
	}

	public boolean isTokenOneOf(FTokenKind... kinds) {
		return Arrays.asList(kinds).contains(token.kind);
	}

	public void setErrorEndPos(int errPos) {
		// endPosTable.setErrorEndPos(errPos)
	}

	public void reportSyntaxError(int pos, String key, FTokenKind... kinds) {
		// reporter.syntaxError(token.offset, msg)
	}

	public void p_ident() {
		accept(ID);
	}

	public void p_qualId() {
		p_ident();
		while (token.kind == DOT) {
			next();
			p_ident();
		}
	}

	public void _package() {
		accept(PACKAGE);
		p_qualId();
	}

	public boolean p_import() {
		if (isToken(IMPORT)) {
			next();
			p_stableId();
			if (isToken(DOT)) {
				switch (token.kind) {
					case ID:
					case UNDERSCORE:
						next();
						break;
					case LCURL:
						next();
						if (isToken(ID)) {
							p_ident();
							if (isToken(FAT_ARROW)) {
								next();
								acceptOneOf(ID, UNDERSCORE);
							}
							while (isToken(COMMA)) {
								next();
								p_ident();
								if (isToken(FAT_ARROW)) {
									next();
									acceptOneOf(ID, UNDERSCORE);
								}
							}
						} else if (isToken(UNDERSCORE)) {
							next();
						} else {
							reportSyntaxError(token.pos, "expected", ID, UNDERSCORE);
						}
						accept(RCURL);
						break;
				}
			}
			return true;
		}
		return false;
	}

	public void p_annotType() {
		p_simpleType();
		while (token.kind == AT) {
			next();
			p_simpleType();
		}
	}

	public boolean p_refineStat() {
		return p_dcl();
	}

	public boolean p_refinement() {
		if (isToken(LCURL)) {
			p_refineStat();
			accept(RCURL);
			return true;
		}
		return false;
	}

	public void p_compoundType() {
		p_annotType();
		while (token.kind == WITH) {
			next();
			p_annotType();
		}
	}

	public boolean p_infixType() {
		p_compoundType();
		while (token.kind == ID) {
			next();
			p_compoundType();
		}
		return true;
	}

	public boolean p_simpleType() {
		if (isToken(LPAREN)) {
			next();
			p_types();
			accept(RPAREN);
			p_simpleTypeRest();
			return true;
		} else if (p_stableId()) {
			if (isTokenPrefix(DOT, TYPE)) {
				skip(2);
			}
			p_simpleTypeRest();
			return true;
		}
		return false;
	}

	public boolean p_simpleTypeRest() {
		if (p_typeArgs()) {
			p_simpleTypeRest();
		} else if (isTokenPrefix(POUND, ID)) {
			skip(2);
			p_simpleTypeRest();
		}
		return true;
	}

	public boolean p_type() {
		if (p_functionArgTypes()) {
			if (isToken(FAT_ARROW)) {
				next();
				assrt(p_type());
			}
			return true;
		}
		return false;
	}

	public boolean p_typeArgs() {
		if (isToken(LBRACKET)) {
			next();
			p_types();
			accept(RBRACKET);
			return true;
		}
		return false;
	}

	public boolean p_functionArgTypes() {
		if (isToken(LPAREN)) {
			next();
			if (!isToken(RPAREN)) {
				p_paramType();
				while (isToken(COMMA)) {
					next();
					p_paramType();
				}
			}
			accept(RPAREN);
			return true;
		} else if (p_simpleType()) {
			return true;
		}
		return false;
	}

	public boolean p_paramType() {
		if (isToken(FAT_ARROW)) {
			next();
			assrt(p_type());
		} else {
			assrt(p_type());
			if (isToken(STAR)) next();
		}
		return true;
	}

	public boolean p_classParam() {
		p_modifiers();
		if (isTokenOneOf(VAR, VAL) && isTokenLaOneOf(1, ID)) {
			next();
			p_ident();
			accept(COLON);
			p_paramType();
			if (token.kind == EQ) {
				next();
				p_expr();
			}
			return true;
		}
		return false;
	}

	public boolean p_typeParamClause() {
		if (isToken(LBRACKET)) {
			p_variantTypeParam();
			while (token.kind == COMMA) {
				next();
				p_variantTypeParam();
			}
			accept(RBRACKET);
			return true;
		}
		return false;
	}

	public void p_typeParam() {
		if (isTokenOneOf(ID, UNDERSCORE)) {
			next();
		}
		if (isToken(LBRACKET)) {
			p_typeParamClause();
		}

		if (isToken(LOWER_BOUND)) {
			next();
			assrt(p_type());
		}
		if (isToken(UPPER_BOUND)) {
			next();
			assrt(p_type());
		}
		if (isToken(COLON)) {
			next();
			assrt(p_type());
		}
	}

	public void p_types() {
		assrt(p_type());
		while (token.kind == COMMA) {
			next();
			assrt(p_type());
		}
	}

	public void p_classQualifier() {
		accept(LBRACKET);
		p_ident();
		accept(RBRACKET);
	}

	public void p_stableId2() {
		if (token.kind == LBRACKET) {
			p_classQualifier();
		}
		accept(DOT);
		accept(ID);
	}

	public void p_stableIdRest() {
		while (isTokenPrefix(DOT, ID)) {
			next();
			accept(ID);
		}
	}

	public boolean p_stableId() {
		if (isToken(ID)) {
			p_ident();
			if (token.kind == DOT) {
				if (isTokenLaOneOf(1, THIS, SUPER)) {
					skip(2);
					p_stableId2();
				}
			}
			p_stableIdRest();
		} else if (isTokenOneOf(THIS, SUPER)) {
			next();
			p_stableId2();
			p_stableIdRest();
		} else {
			return false;
		}
		return true;
	}

	public void p_variantTypeParam() {
		if (isTokenOneOf(PLUS, SUB)) {
			next();
		}
		p_typeParam();
	}

	public boolean p_pattern2() {
		if (isToken(ID) && isTokenLaOneOf(1, AT)) {
			p_ident();
			next();
			assrt(p_pattern3());
		} else {
			assrt(p_pattern3());
		}
		return true;
	}

	public boolean p_pattern1() {
		if (isTokenOneOf(UNDERSCORE, ID) && isTokenLaOneOf(1, COLON)) {
			skip(2);
			assrt(p_type());
		} else if (p_pattern2()) {
		} else {
			return false;
		}
		return true;
	}

	public boolean p_pattern() {
		if (p_pattern1()) {
			while (isToken(PIPE)) {
				next();
				p_pattern1();
			}
		} else {
			return false;
		}
		return true;
	}

	public void p_patterns() {
		if (token.kind == UNDERSCORE) {
			next();
			accept(STAR);
		} else {
			p_pattern();
			while (token.kind == COMMA) {
				next();
				p_patterns();
			}
		}
	}

	public boolean p_simplePatternRest() {
		if (isToken(LPAREN)) {
			next();
			if (token.kind != RPAREN) {
				p_patterns();
			}
			accept(RPAREN);
			return true;
		}
		return false;
	}

	public boolean p_simplePattern() {
		if (isToken(UNDERSCORE) || p_literal()) {
			next();
			return true;
		}
		if (p_stableId()) {
			p_simplePatternRest();
			return true;
		}
		return p_simplePatternRest();
	}

	public boolean p_guard() {
		if (isToken(IF)) {
			next();
			p_postfixExpr();
			return true;
		}
		return false;
	}

	public boolean p_generatorRest() {
		if (p_guard()) {
		} else if (p_pattern1()) {
			accept(EQ);
			p_expr();
		} else {
			return false;
		}
		return true;
	}

	public boolean p_generator() {
		if (p_pattern1()) {
			accept(LEFT_ARROW);
			p_expr();
			while (p_generatorRest()) {
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean p_enumerators() {
		if (p_generator()) {
			while (p_generator()) {
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean p_simpleExpr() {
		if (isToken(NEW)) {
			next();
			return p_templateBody() || p_classTemplate();
		} else if (p_blockExpr()) {
			return true;
		}
		return false;
	}

	public boolean p_caseClauses() {
		if (p_caseClause()) {
			while (p_caseClause()) {
			}
			return true;
		}
		return false;
	}

	public boolean p_blockExpr() {
		if (isToken(LCURL)) {
			next();
			if (p_caseClauses()) {
			} else {
				assrt(p_block());
			}
			accept(RCURL);
			return true;
		}
		return false;
	}

	public boolean p_simpleExpr1() {
		if (isToken(UNDERSCORE)) {
			next();
			p_simpleExpr1Rest();
			return true;
		}
		if (p_literal() || p_stableId()) {
			p_simpleExpr1Rest();
			return true;
		} else if (isToken(LPAREN)) {
			next();
			if (!isToken(RPAREN)) {
				p_exprs();
			}
			accept(RPAREN);
			p_simpleExpr1Rest();
			return true;
		} else if (p_simpleExpr()) {
			if (isToken(DOT)) {
				next();
				accept(ID);
			} else if (isToken(LBRACKET)) {
				p_types();
				accept(RBRACKET);
			}
			p_simpleExpr1Rest();
			return true;
		}
		return false;
	}

	public boolean p_simpleExpr1Rest() {
		if (isToken(UNDERSCORE)) {
			next();
			p_simpleExpr1Rest2();
		} else if (isTokenOneOf(DOT, LBRACKET)) {
			p_simpleExpr1Rest2();
		} else if (isTokenOneOf(LPAREN, LCURL)) {
			p_argumentExprs();
		} else {
			return false;
		}
		return true;
	}

	public boolean p_simpleExpr1Rest2() {
		if (isToken(DOT)) {
			next();
			p_ident();
		} else if (isToken(LBRACKET)) {
			next();
			p_types();
			accept(RBRACKET);
		} else {
			return false;
		}
		return true;
	}

	public boolean p_prefixExpr() {
		if (isTokenOneOf(SUB, PLUS, BANG, TILDE)) {
			next();
		}
		if (p_simpleExpr()) {
			return true;
		} else if (p_simpleExpr1()) {
			if (isToken(UNDERSCORE)) {
				next();
			}
			return true;
		}
		return false;
	}

	public boolean p_infixExpr() {
		if (p_prefixExpr()) {
			if (isToken(ID)) {
				while (true) {
					next();
					p_prefixExpr();
					if (!isToken(ID)) break;
				}
			}
			return true;
		}
		return false;
	}

	public boolean p_prefixDef() {
		if (isTokenOneOf(SUB, PLUS, TILDE, BANG)) {
			next();
			return true;
		}
		return false;
	}

	public boolean p_postfixExpr() {
		if (p_infixExpr()) {
			if (isToken(ID)) {
				p_ident();
			}
			while (p_prefixDef()) {
				p_simpleExpr1();
			}
			return true;
		}
		return false;
	}

	public boolean p_caseClause() {
		if (isToken(CASE)) {
			next();
			p_pattern();
			p_guard();
			accept(FAT_ARROW);
			assrt(p_block());
			return true;
		}
		return false;
	}

	public boolean p_expr1() {
		if (isToken(IF)) {
			accept(LPAREN);
			p_expr();
			accept(RCURL);
			p_expr();
			if (token.kind == ELSE) {
				next();
				p_expr();
			}
			return true;
		}

		if (isToken(WHILE)) {
			accept(LPAREN);
			p_expr();
			accept(RCURL);
			p_expr();
			return true;
		}

		if (isToken(TRY)) {
			p_expr();
			if (token.kind == CATCH) {
				next();
				p_expr();
			}
			if (token.kind == FINALLY) {
				next();
				p_expr();
			}
			return true;
		}

		if (isToken(DO)) {
			p_expr();
			accept(WHILE);
			accept(LPAREN);
			p_expr();
			accept(RPAREN);
			return true;
		}

		if (isToken(FOR)) {
			if (token.kind == LPAREN) {
				next();
				p_enumerators();
				accept(RPAREN);
			} else {
				accept(LCURL);
				p_enumerators();
				accept(RCURL);
			}
			if (token.kind == YIELD) {
				next();
			}
			p_expr();
			return true;
		}

		if (isToken(THROW)) {
			assrt(p_expr());
			return true;
		}

		if (isToken(RETURN)) {
			p_expr();
			return true;
		}

		if (p_postfixExpr()) {

			if (isToken(MATCH)) {
				next();
				accept(LCURL);
				while (token.kind != RCURL) {
					next();
					p_caseClause();
				}
				next();
				return true;
			}

			if (p_ascription()) {
				return true;
			}

			if (p_argumentExprs()) {
				accept(EQ);
				p_expr();
				return true;
			}

			if (isToken(ID)) {
				p_ident();
				accept(EQ);
				p_expr();
				return true;
			}

			if (isToken(EQ)) {
				next();
				p_expr();
				return true;
			}

			return true;
		}
		return false;
	}

	public boolean p_annotations() {
		if (isToken(AT)) {
			while (true) {
				next();
				p_simpleType();
				p_argumentExprs();
				if (!isToken(AT)) break;
			}
			return true;
		}
		return false;
	}

	public boolean p_ascription() {
		if (isToken(COLON)) {
			if (isTokenPrefix(UNDERSCORE, STAR)) {
				skip(2);
				return true;
			} else if (p_annotations() || p_infixType()) {
				return true;
			}
		}
		return false;
	}

	public boolean p_args() {
		if (p_expr()) {
			while (isToken(COMMA)) {
				next();
				p_expr();
			}
			return true;
		} else if (p_postfixExpr()) {
			if (isTokenOneOf(COLON, UNDERSCORE, STAR)) {
				next();
			}
			return true;
		}
		return false;
	}

	public boolean p_bindings() {
		if (isToken(LPAREN)) {
			while (true) {
				next();
				acceptOneOf(ID, UNDERSCORE);
				if (isToken(COLON)) {
					next();
					assrt(p_type());
				}
				if (!isToken(COMMA)) break;
			}
			return true;
		}
		return false;
	}

	public boolean p_exprs() {
		assrt(p_expr());
		while (isToken(COMMA)) {
			next();
			assrt(p_expr());
		}
		return true;
	}

	public boolean p_expr() {
		if (p_bindings()) {
			accept(FAT_ARROW);
			return p_expr();
		}

		if (isToken(IMPLICIT)) {
			acceptOneOf(ID, UNDERSCORE);
			accept(FAT_ARROW);
			return p_expr();
		}

		if (isTokenOneOf(ID, UNDERSCORE) && isTokenLaOneOf(1, FAT_ARROW)) {
			next();
			accept(FAT_ARROW);
			return p_expr();
		}

		return p_expr1();
	}

	public boolean p_resultExprRest() {
		accept(FAT_ARROW);
		p_block();
		return true;
	}

	public boolean p_resultExpr() {
		if (p_bindings()) {
			p_resultExprRest();
		} else if (isTokenPrefix(IMPLICIT, ID) || isToken(ID) || isToken(UNDERSCORE)) {
			if (isToken(IMPLICIT)) next();
			next();
			accept(COLON);
			p_compoundType();
			p_resultExprRest();
		} else if (p_expr1()) {
		} else {
			return false;
		}
		return true;
	}

	public boolean p_argumentExprs() {
		if (isTokenOneOf(LPAREN, LCURL)) {
			FToken left = token;
			next();
			p_args();
			accept(left.kind == LPAREN ? RPAREN : RCURL);
			return true;
		}
		return false;
	}

	public void p_constr() {
		p_simpleType();
		p_argumentExprs();
	}

	public void p_classParents() {
		p_constr();
		while (isToken(WITH)) {
			next();
			p_simpleType();
		}
	}

	public boolean p_pattern3() {
		assrt(p_simplePattern());
		while (isToken(ID)) {
			p_ident();
			assert (p_simplePattern());
		}
		return true;
	}

	public boolean p_patDef() {
		if (p_pattern2()) {
			while (isToken(COMMA)) {
				next();
				p_pattern2();
			}
			if (isToken(COLON)) {
				next();
				assrt(p_type());
			}
			accept(EQ);
			p_expr();
		} else {
			return false;
		}
		return true;
	}

	public boolean p_ids() {
		if (isToken(ID)) {
			p_ident();
			while (isToken(COMMA)) {
				next();
				p_ident();
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean p_varDef() {
		if (p_patDef()) {
		} else if (p_ids()) {
			accept(COLON);
			assrt(p_type());
			accept(EQ);
			accept(UNDERSCORE);
		} else {
			return false;
		}
		return true;
	}

	public boolean p_patVarDef() {
		if (isToken(VAL)) {
			next();
			return p_patDef();
		} else if (isToken(VAR)) {
			next();
			return p_varDef();
		}
		return false;
	}

	public void p_earlyDefs() {
		accept(LCURL);
		p_modifiers();
		p_patVarDef();
		accept(RCURL);
		accept(WITH);
	}

	public boolean p_selfInvocation() {
		if (isToken(THIS)) {
			next();
			p_argumentExprs();
			return true;
		}
		return false;
	}

	public boolean p_constrBlock() {
		if (isToken(LCURL)) {
			p_selfInvocation();
			while (p_blockStat()) {
			}
			accept(RCURL);
			return true;
		}
		return false;
	}

	public boolean p_constrExpr() {
		return p_selfInvocation() || p_constrBlock();
	}

	public boolean p_defDcl() {
		if (isToken(VAL)) {
			return p_valDcl() || p_patVarDef();
		} else if (isToken(VAR)) {
			return p_varDcl() || p_patVarDef();
		} else if (isToken(DEF)) {
			return p_funDclDef();
		} else if (isToken(TYPE)) {
			return p_typeDclDef();
		}
		return false;
	}

	public boolean p_typeDclDef() {
		if (isToken(TYPE)) {
			next();
			p_ident();
			p_typeParamClause();
			if (isToken(EQ)) {
				next();
				assrt(p_type());
				return true;
			}
			if (isToken(LOWER_BOUND)) {
				next();
				assrt(p_type());
			}
			if (isToken(UPPER_BOUND)) {
				next();
				assrt(p_type());
			}
			return true;
		}
		return false;
	}

	public boolean p_varDcl() {
		if (isToken(VAR) && isTokenLaOneOf(1, ID)) {
			p_ids();
			accept(COLON);
			assrt(p_type());
			return true;
		}
		return false;
	}

	public boolean p_valDcl() {
		if (isToken(VAL) && isTokenLaOneOf(1, ID)) {
			p_ids();
			accept(COLON);
			assrt(p_type());
			return true;
		}
		return false;
	}

	public boolean p_funTypeParamClause() {
		if (isToken(LBRACKET)) {
			next();
			p_typeParam();
			while (isToken(COMMA)) {
				next();
				p_typeParam();
			}
			accept(RBRACKET);
			return true;
		}
		return false;
	}

	public boolean p_param() {
		if (isTokenOneOf(ID)) {
			p_ident();
			if (isToken(COLON)) {
				next();
				p_paramType();
			} else if (isToken(EQ)) {
				next();
				p_expr();
			}
			return true;
		}
		return false;
	}

	public boolean p_params() {
		if (p_param()) {
			while (isToken(COMMA)) {
				next();
				p_param();
			}
			return true;
		}
		return false;
	}

	public boolean p_paramClausesRest() {
		if (isToken(LPAREN) && isTokenLaOneOf(1, IMPLICIT)) {
			skip(2);
			p_params();
			accept(RPAREN);
			return true;
		}
		return false;
	}

	public boolean p_paramClause() {
		if (isToken(LPAREN)) {
			next();
			p_params();
			accept(RPAREN);
			return true;
		}
		return false;
	}

	public boolean p_paramClauses() {
		if (p_paramClausesRest()) {
			return true;
		} else if (p_paramClause()) {
			p_paramClausesRest();
			return true;
		}
		return false;
	}

	public boolean p_funSig() {
		if (isToken(ID)) {
			p_ident();
			p_funTypeParamClause();
			p_paramClauses();
			return true;
		}
		return false;
	}

	public boolean p_funDclDef() {
		if (isToken(DEF)) {
			next();
			if (p_funSig()) {
				if (isToken(COLON)) {
					next();
					assrt(p_type());
					if (isToken(EQ)) {
						next();
						p_expr();
					}
				} else if (isToken(LCURL)) {
					p_block();
					accept(RCURL);
				}
				return true;
			} else if (isToken(THIS)) {
				next();
				p_paramClause();
				p_paramClauses();
				if (isToken(EQ)) {
					next();
					p_constrExpr();
				} else {
					p_constrBlock();
				}
				return true;
			}
		}
		return false;
	}

	public boolean p_dcl() {
		if (isToken(VAL)) {
			return p_valDcl();
		} else if (isToken(VAR)) {
			return p_varDcl();
		} else if (isToken(DEF)) {
			return p_funDclDef();
		} else if (isToken(TYPE)) {
			return p_typeDclDef();
		}
		return false;
	}

	public boolean p_def() {
		return p_patVarDef() || p_funDclDef() || p_typeDclDef() || p_tmplDef();
	}

	public boolean p_block() {
		assrt(p_blockStat());
		while (p_blockStat()) {
		}
		p_resultExpr();
		return true;
	}

	public boolean p_modifiers() {
		boolean isModifier = false;
		while (p_modifier()) {
			isModifier = true;
		}
		return isModifier;
	}

	public boolean p_modifier() {
		if(isToken(OVERRIDE)) {
			next();
			return true;
		} else {
			return p_localModifier() || p_accessModifier();
		}
	}

	public boolean p_localModifier() {
		if (isTokenOneOf(ABSTRACT, FINAL, SEALED, IMPLICIT, LAZY)) {
			next();
			return true;
		}
		return false;
	}

	public boolean p_accessModifier() {
		if (isTokenOneOf(PRIVATE, PROTECTED)) {
			next();
			p_accessQualifier();
			return true;
		}
		return false;
	}

	public boolean p_blockStat() {
		if (p_import()) {
			return true;
		}

		if (p_localModifier()) {
			p_tmplDef();
			return true;
		}

		if (isTokenOneOf(IMPLICIT, LAZY)) {
			next();
			p_def();
			return true;
		}

		return p_def() || p_expr1();
	}

	public boolean p_templateStat() {
		if (p_import()) {
		} else if (p_modifiers()) {
			return p_defDcl();
		}
		return p_defDcl() || p_expr();
	}

	public boolean p_literal() {
		if (isToken(SUB) && isTokenLaOneOf(1, INTLITERAL, FLOATLITERAL)) {
			skip(2);
			return true;
		} else if (isTokenOneOf(INTLITERAL, FLOATLITERAL, STRINGLITERAL, CHARLITERAL, BOOLEANLITERAL, NULL)) {
			next();
			return true;
		}
		return false;
	}

	public boolean p_selfType() {
		if (isToken(ID)) {
			if (isToken(COLON)) {
				next();
				assrt(p_type());
			}
		} else if (isToken(THIS)) {
			accept(COLON);
			assrt(p_type());
		} else {
			return false;
		}
		accept(FAT_ARROW);
		return true;
	}

	public boolean p_classParamClause() {
		if (isToken(LPAREN)) {
			next();
			p_classParams();
			accept(RPAREN);
			return true;
		}
		return false;
	}

	public boolean p_classParams() {
		if (p_classParam()) {
			while (isToken(COMMA)) {
				next();
				p_classParam();
			}
			return true;
		}
		return false;
	}

	public boolean p_classParamClausesRest() {
		if (isTokenPrefix(LPAREN, IMPLICIT)) {
			skip(2);
			p_classParams();
			return true;
		}
		return false;
	}

	public boolean p_classParamClauses() {
		if (p_classParamClausesRest()) {
			return true;
		}
		if (p_classParamClause()) {
			while (p_classParamClause()) {
			}
			p_classParamClausesRest();
			return true;
		}
		return false;
	}

	public boolean p_classTemplate() {
		p_classParents();
		p_templateBody();
		return true;
	}

	public boolean p_templateBody() {
		if (isToken(LCURL)) {
			next();
			p_selfType();
			while (!isToken(RCURL)) {
				p_templateStat();
			}
			accept(RCURL);
			return true;
		}
		return false;
	}

	public boolean p_classTemplateOpt() {
		if (isToken(EXTENDS)) {
			next();
			p_classParents();
			p_templateBody();
			return true;
		} else if (p_templateBody()) {
			return true;
		}
		return false;
	}

	public boolean p_traitTemplateOpt() {
		if (isToken(EXTENDS)) {
			next();
			p_classParents();
			p_templateBody();
			return true;
		} else if (p_templateBody()) {
			return true;
		}
		return false;
	}

	public boolean p_classDef(boolean isCase) {
		if (isToken(CLASS)) {
			next();
			p_ident();
			p_typeParamClause();
			p_accessModifier();
			p_classParamClauses();
			p_classTemplateOpt();
			return true;
		}
		return false;
	}

	public boolean p_objectDef(boolean isCase) {
		if (isToken(OBJECT)) {
			next();
			p_ident();
			p_classTemplateOpt();
			return true;
		}
		return false;
	}

	public boolean p_traitDef() {
		if (isToken(TRAIT)) {
			next();
			p_ident();
			p_typeParamClause();
			p_traitTemplateOpt();
			return true;
		}
		return false;
	}

	public boolean p_accessQualifier() {
		if (isToken(LBRACKET)) {
			next();
			acceptOneOf(ID, THIS);
			accept(RBRACKET);
			return true;
		}
		return false;
	}

	public boolean p_tmplDef() {
		boolean isCase = false;
		if (isToken(CASE)) {
			if (!isTokenLaOneOf(1, CLASS, OBJECT)) {
				return false;
			}
			isCase = true;
			next();
		}

		return p_traitDef() || p_objectDef(isCase) || p_classDef(isCase);
	}

	public boolean p_topStatement() {
		if (isToken(IMPORT)) {
			p_import();
		} else {
			p_modifiers();
			p_tmplDef();
		}
		return true;
	}

	public void p_topStatements() {
		while (token.kind != EOF) {
			p_topStatement();
		}
	}

	public void p_compilationUnit() {
		while (token.kind == PACKAGE) {
			_package();
		}
		p_topStatements();
	}
}
