/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Edgar
 */
package source;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.*;
import org.jfree.util.Log;
import org.jfree.util.PrintStreamLogTarget;

public class OlapGraphicsData extends JPanel{
    
    private static final int PWIDTH = 400;   // size of panel
    private static final int PHEIGHT = 200; 

    private static int BOUNDSIZE = 100;  // larger than world
    
    DBSQLConnect DBC;
    DBSQLStatement DBS;
    
    Vector DimensionsVect = new Vector();
    Vector MeasuresVect = new Vector();
    
    String TblName;
    String Relation;
    
    int Test;
    
    
     /*VGH*/
    String dataset_name;
    String first_dataset_name;
    String second_dataset_name;
    
    CategoryDataset first_dataset, second_dataset;
    

  public OlapGraphicsData(String nombre,Vector MVector) {
    setLayout( new BorderLayout() );
    setOpaque( false );
    setPreferredSize( new Dimension(PWIDTH, PHEIGHT));
    
    MeasuresVect = MVector;
    this.add(createDemoPanel("Example"));
    //JFreeChart jfreechart = createChart(createDataset());
    //ChartPanel chart= new ChartPanel(jfreechart);
    //createDemoPanel();
    
  }

  
  /**
   * Devuelve los datos del grafico
   */
  private CategoryDataset createDataset(String nombre) {
  
      
      DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
    
    if(DimensionsVect.size() == 0){
        defaultcategorydataset.addValue(1,nombre, "0-10");
        defaultcategorydataset.addValue(3,nombre, "10-20");
        defaultcategorydataset.addValue(7,nombre, "20-30");
        defaultcategorydataset.addValue(10,nombre, "30-40");
        defaultcategorydataset.addValue(15,nombre, "40-50");
        defaultcategorydataset.addValue(19,nombre, "50-60");
        defaultcategorydataset.addValue(13,nombre, "60-70");
        defaultcategorydataset.addValue(9,nombre, "70-80");
        defaultcategorydataset.addValue(2,nombre, "80-90");
        defaultcategorydataset.addValue(0,nombre, "90-100");
    }else{
        Vector Columns = new Vector ();
        Vector TableName = new Vector ();
	Vector Predicate = new Vector ();
	Vector OrderBy = new Vector ();
	Vector GroupBy = new Vector ();
	String[] values = nombre.split("-");
        
        
        
        if(Test == 1 || Test == 3 || Test == 5 || Test == 6){
            
        String[] names = new String [2];
        String[] results = new String [2];
        
	TableName.addElement(TblName);
        Columns.addElement(MeasuresVect.elementAt(0));
        Columns.addElement("count("+MeasuresVect.elementAt(0)+") as n");
        
        String pred = "";
        
        for(int i=0; i < DimensionsVect.size() ; i++){
            if(i > 0){
                pred += " AND ";
            }
            pred += DimensionsVect.elementAt(i) + "=" + values[i];
        }
        /*if(DimensionsVect.size() == 3){
            Predicate.addElement(DimensionsVect.elementAt(0) + "=" + values[0] + " AND " + DimensionsVect.elementAt(1) + "=" + values[1] + " AND " + DimensionsVect.elementAt(2) + "=" + values[2]);
        }else if(DimensionsVect.size() == 2){    
            Predicate.addElement(DimensionsVect.elementAt(0) + "=" + values[0] + " AND " + DimensionsVect.elementAt(1) + "=" + values[1] );
        }else if(DimensionsVect.size() == 1){
            Predicate.addElement(DimensionsVect.elementAt(0) + "=" + values[0]);
        }*/
        Predicate.addElement(pred);
        
        GroupBy.addElement(MeasuresVect.elementAt(0));
        OrderBy.addElement(MeasuresVect.elementAt(0));
        
	String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
        
        DBC.sendQuery (sqlStat);
        names[0] = (String)MeasuresVect.elementAt(0);
        names[1] = "n";
        
        results = DBC.getResult (names);
        
        Vector measure = new Vector();
        Vector count = new Vector();
        
        while (results.length > 0){
            measure.addElement(results[0]);
            count.addElement(results[1]);
            results = DBC.getResult (names);
        }
        
        int divs = measure.size()/10;
        divs++;
        int posicion = 0;
        int sum = 0;
        String st="",fsh="";
        
        for(int i=0; i<10; i++){
            if(posicion < measure.size()){
                st=(String)measure.get(posicion);
                for(int j=0; j<divs; j++){
                    if(posicion < measure.size()){
                        sum += Integer.valueOf((String)count.get(posicion));
                        posicion++;
                    }else{
                        i=10;
                    }
                }
                fsh=(String)measure.get(posicion-1);
                defaultcategorydataset.addValue(sum,nombre,st+"-"+fsh);
                sum=0;
            }
        }
        }
        else if(Test == 4){
            
            String[] names = new String [4];
            String[] results = new String [4];
            String relations[] = Relation.split("-");
            
            TableName.addElement(OLAPStatTest.CompareStatementsTableName + "PP" );
            Columns.addElement("n1");names[0]="n1";
            Columns.addElement("n2");names[1]="n2";
            Columns.addElement("nt1");names[2]="nt1";
            Columns.addElement("nt2");names[3]="nt2";
            
            if(relations.length == 1 && relations[0].equals("")){
                if(DimensionsVect.size() == 3){
                    Predicate.addElement(DimensionsVect.elementAt(0) + "1=" + values[0] +  
                                    " AND " + DimensionsVect.elementAt(1) + "1=" + values[1] +
                                    " AND " + DimensionsVect.elementAt(2) + "1=" + values[2]);
                }
                else if(DimensionsVect.size() == 2){
                    Predicate.addElement(DimensionsVect.elementAt(0) + "1=" + values[0] +  
                                    " AND " + DimensionsVect.elementAt(1) + "1=" + values[1]);
                }
                else if(DimensionsVect.size() == 1){
                    Predicate.addElement(DimensionsVect.elementAt(0) + "1=" + values[0]);
                }
            }else{
                if(DimensionsVect.size() == 3){
                    Predicate.addElement(DimensionsVect.elementAt(0) + "1=" + values[0] + " AND " + DimensionsVect.elementAt(0) + "2=" + relations[0] + 
                                    " AND " + DimensionsVect.elementAt(1) + "1=" + values[1] + " AND " + DimensionsVect.elementAt(1) + "2=" + relations[1] + 
                                    " AND " + DimensionsVect.elementAt(2) + "1=" + values[2] + " AND " + DimensionsVect.elementAt(2) + "2=" + relations[2]);
                }
                else if(DimensionsVect.size() == 2){
                    Predicate.addElement(DimensionsVect.elementAt(0) + "1=" + values[0] + " AND " + DimensionsVect.elementAt(0) + "2=" + relations[0] + 
                                    " AND " + DimensionsVect.elementAt(1) + "1=" + values[1] + " AND " + DimensionsVect.elementAt(1) + "2=" + relations[1]);
                }
                else if(DimensionsVect.size() == 1){
                    Predicate.addElement(DimensionsVect.elementAt(0) + "1=" + values[0] + " AND " + DimensionsVect.elementAt(0) + "2=" + relations[0]);
                }
            }
        
            String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
            
            DBC.sendQuery (sqlStat);
            results = DBC.getResult (names);
            
            if(results.length == 0){
                this.removeAll();
            }
            else{
                defaultcategorydataset.addValue(Integer.parseInt(results[2]),nombre,"Total");
                defaultcategorydataset.addValue(Integer.parseInt(results[0]),nombre,"N");
            }
            
        }
        else if(Test == 2){
            
            String[] names = new String [2];
            String[] results = new String [2];
            
            TableName.addElement(OLAPStatTest.MSVTableName );
            Columns.addElement("n");names[0]="n";
            Columns.addElement("nt");names[1]="nt";
            
            if(DimensionsVect.size() == 3){
                Predicate.addElement(DimensionsVect.elementAt(0) + "=" + values[0] +  
                                    " AND " + DimensionsVect.elementAt(1) + "=" + values[1] +
                                    " AND " + DimensionsVect.elementAt(2) + "=" + values[2]);
            }
            else if(DimensionsVect.size() == 2){
                Predicate.addElement(DimensionsVect.elementAt(0) + "=" + values[0] +  
                                    " AND " + DimensionsVect.elementAt(1) + "=" + values[1]);
            }
            else if(DimensionsVect.size() == 1){
                Predicate.addElement(DimensionsVect.elementAt(0) + "=" + values[0]);
            }
            
            String sqlStat = DBS.dbSelect (Columns, TableName, Predicate, OrderBy, GroupBy, -1);
            
            DBC.sendQuery (sqlStat);
            results = DBC.getResult (names);
            
            if(results.length == 0){
                this.removeAll();
            }
            else{
                defaultcategorydataset.addValue(Integer.parseInt(results[1]),nombre,"Total");
                defaultcategorydataset.addValue(Integer.parseInt(results[0]),nombre,"N");
            }
            
        }
                
    }
    
    
    /*VGH*/
    
   // System.out.println("NonVan---------------"+this.dataset_name);
    
   
    
   
      //if(dataset_name.equals("Primero") || this.dataset_name == null || this.dataset_name.equals("") )
      /* if((this.dataset_name == null) || (this.dataset_name.equals("Primero")))
     
     {
       System.out.println("Nombre primero es : **********"+this.dataset_name);
        this.first_dataset=defaultcategorydataset;
         System.out.println("PriValor"+defaultcategorydataset.getValue(0, 0));
         System.out.println("SegValor"+defaultcategorydataset.getValue(0, 1));
       }
       else if(dataset_name.equals("Segundo"))
       {
       System.out.println("Nombre segundo es : **********"+dataset_name);
        this.second_dataset=defaultcategorydataset;
         System.out.println("TerValor"+defaultcategorydataset.getValue(0, 0));
         System.out.println("CuarValor"+defaultcategorydataset.getValue(0, 1));
       
       
      
         
         
           //return new ChartPanel(jfreechartT);
       
       
       }
    */
    /*VGH*/
    
    
    return defaultcategorydataset;
  }

  /**
   * Devuelve el grafico construido en funcion de los datos que le son pasados.
   */
  private JFreeChart createChart(CategoryDataset categorydataset) {
    String measure = "measure";
    if(MeasuresVect.size() != 0){
        measure=(String)MeasuresVect.elementAt(0);
    }
    JFreeChart jfreechart = ChartFactory.createBarChart("",measure,"Frecuencia", categorydataset, PlotOrientation.VERTICAL, true, true, false);
    
    CategoryPlot categoryplot = jfreechart.getCategoryPlot();
    
    categoryplot.setAnchorValue(2D);
    
    categoryplot.setForegroundAlpha(1.0F);
    CategoryAxis categoryaxis = categoryplot.getDomainAxis();
    categoryaxis.setLabelFont(new Font("Arial Black",Font.PLAIN,8));
    
    CategoryLabelPositions categorylabelpositions = categoryaxis.getCategoryLabelPositions();
    CategoryLabelPosition categorylabelposition = new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, 0.0D, CategoryLabelWidthType.RANGE, 0.5F);
    categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.replaceRightPosition(categorylabelpositions, categorylabelposition));
    
    return jfreechart;
  }
/*VGH*/
  /*This is where the grafhic two bar is made */
  
   private JFreeChart createChartT(CategoryDataset categorydataset,CategoryDataset categorydataset1) {
 
       categorydataset1.getValue(0, 0);
       System.out.println("Primer Valor"+categorydataset.getValue(0, 0));
       System.out.println("Segundo Valor"+categorydataset.getValue(0, 1));
       System.out.println("Tercer Valor"+categorydataset1.getValue(0, 0));
       System.out.println("Cuarto Valor"+categorydataset1.getValue(0, 1));
       
       /*VGH voy a integrar en un nuevo data set para mostrar los datos*/
       DefaultCategoryDataset datasetUnion = new DefaultCategoryDataset();
       
       datasetUnion.setValue(categorydataset.getValue(0, 0),categorydataset.getColumnKey(0),categorydataset.getRowKey(0));
       datasetUnion.setValue(categorydataset.getValue(0, 1),categorydataset.getColumnKey(1),categorydataset.getRowKey(0));
       datasetUnion.setValue(categorydataset1.getValue(0, 0),categorydataset1.getColumnKey(0),categorydataset1.getRowKey(0));
       datasetUnion.setValue(categorydataset1.getValue(0, 1),categorydataset1.getColumnKey(1),categorydataset1.getRowKey(0));
       
      
     //Nombres de las columnas  
     System.out.print("Col1: "+categorydataset.getColumnKey(0)+"--");
     System.out.print("Col2:"+categorydataset.getColumnKey(1)+"--");
     
     //VGH Valor de la coordenada
     System.out.print("Coordenada: "+categorydataset.getRowKey(0) +"--");
    
      
      
       String measure = "Medida";
    if(MeasuresVect.size() != 0){
        measure=(String)MeasuresVect.elementAt(0);
    }
   // JFreeChart jfreechart = ChartFactory.createBarChart("Integracion",measure,"Frecuencia", categorydataset, PlotOrientation.VERTICAL, true, true, false);
 JFreeChart jfreechart = ChartFactory.createBarChart(
            "Integracion",         // chart title
            "Measure",               // domain axis label
            "Frecuencia",                  // range axis label
            datasetUnion,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips?
            false                     // URLs?
        );
    
    jfreechart.setBackgroundPaint(Color.white); //Background color
    CategoryPlot categoryplot = jfreechart.getCategoryPlot();
    
    categoryplot.setAnchorValue(2D);
    
    categoryplot.setForegroundAlpha(1.0F);
    CategoryAxis categoryaxis = categoryplot.getDomainAxis();
    categoryaxis.setLabelFont(new Font("Arial Black",Font.PLAIN,8));
    
    CategoryLabelPositions categorylabelpositions = categoryaxis.getCategoryLabelPositions();
    CategoryLabelPosition categorylabelposition = new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, 0.0D, CategoryLabelWidthType.RANGE, 0.5F);
    categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.replaceRightPosition(categorylabelpositions, categorylabelposition));
    
    return jfreechart;
  }
  
  
  /**
   * Devuelve un JPanel con el grafico
   */
  public JPanel createDemoPanel(String nombre) {
    JFreeChart jfreechart = createChart(createDataset(nombre));
    return new ChartPanel(jfreechart);
  }
  
  /*VGH*/
  
  
  
  
  public JPanel createDemoPanelT(String nombre) {
    
      
      
    System.out.println("Nombre del dataset"+this.dataset_name);  
    if(this.dataset_name.equals("Primero"))
    {
       // JFreeChart jfreechart = createChart(createDataset(nombre));
        // return new ChartPanel(jfreechart);
        
        this.first_dataset = createDataset(nombre);
        
    }
    else if(this.dataset_name.equals("Segundo"))
    {
       this.second_dataset = createDataset(nombre);
      JFreeChart jfreechartT = createChartT(this.first_dataset, this.second_dataset);
       
   // this.first_dataset.
    
      return new ChartPanel(jfreechartT);
    }
    
   JFreeChart jfreechart = createChart(createDataset(nombre));
     
  
       return new ChartPanel(jfreechart);
      
      
      
      
      //this.getNombrePrimerDataset();
     
      // System.out.println("Nombre es : **********"+dataset_name);
    
  /*     if(dataset_name.equals("Primero"))
       {
       System.out.println("Nombre primero es : **********"+dataset_name);
        first_dataset=createDataset(nombre);
        * 
        * 
       }*/
      /*createDataset(nombre);
        if(dataset_name.equals("Segundo"))
       {
       System.out.println("Nombre segundo aqui es : **********"+dataset_name);
        //second_dataset=createDataset(nombre);
       System.out.println("1er val"+this.first_dataset.getValue(0, 0)); 
       System.out.println("2do val"+this.first_dataset.getValue(0, 1));
       
       JFreeChart jfreechartT = createChartT(first_dataset,second_dataset);
        return new ChartPanel(jfreechartT);
    
    
       
       }
           else
       {
       
     
       }
           
      /*8 de Julio en este punto tengo dos datasets, los tengo que agrupar para mostrarlos grafica*/     
      
      //System.out.println("Columnas"+first_dataset.getColumnCount());
      
      //System.out.println("Primer Valor"+ first_dataset.getValue(0, 0));
      // System.out.println("Segundo Valor"+ first_dataset.getValue(0, 1));
        
    /*   if(second_dataset.getValue(0, 0) == null)
       {
       System.out.println("No es posible imprimir valores");
       }
       else
       {
       System.out.println("Tercer Valor"+ second_dataset.getValue(0, 0));
        System.out.println("Cuarto Valor"+ second_dataset.getValue(0, 1));
       }*/
     
     // JFreeChart jfreechartT = createChartT(first_dataset,second_dataset);
      //JFreeChart jfreechart = createChart(createDataset(nombre));
     
   // createDataset(nombre);
       //return new ChartPanel(jfreechart);
  }
  
  
  /*VGH*/
  private CategoryDataset createDatasetT() {
        
        // row keys...
        final String series1 = "Primero";
        final String series2 = "Segundo";
        //final String series3 = "Tercero";

        // column keys...
        final String category1 = "Categoria 1";
        final String category2 = "Categoria 2";
        //final String category3 = "Categoria 3";
        //final String category4 = "Categoria 4";
        //final String category5 = "Categoria 5";

        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(1.0, series1, category1);
        dataset.addValue(4.0, series1, category2);
       // dataset.addValue(3.0, series1, category3);
       // dataset.addValue(5.0, series1, category4);
       // dataset.addValue(5.0, series1, category5);

        dataset.addValue(5.0, series2, category1);
        dataset.addValue(7.0, series2, category2);
       // dataset.addValue(6.0, series2, category3);
       // dataset.addValue(8.0, series2, category4);
       // dataset.addValue(4.0, series2, category5);

     //   dataset.addValue(4.0, series3, category1);
     //   dataset.addValue(3.0, series3, category2);
     //   dataset.addValue(2.0, series3, category3);
     //   dataset.addValue(3.0, series3, category4);
     //   dataset.addValue(6.0, series3, category5);
        
        return dataset;
        
    }
  
  
  public void refresh(DBSQLConnect dbConn, DBSQLStatement dbStat, String nombre, String relation, Vector dimensions, Vector measures, String tblname, int test){
      DBC = dbConn;
      DBS = dbStat;
      DimensionsVect = dimensions;
      MeasuresVect = measures;
      TblName = tblname;
      Test = test;
      Relation = relation;
      
      this.removeAll();
      this.add(createDemoPanel(nombre));
      this.updateUI();
  }

  /*VGH*/
  public void refreshT(DBSQLConnect dbConn, DBSQLStatement dbStat, String nombre, String relation, Vector dimensions, Vector measures, String tblname, int test, String wdataset){
      DBC = dbConn;
      DBS = dbStat;
      DimensionsVect = dimensions;
      MeasuresVect = measures;
      TblName = tblname;
      Test = test;
      Relation = relation;
      
       System.out.println("Nombre es : **********"+wdataset);
      this.dataset_name="";
      this.dataset_name=wdataset;
      
      this.removeAll();
      this.add(createDemoPanelT(nombre));
      this.updateUI();
  }
  
  /*public static void main(String[] args) {
    OlapGraphicsData barra = new OlapGraphicsData();
  }*/

  
  /*VGH*/
public void setNombrePrimerDataset(String nombre){
first_dataset_name=nombre;
}

public String getNombrePrimerDataset()
{
return first_dataset_name;
}

  
}
    

