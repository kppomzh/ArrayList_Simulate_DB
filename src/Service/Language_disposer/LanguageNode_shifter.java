package Service.Language_disposer;

import Data.classes.Language_node;
import Data.Vessel.Word;
import static java.lang.System.exit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import m_Exception.Language_error;

/**
 *
 * @author gosiple
 */
public class LanguageNode_shifter 
{
    final HashMap<String,Language_node> word_Map;
    public LanguageNode_shifter(HashMap word_Map)
    {
        this.word_Map=word_Map;
    }
    
    public void shifter(LinkedList<Word> words) throws Language_error
    {
        for(Language_node Ln:word_Map.values())
        {
            try{
            Ln.unlockAll();
            }
            catch(Exception ex){}
        }
        switch(words.get(0).getName())
        {//通过限制SQL单词节点能转移到的下一节点来对语法DFA进行规范，避免一些不必要的错误
            case "create":
            {
                word_Map.get("L_name").lock_node("newList_name");
                word_Map.get("T_name").lock_node(",",".",";","where","values","set","union","add","del");
                break;
            }
            case "drop":
                break;
            case "select":
                break;
            case "update":
                break;
            case "insert":
                break;
            case "delete":
                break;
            case "commit":
                break;
            case "alter":
            {
                word_Map.get("int").lock_node(")",",");
                word_Map.get("double").lock_node(")",",");
                word_Map.get("string").lock_node(")",",");
                break;
            }
            case "show":
                break;
            default :
                throw new Language_error("不正确的开始符号");
        }
        Language_node fountain=word_Map.get(words.get(0).getName());
        
        for(int loop=1;loop<words.size();loop++)
        {
            //System.out.println(words[loop].getName());
            fountain=fountain.to_status(words.get(loop).getName());
            if(fountain==null)
                throw new Language_error(words.get(loop).getName()+"在此处是不允许的单词类型");
        }
        if(fountain.getoption().equals(";")) {
        } else
            throw new Language_error("必须以';'结尾");
    }
    
    public static void main(String[] ar)
    {
        String[] SQL={
"select * from first where id =3+5;",
        //"update first set id=1.2*3;",
        //"alter table first add column im int;",
        "select * from first\n" +
"union\n" +
"select id pid,name pname,time from first\n" +
"where id=3\n" +
"or name='kppom';",
        "select tra,rbg,dsf,dsa from fisrt,(select *from second) second where fisrt.name is null;"};
        for(int loopo=2;loopo<SQL.length;loopo++)
        {
        LinkedList<Word> words = null;
        try {
            HashMap test=new InitLanguage_node().setWord_Map();
            words=new Word_Segmentation_Machine(test).Segment(SQL[loopo]);
            new LanguageNode_shifter(test).shifter(words);
            for(int loop=0;loop<words.size();loop++)
            {
                System.out.print(words.get(loop).getName());
                System.out.print(' ');
                System.out.println(words.get(loop).getSubstance());
            }
        } catch (Exception ex) {
             System.out.println(ex.getMessage());
            //exit(1);
        }
    }
}
}