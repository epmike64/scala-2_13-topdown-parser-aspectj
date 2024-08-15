package com.fdk.compiler.parser;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class ParserAspect {

	private int level = 0;

	@Around("execution(* com.fdk.compiler.parser.FParser.p_*(..))")
	public Object employeeAroundAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		level++;
		for (int i = 0; i < level; i++) {
			System.out.print("  ");
		}
		System.out.println(" > " + proceedingJoinPoint.getSignature().getName());
		Object value = proceedingJoinPoint.proceed();
		for (int i = 0; i < level; i++) {
			System.out.print("  ");
		}
		if(value != null && value instanceof Boolean && value == Boolean.TRUE) {
			System.out.println("    < " + proceedingJoinPoint.getSignature().getName() + " TRUE");
		} else {
			System.out.println("    < " + proceedingJoinPoint.getSignature().getName() + " " + value);
		}
		level--;
		return value;
	}
}
