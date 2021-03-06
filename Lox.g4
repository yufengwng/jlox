grammar Lox;

program        : declaration* EOF ;

declaration    : class_decl | fun_decl | var_decl | statement ;
class_decl     : 'class' IDENTIFIER ( '<' IDENTIFIER )? '{' function* '}' ;
fun_decl       : 'fun' function ;
var_decl       : 'var' IDENTIFIER ( '=' expression )? ';' ;

statement      : expr_stmt | for_stmt | if_stmt | print_stmt | return_stmt | while_stmt | block ;
expr_stmt      : expression ';' ;
for_stmt       : 'for' '(' ( var_decl | expr_stmt | ';' ) expression? ';' expression? ')' statement ;
if_stmt        : 'if' '(' expression ')' statement ( 'else' statement )? ;
print_stmt     : 'print' expression ';' ;
return_stmt    : 'return' expression? ';' ;
while_stmt     : 'while' '(' expression ')' statement ;
block          : '{' declaration* '}' ;

function       : IDENTIFIER '(' parameters? ')' block ;
parameters     : IDENTIFIER ( ',' IDENTIFIER )* ;
arguments      : expression ( ',' expression )* ;

expression     : assignment ;
assignment     : ( call '.' )? IDENTIFIER '=' assignment | logical_or ;
logical_or     : logical_and ( 'or' logical_and )* ;
logical_and    : equality ( 'and' equality )* ;
equality       : comparison ( ( '!=' | '==' ) comparison )* ;
comparison     : addition ( ( '<' | '<=' | '>' | '>=' ) addition )* ;
addition       : multiplication ( ( '+' | '-' ) multiplication )* ;
multiplication : unary ( ( '*' | '/' ) unary )* ;
unary          : ( '!' | '-' ) unary | call ;
call           : primary ( '(' arguments? ')' | '.' IDENTIFIER )* ;

primary        : 'nil' | 'true' | 'false' | 'this'
               | NUMBER | STRING | IDENTIFIER
               | '(' expression ')'
               | 'super' '.' IDENTIFIER ;

NUMBER         : DIGIT+ ( '.' DIGIT+ )? ;
STRING         : '"' .*? '"' ;
IDENTIFIER     : ALPHA ( ALPHA | DIGIT )* ;
ALPHA          : 'a'..'z' | 'A'..'Z' | '_' ;
DIGIT          : '0'..'9' ;
COMMENT        : '//' .*? '\r'? '\n' -> skip ;
WHITESPACE     : [ \t\r\n]+          -> skip ;
