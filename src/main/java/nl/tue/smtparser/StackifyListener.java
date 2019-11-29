package nl.tue.smtparser;

import nl.tue.smtparser.smtlib.SMTLIBv2BaseListener;
import nl.tue.smtparser.smtlib.SMTLIBv2Parser;

import java.util.*;

public class StackifyListener extends SMTLIBv2BaseListener {
	public Stack<Symbol> stack = new Stack<>();
	
	public static abstract class Symbol {
		public abstract String getLatex();
		public abstract String toString();
		public abstract int getNumOperands();
	}
	
	public static class SingleOperatorSymbol extends Symbol {
		private final String operator;
		private final static Map<String, String> singleOperators = new HashMap<String, String>() {{
			put("not", "\\neg");
		}};
		
		public static boolean isValid(String str) {
			return singleOperators.containsKey(str);
		}
		
		public SingleOperatorSymbol(String operator) {
			this.operator = operator;
		}
		
		@Override
		public String getLatex() {
			return singleOperators.get(operator);
		}
		
		@Override
		public int getNumOperands() {
			return 1;
		}
		
		@Override
		public String toString() {
			return operator;
		}
	}
	
	public static class DualOperatorSymbol extends Symbol {
		private final String operator;
		private final static Map<String, String> dualOperators = new HashMap<String, String>() {{
			put("=", "=");
			put("and", "\\wedge");
			put("or", "\\vee");
			put("*", "\\cdot");
			put("+", "+");
			put("<<", "\\ll");
		}};
		
		public static boolean isValid(String str) {
			return dualOperators.containsKey(str);
		}
		
		public DualOperatorSymbol(String operator) {
			this.operator = operator;
		}
		
		@Override
		public String getLatex() {
			return dualOperators.get(operator);
		}
		
		@Override
		public int getNumOperands() {
			return 2;
		}
		
		@Override
		public String toString() {
			return operator;
		}
	}
	
	public static class ZeroOperatorSymbol extends Symbol {
		private final String operator;
		private final static Map<String, String> zeroOperators = new HashMap<String, String>() {{
			put("true", "\\mathrm{true}");
			put("false", "\\mathrm{false}");
		}};
		
		public static boolean isValid(String str) {
			return zeroOperators.containsKey(str);
		}
		
		public ZeroOperatorSymbol(String operator) {
			this.operator = operator;
		}
		
		@Override
		public String getLatex() {
			return zeroOperators.get(operator);
		}
		
		@Override
		public int getNumOperands() {
			return 0;
		}
		
		@Override
		public String toString() {
			return operator;
		}
	}
	
	public static class QuotedSymbol extends Symbol {
		private final String operator;
		
		public static boolean isValid(String str) {
			return true;
		}
		
		public QuotedSymbol(String operator) {
			this.operator = operator;
		}
		
		@Override
		public String getLatex() {
			return String.format("{\\texttt{%s}}", operator.substring(1, operator.length()-1));
		}
		
		@Override
		public int getNumOperands() {
			return 0;
		}
		
		@Override
		public String toString() {
			return operator;
		}
	}
	
	public static class RawSymbol extends Symbol {
		private final String operator;
		
		public static boolean isValid(String str) {
			return true;
		}
		
		public RawSymbol(String operator) {
			this.operator = operator;
		}
		
		@Override
		public String getLatex() {
			return operator;
		}
		
		@Override
		public int getNumOperands() {
			return 0;
		}
		
		@Override
		public String toString() {
			return operator;
		}
	}
	
	@Override
	public void enterSimpleSymbol(SMTLIBv2Parser.SimpleSymbolContext ctx) {
		if (DualOperatorSymbol.isValid(ctx.getText())) {
			stack.push(new DualOperatorSymbol(ctx.getText()));
		}
		else if (SingleOperatorSymbol.isValid(ctx.getText())) {
			stack.push(new SingleOperatorSymbol(ctx.getText()));
		}
		else if (ZeroOperatorSymbol.isValid(ctx.getText())) {
			stack.push(new ZeroOperatorSymbol(ctx.getText()));
		}
		else {
			stack.push(new RawSymbol(ctx.getText()));
		}
	}
	
	@Override
	public void enterQuotedSymbol(SMTLIBv2Parser.QuotedSymbolContext ctx) {
		stack.push(new QuotedSymbol(ctx.getText()));
	}
	
	@Override
	public void enterNumeral(SMTLIBv2Parser.NumeralContext ctx) {
		stack.push(new RawSymbol(ctx.getText()));
	}
}
