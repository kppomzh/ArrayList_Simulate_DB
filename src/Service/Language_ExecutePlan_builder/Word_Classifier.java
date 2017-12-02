package Service.Language_ExecutePlan_builder;

import Data.Vessel.ExecutePlan_Package;
import Data.Vessel.Word;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import m_Exception.FileSystem.ClassNotFound;
import m_Exception.Language_error;

/** 
 *
 * @author gosiple
 * 单词分类器，用于第一步粗筛，将整句中的单词按照一定得语法格子分开（也就是俗称的子句），传递到下一层
 * 处理子查询的时候尤其要小心，最好设置两个方式来传递
 * 目前自从对分析复杂select的计划提上日程之后，分类器就负担了绝大部分语义分析的工作
 * 被迫写成了复杂的递归，我真的不想这么写的，但是光是写成这样计算量已经有点大了，递推执行……还完全没有方案。
 */
public class Word_Classifier 
{
    private ExecutePlan_builder backstage;
    private String start;
    private LinkedList<String> s_vertical=null;
    private LinkedList<String> s_condition=null;
    private LinkedList<String> newnames=null;
    private LinkedList<String> linkmark=null;
    private LinkedList<String> f_vertical=null;
    private LinkedList<String> f_option=null;
    private LinkedList<String> f_condition=null;
    private LinkedList<String> insertion_sequence=null;
    private LinkedList<Word> toClassify;
    private ExecutePlan_Package EPP;
    private ExecutePlan_Package childEPP;
    private ExecutePlan_Package unionEPP;
    private LinkedList<ExecutePlan_Package> joinEPP;
    private LinkedList<String> call_tablespace;
    private LinkedList<String> call_table;
    private LinkedList<Integer> Division_line_is_read;
    public Word_Classifier(ExecutePlan_builder backstage,LinkedList<Word> words)
    {
        this.backstage=backstage;
        toClassify=words;
        start=words.get(0).getName();
        call_tablespace=new LinkedList<>();
        call_table=new LinkedList<>();
        EPP=new ExecutePlan_Package(start);
        Division_line_is_read=new LinkedList<>();
        joinEPP=new LinkedList<>();
    }
    public ExecutePlan_Package Classify(LinkedList<LinkedList<Word>> Division) throws ClassNotFound, Language_error
    {
        LinkedHashSet<Word> tableSet;
        switch(start)
        {
            case "create":
            {
                create();
                break;
            }
            case "drop":
            {
                drop();
                break;
            }
            case "insert":
            {
                insert();
                break;
            }
            case "delete":
            {
                delete();
                break;
            }
            case "update":
            {
                update();
                break;
            }
            case "select":
            {
                LinkedList<String> bracketstack=new LinkedList<>();
                tableSet=new LinkedHashSet<>();
                int Tablenumber=0;
                int readingline=1;//正在读取的行号
                //from子句应该放到最前面进行分析
                LinkedList<Word> temp=Division.get(readingline);
                for(int loop=0;loop<temp.size();loop++)
                {
                    if(temp.get(loop).getName().equals("T_name")||temp.get(loop).getName().equals("newTable_name"))
                    {
                        Tablenumber++;
                        tableSet.add(temp.get(loop));
                        if(tableSet.size()!=Tablenumber)
                            throw new Language_error("对不同的表使用了同一个名称或别名");
                    }
                    else if(temp.get(loop).getName().equals("(")) //说明发现了嵌套子查询
                    {
                        //childEPP=new ExecutePlan_Package("select");
                        bracketstack.add(temp.get(loop).getName());
                        LinkedList<Word> child=new LinkedList<>();
                        child.addAll(temp.subList(loop+1, temp.size()-1));//先把当前一行的select子句添加进去
                        Division_line_is_read.add(readingline);
                        //一直找到最后一个右括号为止
                        OUTER:
                        for(readingline++;readingline<Division.size();readingline++)
                        {
                            temp=Division.get(readingline);
                            Division_line_is_read.add(readingline);
                            for(int loopi=0;loopi<temp.size();loopi++)
                            {
                                switch (temp.get(loopi).getName()) {
                                    case ")":
                                        bracketstack.remove();
                                        if(!bracketstack.isEmpty())
                                            child.add(temp.get(loopi));
                                        break;
                                    case "(":
                                        bracketstack.add(temp.get(loop).getName());
                                    default:
                                        child.add(temp.get(loopi));
                                        break;
                                }
                                if(bracketstack.isEmpty())
                                {
                                    loop=loopi;
                                    //child.remove();//将最后一个多出来的）抹去
                                    LinkedList<LinkedList<Word>> ch_Division=backstage.call_clause(child);
                                    childEPP=new Word_Classifier(backstage,child).Classify(ch_Division);
                                    EPP.initchildEPP(EPP);
                                    break OUTER;
                                }
                            }
                        }
                    }
                }
                Division_line_is_read.add(readingline);
                Iterator<Word> itableset=tableSet.iterator();
                while(itableset.hasNext())//统计总共有几张表参与了这个union内的关联
                {
                    if(itableset.next().getName().equals("T_name"))
                        joinEPP.add(new ExecutePlan_Package("T_name",""));
                }
                for(int loop=0;loop<Division.size();loop++)
                {
                    if(Division_line_is_read.get(loop)!=-1)
                        continue;//已经分析过的行不再进行分析
                    temp=Division.get(loop);
                    switch(temp.get(0).getName())
                    {
                        case "select":
                        {
                            for(int loopn=1;loopn<temp.size();loopn++)
                            {
                                switch(temp.get(loopn).getName())
                                {
                                    case "newList_name":
                                        newnames.set(loopn, temp.get(loopn).getSubstance());
                                        break;
                                    case "L_name":
                                        s_vertical.addLast(temp.get(loopn).getSubstance());
                                        newnames.addLast(null);
                                        break;
                                    case "T_name":
                                    case "TS_name":
                                        
                                }
                            }
                            break;
                        }
                    }
                    //switch()
                }
                break;
            }
            case "show":
            {
                show();
                break;
            }
            case "alter":
            {
                alter();
                break;
            }
            case "commit":break;
            default:
        }
        return EPP;
    }
    public ExecutePlan_Package Classify() throws ClassNotFound, Language_error
    { 
        int point=0;
        switch(start)
        {
            case "create":
            {
                create();
                break;
            }
            case "drop":
            {
                drop();
                break;
            }
            case "insert":
            {
                //String[] s_vertical,String[] insertion_sequence
                insert();
                break;
            }
            case "delete":
            {
                delete();
                break;
            }
            case "update":
            {
                //String[] s_vertical,String[] s_condition,String[] linkmark,String[] f_vertical,String[] f_option,String[] f_condition
                update();
                break;
            }
            case "select":
            {
                //String[] s_vertical,String[] newnames,String[] linkmark,String[] f_vertical,String[] f_option,String[] f_condition
                boolean value=true;
                boolean where=true;
                boolean slc=true;
                boolean from=true;
                int Tablenumber=0;
                
                if(!toClassify.get(1).getName().equals("*")&value)
                    {
                        s_vertical=new LinkedList<>();
                        newnames=new LinkedList<>();
                        value=false;
                    }
                for(int loop=1,loopn=-1;loop<toClassify.size();loop++)
                {
                    if(slc)
                        ;
                    if(where)
                        switch(toClassify.get(loop).getName())
                        {
                            case "T_name":
                                call_table.add(toClassify.get(loop).getSubstance()); break;
                            case "TS_name":
                                call_tablespace.add(toClassify.get(loop).getSubstance()); break;
                            case "newList_name":
//这里对newList_name的处理方式有问题，如果一个SQL当中完全没有newList_name的话会导致其不为null但是长度为0
                                //newnames.addLast(toClassify.get(loop).getSubstance());break;
                                newnames.set(loopn, toClassify.get(loop).getSubstance());
                                break;
                            case "L_name":
                                s_vertical.addLast(toClassify.get(loop).getSubstance());
                                newnames.addLast(null);
//这样先把newname设为null
                                loopn++;//每录入一个L_name，就让loopn加一，为case "newList_name"做指示
                                break;
                            case "where":
                                where=false;
                                linkmark=new LinkedList<>();
                                f_vertical=new LinkedList<>();
                                f_option=new LinkedList<>();
                                f_condition=new LinkedList<>();
                                break;
                            case "union":
                            {
                                LinkedList<Word> union=new LinkedList<>();
                                for(int loopi=loop+1;;loopi++)
                                {
                                    if(toClassify.get(loopi).getName().equals("union"))
                                        break;
                                    if(loopi+1==toClassify.size())
                                        break;
                                    union.add(toClassify.get(loopi));
                                }
                                this.unionEPP=new Word_Classifier(backstage,union).Classify();
                                EPP.initunionEPP(unionEPP);
                                loop=loop+union.size();
                                break;
                            }
                        }
                    else
                        switch(toClassify.get(loop).getName())
                        {
                            case "L_name":
                                f_vertical.addLast(toClassify.get(loop).getSubstance());break;
                            case "Integer":case "Double":case "String":case "null":
                                f_condition.addLast(toClassify.get(loop).getSubstance());break;
                            case "is":case "isnot": case "=":case "!=":case ">":case "<":case ">=":case "<=":
                                f_option.addLast(toClassify.get(loop).getName());break;
                            case "and":case "or":
                                linkmark.addLast(toClassify.get(loop).getName());break;
                            case "union":
                            {
                                LinkedList<Word> union=new LinkedList<>();
                                for(int loopi=loop+1;;loopi++)
                                {
                                    if(toClassify.get(loopi).getName().equals("union"))
                                        break;
                                    if(loopi+1==toClassify.size())
                                        break;
                                    union.add(toClassify.get(loopi));
                                }
                                this.unionEPP=new Word_Classifier(backstage,union).Classify();
                                EPP.initunionEPP(unionEPP);
                                loop=loop+union.size();
                                break;
                            }
                        }
                }
                if(value&&where)
                    ;
                else if(value&&!where)
                    EPP.setSelect(null, null, linkmark.toArray(new String[0]), f_vertical.toArray(new String[0]), f_option.toArray(new String[0]), f_condition.toArray(new String[0]));
                else if(!value&&where)
                    EPP.setSelect(s_vertical.toArray(new String[0]), newnames.toArray(new String[0]), null, null, null, null);
                else //if(!value&&!where)
                    EPP.setSelect(s_vertical.toArray(new String[0]), newnames.toArray(new String[0]), linkmark.toArray(new String[0]), f_vertical.toArray(new String[0]), f_option.toArray(new String[0]), f_condition.toArray(new String[0]));
                break;
            }
            case "alter":
                alter();
                break;
            case "show":
                show();
                break;
            case "commit":break;
            default:
        }
        this.backstage.CheckClasses(EPP, call_tablespace, call_table);
        return EPP;
    }
    
    private void show()
    {
        s_vertical=new LinkedList<>();
        for(int loop=1;loop<toClassify.size();loop++)
            s_vertical.addLast(toClassify.get(loop).getName());
        EPP.setShow(s_vertical.toArray(new String[0]));
    }
    private void create() throws Language_error
    {
        for(int loop=1;loop<toClassify.size();loop++)
                {
                    switch(toClassify.get(loop).getName())
                    {
                        case "T_name": 
                            call_table.add(toClassify.get(loop).getSubstance()); break;
                        case "TS_name": 
                            call_tablespace.add(toClassify.get(loop).getSubstance()); break;
                        case "L_name":
                            s_vertical.addLast(toClassify.get(loop).getSubstance());break;
                        case "string":case "int":case "double":
                            s_condition.addLast(toClassify.get(loop).getName());break;
                        case "table":
                            s_vertical=new LinkedList<>();
                            s_condition=new LinkedList<>();
                        case "tablespace":
                            EPP.setCreateType(toClassify.get(loop).getName());
                            break;
                    }
                }
                if(EPP.getCreateType().equals("table")&&s_vertical.isEmpty())
                    throw new Language_error("建表语句不能没有实际列项");
                if(s_vertical!=null)
                    EPP.setCreate(s_vertical.toArray(new String[0]), s_condition.toArray(new String[0]));
    }
    private void drop()
    {
        for(int loop=1;loop<toClassify.size();loop++)
                {
                    switch(toClassify.get(loop).getName())
                    {
                        case "T_name": 
                            call_table.add(toClassify.get(loop).getSubstance()); break;
                        case "TS_name": 
                            call_tablespace.add(toClassify.get(loop).getSubstance()); break;
                        case "table":case "tablespace":
                            EPP.setCreateType(toClassify.get(loop).getName());
                            break;
                    }
                }
    }
    private void insert()
    {
        boolean value=true;
                s_vertical=null;
                insertion_sequence=new LinkedList<>();
                for(int loop=1;loop<toClassify.size();loop++)
                {
                    switch(toClassify.get(loop).getName())
                    {
                        case "T_name": 
                            call_table.add(toClassify.get(loop).getSubstance()); break;
                        case "TS_name": 
                            call_tablespace.add(toClassify.get(loop).getSubstance()); break;
                        case "Integer":case "Double":case "String":
                           insertion_sequence.addLast(toClassify.get(loop).getSubstance());break;
                        case "L_name":
                            s_vertical.addLast(toClassify.get(loop).getSubstance());
                            break;
                        case "(":
                            if(toClassify.get(loop-1).getName().equals("T_name"))
                            {
                                s_vertical=new LinkedList<>();
                                value=false;
                            }
                            break;
                    }
                }
                if(value)
                    EPP.setInsert(null, insertion_sequence.toArray(new String[0]));
                else
                    EPP.setInsert(s_vertical.toArray(new String[0]), insertion_sequence.toArray(new String[0]));
    }
    private void delete()
    {
        s_vertical=new LinkedList<>();
                boolean where=true;
                for(int loop=1;loop<toClassify.size();loop++)
                {
                    switch(toClassify.get(loop).getName())
                    {
                        case "where":
                            f_vertical=new LinkedList<>();
                            f_option=new LinkedList<>();
                            f_condition=new LinkedList<>();
                            linkmark=new LinkedList<>();
                            where=false;
                            break;
                        case "Integer":case "Double":case "String":case "null":
                            f_condition.addLast(toClassify.get(loop).getSubstance());break;
                        case "L_name":
                            f_vertical.addLast(toClassify.get(loop).getSubstance());break;
                        case "is":case "isnot": case "=":case "!=":case ">":case "<":case ">=":case "<=":
                            f_option.addLast(toClassify.get(loop).getName());break;
                        case "and":case "or":
                            linkmark.addLast(toClassify.get(loop).getName());break;
                        case "T_name":
                            call_table.add(toClassify.get(loop).getSubstance()); break;
                        case "TS_name": 
                            call_tablespace.add(toClassify.get(loop).getSubstance()); break;
                    }
                }
                if(where)
                    EPP.setDelete(s_vertical.toArray(new String[0]), null, null, null, null);
                else
                    EPP.setDelete(s_vertical.toArray(new String[0]), linkmark.toArray(new String[0]), f_vertical.toArray(new String[0]), f_option.toArray(new String[0]), f_condition.toArray(new String[0]));
    }
    private void update()
    {
        boolean where=true;
                s_vertical=new LinkedList<>();
                s_condition=new LinkedList<>();
                for(int loop=1;loop<toClassify.size();loop++)
                {
                    if(where)
                        switch(toClassify.get(loop).getName())
                        {
                            case "T_name":
                                call_table.add(toClassify.get(loop).getSubstance()); break;
                            case "TS_name":
                                call_tablespace.add(toClassify.get(loop).getSubstance()); break;
                            case "L_name":
                                s_vertical.addLast(toClassify.get(loop).getSubstance());break;
                            case "Integer":case "Double":case "String":case "null":
                                s_condition.addLast(toClassify.get(loop).getSubstance());break;
                            case "where":
                                where=false;
                                linkmark=new LinkedList<>();
                                f_vertical=new LinkedList<>();
                                f_option=new LinkedList<>();
                                f_condition=new LinkedList<>();
                                break;
                        }
                    else
                        switch(toClassify.get(loop).getName())
                        {
                            case "L_name":
                                f_vertical.addLast(toClassify.get(loop).getSubstance());break;
                            case "Integer":case "Double":case "String":case "null":
                                f_condition.addLast(toClassify.get(loop).getSubstance());break;
                            case "is":case "isnot": case "=":case "!=":case ">":case "<":case ">=":case "<=":
                                f_option.addLast(toClassify.get(loop).getName());break;
                            case "and":case "or":
                                linkmark.addLast(toClassify.get(loop).getName());break;
                        }
                }
                if(where)
                    EPP.setUpdate(s_vertical.toArray(new String[0]), s_condition.toArray(new String[0]), null, null, null, null);
                else
                    EPP.setUpdate(s_vertical.toArray(new String[0]), s_condition.toArray(new String[0]), linkmark.toArray(new String[0]), f_vertical.toArray(new String[0]), f_option.toArray(new String[0]), f_condition.toArray(new String[0]));
                
    }
    private void alter()
    {
        s_vertical=new LinkedList<>();
                insertion_sequence=new LinkedList<>();
                for(int loop=1;loop<toClassify.size();loop++)
                {
                    switch(toClassify.get(loop).getName())
                    {
                        case "T_name": 
                            call_table.add(toClassify.get(loop).getSubstance()); break;
                        case "TS_name": 
                            call_tablespace.add(toClassify.get(loop).getSubstance()); break;
                        case "int":case "double":case "string":
                           insertion_sequence.addLast(toClassify.get(loop).getName());
                           break;
                        case "L_name":
                            s_vertical.addLast(toClassify.get(loop).getSubstance());
                            break;
                        case "add":case "del":
                            this.EPP.setCreateType(toClassify.get(loop).getName());
                            break;
                    }
                }
                EPP.setAlter(s_vertical.toArray(new String[0]), insertion_sequence.toArray(new String[0]));
    }
}
