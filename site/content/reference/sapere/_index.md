+++
title = "SAPERE Incarnation"
weight = 5
tags = ["sapere", "lsa", "live semantic annotation", "tuple space", "tuple centre"]
summary = "Reference API for the SAPERE Incarnation."
+++

## LSAs

LSAs, similarly to Prolog terms, support [unification and substitution](http://archive.is/oLSpq):
it is possible to create tuple templates,
match them against sets of ground tuples,
and obtain a matching ground tuple as result.

A tuple argument is considered a variable if it begins with an uppercase letter.
Additionally, it is possible to discard some matches by expressing constraints on values.

### Ground LSA syntax

```ebnf
GroundLSA ::= GroundArgument (',' GroundArgument)*
GroundArgument ::= Number | Atom | Set
Atom ::= [a-z]([a-z]|[A-Z]|[0-9])*
Number ::= [0-9]+('.'[0-9]*)
Set ::= '[' ((Atom | Number)';')* ']'
```


### LSA Syntax

```ebnf
LSA ::= '{' GroundLSA | TemplateLSA '}'
TemplateLSA ::= Argument (',' Argument)*
Argument ::= GroundArgument | Variable | Constraint
Variable ::= [A-Z]([a-z]|[A-Z]|[0-9])*
Constraint ::= 'def:' Variable Operation
Operation ::= ('>'|'>'|'='|'!=') Number | 'add ' Variable | 'del ' Variable
```

