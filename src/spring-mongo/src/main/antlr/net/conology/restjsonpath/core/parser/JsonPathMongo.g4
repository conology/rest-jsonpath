grammar JsonPathMongo;

@header {
package net.conology.restjsonpath.core.parser;
}

restQueries: restQuery (',' restQuery)* EOF;
restQuery: restOrQuery;
restOrQuery: restAndQuery ( '||' restAndQuery)*;
restAndQuery: restBasicQuery ( '&&' restBasicQuery)*;
restBasicQuery: restParenthesesQuery | restExistenceQuery | restComparisonQuery;
restParenthesesQuery: '(' restOrQuery ')'; 
restExistenceQuery: restRelativeQuery;
restComparisonQuery:
    restRelativeQuery comparisonOperator literal
    | literal comparisonOperator restRelativeQuery
    | restRelativeQuery regexComparison
    ;
restRelativeQuery: simplifiedRelativeQuery | relativeQuery;
simplifiedRelativeQuery: (memberNameShortHand|bracketedExpression) segment*;

relativeQuery: CURRENT_NODE_IDENTIFIER segment+;
segment:
	'.' memberNameShortHand
	| bracketedExpression
	;
memberNameShortHand: SAFE_IDENTIFIER;
bracketedExpression: '[' (QUOTED_TEXT|filterSelector|WILDCARD_SELECTOR|INT) ']';
filterSelector: '?' orExpression;
orExpression: andExpression ('||' andExpression)*; 
andExpression: logicalExpression ( '&&' logicalExpression)*;
logicalExpression: paranthesesExpression | comparisonExpression | existenceExpression;
paranthesesExpression: '(' orExpression ')';
existenceExpression: relativeQuery;
comparisonExpression:
    relativeQuery comparisonOperator literal
    | literal comparisonOperator relativeQuery
    | relativeQuery regexComparison
    ;
regexComparison: REGEX_COMPARISON_OPERATOR REGULAR_EXPRESSION;
literal: INT | FLOAT | QUOTED_TEXT | TRUE | FALSE | NULL;
comparisonOperator: COMPARISON_OPERATOR;

fragment QUOTED_SAFECODEPOINT: ~[["\\\u0000-\u001F];
fragment UNICODE: 'u' HEX HEX HEX HEX;

fragment HEX: [0-9a-fA-F];

NULL: 'null';
FALSE: 'false';
TRUE: 'true';
INT: '0' | '-'?[1-9][0-9]* ;
FLOAT: INT '.' [0-9]+;
CURRENT_NODE_IDENTIFIER: '@';
WILDCARD_SELECTOR: '*';
COMPARISON_OPERATOR: '<' | '>' | '==' | '>=' | '<=' | '!=';
REGEX_COMPARISON_OPERATOR: '=~' | '!~';
REGULAR_EXPRESSION: '/' (~[/\r\n] | '\\/' )+ '/' [gimscxdtu]*;
SAFE_IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
ESCAPESEQUENCE: '\\' (["\\/bfnrt] | UNICODE);
QUOTED_TEXT: '"' (ESCAPESEQUENCE | QUOTED_SAFECODEPOINT)* '"';
WS  :   [ \t]+ -> skip ;