package nl.tue.smtparser;

import nl.tue.smtparser.smtlib.SMTLIBv2BaseListener;
import nl.tue.smtparser.smtlib.SMTLIBv2Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrinterListener extends SMTLIBv2BaseListener {
	private final StringBuilder sb = new StringBuilder();
	private final Pattern p = Pattern.compile("bv([0-9]+)");
	
	@Override
	public void enterSimpleSymbol(SMTLIBv2Parser.SimpleSymbolContext ctx) {
		Matcher m = p.matcher(ctx.getText());
		if (m.find()) {
			sb.append(m.group(1));
			sb.append(' ');
			return;
		}
		
		if (ctx.getText().startsWith(".")) {
			throw new RuntimeException("NONEXPANDED VARIABLE");
		}
		
		switch (ctx.getText()) {
			case "bvmul":
				sb.append('*');
				break;
			case "bvadd":
				sb.append('+');
				break;
			case "bvshl":
				sb.append("<<");
				break;
			case "true":
			case "false":
			case "=":
			case "and":
			case "or":
			case "not":
				sb.append(ctx.getText());
				break;
			default:
				throw new RuntimeException("UNKNOWN OPERATOR: " + ctx.getText());
		}
		sb.append(' ');
	}
	
	@Override
	public void enterQuotedSymbol(SMTLIBv2Parser.QuotedSymbolContext ctx) {
		sb.append(ctx.getText());
		sb.append(' ');
	}
	
	@Override
	public void enterTerm(SMTLIBv2Parser.TermContext ctx) {
		if (ctx.getChildCount() > 1) {
			sb.append('(');
		}
	}
	
	@Override
	public void exitTerm(SMTLIBv2Parser.TermContext ctx) {
		if (ctx.getChildCount() > 1) {
			sb.append(')');
		}
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
}
