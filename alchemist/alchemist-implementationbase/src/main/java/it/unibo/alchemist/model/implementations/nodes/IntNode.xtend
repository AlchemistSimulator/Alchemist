package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Environment

class IntNode extends AbstractNode<Integer> {
	
	new(Environment<?, ?> env) {
		super(env)
	}
	
	override protected createT() { 0 }
	
}