(* Dynamic semantics *)

type ident = Id of string;;

(* syntax definitions, same as for the statis semantics *)

(* AST of expressions *)
type exp = StringLit of string | SetLit of exp_seq | Cat of exp*exp | Union of exp*exp | Intersect of exp*exp | In of exp*exp | Dim of exp | Add of exp*exp | Mul of exp*exp | And of exp*exp | Eq of exp*exp | Pair of exp*exp | Fst of exp | Snd of exp | Sign of exp | Not of exp | Num of int | Bool of bool | Var of ident
and
  exp_seq = SingleExp of exp | MoreExp of exp * exp_seq;;

(* AST of statements and sequence of statements, mutually recursive *)
type
  stmt = While of exp*stmt_seq | Assign of ident*exp | Dec of ident*exp | Print of exp | If of exp*stmt_seq | IfElse of exp*stmt_seq*stmt_seq
and
  stmt_seq = SingleStmt of stmt | MoreStmt of stmt * stmt_seq;;

(* AST of programs *)
type prog = Prog of stmt_seq;;

(* dynamic errors *)

exception ExpectingTypeError of string;;

exception UndefinedVariable of ident;;

(* environments *)

let empty_scope = [];; 

let starting_env = [empty_scope];; (* just the empty top-level scope *)

(* enter_scope : 'a list list -> 'a list list *)

let enter_scope env = empty_scope::env;; (* enters a new nested scope *)

let exit_scope = function (* removes the most nested scope, not needed in the static semantics *)
    _::env -> env
  | [] -> failwith "assertion error";; (* should never happen *)

(* variable look up *)

(* resolve : ident -> (ident * 'a) list list -> (ident * 'a) list *)

let rec resolve id = function
    scope::env -> if(List.mem_assoc id scope) then scope else resolve id env
  | [] -> raise (UndefinedVariable id);;

(* lookup : ident -> (ident * 'a) list list -> 'a *)

let lookup id env = List.assoc id (resolve id env);;

(* variable update, not needed in the static semantics *)

let rec update id info = function
    scope::env -> if(List.mem_assoc id scope) then ((id,info)::scope)::env else scope::update id info env
  | [] -> raise (UndefinedVariable id);;

(* variable declaration *)

(* dec : 'a -> 'b -> ('a * 'b) list list -> ('a * 'b) list list *)

let dec id info = function
    scope::env -> ((id,info)::scope)::env
  | [] -> failwith "assertion error";; (* should never happen *)


(* dynamic semantics *)

type value = Int of int | Bool of bool | Pair of value*value | String of string | Set of value list;;

type dynamic_env = (ident * value) list list;;

(* auxiliary functions *)

(* conversions *)

(* int : value -> int *)

let int = function
    Int i -> i |
    _ -> raise (ExpectingTypeError "int")

(* bool : value -> bool *)

let bool = function
    Bool b -> b |
    _ -> raise (ExpectingTypeError "bool")

(* bool : value -> string *)

let string = function
    String s -> s |
    _ -> raise (ExpectingTypeError "string")


(* pair : value -> value * value *)

let pair = function
    Pair (e1,e2) -> e1,e2 |
    _ -> raise (ExpectingTypeError "pair");;

(* fst : 'a * 'b -> 'a *)

let fst (v1,_) = v1;;

(* snd : 'a * 'b -> 'b *)

let snd (_,v2) = v2;;

(* print functions *)

(* print : value -> unit *)
(* print_list : value list -> unit *)

let rec print = function
    Int i -> print_int(i) 
  | Bool false -> print_string("false")
  | Bool true -> print_string("true")
  | Pair(v1,v2) -> let _=print_string("[") and _=print(v1) and _=print_string(" ,") and _=print(v1) in print_string("]")
  | String s -> print_string s
  | Set l -> let _=print_string("{") and _=print_list l in print_string("}")

and

  print_list = function
    [] -> print_string ""
  | [v] -> print v
  | hd::tl -> let _=print hd and _=print_string ", " in print_list tl;;

(* println : value -> unit *)

let println v = let _=print v in print_newline();;

(* functions on sets *)

(* for simplicity set values are represented with lists with no repetitions *)
(* important: this choice is independent from the possible Java implementation *)

(* member : value -> value -> value *)

let member v = function
    Set l -> Bool(List.mem v l)
  | _ -> raise (ExpectingTypeError "set");;

(* insertion with no repetitions *)

(* ins_no_dup : 'a list -> 'a -> 'a list *)

let rec ins_no_dup l v = match l with
    [] -> [v]
  | hd::tl -> hd::if(v=hd) then tl else ins_no_dup tl v;;

(* add : value -> value -> value *)

let add v = function
    Set l -> Set (ins_no_dup l v)
  | _ -> raise (ExpectingTypeError "set");;

(* merge : 'a list -> 'a list -> 'a list *)

let merge l1 l2 = List.fold_left ins_no_dup l1 l2;;

(* union : value -> value -> value *)

let union s1 s2 = match s1,s2 with
    Set l1, Set l2 -> Set (merge l1 l2)
  | _ -> raise (ExpectingTypeError "set");;

(* both : 'a list -> 'a list -> 'a list *)

let both l1 l2 = List.filter (fun el -> List.mem el l1) l2;;

(* intersect : value -> value -> value *)

let intersect s1 s2 = match s1,s2 with
    Set l1, Set l2 -> Set (both l1 l2)
  | _ -> raise (ExpectingTypeError "set");;

(* mutually recursive
   evExp : dynamic_env -> exp -> value 
   evExpSeq : dynamic_env -> exp_seq -> value
*)

let rec evExp env =function
    Cat(exp1,exp2) -> String(string(evExp env exp1) ^ string(evExp env exp2))
  | SetLit exp_seq -> evExpSeq env exp_seq
  | Intersect(exp1, exp2) -> intersect (evExp env exp1) (evExp env exp2)
  | Union(exp1, exp2) -> union (evExp env exp1) (evExp env exp2)
  | In(exp1,exp2) -> member (evExp env exp1) (evExp env exp2)
  | Dim(exp) -> (match evExp env exp with String s -> Int(String.length s) | Set l -> Int(List.length l) | _ -> raise (ExpectingTypeError "string or set"))           
  | StringLit s -> String s
  | Add(exp1,exp2) -> Int(int(evExp env exp1)+int(evExp env exp2))
  | Mul(exp1,exp2) -> Int(int(evExp env exp1)*int(evExp env exp2))
  | And(exp1,exp2) -> Bool(bool(evExp env exp1)&&bool(evExp env exp2))
  | Eq(exp1,exp2) -> Bool(evExp env exp1=evExp env exp2)
  | Pair(exp1,exp2) -> Pair(evExp env exp1,evExp env exp2)
  | Fst exp -> fst (pair (evExp env exp))
  | Snd exp -> snd (pair (evExp env exp))
  | Sign exp -> Int(-int(evExp env exp))
  | Not exp -> Bool(not (bool(evExp env exp)))
  | Num i -> Int i
  | Bool b -> Bool b
  | Var id -> lookup id env

and
(* a sequence of expressions evaluates into set values *)
  evExpSeq env=function 
    SingleExp exp -> Set [evExp env exp]
  | MoreExp(exp,exp_seq) -> add (evExp env exp) (evExpSeq env exp_seq);;

(* mutually recursive
   evStmt : dynamic_env -> stmt -> dynamic_env
   evStmtSeq : dynamic_env -> stmt_seq -> dynamic_env
*)

let rec evStmt env=function
    While(exp,stmt_seq) as stmt -> if bool(evExp env exp) then let env2=exit_scope(evStmtSeq (enter_scope env) stmt_seq) in evStmt env2 stmt else env 
  | Assign(id,exp) -> update id (evExp env exp) env
  | Dec(id,exp) -> dec id (evExp env exp) env
  | Print exp -> let _=println (evExp env exp) in env
  | If(exp,stmt_seq) ->
      if bool(evExp env exp) then  
        let env2=enter_scope env in exit_scope(evStmtSeq env2 stmt_seq) (* note the difference with the static semantics *)
      else env
  | IfElse(exp,stmt_seq1,stmt_seq2) ->
      let env2=enter_scope env in
        if bool(evExp env exp) then  
          exit_scope(evStmtSeq env2 stmt_seq1) (* note the difference with the static semantics *)
        else 
          exit_scope(evStmtSeq env2 stmt_seq2) (* note the difference with the static semantics *)

and 

  evStmtSeq env=function 
    SingleStmt stmt -> evStmt env stmt
  | MoreStmt(stmt,stmt_seq) -> evStmtSeq (evStmt env stmt) stmt_seq;;

(* evProg : prog -> unit *)

let evProg = function Prog stmt_seq -> let _=evStmtSeq starting_env stmt_seq in ();;

(* some simple tests with the dynamic semantics *)

let stmt1=Dec(Id "x",StringLit "Hello ");;

let stmt2=Assign(Id "x",Cat(Var(Id "x"),StringLit "world"));;

let stmt3=Print(Add(Num 1,Dim(Var(Id "x"))));;

let prog1=Prog(MoreStmt(stmt1,(MoreStmt(stmt2,SingleStmt stmt3))));;

evProg prog1;;

let stmt1=Dec(Id "x",SetLit(MoreExp(Num 1,MoreExp(Num 2,SingleExp(Num 2)))));;

let stmt2=Print(In(Num 3,Var(Id "x")));;

let stmt3=Print(Add(Num 1,Dim(Union(Var(Id "x"),Var(Id "x")))));;

let stmt4 = Assign(Id "x",Intersect(Var(Id "x"),SetLit(MoreExp(Num 0,SingleExp(Num 2)))));;

let stmt5 = Print(Var(Id "x"));;

let prog2=Prog(MoreStmt(stmt1,(MoreStmt(stmt2,MoreStmt(stmt3,MoreStmt(stmt4,SingleStmt stmt5))))));;

evProg prog2;;

let stmt1=Dec(Id "x",Num 10);;

let stmt2=While(Not(Eq(Var(Id "x"),Num 0)),MoreStmt(Assign(Id "x",Add(Var (Id "x"),Sign(Num 1))),MoreStmt(Print(Var(Id "x")),MoreStmt(Dec(Id "x",Bool true),SingleStmt(Print(And(Var(Id "x"),Bool true)))))));;

let stmt3=Assign(Id "x",Num 2);;

let prog3=Prog(MoreStmt(stmt1,(MoreStmt(stmt2,SingleStmt stmt3))));;

evProg prog3;;
