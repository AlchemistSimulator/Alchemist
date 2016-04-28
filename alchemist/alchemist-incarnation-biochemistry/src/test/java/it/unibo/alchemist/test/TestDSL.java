package it.unibo.alchemist.test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.junit.Test;

import it.unibo.alchemist.biochemistrydsl.BiochemistrydslLexer;
import it.unibo.alchemist.biochemistrydsl.BiochemistrydslParser;

/**
 * Tests for domain specific language of the incarnation.
 */
public class TestDSL {

    private static BiochemistrydslParser getParser(final String reaction) {
        final ANTLRInputStream in = new ANTLRInputStream(reaction);
        final BiochemistrydslLexer lexer = new BiochemistrydslLexer(in);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final BiochemistrydslParser parser = new BiochemistrydslParser(tokens);
        parser.removeErrorListeners(); // default error listener log on the console and not throwing any error
        parser.addErrorListener(new MyAntlrErrorListener());
        return parser;
    }

    private void testValidReaction(final String reaction) {
        try {
            getParser(reaction).explicitCellReaction();
        } catch (RuntimeException e) {
            fail();
        }
    }

    private void testInvalidReaction(final String reaction) {
        try {
            getParser(reaction).explicitCellReaction();
            fail();
        } catch (RuntimeException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * Tests.
     */
    @Test
    public void test() {
        // Valid reactions 
        testValidReaction("[] --> []");
        testValidReaction("[a] --> []");
        testValidReaction("[] --> [b]");
        testValidReaction("[a] --> [b]");
        testValidReaction("[a]--> [b]");
        testValidReaction("[a] -->[b]");
        testValidReaction("[a]-->[b]");
        testValidReaction("[ a]-->[b]");
        testValidReaction("[a]-->[b ]");
        testValidReaction("[ a ]-->[ b ]");
        testValidReaction("[MolA] --> [MolB]");
        testValidReaction("[3 MolA] --> [2 MolB]");
        testValidReaction("[3MolA] --> [2MolB]"); // concentration CAN be adjacent to molecule name
        testValidReaction("[3.5MolA] --> [0.8MolB]");
        testValidReaction("[2 MolA + MolB] --> [2 MolC]");
        testValidReaction("[2 MolA+MolB] --> [2 MolC+MolE]");
        testValidReaction("[5.5 MolA + 2.0 MolB + MolC] --> [1.1 MolD + MolE + 3 MolF]");
        testValidReaction("[5 MolA + 2 MolB in cell] + [MolC in neighbor] --> [2 MolD in cell] + [MolE in env]");
        testValidReaction("[MolA in env]+[MolB + 3MolC in cell] --> [MolD]");
        testValidReaction("[MolA] + [MolB + 3MolC in neighbor] --> [4 MolD in env]");
        testValidReaction("[MolA + MolB] --> [junction A-B]");
        testValidReaction("[3 MolA in cell] + [junction A-B] --> [MolA in neighbor]");
        testValidReaction("[3 MolA + MolB] + [junction A-B] --> [junction A-B] + [2 MolC in env]");
        testValidReaction("[MolA + MolB] -20-> [2 MolC]");
        testValidReaction("[junction A-B + MolA] -0.1-> [MolB in env]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction()]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction(1, 2, 3)]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction(a, bb, ccc, 1, 2)]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [3 MolA + MyCustomAction()]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction1() + MyCustomAction2(1.5)]");
        testValidReaction("[] --> [2 MolA in neighbor] if MyCustomCondition()");
        testValidReaction("[5 MolA + junction A-B in cell] --> [2 MolB in env] if MyCustomCondition(1, 2, 3)");
        testValidReaction("[5 MolA + junction A-B in cell] --> [2 MolB in env] if MyCustomCondition(a, bb, ccc)");
        testValidReaction("[5 MolA] --> [2 MolB in env] if MyCustomCondition1(), MyCustomCondition2(a, 1.2)");
        testValidReaction("[3 MolA in env] --> [2 MolB] reaction type MyCustomReaction()");
        testValidReaction("[3 MolA in env] --> [2 MolB] reaction type MyCustomReaction(1, 2, 3)");
        testValidReaction("[3 MolA in env] --> [2 MolB] reaction type MyCustomReaction(a, bb, ccc)");
        testValidReaction("[3 MolA in env] --> [2 MolB] reaction type MyCustomReaction1(), MyCustomReaction2(1.2, qwerty)");
        testValidReaction("[3 MolA + 2.5 MolB] + [ MolC + 1.1 MolD in cell ] + [MolE in env] + "
                + "[MolF+2 MolG in env]+[junction A-B] -5.5-> [junction C-D] + [3 MolH in neighbor] + "
                + "[2 MolI + 4.8 MolL in cell] + [MyCustomAction1() + MyCustomAction2(1.2, abcde, 5)] "
                + "if MyCustomCondition(1, 2, 3), MyCustomCondition2() reaction type MyCustomReaction(2.2, abcde, 5)");

        // Invalid reactions
        testInvalidReaction("");
        testInvalidReaction("-->");
        testInvalidReaction("--> []");
        testInvalidReaction("[] -->");
        testInvalidReaction("[] -> []"); // -> instead of -->
        testInvalidReaction("[] ---> []"); // ---> instead of -->
        testInvalidReaction("MolA --> MolB"); // conditions and actions must be surrounded by []
        testInvalidReaction("[Mol@] --> [MolA]"); // in molecule name only numbers and letters from a to z (upper and lower case) are allowed
        testInvalidReaction("[@°§à°è] --> [^'ì+?§ù]");
        testInvalidReaction("[-3 MolA] --> [MolB]"); // concentration must be positive
        testInvalidReaction("[3 MolA + 2 MolB in env] --> [-5 MolB in cell]"); // concentration must be positive
        testInvalidReaction("[2,1 MolA] --> [MolB]"); // if concentration is a real number use dot to separate integer part to decimals
        testInvalidReaction("[2 3molA] --> [MolB]"); // molecule name cannot begin with a number
        testInvalidReaction("[2 MolA * MolB] --> [MolC]"); // * instead of +
        testInvalidReaction("[MolA MolB] --> [MolC]");
        testInvalidReaction("[3 MolA + MolB] --> [MolC and MolD]"); // and instead of +
        testInvalidReaction("[3 MolA][2 MolB] --> [MolC]"); // + must be inserted between conditions
        testInvalidReaction("[3 MolA]+[2 MolB] --> 2[MolC + MolD]"); // concentration must be inside []
        testInvalidReaction("[3 MolA + MolB] --> [2(MolC + MolD)]"); // cannot group molecules
        testInvalidReaction("[MolA, MolB] --> [MolC]"); // , instead of +
        testInvalidReaction("[MolA in Cell] --> [MolB]"); // Cell instead of cell
        testInvalidReaction("[MolA in Neighbor] --> [MolB]"); // Neighbor instead of neighbor
        testInvalidReaction("[MolA in neighbour] --> [MolB]"); // neighbour instead of neighbor (we use American spelling)
        testInvalidReaction("[MolA in Env] --> [MolB]"); // Env instead of env
        testInvalidReaction("[MolA in environment] --> [MolB]"); // environment instead of env
        testInvalidReaction("[MolA cell] --> [MolB in env]"); // cell instead of in cell
        testInvalidReaction("[MolA in env in cell] --> [MolB]"); // duplicated context declaration
        testInvalidReaction("[MolA in cell + MolB in neighbor] --> [MolB]"); // duplicated context declaration (use [.. in cell] + [.. in neighbor])
        testInvalidReaction("[Junction A-B] --> [MolA in env]"); // Junction instead of junction
        testInvalidReaction("[junction AB] --> [MolB]"); // correct syntax for junction is LITERAL-LITERAL
        testInvalidReaction("[junction A-2B] --> [MolB]"); // LITERAL cannot begin with a number
        testInvalidReaction("[3 junction A-B] --> [2 MolA]"); // junctions cannot have concentration
        testInvalidReaction("[MolA + MolB] --> MyCustomAction()"); // custom actions must be surrounded by []
        testInvalidReaction("[MolA + MolB] --> [MyCustomAction(), MyCustomAction()]"); // , instead of +
        testInvalidReaction("[MolA] --5-> [MolB]"); // rate must be -rate->
        testInvalidReaction("[MolA] -5--> [MolB]");
        testInvalidReaction("[MolA] 5--> [MolB]");
        testInvalidReaction("[MolA] --5> [MolB]");
        testInvalidReaction("[MolA] -->5 [MolB]");
        testInvalidReaction("[MolA] -a-> [MolB]"); // rate must be a number
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition"); // custom conditions must have brackets (MyCustomCondition())
        testInvalidReaction("MyCustomCondition() --> [MolB]");
        testInvalidReaction("[MyCustomCondition()] --> [MolB]");
        testInvalidReaction("[MolB] if MyCustomCondition()");
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition() + MyCustomCondition2()"); // use , to separate custom conditions
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition(@)"); // only alphabetic letters or numbers can be passed as parameters
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition() if MyCustomCondition2()"); // duplicate if
        testInvalidReaction("[MolA] --> [MolB] reaction type MyCustomReaction"); // custom reactions must have brackets (MyCustomReaction())
        testInvalidReaction("[MolA] --> [MolB] reaction MyCustomReaction()"); // reaction instead reaction type
    }

    private static class MyAntlrErrorListener implements ANTLRErrorListener {

        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer,
                final Object offendingSymbol, 
                final int line,
                final int charPositionInLine,
                final String msg,
                final RecognitionException e) {
            throw new RuntimeException("syntax error");
        }
        @Override
        public void reportAmbiguity(final Parser recognizer, 
                final DFA dfa, 
                final int startIndex,
                final int stopIndex,
                final boolean exact,
                final BitSet ambigAlts, 
                final ATNConfigSet configs) {
            throw new RuntimeException("report ambiguity");
        }
        @Override
        public void reportAttemptingFullContext(final Parser recognizer,
                final DFA dfa,
                final int startIndex,
                final int stopIndex,
                final BitSet conflictingAlts,
                final ATNConfigSet configs) {
            throw new RuntimeException("report attempting full context");
        }

        @Override
        public void reportContextSensitivity(final Parser recognizer,
                final DFA dfa, 
                final int startIndex,
                final int stopIndex,
                final int prediction,
                final ATNConfigSet configs) {
            throw new RuntimeException("report context sensitivity");
        }
    }
}
