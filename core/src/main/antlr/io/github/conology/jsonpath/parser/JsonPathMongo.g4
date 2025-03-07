grammar JsonPathMongo;

@header {
package io.github.goatfryed.json_path_mongo;
}

mongoQueries: mongoQuery (',' mongoQuery)*;

mongoQuery: startSegment segment*;

startSegment:
	memberNameShortHand
	| '$.' memberNameShortHand
	;

segment:
	'.' memberNameShortHand
	| bracketedFilterSelector
	;

memberNameShortHand: SAFE_IDENTIFIER;
bracketedFilterSelector: '[' '?' logicalExpression ']';
logicalExpression: comparisonExpression;
comparisonExpression: relativeQuery comparisonOperator literal;
literal: INT | quotedText;
quotedText: '"' quotedTextInner '"';
quotedTextInner: (SAFE_IDENTIFIER | SAFE_QUOTED_TEXT | ESCAPESEQUENCE | SPECIAL_CHAR )* ;
comparisonOperator: '<' | '>' | '==' | '>=' | '<=' | '!=';
relativeQuery: '@.' memberNameShortHand segment+;

fragment SAFECODEPOINT: ~[[?@."><=!\\\u0000-\u001F];

fragment UNICODE: 'u' HEX HEX HEX HEX;

fragment HEX: [0-9a-fA-F];


SPECIAL_CHAR: [[?@."><=!];
SAFE_IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
SAFE_QUOTED_TEXT: SAFECODEPOINT+;
ESCAPESEQUENCE: '\\' (["\\/bfnrt] | UNICODE);
INT         : '0' | [1-9][0-9]* ;
WS  :   [ \t]+ -> skip ;