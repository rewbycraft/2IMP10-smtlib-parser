package nl.tue.smtparser;

import nl.tue.smtparser.smtlib.SMTLIBv2BaseListener;
import nl.tue.smtparser.smtlib.SMTLIBv2Parser;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;

public class CommandListener extends SMTLIBv2BaseListener {
	
	public final SMTLIBv2Parser parser;
	
	public CommandListener(SMTLIBv2Parser parser) {
		this.parser = parser;
	}
	
	@Override
	public void enterTerm(SMTLIBv2Parser.TermContext ctx) {
		ParseTreePattern p = parser.compileParseTreePattern("(let(<var_binding>)<term>)", SMTLIBv2Parser.RULE_term);
		ParseTreeMatch m = p.match(ctx);
		if ( m.succeeded() ) {
			ParseTreePattern varPattern = parser.compileParseTreePattern("(<simpleSymbol><term>)", SMTLIBv2Parser.RULE_var_binding);
			ParseTreeMatch varMatch = varPattern.match(m.get("var_binding"));
		
			if (varMatch.succeeded()) {
				SMTLIBv2Parser.TermContext target = (SMTLIBv2Parser.TermContext) m.get("term");
				SMTLIBv2Parser.SimpleSymbolContext lhs = (SMTLIBv2Parser.SimpleSymbolContext) varMatch.get("simpleSymbol");
				SMTLIBv2Parser.TermContext rhs = (SMTLIBv2Parser.TermContext) varMatch.get("term");
				
				//System.out.println("Found variable " + lhs.getText() + " with contents " + rhs.getText());
				
				VariableReplacer replacer = new VariableReplacer(lhs, rhs);
				ParseTreeWalker walker = new ParseTreeWalker();
				walker.walk(replacer, target);
				
				ctx.children.clear();
				ctx.children.add(target);
			}
		}
	}
	
	private class VariableReplacer extends SMTLIBv2BaseListener {
		private final SMTLIBv2Parser.SimpleSymbolContext targetSymbol;
		private final SMTLIBv2Parser.TermContext targetTerm;
		
		private VariableReplacer(SMTLIBv2Parser.SimpleSymbolContext targetSymbol, SMTLIBv2Parser.TermContext targetTerm) {
			this.targetSymbol = targetSymbol;
			this.targetTerm = targetTerm;
		}
		
		@Override
		public void enterTerm(SMTLIBv2Parser.TermContext ctx) {
			ParseTreePattern p = parser.compileParseTreePattern("<simpleSymbol>", SMTLIBv2Parser.RULE_term);
			ParseTreeMatch m = p.match(ctx);
			if ( m.succeeded() ) {
				if (m.get("simpleSymbol").getText().equals(targetSymbol.getText())) {
					//System.out.println("Replacing instance of " + targetSymbol.getText() + "...");
					ctx.children.clear();
					ctx.children.add(targetTerm);
				}
			}
		}
	}
}
