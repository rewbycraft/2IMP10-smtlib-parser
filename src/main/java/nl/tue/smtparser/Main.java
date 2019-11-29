package nl.tue.smtparser;

import nl.tue.smtparser.smtlib.SMTLIBv2Lexer;
import nl.tue.smtparser.smtlib.SMTLIBv2Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Scanner;
import java.util.Stack;

public class Main {
	public static SMTLIBv2Parser parseString(String str) {
		SMTLIBv2Lexer lexer = new SMTLIBv2Lexer(CharStreams.fromString(str));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		return new SMTLIBv2Parser(tokens);
	}
	
	public static String simplifyFormula(String str) {
		SMTLIBv2Parser parser = parseString(str);
		ParseTree tree = parser.command();
		CommandListener listener = new CommandListener(parser);
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(listener, tree);
		PrinterListener listener2 = new PrinterListener();
		walker.walk(listener2, tree);
		
		return listener2.toString();
	}
	
	public static String formatFormula(String str, boolean latex) {
		SMTLIBv2Parser parser = parseString(str);
		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTree tree = parser.term();
		
		StackifyListener listener = new StackifyListener();
		walker.walk(listener, tree);
		
		Stack<String> stack = new Stack<>();
		
		String lbracket = "(";
		String rbracket = ")";
		if (!latex)
		{
			lbracket = "(";
			rbracket = ")";
		}
		
		while (!listener.stack.empty()) {
			StackifyListener.Symbol item = listener.stack.pop();
			
			if (item.getNumOperands() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(lbracket);
				
				if (item.getNumOperands() > 1) {
					sb.append(stack.pop());
					sb.append(' ');
				}
				
				if (latex)
					sb.append(item.getLatex());
				else
					sb.append(item.toString());
				sb.append(' ');
				
				sb.append(stack.pop());
				
				sb.append(rbracket);
				
				stack.push(sb.toString());
			} else {
				if (latex)
					stack.push(item.getLatex());
				else
					stack.push(item.toString());
			}
		}
		
		
		return stack.pop();
	}
	
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.length() == 0) {
				System.out.println();
			} else if (line.startsWith("(assert")) {
				System.out.println("## Predicate");
				System.out.println("```");
				System.out.println(line);
				String simplified = simplifyFormula(line);
				String formatted = formatFormula(simplified, true);
				String text = formatFormula(simplified, false);
				System.out.println("```");
				System.out.println("```");
				System.out.println(text);
				System.out.println("```");
				System.out.println("$$");
				System.out.println(formatted);
				System.out.println("$$");
			} else if (line.startsWith("(")) {
				System.out.println("```");
				System.out.println(line);
				System.out.println("```");
			} else {
				System.out.println("# " + line);
			}
		}
	}
}
