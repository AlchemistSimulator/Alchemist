grammar Biochemistrydsl;

reaction: (
    biochemicalReaction
    | createJunction
    | junctionReaction
    ) EOF;

biochemicalReaction:
    biochemicalReactionLeft
    '-->'
    biochemicalReactionRight
    customConditions?
    customReactionType?
    ;

createJunction: 
    createJunctionLeft
    '-->'
    createJunctionRight
    customConditions?
    customReactionType?
    ;

junctionReaction:
    junctionReactionLeft
    '-->'
    junctionReactionRight
    customConditions?
    customReactionType?
    ;

javaConstructor
    : javaClass '(' (argList)? ')'
    ;

javaClass:
    (LITERAL '.')* LITERAL
    ;
    
 argList
    : arg (',' arg)*
    ;

arg
    : decimal
    | LITERAL
    ;   

createJunctionLeft
    :   ((biochemicalReactionLeftContext '+' )*
        biochemicalReactionLeftInCellContext '+' 
        (biochemicalReactionLeftContext '+' )*
        biochemicalReactionLeftInNeighborContext
        ('+' biochemicalReactionLeftContext)*)
    |   ((biochemicalReactionLeftContext '+' )*
        biochemicalReactionLeftInNeighborContext '+' 
        (biochemicalReactionLeftContext '+' )*
        biochemicalReactionLeftInCellContext
        ('+' biochemicalReactionLeftContext)*)
    ;

createJunctionRight: 
    (biochemicalReactionRightContext '+')*
    createJunctionJunction
    ('+' biochemicalReactionRightContext)*
    ;
    
createJunctionJunction
    : '[' junction ']'
    ;
    
junctionReactionLeft: 
    (biochemicalReactionLeftContext '+')*
    junctionReactionJunctionCondition
    ('+' (biochemicalReactionLeftContext | junctionReactionJunctionCondition))*
    ;
    
junctionReactionRight
    :   (biochemicalReactionRightContext | junctionReactionJunction) 
        ('+' (biochemicalReactionRightContext | junctionReactionJunction))* 
    ;
    
junctionReactionJunctionCondition
    : '[' junction ']'
    ;
    
junctionReactionJunction
    : '[' junction ']'
    ;
    

biochemicalReactionLeft:
    biochemicalReactionLeftContext
    ('+' biochemicalReactionLeftContext)*
    ;

biochemicalReactionRight:
    biochemicalReactionRightContext
    ('+' biochemicalReactionRightContext)*
    ;

customConditions:
    'if' customCondition (',' customCondition)*
    ;

customReactionType:
    'reaction' 'type' javaConstructor
    ;

biochemicalReactionLeftContext
    : biochemicalReactionLeftInCellContext 
    | biochemicalReactionLeftInEnvContext 
    | biochemicalReactionLeftInNeighborContext
    ;

biochemicalReactionLeftInCellContext:
    '[' ( biomolecule ('+' biomolecule)* )? ('in' 'cell')? ']'
    ;

biochemicalReactionLeftInEnvContext:
    '[' ( biomolecule ('+' biomolecule)* )? 'in' 'env' ']'
    ;

biochemicalReactionLeftInNeighborContext:
    '[' ( biomolecule ('+' biomolecule)* )? 'in' 'neighbor' ']'
    ;

biochemicalReactionRightContext
    : biochemicalReactionRightInCellContext 
    | biochemicalReactionRightInEnvContext 
    | biochemicalReactionRightInNeighborContext
    ;

biochemicalReactionRightInCellContext:
    '[' ( biochemicalReactionRightElem ('+' biochemicalReactionRightElem)* )? ('in' 'cell')? ']'
    ;

biochemicalReactionRightInEnvContext:
    '[' ( biochemicalReactionRightElem ('+' biochemicalReactionRightElem)* )? 'in' 'env' ']'
    ;

biochemicalReactionRightInNeighborContext:
    '[' ( biochemicalReactionRightElem ('+' biochemicalReactionRightElem)* )? 'in' 'neighbor' ']'
    ;

biochemicalReactionRightElem
    : biomolecule 
    | javaConstructor
    ;

customCondition
    : javaConstructor
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
    (concentration)? name=LITERAL
    ;

concentration
    : POSDOUBLE
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
