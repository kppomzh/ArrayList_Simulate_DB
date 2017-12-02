package Service.Language_ExecutePlan_builder;

import Data.Vessel.ExecutePlan_Package;
import Service.Language_disposer.InitLanguage_node;
import Data.classes.Language_node;
import Data.Vessel.Word;
import Service.Language_disposer.Word_Segmentation_Machine;
import Service.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import m_Exception.FileSystem.ClassNotFound;
import m_Exception.Language_error;

/**
 *
 * @author rkppo
 */
public class ExecutePlan_builder 
{
    Service service;
    //LinkedList<Word> words;
    String default_tbs="public";
    ExecutePlan_Package EPP;
    ArrayList<String> clause_stop;
    public ExecutePlan_builder(Service backstage,ArrayList<String> clause_stop)
    {
        this.service=backstage;
        //this.words=words;
        this.clause_stop=clause_stop;
    }
    public ExecutePlan_Package make_ExecutePlan(LinkedList<Word> words) throws ClassNotFound, Language_error
    {
        LinkedList<LinkedList<Word>> Division=this.Pretreatment_Division_clause(words);
        Word_Classifier WC=new Word_Classifier(this,words);
        //EPP=WC.Classify();
        EPP=WC.Classify(Division);
        return EPP;
    }
    public LinkedList<LinkedList<Word>> call_clause(LinkedList<Word> toDiv)
    {
        return this.toFind_clause_start(toDiv);
    }
    private LinkedList<LinkedList<Word>> Pretreatment_Division_clause(LinkedList<Word> words)
    {
        LinkedList<Word> toDiv=(LinkedList<Word>) words.clone();
        return toFind_clause_start(toDiv);
    }
    private LinkedList<LinkedList<Word>> toFind_clause_start(LinkedList<Word> toDiv)
    {
        boolean in=false;
        LinkedList<LinkedList<Word>> Return=new LinkedList<>();
        LinkedList<Word> lw=new LinkedList<>();
        for(;!toDiv.isEmpty();)
        {
            //toDiv.get(0).getName();
            if(clause_stop.indexOf(toDiv.get(0).getName())!=-1)
            {
                in=!in;
            }
            if(in)
            {
                lw.add(toDiv.remove());
            }
            else
            {
                Return.add(lw);
                lw=new LinkedList<>();
            }
        }
        Return.add(lw);
        return Return;
    }
    
    public void CheckClasses(ExecutePlan_Package p,LinkedList<String> Lttbs,LinkedList<String> Lttb) throws ClassNotFound
    {
        while(!Lttb.isEmpty()||!Lttbs.isEmpty())
        {
            String ttbs,ttb;//=call_tablespace.removeFirst();
            try{ttbs=Lttbs.removeLast();}//这里如果不捕获异常的话会直接抛出异常，尽管removeFirst的返回值是null。
            catch(Exception e){ttbs=default_tbs;}
            try{ttb=Lttb.removeLast();}
            catch(Exception e){ttb=null;}
            //非create的状态下检查SQL当中提到的表空间和表是否存在，不存在则抛出异常
            if(p.getCommand().equals("create")&&p.getCreateType().equals("table")&&!service.check_File_exist(ttbs, ttb))
                ;
            else if(p.getCommand().equals("create")&&p.getCreateType().equals("tablespace")&&!service.check_File_exist(ttbs, ttb))
                    ;
            else if(p.getCommand().equals("drop")&&p.getCreateType().equals("tablespace")&&service.check_File_exist(ttbs, ttb))
                ;
            else if(service.check_File_exist(ttbs, ttb))
                ;
            else
                throw new ClassNotFound(ttbs+'.'+ttb);
            p.PushClasses(ttbs, ttb);
        }
    }
    
    public List<String> callTablecheckselectlist(String tbs,String tb,String[] s_vertical)
    {//为了让分词器中能够进行动态的列名检查而设计的
        return service.call_FileSystem().getTable(tbs, tb).checkselectlist(s_vertical);
    }
    
    public static void main(String[] ar) throws Exception
    {
        String SQL="select tra,rbg,dsf,dsa from fisrt,(select *from second) second where fisrt.name is null;";
        
        InitLanguage_node allNodes=new InitLanguage_node();
        ArrayList<String> clause_stop=allNodes.setclause_stop();
        HashMap<String,Language_node> word_Map=allNodes.setWord_Map();
        Word_Segmentation_Machine WSS=new Word_Segmentation_Machine(word_Map);
        LinkedList<Word> word_list=WSS.Segment(SQL);
        ExecutePlan_builder EPB=new ExecutePlan_builder(null,clause_stop);
        
        //LinkedList<LinkedList<Word>> Return=EPB.toFind_clause_start(word_list);
        /*        for(int loop=0;loop<Return.size();loop++)
        {
        LinkedList<Word> tlw=Return.get(loop);
        for(int loopi=0;loopi<tlw.size();loopi++)
        if(tlw.get(loopi).getSubstance()!=null)
        System.out.print(tlw.get(loopi).getSubstance()+' ');
        else
        System.out.print(tlw.get(loopi).getName()+' ');
        System.out.println();
        }*/
        ExecutePlan_Package make_ExecutePlan = EPB.make_ExecutePlan(word_list);
        System.out.println();
    }
}
