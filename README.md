# cx-interpreter

**CX** is a simple _JavaScript_-like language parser and interpreter written in _Java_.

It uses simple read-ahead parsing algorithm that can be easily customized to support additional language structured: in the current version a _SQL_ queries were implemented with := operator.

The interpreter is context/closures based similar to _LISP_ that gives the name of the language: **CX**,
where 

**CX** stands for **CONTEXT**.

## Currently supported _C/C#/Java/JavaScript_ semantics
**CX** is **case sensitive** and all statements **MUST** be completed with **semicolon** (C-like statements).
```js
// [] optional terms
if(condition)true_statement [ else false_statement ] 
for(initialization; expression; iteration) statement 
for(values_and_keys : list_or_map) statement 
while(condition) statement 
do statement while(condition) 
switch(statement){case value: statement ... [ default: statement ] } 
break [condition]     
continue [condition]
return [expression]  
var variable_name// for variable definition in the current context 
function(args) statement // for lambda function 
function name(args) statement // for named function 
[1,2,"test",'string'] //for array definition 
new {a:4, str: "str", foo:function(){return 0;} } // for objects/maps definition
variable = {a:4, str: "str"} // for objects/maps definition

try statement catch(Exception_name) statement [ finally statement ] // supported if: parser.supportTryCatchThrow = true; 
throw Exception_name // supported if: parser.supportTryCatchThrow = true;
variable := SQL statement; //supported if: parser.supportSQLEscaping = true;
```

##Operator precedence
|*Operator Type*     |*Operator*                            |*Associativity*|
|--------------------|--------------------------------------|---------------|
|Expression Operators| () [] . expr++ expr--                |left-to-right  |
|Unary Operators     | + - ! ~ ++expr --expr                |right-to-left  |
|Binary Operators    | \* / %                               |left-to-right  |
|                    | + -                                  |               |
|                    | << >> >>>                            |               |
|                    | < > <= >=                            |               |
|                    | == !=                                |               |
|                    | & \| ^                               |               |
|                    | &&   \|\|                            |               |
|Ternary Operator    | ? :                                  |right-to-left  |
|Assignment Operators| = += -= *= /= %= >>= <<= &= ^= \|=   |right-to-left  |

*Note*: Unit test were added for clearing the semantics


##Variables
Variables have no type attached, and any value can be stored in any variable. Variables declared with **var** are defined directly in the current context/block scoping/hoisting, and will hide any existing variables with the same name in the parent contexts. 
```js
var i = 2; 
{ 
    var i = 4; // i in the current context will hide the i in the parent context 
    j = 5; // j will be created in the current context 
} 
// i = 2 
// j = null
```


##Data types
in **CX** we assume the following as equivalent common bottom element in the language semantic:
```js
null == false == 0 == 0.0 == "" == ''
```

###null
Any used uninitialized variable is null

###Boolean
Boolean data type constants are true and false. they are cast to numbers as 1 and 0 respectively.

###Number
Internal implementation uses only Long and Double presentation for numbers. 
```js
345; // an "integer", although there is only one numeric type in JavaScript 
34.5; // a floating-point number 
-3.45e2; // another floating-point, equivalent to -345.0 
0xFF; // a hexadecimal integer equal to 255 
0xab; // a hexadecimal digits represented by the letters A-F may be upper or lower case
```

###String
A string in **CX** is a immutable multi-line sequence of characters. Strings can be created directly by placing the series of characters between double or single quotes. Opening quotes inside the string **must be escaped**. 
```js
var helloWorld= "Hello, world!"; 
var anotherString = 'So Long, and Thanks for All the Fish'; 
var multiLineString = " this is a multi line 'string' 
with all characters between 
the opening quotes (\' or \") ";
```

You can access individual characters within a string using: 
```js
var helloWorld= "Hello, world!"; 
var h = helloWorld[0]; // ='H' 0-based index 
var r = helloWorld[-3]; // ='l' reverse indexing from the end of the string 
var n = helloWorld[100];// =null anything outside the length of the string is null
var m = helloWorld[-100];// =null anything outside the length of the string is null
```

###Arrays
Arrays are designed to store values indexed by integer keys. 
```js
var array = ['string', 1, 2.3, , 4 ]; // empty elements are null 
array += 42; // add element to the array 
array = array + 'another string'; // add another element 
b = array[0]; // 'string' 
b = array[-2]; // 42 - reverse indexing 
b = array[100]; // null - out of index
```

###Objects (Maps)
Object in **CX** is just a map with all keys that are Strings.
If a function belongs to a object - then the object is a parent context to the function execution (see Functions).

Objects are defined by:
```js
var obj = new {}; // obj1 is an empty object 
var obj1 = new { a: 0; setA: function(value){a = value;} }; 
var objEmpty = {}; // objEmpty is an empty object - new can be omitted after assignment 
var arrayWithObjects = [new {}, new {}]; //Objects are implemented and behave as map.

var obj = new { a: 0; str: 'string', 42: 'meaning of life' }; 
t = obj.a; // t = 0 
t = obj[str]; // t = 'string' 
t = obj['str']; // t = 'string' 

var id = 42;
t = obj[""+id]; // t = 'meaning of life' 
t = obj[id]; // t = null - all keys are strings

obj['message'] = 'string'; // define new key 'message' with value 'string' 
obj.message = 'string'; // equivalent to the previous one
t = obj3['message']; // t = string 
t = obj3.message; // t = string
```

##Function
Every function is a predefined closure of statements with finite dynamic variables represented by the function parameters. 
```js
// variable referencing to a anonymous function (lambda) with two parameters:
var add1 = function(x, y) { return x + y; }; 
// named functions does not require closing column because they are declaration not a statement
function add2(x, y) { return x + y; }

a = add1(3,4); // a = 7 
b = add2(2,2); // b = 4

// function translate() context is the instance of the obj variable 
var obj = { base: 5, function translate(a){return a + base;}};

a = obj.translate(4); // a = 9 
obj.base = 10; 
a = obj.translate(4); // a = 14
```

##Arithmetic operations

###unary arithmetic operations
**CX** supports the following unary arithmetic operators:
```js
+ Unary conversion of parsable number to its absolute value 
- Unary negation (reverses the sign of a number) 
~ Unary bit complement of a parsable number to long 
++ Increment parsable number (can be prefix or postfix) else null 
-- Decrement parsable number (can be prefix or postfix) else null 
```
Examples :
```js
str = '-42'; 
val1 = +str; //val1 = 42 
n = -12.34; 
val2 = +n; // val2 = 12.34 
++str; // str = -41 
val3 = --'13'; // val3 = 12 
val4 = '13'++; // val4 = '13' 
val5 = ~~'-5'; // val5 = -5 
val6 = ~~'3.14'; // val6 = 3
```
###binary arithmetic operations
**CX** supports the following binary arithmetic operators: 
```js
+ Addition 
- Subtraction 
* Multiplication 
/ Division (returns a floating-point value) 
% Modulus (returns the remainder) 
```

###Operations result table

|*types*          |*null*       |*Bool* b   |*Number* b         |*String* b         |*Array* b          |*Object* b         |*else*|
|-----------------|-------------|--------------|-------------------|-------------------|-------------------|-------------------|------|
|**Bool** a +     | a           | a xor b      | a xor (b != 0)    | a xor notEmpty(b) | a xor notEmpty(b) | a xor notEmpty(b) | null |
|**Bool** a -     | a           | a xor b      | a xor (b != 0)    | a xor notEmpty(b) | a xor notEmpty(b) | a xor notEmpty(b) | null |
|**Bool** a \*    | a           | a and b      | a and (b != 0)    | a and notEmpty(b) | a and notEmpty(b) | a and notEmpty(b) | null |
|**Bool** a /     | a           | a or b       | a or (b != 0)     | a or notEmpty(b)  | a or notEmpty(b)  | a or notEmpty(b)  | null |
|**Bool** a %     | a           | null         | null              | null              | null              | null              | null |
|**Number** a +   | a           | a + b ? 1:0  | a + b             | a + (Number)b     | null              | null              | null |
|**Number** a -   | a           | a - b ? 1:0  | a - b             | a - (Number)b     | null              | null              | null |
|**Number** a \*  | a           | a \* b ? 1:0 | a \* b            | a \* (Number)b    | null              | null              | null |
|**Number** a /   | a           | a / b ? 1:0  | a / b             | a / (Number)b     | null              | null              | null |
|**Number** a %   | a           | a % b ? 1:0  | (long)a % (long)b | (long)a % (long)b | null              | null              | null |
|**String** a +   | a           | a +(String)b | a +(String)b      | a +(String)b      | a +(String)b      | a +(String)b      | null |
|**Array** a +    | a.add(null) | a.add(b)     | a.add(b)          | a.add(b)          | a.add(b)          | a.add(b)          | null |
|**else**         | null        | null         | null              | null              | null              | null              | null |

###bit operators
**CX** supports the following unary and binary bit operators: 
``` js
// Unary operator
~ Not - converts to Long and inverts the bits 

// Binary operator
& And - converts to Long and apply operation 
| Or  - converts to Long and apply operation 
^ Xor - converts to Long and apply operation

<<  Shift left - converts to Long and shift left with zero fill
>>  Shift right (sign-propagating) - converts to Long and copies of the leftmost bit (sign bit) are shifted in from the left.
>>> Shift right - converts to Long and shift right with zero fill. For positive numbers, >> and >>> yield the same result. 
```

###Assignments
**CX** supports the following assignments: 
```js
=  Assign 
+= Add and assign 
-= Subtract and assign 
*= Multiply and assign 
/= Divide and assign 
%= Modulus and assign 
```
All these operator assignments are converted to their long versions ( i.e. a += b; is executed as : a = a + b; )

###Comparisons
**CX** supports the following comparisons that produce Boolean result: 
```js
 ==  Equal 
 !=  Not equal
 >   Greater than
 >=  Greater than or equal to 
 <   Less than 
 <=  Less than or equal to 
```

###Logical operators
CX supports the following Logical operators: 
```js
!     Not/Negation // true if parameter is not: null, false, 0, 0.0, "", '', [], {} 
&&    logical And - converts to Boolean and apply operation 
||    logical Or - converts to Boolean and apply operation 
?:    Ternary operator as defined in C : result = condition ? expression : alternative; 
??    isNull operator as defined in C# : result = expression ?? alternative; 
```

Examples: 
```js
var a = true; 
var b = a ? "" : "otherwise"; // b = "" 
var c = b ?? 5; // c = 5 since b is empty string
```

##Control structures

###Compound statements
A pair of curly brackets { } and an enclosed sequence of statements constitute a compound statement, which can be used wherever a statement can be used.

###if...else
```js
if (expr) { 
    //statements; 
} [ else { 
    // else part is optional; not like the ? ternary operator 
    //statements; 
} ]
```

###Switch statement
```js
switch (expr) { 
    case SOMEVALUE:         //Strings literals and numbers can be used for the case values.
         //statements; 
         break; 
    case ANOTHERVALUE: 
         //statements; 
         break;
    default:               // optional
         //statements; 
         break;            // optional
} 
```

- **break** is optional; however, it is usually needed, since otherwise code execution will continue to the body of the next case block. 
- Add a break statement to the end of the last case as a precautionary measure, in case additional cases are added later. 
- Strings literals and numbers can be used for the case values. 
- Expressions can NOT be used instead of values. 
- case default: is optional. 
- Braces are required.

###For loop
The syntax of the **CX** for loop is as follows: 
```js
for (initial; condition; loop statement) { 
      /* statements will be executed every time the for{} loop cycles, while the condition is satisfied */
     // break 
     // continue 
}
```

###For in loop
The syntax of the **CX** for in loop is as follows: 
```js
for (variable : some_array_or_object) { 
    //variable = {each element of the array} or {each key in the object} 
    // break
    // continue 
} 
```
Iterates through all enumerable properties of an object. 
Iterates through all elements of an array from first to last.

###While loop
The syntax of the **CX** While loop is as follows: 
```js
while (condition) { 
      statement1; 
      statement2; 
      statement3; 
      ... 
      // break 
      // continue 
}
```
###Do ... while loop
The syntax of the **CX** Do ... while loop is as follows: 
```js
do { 
     statement1; 
     statement2; 
     statement3; 
     ... 
     // break 
     // continue 
} while (condition);
```

##Functions
A function is a combination of context with parent context to the current place of definition, statements sequence, (possibly empty) parameter list and optionally a given a name.
A function may define its local variables via var. 
Any used variable not defined in the function or anywhere in the context will be automatically defined as local variable.
```js
function gcd(A, B) { 
    var diff = A- B; 
    if (diff == 0) return A; 
    return diff > 0 ? gcd(B, diff) : gcd(A, -diff);
} 
var result = gcd(60, 40); // result = 20 
```
Functions are contexts with execution statements and may be assigned to other variables. 
```js
var mygcd = gcd; // mygcd is a reference to the same function as gcd. 
var result = mygcd(60, 40); // result = 20
```

If function exits without a return statement, the result of the function will be the execution context (the this of the function). 
```js
obj = { 
    r:5, 
    inc:function(){r++;}, // no return gives the obj instance 
    dec:function(){r--;}  // no return gives the obj instance 
}; 
obj.inc().inc().inc().dec().inc(); // chain calls using the function context 
r = obj.r; // r = 8
```

The number of arguments given when calling a function may not necessarily correspond to the number of arguments in the function definition; a named argument in the definition that does not have a matching argument in the call will have the value null. Within the function, the arguments may also be accessed through the **arguments** object; which provides access to all arguments using indices (e.g. arguments[0], arguments[1], ... arguments[n]), including those beyond the number of named arguments (since the arguments list has a .length property). 
```js
function argsCount(){return arguments.length;} 
var result1 = f(1,2,3,4); // result1 = 4 
var result2 = f(); // result2 = 0
```


##Object Inheritance (sort of)

In **CX** inheritance is done via: 
```js
var newObject = new oldObject {...};
```
All context content of the oldObject is flatten into the newObject: only the parents are merged into the new instance and functions' context are updated to be to the new object. 
```js
obj = { key:'value', setValue: function(value){ key = value; } }; 
obj.setValue('oldValue'); 
newObj = new obj{}; // a new instance of obj newObj.setValue('newValue');

key1 = obj.key; // key1 = 'oldValue' 
key2 = newObj.key; // key2 = 'newValue'

//In case we want to use the parent functions or variables: the following is possible but not recommended:
newObj.key = null; // this will remove the value in the newObj and will let the newObj to access the parent variable 
key3 = newObj.key; // key3 = 'oldValue' 
//The same is valid for the functions in case there is a need to use global functions from the parent.
```

##eval (expression)
eval (expression) Evaluates expression string parameter, which can include assignment statements. Variables local to functions can be referenced by the expression. 
```js
a = 5; eval('a++;'); // a = 6 
inc = eval('function _(x){return ++x;};'); 
a = inc(a); // a = 7
```

##Exception handling

**CX** includes a **try ... catch ... finally** exception handling statement to handle run-time errors. This functionality should be enabled in the parser via: **parser.supportTryCatchThrow = true;** 
```js
try { 
    // Statements in which exceptions might be thrown 
    throw ExceptionName(expression); // an exception with name "ExceptionName" and value = eval(expression)  will be thrown 
} catch(ExceptionName variableName) { 
    // Statements that execute in the event of an exception 
    // in current case variableName will contain the exception value 
} catch(variableName) { 
    // any internal exception will be caught here 
} finally { 
    // Statements that execute afterwards either way 
} 
```
Either the catch or the finally clause may be omitted. The catch arguments are required, it is the name of the exception that is thrown and the local variable that will contain the exception message.

Examples: 
```js
function x(){ 
    try{ 
         return 1; 
    }finally{ 
         return 2; 
    } 
}
r = x(); // r = 2 

try{ 
    throw MyException; 
}catch(MyException e){ 
    // e = null 
}

try{ 
    throw MyException(5); 
}catch(MyException e){ 
    // e = 5  
}
```

##Handlers
Handlers are custom implementation (like plugins or add-ons) that can be dynamically attached to a Context via method addHandler. All handlers needs to implement **cx.runtime.Handler** interface.
with the current **CX** interpreter there are several handlers already implemented:

###StringHandler

String handler provides the following methods:
```js
trim(); 
substring(start); 
substring(start,end); 
replace(from,to); 
indexOf(char); 
lastIndexOf(char); 
startsWith(string); 
endsWith(string); 
toLowerCase(); 
toUpperCase();
```
that can be applied to any string object. 

Examples:
```js
var str = ' trim '; 
str = str.trim(); // str = 'trim' 
str = 'smallCammelCase'.toLowerCase(); // str = 'smallcammelcase'
```

###DateHandler
Date Handler provides the following object generated by global functions: newDate() or newDate(string, format): 
```js
Date = { 
    year, month, day, hour, minute, second, millisecond;
    zone;
    time; // milliseconds after 1970-01-01 
} 
//global functions
newDate() // return new date object
newDate(string, format); // return new date object from a string representation.
formatDate(date, string_format) //return string representation for a date object.
```
Examples: 
```js
var date = newDate(); 
date.year=2013; 
date.month=07; 
date.day=08; 

var datestr = formatDate(date,'yyyy-MM-dd'); // datestr = '2013-07-08'
var date = newDate('08/07/2013','dd/MM/yyyy'); // creates a date object
var datestr = formatDate(date,'yyyy-MM-dd'); // datestr = '2013-07-08' 
```

###MathHandler
Math Handler provides the global object Math with functions: 
```js
Math { 
    random(); 
    abs(number); 
    ceil(number); 
    floor(number); 
    round(number); 
    sqrt(number); 
    max(number,...); 
    min(number,...); 
    isNaN(number); 
    parseInteger(string); 
    parseDouble(string); 
}
```
Examples: 
```js
var d = Math.round(0.53); // d = 1 
var d = Math.floor(0.23); // d = 0.0 
var d = Math.max(-1,0,1,2,4); // d = 4.0 
var d = Math.min(2,4,6,8,9); // d = 2.0 
var d = Math.parseInteger('2.3'); // d = 2
```

###ObjectHandler
Object Handler provides a wrapper around POJO objects. 
Example for an object:
```java
TestClass{ 
    public String method1(String value) {..} 
    public long method2(long value) {...} 
    public Object methodList(List<?> list, int i) {...} 
    public Object methodMap(Map<?, ?> map, Object key) {...} 
} 
```

TestClass instance is registered by:
```java
Context cx = new Context(); 
TestClass instance = new TestClass(); 
cx.addHandler(new ObjectHandler(instance, "obj"));
```

Then in the **CX** you may use the object like: 
```js
var str = obj.method1('5'); 
obj.method2(5); 
obj.methodList([1,2,3],1); 
obj.methodMap({a:1, b:2,c:3},'b');
```

###DatabaseHandler
Database Handler provides a wrapper around JDBC implementations and is usefull for the SQL inlined syntax that parser supports. 
Provided methods are: 
```js
// if the registered handler is with name Database then 
var db = Database.create(); // create JDBC instance 
var db = Database.create(connectionString); // create JDBC instance

db.setProperty(key,value); // may be used before open/connect if JDBC driver requires properties 
db.setConnectionString = "new connection string"; // may be used

// the following four methods are initializing the connection 
// and return true if everything is ok 
// otherwize the error is in db.error 
db.open(); 
db.open(connectionstring); 
db.connect(); 
db.connect(connectionString);

// when db connection is ok the following methods can be used 
db.execute(sql); 
db.execute(sql,  function callback(parameters, that, match, the, select, columns) {...} ); 
db.commit(); 
db.rollback();

// to close the connection use 
db.close();
```

Database handler can be added to the Context by: 
```js
Class.forName("org.sqlite.JDBC"); // load JDBC driver to register connection string handler 
Context cx = new Context(); 
cx.addHandler(new DatabaseHandler("Database")); // register database handler under name "Database"
```

To enable SQL syntax set **parser.supportSQLEscaping = true;**. Then in the **CX** you may use the object like: 
```js
var db = Database.create('jdbc:sqlite:test_db.sqlite'); 
try{
    if(!db.connect()){
        throw Error("connect fails: " + db.error); 
    }

    sql := CREATE TABLE IF NOT EXISTS 'TestTable' ( 'number' INT , 'string' VARCHAR ); 
    if(!db.execute(sql)){ 
        throw Error("Create table fails: " + db.error); 
    }

    number = 42;
    string = 'just a string';
    sql := INSERT INTO 'TestTable' ( 'number', 'string' ) VALUES (number, string);
    if(!db.execute(sql)){
        throw Error("insert fails: " + db.error);
    }

    sql := SELECT 'number', 'string' FROM 'TestTable';
    var result = db.execute(sql, 
        function(id, name){ 
            // parameters should match the SELECT
            // do here what you need to do
        }
    );
    if(!result){
        throw Error("select fails: " + db.error);
    }
    if(!db.commit()){
        throw Error("commit fails: " + db.error);
    }
    if(!db.rollback()){
        throw Error("rollback fails: " + db.error);
    }
    if(!db.close()){
        throw Error("close fails: " + db.error);
    }
}catch(errorMessage){
}
```
