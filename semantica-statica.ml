(* Semantica statica del linguaggio del laboratorio 9 *)

type ident = Id of string;;

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

(* type constants and constructors *)
type typ = String | Set of typ | Int | Bool | Pair of typ*typ;;

(* static errors *)

exception ExpectingSetOrString of unit;;  

exception ExpectingSetError of unit;;  

exception ExpectingTypeError of typ;;

exception ExpectingPairError of unit;;  

exception UndefinedVariable of ident;;

(* environments *)

let empty_scope = [];; 

let starting_env = [empty_scope];; (* just the empty top-level scope *)

(* enter_scope : 'a list list -> 'a list list *)

let enter_scope env = empty_scope::env;; (* enters a new nested scope *)

(* variable look up *)

(* resolve : ident -> (ident * 'a) list list -> (ident * 'a) list *)

let rec resolve id = function
    scope::env -> if(List.mem_assoc id scope) then scope else resolve id env
  | [] -> raise (UndefinedVariable id);;

(* lookup : ident -> (ident * 'a) list list -> 'a *)

let lookup id env = List.assoc id (resolve id env);;

(* variable declaration *)

(* dec : 'a -> 'b -> ('a * 'b) list list -> ('a * 'b) list list *)

let dec id info = function
    scope::env -> ((id,info)::scope)::env
  | [] -> failwith "assertion error";; (* should never happen *)


(* static semantics *)

type static_env = (ident * typ) list list;;

(* mutually recursive
   wfExp : static_env -> exp -> typ 
   wfExpSeq : static_env -> exp_seq -> typ
*)

let rec wfExp env=function
    StringLit _ -> String
  | SetLit exp_seq -> Set(wfExpSeq env exp_seq)
  | Cat(exp1,exp2) -> if wfExp env exp1=String && wfExp env exp2=String then String else raise (ExpectingTypeError String)
  | Union(exp1,exp2) | Intersect(exp1,exp2) ->
      (match wfExp env exp1 with Set type1 -> if wfExp env exp2=Set type1 then Set type1 else raise (ExpectingTypeError (Set type1))
                               | _ -> raise  (ExpectingSetError()))
  | In(exp1,exp2) -> let type1=wfExp env exp1 in if wfExp env exp2=Set type1 then Bool else raise (ExpectingTypeError (Set type1))
  | Dim exp -> (match wfExp env exp with Set _ | String -> Int | _ -> raise (ExpectingSetOrString ()))
  | Add(exp1,exp2) | Mul(exp1,exp2) -> if wfExp env exp1=Int && wfExp env exp2=Int then Int else raise (ExpectingTypeError Int)
  | And(exp1,exp2) -> if wfExp env exp1=Bool && wfExp env exp2=Bool then Bool else raise (ExpectingTypeError Bool)
  | Eq(exp1,exp2) -> let type1=wfExp env exp1 in if wfExp env exp2=type1 then Bool else raise (ExpectingTypeError type1)
  | Pair(exp1,exp2) -> let type1=wfExp env exp1 and type2=wfExp env exp2 in Pair(type1,type2)
  | Fst exp -> (match wfExp env exp with Pair(type1,_) -> type1 | _ -> raise (ExpectingPairError ()))
  | Snd exp -> (match wfExp env exp with Pair(_,type2) -> type2 | _ -> raise (ExpectingPairError()))
  | Sign exp -> if wfExp env exp=Int then Int else raise (ExpectingTypeError Int)
  | Not exp -> if wfExp env exp=Bool then Bool else raise (ExpectingTypeError Bool)
  | Num _ -> Int
  | Bool _ -> Bool
  | Var id -> lookup id env

and

  wfExpSeq env=function 
    SingleExp exp -> wfExp env exp
  | MoreExp(exp,exp_seq) -> let type1=wfExp env exp in if wfExpSeq env exp_seq=type1 then type1 else raise (ExpectingTypeError type1);;

(* mutually recursive
   wfStmt : static_env -> stmt -> static_env
   wfStmtSeq : static_env -> stmt_seq -> static_env
*)

let rec wfStmt env=function
    Assign(id,exp) -> 
      let type1=lookup id env in 
        if wfExp env exp=type1 then env else raise (ExpectingTypeError type1)
  | Dec(id,exp) -> dec id (wfExp env exp) env
  | Print exp -> let _=wfExp env exp in env
  | While(exp,stmt_seq) | If(exp,stmt_seq) ->
      if wfExp env exp=Bool then 
        let env2=enter_scope env in 
        let _=wfStmtSeq env2 stmt_seq in env 
      else raise (ExpectingTypeError Bool)
  | IfElse(exp,stmt_seq1,stmt_seq2) -> 
      if wfExp env exp=Bool then 
        let env2=enter_scope env in 
        let _=wfStmtSeq env2 stmt_seq1 and _=wfStmtSeq env2 stmt_seq2 in env 
      else raise (ExpectingTypeError Bool)

and 

  wfStmtSeq env=function 
    SingleStmt stmt -> wfStmt env stmt
  | MoreStmt(stmt,stmt_seq) -> wfStmtSeq (wfStmt env stmt) stmt_seq;;

(* wfProg : prog -> unit *)

let wfProg = function Prog stmt_seq -> let _=wfStmtSeq starting_env stmt_seq in ();;

(* some simple tests with the static semantics *)

let stmt1=Dec(Id "x",StringLit "Hello ");;

let stmt2=Assign(Id "x",Cat(Var(Id "x"),StringLit "world"));;

let stmt3=Print(Add(Num 1,Dim(Var(Id "x"))));;

let prog1=Prog(MoreStmt(stmt1,(MoreStmt(stmt2,SingleStmt stmt3))));;

wfProg prog1;;

let stmt1=Dec(Id "x",SetLit(MoreExp(Num 1,SingleExp(Num 2))));;

let stmt2=Print(In(Num 3,Var(Id "x")));;

let stmt3=Print(Add(Num 1,Dim(Union(Var(Id "x"),Var(Id "x")))));;

let stmt4 = Assign(Id "x",Intersect(Var(Id "x"),Var(Id "x")));;

let prog2=Prog(MoreStmt(stmt1,(MoreStmt(stmt2,MoreStmt(stmt3,SingleStmt stmt4)))));;

wfProg prog2;;

let stmt1=Dec(Id "x",Num 10);;

let stmt2=While(Not(Eq(Var(Id "x"),Num 0)),MoreStmt(Assign(Id "x",Add(Var (Id "x"),Sign(Num 1))),MoreStmt(Dec(Id "x",Bool false),SingleStmt(Print(And(Var(Id "x"),Bool true))))));;

let stmt3=Assign(Id "x",Num 2);;

let prog3=Prog(MoreStmt(stmt1,(MoreStmt(stmt2,SingleStmt stmt3))));;

wfProg prog3;;





