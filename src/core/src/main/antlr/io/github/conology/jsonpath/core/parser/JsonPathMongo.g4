grammar JsonPathMongo;

@header {
package io.github.conology.jsonpath.core.parser;
}

restQuery: restBasicQuery EOF;
restOrQuery: restAndQuery ( '||' restAndQuery)* ;
restAndQuery: restBasicQuery ( '&&' restBasicQuery)*;
restBasicQuery: restExistenceQuery | restComparisonQuery;
restExistenceQuery: restShortRelativeQuery | relativeQuery;
restComparisonQuery:
    restShortRelativeQuery comparisonOperator literal
    | literal comparisonOperator restShortRelativeQuery
    ;
restShortRelativeQuery: restMemberSelector segment*;
restMemberSelector: SAFE_IDENTIFIER;

segment:
	memberNameShortHand
	| bracketedExpression
	;

memberNameShortHand: '.' SAFE_IDENTIFIER;
bracketedExpression: '[' filterSelector ']';
filterSelector: '?' logicalExpression;
logicalExpression: comparisonExpression;
existenceExpression: relativeQuery;
comparisonExpression:
    relativeQuery comparisonOperator literal
    | literal comparisonOperator relativeQuery
    ;
literal: INT | QUOTED_TEXT;
comparisonOperator: COMPARISON_OPERATOR;
relativeQuery: '@' segment+;

fragment SAFECODEPOINT: ~[["\\\u0000-\u001F];

fragment UNICODE: 'u' HEX HEX HEX HEX;

fragment HEX: [0-9a-fA-F];


COMPARISON_OPERATOR: '<' | '>' | '==' | '>=' | '<=' | '!=';
SAFE_IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
ESCAPESEQUENCE: '\\' (["\\/bfnrt] | UNICODE);
QUOTED_TEXT: '"' (ESCAPESEQUENCE | SAFECODEPOINT)* '"';
INT         : '0' | [1-9][0-9]* ;
WS  :   [ \t]+ -> skip ;