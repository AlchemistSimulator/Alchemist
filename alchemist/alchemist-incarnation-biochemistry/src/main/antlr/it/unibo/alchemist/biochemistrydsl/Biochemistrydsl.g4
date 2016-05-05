grammar Biochemistrydsl;

reaction
    : biochemicalReaction 
    | createJunction
    | junctionReaction
    ;

biochemicalReaction:
    BiochemicalReactionLeft=biochemicalReactionLeft
    '-->'
    BiochemicalReactionRight=biochemicalReactionRight
    (CustomConditions=customConditions)?
    (CustomReactionType=customReactionType)?
    ;

createJunction: 
    CreateJunctionLeft=createJunctionLeft
    '-->'
    CreateJunctionRight=createJunctionRight
    (CustomConditions=customConditions)?
    (CustomReactionType=customReactionType)?
    ;

junctionReaction:
    JunctionReactionLeft=junctionReactionLeft
    '-->'
    JunctionReactionRight=junctionReactionRight
    (CustomConditions=customConditions)?
    (CustomReactionType=customReactionType)?
    ;

javaConstructor:
    JavaClass=javaClass '(' (javaArgList=argList)? ')'
    ;

javaClass:
    (LITERAL '.')* LITERAL
    ;
    
 argList
    : args+=arg (',' args+=arg)*
    ;

arg
    : doubleArg=decimal
    | literalArg=LITERAL
    ;   

createJunctionLeft
    :   ((biochemicalReactionLeftContexts+=biochemicalReactionLeftContext '+' )*
        createJunctionCellConditions=createJunctionCellCondition '+' 
        (biochemicalReactionLeftContexts+=biochemicalReactionLeftContext '+' )*
        createJunctionNeighborConditions=createJunctionNeighborCondition
        ('+' biochemicalReactionLeftContexts+=biochemicalReactionLeftContext)*)
    |   ((biochemicalReactionLeftContexts+=biochemicalReactionLeftContext '+' )*
        createJunctionNeighborConditions=createJunctionNeighborCondition '+' 
        (biochemicalReactionLeftContexts+=biochemicalReactionLeftContext '+' )*
        createJunctionCellConditions=createJunctionCellCondition
        ('+' biochemicalReactionLeftContexts+=biochemicalReactionLeftContext)*)
    ;

createJunctionCellCondition: 
    '[' elems+=biomolecule ('+' elems+=biomolecule)* ('in' 'cell')? ']'
    ;

createJunctionNeighborCondition: 
    '[' elems+=biomolecule ('+' elems+=biomolecule)* 'in' 'neighbor' ']'
    ;

createJunctionRight: 
    (biochemicalReactionRightContexts+=biochemicalReactionRightContext '+')*
    CreateJunctionJunction=createJunctionJunction
    ('+' biochemicalReactionRightContexts+=biochemicalReactionRightContext)*
    ;
    
createJunctionJunction
    : '[' Junction=junction ']'
    ;
    
junctionReactionLeft: 
    (biochemicalReactionLeftContexts+=biochemicalReactionLeftContext '+')*
    JunctionReactionJunctionConditions+=junctionReactionJunctionCondition
    ('+' (biochemicalReactionLeftContexts+=biochemicalReactionLeftContext | JunctionReactionJunctionConditions+=junctionReactionJunctionCondition))*
    ;
    
junctionReactionRight
    :   (biochemicalReactionRightContext | junctionReactionJunction) 
        ('+' (biochemicalReactionRightContext | junctionReactionJunction))* 
    ;
    
junctionReactionJunctionCondition
    : '[' Junction=junction ']'
    ;
    
junctionReactionJunction
    : '[' Junction=junction ']'
    ;
    

biochemicalReactionLeft:
    biochemicalReactionLeftContexts+=biochemicalReactionLeftContext
    ('+' biochemicalReactionLeftContexts+=biochemicalReactionLeftContext)*
    ;

biochemicalReactionRight:
    biochemicalReactionRightContexts+=biochemicalReactionRightContext
    ('+' biochemicalReactionRightContexts+=biochemicalReactionRightContext)*
    ;

customConditions:
    'if' conditions+=customCondition (',' conditions+=customCondition)*
    ;

customReactionType:
    'reaction' 'type' JavaConstructor=javaConstructor
    ;

biochemicalReactionLeftContext:
    inCell=biochemicalReactionLeftInCellContext | inEnv=biochemicalReactionLeftInEnvContext | inNeighbor=biochemicalReactionLeftInNeighborContext
    ;

biochemicalReactionLeftInCellContext:
    '[' ( elems+=biomolecule ('+' elems+=biomolecule)* )? ('in' 'cell')? ']'
    ;

biochemicalReactionLeftInEnvContext:
    '[' ( elems+=biomolecule ('+' elems+=biomolecule)* )? 'in' 'env' ']'
    ;

biochemicalReactionLeftInNeighborContext:
    '[' ( elems+=biomolecule ('+' elems+=biomolecule)* )? 'in' 'neighbor' ']'
    ;

biochemicalReactionRightContext:
    inCell=biochemicalReactionRightInCellContext | inEnv=biochemicalReactionRightInEnvContext | inNeighbor=biochemicalReactionRightInNeighborContext
    ;

biochemicalReactionRightInCellContext:
    '[' ( elems+=biochemicalReactionRightElem ('+' elems+=biochemicalReactionRightElem)* )? ('in' 'cell')? ']'
    ;

biochemicalReactionRightInEnvContext:
    '[' ( elems+=biochemicalReactionRightElem ('+' elems+=biochemicalReactionRightElem)* )? 'in' 'env' ']'
    ;

biochemicalReactionRightInNeighborContext:
    '[' ( elems+=biochemicalReactionRightElem ('+' elems+=biochemicalReactionRightElem)* )? 'in' 'neighbor' ']'
    ;

biochemicalReactionRightElem:
    Biomolecule=biomolecule | JavaConstructor=javaConstructor
    ;

customCondition:
    JavaConstructor=javaConstructor
    ;

junction:
    'junction' junctionLeft '-' junctionRight
    ;

junctionLeft
    : biomolecule (':' biomolecule)*
    ;
    
junctionRight
    : biomolecule (':' biomolecule)*
    ;

biomolecule:
    (Concentration=concentration)? name=LITERAL
    ;

concentration:
    doubleConc=POSDOUBLE
    ;

decimal : 
    ('-')? POSDOUBLE
    ;

POSDOUBLE : 
    [0-9]+ ('.' [0-9]*)?
    ;

LITERAL: 
    ([a-zA-Z]) ([a-zA-Z0-9] | '_')*
    ;

WS: 
    [ \t\r\n]+ -> skip 
    ;

// handle characters which failed to match any other token
ErrorCharacter: 
    . 
    ;
