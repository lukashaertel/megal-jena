grammar Megal;
@header{package org.softlang.megal.grammar;}

model: 'model' ID ('(' STR ')')? (imp | stm)*;

imp: 'import' ID;
stm: ann* node node node;
ann: ANN ('(' node ')')?;

node: term | func | stms;
term: ID | SYM | STR ;
func: term '(' node (',' node)* ')';
stms: '{' stm* '}';

ANN: '@' ID;

ID: [a-zA-Z_] [a-zA-Z0-9_]* ('.' ID)?;
SYM: [:|]? '->' | '<' | ':' | '=';
STR: '\'' ('\\' . | ~'\'')* '\'';

WS: [ \r\t\n]+ -> channel(HIDDEN);
BC: '/*' .*? '*/' -> channel(HIDDEN);
LC: '//' ~[\r\n]* -> channel(HIDDEN);