package Service;

import Service.Check.check_StringtoNumber;
import Utils.System.IMonitorService;
import Utils.System.MonitorService;
import Data.Vessel.ExecutePlan_Package;
import Service.Fileloader.FileSystem_link_tree;
import Service.Handling.table_handling;
import Service.Language_ExecutePlan_builder.ExecutePlan_builder;
import Service.Language_disposer.InitLanguage_node;
import Service.Language_disposer.LanguageNode_shifter;
import Data.classes.Language_node;
import Data.Vessel.Word;
import Service.Language_disposer.Word_Segmentation_Machine;
import Windows.Personal_Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import m_Exception.FileSystem.ClassNotFound;
import m_Exception.Language_error;
import m_Exception.xml_reader.fileReader_error;
import org.dom4j.DocumentException;

public class Service implements Callback
{
    String rootPath,dbname;
    table_handling th;
    InitLanguage_node allNodes;
    FileSystem_link_tree FLG;
    Personal_Window PW;
    check_StringtoNumber csn;
    HashMap<String,Language_node> word_Map;
    LanguageNode_shifter LNS;
    Word_Segmentation_Machine WSS;
    ExecutePlan_builder EPB;
    ExecutePlan_Package EPP;
    IMonitorService System_info;
    ArrayList<String> clause_stop;
    
    int listsize;
    public Service(String rootpath,String db_name)
    {
        rootPath=rootpath;
        dbname=db_name;
        System_info=new MonitorService();//初始化系统信息类
        csn =new check_StringtoNumber();
        allNodes=new InitLanguage_node();//初始化单词节点
        clause_stop=allNodes.setclause_stop();//初始化子句起始
        word_Map=allNodes.setWord_Map();//建立单词节点转移地图
        EPB=new ExecutePlan_builder(this,clause_stop);
        th = new table_handling(this,csn);//初始化执行引擎，当然现在还没有上升到执行引擎的层次
        WSS=new Word_Segmentation_Machine(word_Map);//分词器
        LNS=new LanguageNode_shifter(word_Map);//自动机
        
        Boolean detectHash;
        listsize=Integer.valueOf(env_properties.getEnvironment("listlength"));
        detectHash=Boolean.valueOf(env_properties.getEnvironment("detectHash"));
        th.setlistsize(listsize);
        
        try {//加载数据文件
            FLG=new FileSystem_link_tree(rootPath,dbname,detectHash,th);//此处有问题，尚待改进//已修改
        } catch (DocumentException ex) {
            System.out.println(ex.getMessage());
            return;
        } catch (fileReader_error ex) {
            System.out.println(ex.getMessage());
            return;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }
        
        PW=new Personal_Window(this);//用户界面放在最后进行初始化
    }
    
    public void running()
    {
        for(;;)
            PW.inputStream();
    }
    
    public void Language_dispose(String SQL) throws ClassNotFound, Exception//由用户界面回调
    {
        LinkedList<Word> word_list;
        try {
            word_list=WSS.Segment(SQL);
            LNS.shifter(word_list);
        } 
        catch(NoSuchElementException ex)
        {
            System.out.println("第一个单词处于允许的单词范围之外");
            return;
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }
        create_ExecutePlan(word_list);
    }
    
    public void create_ExecutePlan(LinkedList<Word> word_list) throws ClassNotFound, Language_error, Exception
    {
        EPP=EPB.make_ExecutePlan(word_list);
        Implement_ExecPlan(EPP);
    }
    public boolean check_File_exist(String tablespace,String table) throws ClassNotFound//执行计划生成时检查表级以上对象
    {
        return FLG.checkFile(tablespace, table);
    }

    public void Implement_ExecPlan(ExecutePlan_Package EPP) throws Exception
    {
        th.Implement_Plan(EPP);
    }
    public FileSystem_link_tree call_FileSystem()
    {
        return FLG;
    }
}
