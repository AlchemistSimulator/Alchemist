/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import it.unibo.alchemist.exceptions.BiochemistryParseException;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslLexer;
import it.unibo.alchemist.model.internal.biochemistry.dsl.BiochemistrydslParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for domain specific language of the incarnation.
 */
class TestDSL {

    private static BiochemistrydslParser getParser(final String reaction) {
        final CharStream in = CharStreams.fromString(reaction);
        final BiochemistrydslLexer lexer = new BiochemistrydslLexer(in);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final BiochemistrydslParser parser = new BiochemistrydslParser(tokens);
        parser.removeErrorListeners(); // default error listener log on the console and not throwing any error
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(
                final Recognizer<?, ?> recognizer,
                final Object offendingSymbol,
                final int line,
                final int charPositionInLine,
                final String msg,
                final RecognitionException e
            ) {
                throw new BiochemistryParseException("Error in " + reaction + "\n" + msg);
            }
            @Override
            public void reportAmbiguity(
                final Parser recognizer,
                final DFA dfa,
                final int startIndex,
                final int stopIndex,
                final boolean exact,
                final BitSet ambigAlts,
                final ATNConfigSet configs
            ) {
                throw new BiochemistryParseException("report ambiguity");
            }
            @Override
            public void reportAttemptingFullContext(
                final Parser recognizer,
                final DFA dfa,
                final int startIndex,
                final int stopIndex,
                final BitSet conflictingAlts,
                final ATNConfigSet configs
            ) {
                throw new BiochemistryParseException("report attempting full context");
            }

            @Override
            public void reportContextSensitivity(
                final Parser recognizer,
                final DFA dfa,
                final int startIndex,
                final int stopIndex,
                final int prediction,
                final ATNConfigSet configs
            ) {
                throw new BiochemistryParseException("report context sensitivity");
            }
        });
        return parser;
    }

    private BiochemistrydslParser.ReactionContext testValidReaction(final String reaction) {
        return getParser(reaction).reaction();
    }

    private void testInvalidReaction(final String reaction) {
        assertThrows(BiochemistryParseException.class, () -> testValidReaction(reaction));
    }

    /**
     * Tests.
     */
    @Test
    void test() { // NOPMD on NCSS line count
        /* ********* Valid reactions **********/
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
        testValidReaction("[MolA + MolB] --> [2 MolC]");
        testValidReaction("[] --> [BrownianMove(10)]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction()]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction(1, 2, 3)]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction(a, bb, ccc, 1, 2)]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [3 MolA + MyCustomAction()]");
        testValidReaction("[3 MolA in cell] + [2 MolB in env] --> [MyCustomAction1() + MyCustomAction2(1.5)]");
        testValidReaction("[] --> [2 MolA in neighbor] if MyCustomCondition()");
        testValidReaction("[5 MolA + MolB in cell] --> [2 MolB in env] if MyCustomCondition(1, 2, 3)");
        testValidReaction("[5 MolA + MolB in cell] --> [2 MolB in env] if MyCustomCondition(a, bb, ccc)");
        testValidReaction("[5 MolA] --> [2 MolB in env] if MyCustomCondition1(), MyCustomCondition2(a, 1.2)");
        testValidReaction("[3 MolA in env] --> [2 MolB] reaction type MyCustomReaction()");
        testValidReaction("[3 MolA in env] --> [2 MolB] reaction type MyCustomReaction(1, 2, 3)");
        testValidReaction("[3 MolA in env] --> [2 MolB] reaction type MyCustomReaction(a, bb, ccc)");
        testValidReaction("[3 MolA + 2.5 MolB] + [ MolC + 1.1 MolD in cell ] + [MolE in env] + "
                + "[MolF+2 MolG in env] --> [3 MolH in neighbor] + "
                + "[2 MolI + 4.8 MolL in cell] + [MyCustomAction1() + MyCustomAction2(1.2, abcde, 5)] "
                + "if MyCustomCondition(1, 2, 3), MyCustomCondition2() reaction type MyCustomReaction(2.2, abcde, 5)");
        // create junction (control of the presence of molecules in the left side of the reaction isn't done here)
        testValidReaction("[MolA] + [MolC in neighbor] --> [junction MolA-MolC]");
        testValidReaction("[MolC  in neighbor] + [MolA] --> [junction MolA-MolC]");
        testValidReaction("[MolC in neighbor] + [MolE in env] + [MolA] --> [junction MolA-MolC]");
        testValidReaction(
                "[MolZ in env] + [4 MolT + MolR in neighbor] + [MolA+ MolB in env] + [MolH + 8 MolF] + [MolS in env]"
                        + " --> [junction MolH:5MolF-4MolT:MolR]"
        );
        testValidReaction("[MolA] + [MolB in neighbor] --> [MolC in env] + [junction MolA-MolB]");
        testValidReaction("[MolA] + [MolB in neighbor] --> [junction MolA-MolB] + [MolB]");
        testValidReaction("[MolA + 3 MolB] + [2 MolC in neighbor] + [MolD in env] --> [junction MolA:2MolB-2MolC]");
        testValidReaction(
                "[2 MolC in neighbor] + [MolD in env] + [MolA + 3 MolB]  --> [MolS] + [junction MolA:3MolB-MolC] + [MolE]"
        );
        // use junctions
        testValidReaction("[junction A-B] --> []");
        testValidReaction("[3 MolA + MolB] + [junction A-B] --> []");
        testValidReaction("[3 MolA + MolB] + [junction A-B] --> [junction A-B]");
        testValidReaction("[3 MolA + MolB] + [junction A-B] + [MolC + 8 MolD in env] + [junction B-C] --> [junction A-B]");
        testValidReaction(
                "[3 MolA + MolB] + [junction A-B] --> [MolA] + [MolB in env] + [junction A-B] + [MolE] + [MolR in neighbor]"
        );
        testValidReaction("[3 MolA + MolB] + [junction A-B] --> [junction A-B] + [MolA] + [MolC]");
        testValidReaction("[junction A-B] --> [MolB in env] + [MolB]");
        testValidReaction("[junction A-B] + [MolA in env] + [junction C-D] --> [MolB in env] + [junction C-D]");
        testValidReaction("[junction A-B] + [MolA in env] + [junction C-D] --> [MolB in env] + [junction C-D]");


        /* ********* Invalid reactions **********/
        testInvalidReaction("");
        testInvalidReaction("-->");
        testInvalidReaction("--> []");
        testInvalidReaction("[] -->");
        testInvalidReaction("[] -> []"); // -> instead of -->
        testInvalidReaction("[] ---> []"); // ---> instead of -->
        testInvalidReaction("MolA --> MolB"); // conditions and actions must be surrounded by []
        // in molecule name only numbers and letters from a to z (upper and lower case) are allowed
        testInvalidReaction("[Mol@] --> [MolA]");
        testInvalidReaction("[@°§à°è] --> [^'ì+?§ù]");
        testInvalidReaction("[-3 MolA] --> [MolB]"); // concentration must be positive
        testInvalidReaction("[3 MolA + 2 MolB in env] --> [-5 MolB in cell]"); // concentration must be positive
        // if concentration is a real number use dot to separate integer part to decimals
        testInvalidReaction("[2,1 MolA] --> [MolB]");
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
        // neighbour instead of neighbor (we use American spelling)
        testInvalidReaction("[MolA in neighbour] --> [MolB]");
        testInvalidReaction("[MolA in Env] --> [MolB]"); // Env instead of env
        testInvalidReaction("[MolA in environment] --> [MolB]"); // environment instead of env
        testInvalidReaction("[MolA cell] --> [MolB in env]"); // cell instead of in cell
        testInvalidReaction("[MolA in env in cell] --> [MolB]"); // duplicated context declaration
        // duplicated context declaration (use [.. in cell] + [.. in neighbor])
        testInvalidReaction("[MolA in cell + MolB in neighbor] --> [MolB]");
        testInvalidReaction("[MolA + MolB] --> MyCustomAction()"); // custom actions must be surrounded by []
        testInvalidReaction("[MolA + MolB] --> [MyCustomAction(), MyCustomAction()]"); // , instead of +
        // custom conditions must have brackets (MyCustomCondition())
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition");
        testInvalidReaction("MyCustomCondition() --> [MolB]");
        testInvalidReaction("[MyCustomCondition()] --> [MolB]");
        testInvalidReaction("[MolB] if MyCustomCondition()");
        // use , to separate custom conditions
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition() + MyCustomCondition2()");
        // only alphabetic letters or numbers can be passed as parameters
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition(@)");
        testInvalidReaction("[MolA] --> [MolB] if MyCustomCondition() if MyCustomCondition2()"); // duplicate if
        // custom reactions must have brackets (MyCustomReaction())
        testInvalidReaction("[MolA] --> [MolB] reaction type MyCustomReaction");
        testInvalidReaction("[MolA] --> [MolB] reaction MyCustomReaction()"); // reaction instead reaction type
        // Multiple reaction types
        testInvalidReaction("[3 MolA in env] "
                + "--> [2 MolB] reaction type MyCustomReaction1(), MyCustomReaction2(1.2, qwerty)");
        //junctions
        testInvalidReaction("[Junction A-B] --> [MolA in env]"); // Junction instead of junction
        // correct syntax for junction is biomolecule(:biomolecule)*-biomolecule(:biomolecule)*
        testInvalidReaction("[junction AB] --> [MolB]");
        testInvalidReaction("[junction A,B-B] --> [MolB]"); // the biomolecule separator is ':'
        testInvalidReaction("[3 junction A-B] --> [2 MolA]"); // junctions cannot have concentration
        testInvalidReaction("[junction A-B in env] --> [2 MolA]"); // junctions cannot have context
        testInvalidReaction("[junction A-B in cell] --> [2 MolA]");
        testInvalidReaction("[junction A-B in neighbor] --> [2 MolA]");
        // the creation of a junction requires at least 1 molecule in cell and 1 molecule in neighbor
        testInvalidReaction("[MolA] --> [junction A-B]");
        testInvalidReaction("[MolA in neighbor] --> [junction A-B]");
        testInvalidReaction("[MolA in neighbor] + [MolB in env] --> [junction A-B]");
        testInvalidReaction("[MolA] + [MolB in env] --> [junction A-B]");
        // the creation of a junction cannot have 2 cell contexts. Use [MolA + MolC].
        testInvalidReaction("[MolA] + [MolB in neighbor] + [MolC in cell] --> [junction A-B]");
        // the creation of a junction cannot have 2 neighbor contexts. Use [MolB + MolC in neighbor].
        testInvalidReaction("[MolA] + [MolB in neighbor] + [MolC in neighbor] --> [junction A-B]");
        // cannot create 2 junction at the same time. Use 2 reactions
        testInvalidReaction("[MolA] + [MolB in neighbor] --> [junction A-B] + [junction C-D]");
        testInvalidReaction("[MolA] + [MolB in neighbor] --> [junction A-B] + [MolF in env] + [junction C-D]");
    }
}
