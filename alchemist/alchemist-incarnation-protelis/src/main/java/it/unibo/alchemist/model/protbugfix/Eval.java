/*******************************************************************************
 * Copyright (C) 2014, 2015, Danilo Pianini and contributors
 * listed in the project's build.gradle or pom.xml file.
 *
 * This file is part of Protelis, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE.txt in this project's top directory.
 *******************************************************************************/
package it.unibo.alchemist.model.protbugfix;

import org.protelis.lang.interpreter.AnnotatedTree;
import org.protelis.lang.interpreter.impl.AbstractAnnotatedTree;
import org.protelis.vm.ExecutionContext;
import org.protelis.vm.ProtelisProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluate a Protelis sub-program.
 */
public class Eval extends AbstractAnnotatedTree<Object> {

    private static final long serialVersionUID = 8811510896686579514L;
    private static final Logger L = LoggerFactory.getLogger(Eval.class);
    private static final byte DYN_CODE_INDEX = -1;

    /**
     * @param arg
     *            argument whose annotation will be used as a string
     *            representing a program
     */
    public Eval(final AnnotatedTree<?> arg) {
        super(arg);
    }

    @Override
    public Eval copy() {
        return new Eval(deepCopyBranches().get(0));
    }

    @Override
    public void eval(final ExecutionContext context) {
        projectAndEval(context);
        final String program = getBranch(0).getAnnotation().toString();
        try {
            final ProtelisProgram result = ProtelisLoader.parseAnonymousModule(program);
            context.newCallStackFrame(DYN_CODE_INDEX);
            context.putMultipleVariables(result.getNamedFunctions());
            result.compute(context);
            setAnnotation(result.getCurrentValue());
            context.returnFromCallFrame();
        } catch (IllegalArgumentException e) {
            L.error("Non parse-able program", e);
            throw new IllegalStateException("The following program can't be parsed:\n" + program, e);
        }
    }

    @Override
    protected void asString(final StringBuilder sb, final int i) {
        sb.append("eval(\n");
        getBranch(0).toString(sb, i + 1);
        sb.append(')');
    }

}
