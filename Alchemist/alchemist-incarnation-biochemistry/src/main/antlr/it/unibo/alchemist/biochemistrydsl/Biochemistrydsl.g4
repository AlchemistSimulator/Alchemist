grammar Biochemistrydsl;

explicitCellReaction:
    CellReactionLeft=cellReactionLeft
    '-'(timeDistribution=javaConstructor | Rate=rate)? '->'
    CellReactionRight=cellReactionRight
    (CustomConditions=customConditions)?
    (CustomReactionType=customReactionType)?
;

javaConstructor:
    JavaClass=javaClass '(' (javaArgList=argList)? ')'
;

javaClass:
    (LITERAL '.')* LITERAL
;
    
 argList:
    args+=arg (',' args+=arg)*
;

arg:
    doubleArg=decimal
    | literalArg=LITERAL
;   

cellReactionLeft:
    cellReactionLeftContexts+=cellReactionLeftContext
    ('+' cellReactionLeftContexts+=cellReactionLeftContext)*
;

cellReactionRight:
    cellReactionRightContexts+=cellReactionRightContext
    ('+' cellReactionRightContexts+=cellReactionRightContext)*
;

customConditions:
    'if' conditions+=customCondition (',' conditions+=customCondition)*
;

customReactionType:
    'reaction' 'type' JavaConstructor=javaConstructor
;

cellReactionLeftContext:
    inCell=cellReactionLeftInCellContext | inEnv=cellReactionLeftInEnvContext | inNeighbor=cellReactionLeftInNeighborContext
;

cellReactionLeftInCellContext:
    '[' ( elems+=cellReactionLeftElem ('+' elems+=cellReactionLeftElem)* )? ('in' 'cell')? ']'
;

cellReactionLeftInEnvContext:
    '[' ( elems+=cellReactionLeftElem ('+' elems+=cellReactionLeftElem)* )? 'in' 'env' ']'
;

cellReactionLeftInNeighborContext:
    '[' ( elems+=cellReactionLeftElem ('+' elems+=cellReactionLeftElem)* )? 'in' 'neighbour' ']'
;

cellReactionLeftElem:
    Biomolecule=biomolecule | Junction=junction
;

cellReactionRightContext:
    inCell=cellReactionRightInCellContext | inEnv=cellReactionRightInEnvContext | inNeighbor=cellReactionRightInNeighborContext
;

cellReactionRightInCellContext:
    '[' ( elems+=cellReactionRightElem ('+' elems+=cellReactionRightElem)* )? ('in' 'cell')? ']'
;

cellReactionRightInEnvContext:
    '[' ( elems+=cellReactionRightElem ('+' elems+=cellReactionRightElem)* )? 'in' 'env' ']'
;

cellReactionRightInNeighborContext:
    '[' ( elems+=cellReactionRightElem ('+' elems+=cellReactionRightElem)* )? 'in' 'neighbour' ']'
;

cellReactionRightElem:
    Biomolecule=biomolecule | Junction=junction | JavaConstructor=javaConstructor
;

customCondition:
    JavaConstructor=javaConstructor
;

junction:
    'junction' cell1=LITERAL '-' cell2=LITERAL
;

biomolecule:
    (Concentration=concentration)? name=LITERAL
;

concentration:
    doubleConc=POSDOUBLE
;

rate:
    doubleRate=POSDOUBLE
;

decimal : 
    ('-')? POSDOUBLE
;

POSDOUBLE : 
    [0-9]+ ('.' [0-9]*)?
;

LITERAL: 
    ([a-zA-Z]) ([a-zA-Z0-9] | '_' | '-')*
;

WS : [ \t\r\n]+ -> skip ;
