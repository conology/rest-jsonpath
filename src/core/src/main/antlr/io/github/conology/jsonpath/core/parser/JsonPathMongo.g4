grammar JsonPathMongo;

@header {
package io.github.conology.jsonpath.core.parser;
}

restQuery: restAndQuery EOF;
restOrQuery: restAndQuery ( '||' restAndQuery)* ;
restAndQuery: restBasicQuery ( '&&' restBasicQuery)*;
restBasicQuery: restExistenceQuery | restComparisonQuery | restRegexQuery;
restExistenceQuery: restShortRelativeQuery | relativeQuery;
restComparisonQuery:
    restShortRelativeQuery comparisonOperator literal
    | restShortRelativeQuery regexComparison
    | literal comparisonOperator restShortRelativeQuery
    ;
restRegexQuery: relativeQuery REGEX_COMPARISON_OPERATOR REGULAR_EXPRESSION;
restShortRelativeQuery: restMemberSelector segment*;
restMemberSelector: SAFE_IDENTIFIER;

segment:
	memberNameShortHand
	| bracketedExpression
	;

memberNameShortHand: '.' SAFE_IDENTIFIER;
bracketedExpression: '[' (filterSelector|WILDCARD_SELECTOR|INT) ']';
filterSelector: '?' andExpression;
andExpression: logicalExpression ( '&&' logicalExpression)*;
logicalExpression: comparisonExpression | existenceExpression;
existenceExpression: relativeQuery;
comparisonExpression:
    relativeQuery comparisonOperator literal
    | relativeQuery regexComparison
    | literal comparisonOperator relativeQuery
    ;
regexComparison: REGEX_COMPARISON_OPERATOR REGULAR_EXPRESSION;
literal: INT | QUOTED_TEXT;
comparisonOperator: COMPARISON_OPERATOR;
relativeQuery: '@' segment+;

fragment QUOTED_SAFECODEPOINT: ~[["\\\u0000-\u001F];
fragment UNICODE: 'u' HEX HEX HEX HEX;

fragment HEX: [0-9a-fA-F];


WILDCARD_SELECTOR: '*';
COMPARISON_OPERATOR: '<' | '>' | '==' | '>=' | '<=' | '!=';
REGEX_COMPARISON_OPERATOR: '=~';
REGULAR_EXPRESSION: '/' (~[/\r\n] | '\\/' )+ '/' [gimscxdtu]*;
SAFE_IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
ESCAPESEQUENCE: '\\' (["\\/bfnrt] | UNICODE);
QUOTED_TEXT: '"' (ESCAPESEQUENCE | QUOTED_SAFECODEPOINT)* '"';
INT         : '0' | [1-9][0-9]* ;
WS  :   [ \t]+ -> skip ;