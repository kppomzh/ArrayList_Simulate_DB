package Data.Verticaltype;

import Service.Check.check_StringtoNumber;
import java.util.ArrayList;
import java.util.LinkedList;
import m_Exception.CantTransacation_error;


public class Vertical_Integer extends Vertical_column_old<Integer> 
{
    public Vertical_Integer(String vertical_name)
    {
        super(vertical_name);
        element=new Integer[1];
    }
    public Vertical_Integer(Integer[] newelement,String newname)
    {
        super(newelement,newname);
    }

    
    @Override
    public boolean insert(Integer element)
    {
        boolean result=false;
        this.element[this.Size]=element;
        this.Size++;
        result=true;
        return result;
    }
    
    @Override
    public void update(Integer[] line,Integer element)
    {
        for(int loop=0;loop<line.length;loop++)
        {
            this.element[line[loop]]=new Integer(element);
        }
    }
    
    @Override
    public  ArrayList<Integer> Pick_Condition(String conditionSymbol, Integer conditionValue) throws Exception
    {
        //Integer temp = null;
        ArrayList<Integer> Return=new ArrayList();
/*        if(!conditionValue.getClass().getSimpleName().equals(element[0].getClass().getSimpleName()))
        {//根据类型进行强制转换，或者不能转换的类型抛出错误
            if(conditionValue.getClass().getSimpleName().equals("String"))
            {
                if(check_StringtoNumber.check_StringtoInteger((String) conditionValue))
                    temp=Integer.valueOf((String)conditionValue);
                else
                {
                    e=new CantTransacation_error();
                    throw e;
                }
            }
            else if(conditionValue.getClass().getSimpleName().equals("Double"))
                temp=((Double)conditionValue).intValue();
        }
        else 
            temp=(Integer)conditionValue;//temp=conditionValue;*/
        
        switch(conditionSymbol)
        {
            case "is":case "=":
                for(int loop=0;loop<this.Size;loop++) 
                    if(element[loop]==null);
                    else if(element[loop]==conditionValue)
                        Return.add(loop);
                break;
            case "isnot":case "!=":
                for(int loop=0;loop<this.Size;loop++) 
                    if(conditionValue==null)
                    {
                        if(element[loop]!=null)
                            Return.add(loop);
                    }
                    else if(element[loop]==null);
                    else if(element[loop]!=conditionValue)
                        Return.add(loop);
                break;
            case ">=":
                for(int loop=0;loop<this.Size;loop++)  
                    if(element[loop]==null);
                    else if(element[loop]>=conditionValue)
                        Return.add(loop);
                break;
            case "<=":
                for(int loop=0;loop<this.Size;loop++) 
                    if(element[loop]==null);
                    else if(element[loop]<=conditionValue) 
                        Return.add(loop);
                break;
            case ">":
                for(int loop=0;loop<this.Size;loop++) 
                    if(element[loop]==null);
                    else if(element[loop]>conditionValue)
                        Return.add(loop);
                break;
            case "<":
                for(int loop=0;loop<this.Size;loop++) 
                    if(element[loop]==null);
                    else if(element[loop]<conditionValue)
                        Return.add(loop);
                break;
        }
        
        return Return;
    }
    @Override
    public Vertical_Integer checkout(String newname, Integer[] p_c)
    {
        Integer[] newelement=new Integer[p_c.length];
        for(int loop=0;loop<p_c.length;loop++)
        {
            newelement[loop]=this.element[p_c[loop]];
        }
        return new Vertical_Integer(newelement,newname);
    }
    
    @Override
    public void realloc(int newmem) throws Exception
    {
        int loopmax;
        if(this.Size>newmem)
            loopmax=newmem;
        else
            loopmax=this.Size;
        Integer[] temp=this.element;
        int[] tempindex=this.index_lable;
        this.element=new Integer[newmem];
        this.index_lable=new int[newmem];
        for(int loop=0;loop<loopmax;loop++)
        {
            this.element[loop]=temp[loop];
            this.index_lable[loop]=tempindex[loop];
        }
        this.mem=newmem;
    }

    @Override
    public int compare(Integer c1, Integer c2) {
        if(c1>=c2)
            return 1;
        else return -1;
    }
}
