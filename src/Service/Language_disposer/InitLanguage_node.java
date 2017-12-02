package Service.Language_disposer;

import Data.classes.Language_node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedList;

/**
 *
 * @author rkppo
 */
public class InitLanguage_node 
{
    Language_node[] word_list;  //新标准英语教材：单词表
    HashMap<String,Language_node> word_Map;
    
    private LinkedList Give_word()
    {//所有关键字列表在此添加
        LinkedList<String> word=new LinkedList<String>();
        //String[] word=new String[100];//维护比较麻烦且并不实用，遂废弃
        word.add("create");
        word.add("drop");
        word.add("select");
        word.add("insert");
        word.add("update");
        word.add("delete");
        word.add("alter");//第三次修订
        word.add("from");
        word.add("where");
        //word.add("order");
        //word.add("by");
        word.add("union");
        word.add("set");
        word.add("into");
        word.add("values");
        word.add("add");//第三次修订
        word.add("del");//第三次修订
        //word.add("distinct");
        
        //word.add("sequence");
        //word.add("cursor");
        word.add("table");
        word.add("tablespace");
        //word.add("view");
        //word.add("dual");
        //以下三个是create的时候定义用的
        word.add("int");
        word.add("double");
        word.add("string");
        //以下三个是标识变量类型用的
        word.add("String");
        word.add("Integer");
        word.add("Double");
        
        word.add("column");
        word.add("T_name");
        word.add("L_name");
        word.add("TS_name");
        word.add("newList_name");
        word.add("newTable_name");
        word.add("C_name");
        word.add("S_name");
        
        word.add("+");
        word.add("-");
        word.add("*");
        word.add("/");
        word.add("=");
        word.add(">");
        word.add("<");
        word.add(">=");
        word.add("<=");
        word.add("!=");
        
        word.add("(");
        word.add(")");
        word.add(",");
        word.add(".");
        word.add("\'");
        word.add("\"");
        word.add(";");//这个单词作为终止符，一开始认为不用添加
        
        word.add("and");
        word.add("or");
        word.add("in");
        
        word.add("null");
        word.add("is");
        word.add("isnot");
        
        //word.add("asc");
        //word.add("desc");
        
        word.add("show");
        word.add("dbtree");
        word.add("memory_use");
        word.add("cpu_use");
        //以上是第一次构想的关键字
        //word.add("sysdate");
        word.add("commit");//单独出现，没有上下文。
        
        //5.2加入
        //word.add("Clause_SQL");//这个是更加高级的语法非终结符，不应该加到这里
        word.add("^");
        word.add("%");
        //适用于各类数学函数符号，例如sqrt、abs、pow等
        word.add("sqrt");
        word.add("abs");
        word.add("pow");
        
        return word;
    }
    
    public HashMap<String,Language_node> setWord_Map() //初始化节点地图，设定每个节点的状态转移
    {
        this.word_Map=new HashMap<>();
        LinkedList<String> words=this.Give_word();
        int word_count=words.size();
        this.word_list=new Language_node[word_count+6];
        for(int loop=0;loop<word_count;loop++)
        {
            this.word_list[loop]=new Language_node();
            this.word_list[loop].setoption(words.get(loop));
            this.word_Map.put(this.word_list[loop].getoption(), this.word_list[loop]);
        }
        
        final HashMap finalMap=this.word_Map; //完成节点增加后的节点地图不允许更改
        for(int loop=0;loop<word_count;loop++)
        {   //设定每个节点能够转移的状态
            switch (this.word_list[loop].getoption()) {
                case "create":
                    {
                        String[] add={"tablespace","table","sequence","cursor"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "drop":
                    {
                        String[] add={"tablespace","table","sequence","cursor"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "select":
                    {
                        String[] add={"*","L_name","sysdate","distinct",""};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "insert":
                    {
                        String[] add={"into"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "update":
                    {
                        String[] add={"T_name","TS_name"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "delete":
                    {
                        String[] add={"from"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "alter":
                {
                    String[] add={"table"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "from":
                    {
                        String[] add={"T_name","TS_name","(","dual","Clause_SQL"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "where":
                    {
                        String[] add={"T_name","TS_name","L_name"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "order":
                    {
                        String[] add={"by"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "by":
                    {
                        String[] add={"asc","desc"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "union":
                    {
                        String[] add={"select"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "set":
                    {
                        String[] add={"L_name"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "into":
                    {
                        String[] add={"T_name","TS_name"};
                        this.word_list[loop].add_status(add,finalMap);
                        break;
                    }
                case "values":
                {
                    String[] add={"("};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "add":
                {
                    String[] add={"column"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "del":
                {
                    String[] add={"column"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "distinct":
                {
                    String[] add={"*","TS_name","T_name","L_name"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "sequence":
                case "cursor":
                {
                    
                }
                case "table":
                {
                    String[] add={"TS_name","T_name"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "tablespace":  ///?????
                {
                    String[] add={"TS_name"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "dual":
                {
                    String[] add={";"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "int":
                case "double":
                case "string":
                {
                    String[] add={",",")",";"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }  
                case "null":
                case "Integer":
                case "Double":
                {
                    String[] add={",",")","and","or",";","where","union","+","-","*","/","%","^"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "String":
                {
                    String[] add={",",")","and","or",";","where","union"};//,"+","-","*","/","%","^","MathFunction"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "column":
                {
                    String[] add={"L_name"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "TS_name":
                {
                    String[] add={".",";"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "T_name":
                {
                    String[] add={"(",",",".",";","where","values","set","union","add","del"/*,"and","or"*/
                    ,")"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "L_name":
                {
                    String[] add={",",")","and","or","newList_name","from","=",">","<",">=","<=","!=","is","isnot","int","double","string",";",
                            "+","-","*","/","%","^"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "newList_name":
                case "sysdate":
                {
                    String[] add={",","from",")"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "newTable_name":
                {
                    String[] add={",",";","where","union"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "=":
                case ">":
                case "<":
                case ">=":
                case "<=":
                case "!=":
                {
                    //LinkedList temp=new LinkedList().addFirst("Clause_SQL");
                }
                case "+":
                case "-":
                case "/":
                case "^":
                case "%":
                {
                    String[] add={"Integer","Double","String","L_name","T_name","MathFunction"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "*":
                {
                    String[] add={"from","Integer","Double","String","L_name","T_name","MathFunction"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "(":
                {
                    String[] add={"Integer","Double","String","L_name","T_name","sysdate"
                    ,"select","("};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case ")":
                {
                    String[] add={"values",";","where","newTable_name",")"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case ",":
                {
                    String[] add={"Integer","Double","L_name","T_name","String","sysdate"
                    ,"(","Clause_SQL"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case ".":
                {
                    String[] add={"L_name","T_name"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "\'":
                {
                    String[] add={"String"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "and":
                case "or":
                {
                    String[] add={"T_name","L_name","Integer","Double","String"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "is":case "isnot":
                {
                    String[] add={"null"};//"not"
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                
                case "show":
                {
                    String[] add={"dbtree","memory_use","cpu_use"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                case "dbtree":
                case "memory_use":
                case "cpu_use":
                case "commit":
                {
                    String[] add={";"};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                
                case "Clause_SQL":
                {
                    String[] add={",",";","where","newTable_name"};
                }
                case "MathFunction":
                {
                    String[] add={"("};
                    this.word_list[loop].add_status(add,finalMap);
                    break;
                }
                default:
                    break;
            }
        }
        
        return finalMap;
    }
    public ArrayList<String> setclause_stop()
    {
        ArrayList<String> stop=new ArrayList<String>();
        stop.add("select");
        //stop.add("set");
        stop.add("where");
        stop.add("from");
        stop.add("union");
        return stop;
    }
    
    public static void main(String[] ar)
    {
        HashMap test=new InitLanguage_node().setWord_Map();
        System.out.print(test.size());
    }
}