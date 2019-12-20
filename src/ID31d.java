/**
 *
 * @author Vipin Jose
 */
import java.io.*;
import java.util.*;

public class ID31d {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        //set cross validation here
        DecisionTree.crossValidation=false;
        
        //set maximum depth required for the tree
        DecisionTree.maxDepth=100000;
        
        //enter depths to be crossvalidated.
        int [] depthVals = {10000};
               
        if(DecisionTree.crossValidation){
            int g=0;
            DecisionTree.depthSize=depthVals.length;
            float [] accuracyCurrDepth=new float[6];
            float [] allAccuracy = new float[DecisionTree.depthSize];
            for (int currDepth : depthVals){
                DecisionTree.iterDeptCount=0;
                System.out.println("\n");
                System.out.println("Depth="+currDepth);
                System.out.println("**************");
                DecisionTree.maxDepth=currDepth;
                
                //Mention the path of files here.
                String path="id3/SettingC/CVSplits/";
                
                for(int i=0;i<6;i++){
 
                    String file0=path+"training_0"+((i)%6)+".data";
                    String file1=path+"training_0"+((i+1)%6)+".data";
                    String file2=path+"training_0"+((i+2)%6)+".data";
                    String file3=path+"training_0"+((i+3)%6)+".data";
                    String file4=path+"training_0"+((i+4)%6)+".data";
                    String file5=path+"training_0"+((i+5)%6)+".data";               
                    String fileout=path+"merge"+i+".data";
                    System.out.println("\nTesting on "+file5);

                    PrintWriter writer = new PrintWriter(fileout);
                    ArrayList arr=new ArrayList();

                    Scanner fileIn0 = new Scanner (new File (file0));
                    Scanner fileIn1 = new Scanner (new File (file1));
                    Scanner fileIn2 = new Scanner (new File (file2));
                    Scanner fileIn3 = new Scanner (new File (file3));
                    Scanner fileIn4 = new Scanner (new File (file4));
                    Scanner fileIn5 = new Scanner (new File (file5));

                    while (fileIn0.hasNextLine()){
                        writer.println(fileIn0.nextLine());
                        writer.flush();
                    }
                    while (fileIn1.hasNextLine()){
                        writer.println(fileIn1.nextLine());
                        writer.flush();
                    }
                    while (fileIn2.hasNextLine()){
                        writer.println(fileIn2.nextLine());
                        writer.flush();
                    }
                    while (fileIn3.hasNextLine()){
                        writer.println(fileIn3.nextLine());
                        writer.flush();
                    }
                    while (fileIn4.hasNextLine()){
                        writer.println(fileIn4.nextLine());
                        writer.flush();
                    } 

                    DecisionTree.trainFile=fileout;
                    DecisionTree.testFile=file5;
                    
                    DecisionTree dt = new DecisionTree();
                    dt.startTree();
                    accuracyCurrDepth[DecisionTree.iterDeptCount++]=DecisionTree.accuracy;
                }

                float avgAccuracy=0;
                float sd=0;
                float variance=0;
                float sdSum=0;
                for(int k=0;k<DecisionTree.iterDeptCount;k++){
                    avgAccuracy+=accuracyCurrDepth[k];
                }
                
                avgAccuracy=avgAccuracy/DecisionTree.iterDeptCount;
                System.out.print("\nAverage accuracy for depth value "+currDepth+" = "+avgAccuracy*100+"%");
                for(int k=0;k<DecisionTree.iterDeptCount;k++){
                    sdSum+=(avgAccuracy-accuracyCurrDepth[k])*(avgAccuracy-accuracyCurrDepth[k]);
                }
                variance=sdSum/DecisionTree.iterDeptCount;
                sd=(float) Math.sqrt(variance);
                System.out.print("\nStandard deviation for depth value "+currDepth+" = "+sd);
                allAccuracy[g++]=avgAccuracy;
                System.out.println("\n*****************************************************");              
            }
            float maxAccu=0;
            int maxAccuInd=0;
            maxAccu=allAccuracy[0];        
            for(int j=1;j<g;j++){
                if(allAccuracy[j]>maxAccu){
                    maxAccu=allAccuracy[j];
                    maxAccuInd=j;
                }
            }
            System.out.println("\n\nMaximum accuracy corresponds to depth "+depthVals[maxAccuInd]+" with value "+maxAccu*100+"%");
        }
        if (!DecisionTree.crossValidation){

            DecisionTree dt = new DecisionTree();

            dt.startTree();
        }
    }
}

class DecisionTree{
    
    //Mention the Training File here.
    public static String trainFile="id3/SettingA/training.data";
    
    //Mention the Test File here.
    public static String testFile="id3/SettingA/training.data";
    
    public static int maxDepth=100000;
    
    public static boolean crossValidation=false;
    public static int depthSize;
    public static int iterDeptCount=0;
    
    public static float accuracy;

    int trainSize=0, columnSize=0;
    int testRow=0, testCol=0;
    int depth=0;
    boolean firstIter=true;
    String maxLabel;
    String[][] train;
    String [][] test;
    String prediction[];
    Node startRoot;

    DecisionTree() throws FileNotFoundException {
        Scanner trainIn = new Scanner (new File (trainFile));
        Scanner testIn = new Scanner (new File (testFile));
        
        int v=0;

        while (trainIn.hasNextLine()){
            trainIn.nextLine();
            trainSize++;
        }
        train = new String[trainSize][];
        Scanner trainIn1 = new Scanner (new File (trainFile));        
        while (trainIn1.hasNextLine()){
            train[v++]=trainIn1.nextLine().split(",");
        }
        columnSize=train[0].length;
        
        v=0;
        while (testIn.hasNextLine()){
            testIn.nextLine();
            testRow++;
        }
        test = new String[testRow][];
        prediction=new String[testRow];
        Scanner testIn1 = new Scanner (new File (testFile));        
        while (testIn1.hasNextLine()){
            test[v++]=testIn1.nextLine().split(",");
        }
        testCol=test[0].length;
    }


    
    class Node{
        String [][] data;
        float [] iG;
        int feature;
        String mapping;
        String isLeaf="No";
        int nodeCol=0;
        int nodeRow=0;
        String leaf;
        int [] labelCount = new int[2];
        String [] labels = new String[2];
        LinkedList fV=new LinkedList();
        List <Node> children;

        
        Node(String [][] data,String mapping,int nodeRow){
            this.nodeCol=columnSize;
            this.data = new String[nodeRow][this.nodeCol];
            iG = new float[this.nodeCol-1];
            this.feature=feature;
            this.mapping=mapping;
            this.nodeRow=nodeRow;
            for(int i=0;i<nodeRow;i++){
                for(int j=0;j<nodeCol;j++){
                    this.data[i][j]=data[i][j];
                }
            }
            children = new ArrayList<>();
        }
        
        void checkLabel(){
            int [] count = new int[2];
            labelCount[0]=0; labelCount[1]=0;
            String strLabel;
            strLabel=data[0][nodeCol-1];
            labels[0]=strLabel;
            for (int k=0;k<nodeRow;k++){
                if (data[k][nodeCol-1].equals(strLabel)){
                    labelCount[0]+=1;
                }
                else{

                    if(labelCount[1]==0) 
                         labels[1]=data[k][nodeCol-1];
                    labelCount[1]+=1;
                }
            }
        }
        
        void calculateiG(){
            int[] labelForFeat=new int[2];
            boolean flag=false;
            int [] valCount = new int[nodeRow];
            String [] valValue = new String[nodeRow];
            int noOfVals,total;
            float labelEntropy;
            float labelTotal=labelCount[0]+labelCount[1];
            labelEntropy= 0;
            for(int i=0;i<=1;i++){
                if(labelCount[i]!=0)
                    labelEntropy = (float) ((labelEntropy + ( -1 * ((labelCount[i])/labelTotal)) * ((Math.log(labelCount[i]/labelTotal))/Math.log(2))));                 
            }
            for(int i=0;i<nodeCol-1;i++){
                if(!(data[0][i].equals("Test#*#*"))){    
                    noOfVals=0;
                    valValue[0]=data[0][i];
                    for(int j=0;j<nodeRow;j++){
                        for(int k=0;k<j;k++){
                            if(data[k][i].equals(data[j][i])){
                                flag=true;
                            }

                        }
                        if(!flag){
                            valValue[noOfVals++]=data[j][i];
                        }
                        else
                            flag=false;
                    }
                    for(int k=0;k<noOfVals;k++){
                        valCount[k]=0;
                    }

                    for(int j=0;j<nodeRow;j++){
                        for(int k=0;k<noOfVals;k++){
                            if((data[j][i]).equals(valValue[k])){
                                valCount[k]+=1;
                            }

                        }

                    }
                    total=0;
                    for(int k=0;k<noOfVals;k++){
                        total+=valCount[k];
                    }
                    //Calculation of Entropy
                    float entropy=0;
                    float entropyPart=0;
                    for(int k=0;k<noOfVals;k++){
                        labelForFeat[0]=0;
                        labelForFeat[1]=0;
                        for(int j=0;j<nodeRow;j++){
                            if(data[j][i].equals(valValue[k])){
                                if(data[j][nodeCol-1].equals(labels[0]))
                                    labelForFeat[0]+=1;
                                else
                                    if(data[j][nodeCol-1].equals(labels[1]))
                                        labelForFeat[1]+=1;
                            }                    
                        }
                        int tempTotal=labelForFeat[0]+labelForFeat[1];
                        entropy=0;
                        for(int n=0;n<=1;n++){
                            if(labelForFeat[n]!=0)
                                entropy = (float) ((entropy + ( -1 * (((float)labelForFeat[n])/(float)tempTotal)) * ((Math.log((float)labelForFeat[n]/(float)tempTotal))/(float)Math.log(2))));
                        }
                        entropyPart+=(float)((valCount[k]/(float)total) * entropy);
                    }
                    iG[i]=labelEntropy-entropyPart;
                }
                else
                    iG[i]=-1;
            }      
        }
    }
    
    void startTree(){
        
        //Pre-Processing: Method 1~Setting the missing feature as the majority feature value.
          preProcess1();
          preProcess11();  
        
        //Pre-Processing: Method 2~Setting the missing feature as the majority value of that label. 
    /*  preProcess2();
        preProcess21();  */
        
        //Pre-Processing: Method 3~Treating the missing feature as a \textit{special} feature
        //by Default
        
        startRoot = new Node(train,null,trainSize);
        buildTree(startRoot,0);

 /*       
        for(int i=0;i<testRow;i++){
            predict(startRoot,i);
            if(prediction[i]==null)
                prediction[i]=maxLabel;
        }
     */   
   //For printing the path 
        for(int i=0;i<testRow;i++){
            showPath(startRoot,i,0);
            System.out.println();
        }
        System.out.println("Maximum Depth of the tree: "+depth);
        
 /*    
   //For printing predictions
        for(int i=0;i<testRow;i++){
            for(int j=0;j<testCol;j++){
                System.out.print(test[i][j]+" ");
            }
            System.out.println(prediction[i]);
        }
  */      
   //For printing accuracy
        checkAccuracy();
    }
    
    void buildTree(Node root,int cDepth){
       root.checkLabel();
       if(firstIter){
           firstIter=false;
           if((root.labelCount[0])>(root.labelCount[1]))
               maxLabel=root.labels[0];
           else    
               maxLabel=root.labels[1];
       }
       if(root.labelCount[0]==0){
            root.isLeaf="Yes";
            root.leaf=root.labels[1];
            return;
        }
        if(root.labelCount[1]==0){
            root.isLeaf="Yes";
            root.leaf=root.labels[0];
            return;
        } 
        
        if(cDepth==(maxDepth)){
            if((root.labelCount[0])>(root.labelCount[1])){
                root.isLeaf="Yes";
                root.leaf=root.labels[0];
                return;               
            }
            else{    
               root.isLeaf="Yes";
               root.leaf=root.labels[1];
               return; 
            }
        }

        root.calculateiG();
        float max=-1;
        int maxFeat=0;
        for(int i=0;i<root.nodeCol-1;i++){   
            if(root.iG[i]>max){
                max=root.iG[i]; 
                maxFeat=i;
            }
        }

        root.feature=maxFeat;
        boolean flag=false;
        int [] valCount = new int[root.nodeRow];
        String [] valValue = new String[root.nodeRow];
        int noOfVals,total;
        noOfVals=0;
        valValue[0]=root.data[0][maxFeat];
        for(int j=0;j<root.nodeRow;j++){
            for(int k=0;k<j;k++){
                if(root.data[k][maxFeat].equals(root.data[j][maxFeat])){
                    flag=true;
                }

            }
            if(!flag){
                valValue[noOfVals++]=root.data[j][maxFeat];
            }
            else
                flag=false;
        }
        int k=0,rowSize;
        for(int i=0;i<noOfVals;i++){
            root.fV.add(valValue[i]);
        }
        while(!root.fV.isEmpty()){
            String llNext;
            llNext=(String) root.fV.pop();          
            String [][] tempData=new String [root.nodeRow][root.nodeCol];
            String [][] newData=new String [root.nodeRow][root.nodeCol];
            rowSize=0;
            for(int m=0;m<root.nodeRow;m++){
                k=0;
                for(int n=0;n<root.nodeCol;n++){
                    if(root.data[m][maxFeat].equals(llNext)){
                        if(maxFeat==n){
                            tempData[m][n]="Test#*#*";
                        }
                        else{
                            tempData[m][n]=root.data[m][n];                           
                        }
                    }

                }
            }
           for(int t=0;t<root.nodeRow;t++){
               if((tempData[t][0]!=null)||(tempData[t][2]!=null)){
                   for(int s=0;s<root.nodeCol;s++){
                       newData[rowSize][s]=tempData[t][s]; 
                   }
                   rowSize++;
               }
            }
            Node child=new Node(newData,llNext,rowSize);
            root.children.add(child);
            buildTree(child,cDepth+1); 
        }
   
   }

 
    
    void predict(Node root,int i){
        if(root.isLeaf.equals("Yes")){
            prediction[i]=root.leaf;
            return;
        }
        else{
            Node tempNode;
            for(int j=0;j<root.children.size();j++){
                tempNode=root.children.get(j);
                if(tempNode.mapping.equals(test[i][root.feature])){
                    predict(tempNode,i);
                    return;
                }
  
            }

        }
        
    }
    
    
    void showPath(Node root,int i,int tempDepth){

     if(root.isLeaf.equals("Yes")){
         System.out.print(root.leaf);
         if(tempDepth>depth)
             depth=tempDepth;
         return;
     }
     else{
         Node tempNode;
         for(int j=0;j<root.children.size();j++){
             tempNode=root.children.get(j);
             if(tempNode.mapping.equals(test[i][root.feature])){
                 System.out.print(tempNode.mapping+"->");
                 tempDepth++;
                 showPath(tempNode,i,tempDepth); 
             } 
         }
      }  
    }
    
    void checkAccuracy(){
        float accuracyCount=0;
        float error=0;
        float accu=0;
        for(int i=0;i<testRow;i++){
            if(test[i][columnSize-1].equals(prediction[i])){
                accuracyCount++;
            }
        }
        error=(float)(testRow-accuracyCount)/(float)testRow;
        accu=accuracyCount/testRow;
        System.out.println("Error: "+(error*100)+"%");
        System.out.println("Accuracy: "+(accu*100)+"%");
        if(crossValidation){
            DecisionTree.accuracy=accu;
        }
    }
    
    void preProcess1(){
        String max;
        for(int i=0;i<trainSize;i++){
            for(int j=0;j<columnSize;j++){
                if(train[i][j].equals("?"))
                    max=preProcessX1(j);
            }          
       }       
    }
    
    void preProcess2(){
        String max;
        for(int i=0;i<trainSize;i++){
            for(int j=0;j<columnSize;j++){
                    if(train[i][j].equals("?")){
                        max=preProcessX2(i,j);
                        train[i][j]=max;
                     //   System.out.println(max);
                }
            }          
       }
    
    }

    
    String preProcessX1(int i){
        int noOfVals=0;
        int total=0;
        boolean flag=false;
        String valValue[]=new String[trainSize];
        int valCount[]=new int[trainSize];
        noOfVals=0;
        for(int l=0;l<trainSize;l++){
            if(!train[l][i].equals("?")){
                valValue[0]=train[l][i];
                break;
            }
        }

        for(int j=0;j<trainSize;j++){
            if(!train[j][i].equals("?")){
                for(int k=0;k<j;k++){
                    if(train[k][i].equals(train[j][i])){
                        flag=true;
                    }

                }
                if(!flag){
                    valValue[noOfVals++]=train[j][i];
                }
                else
                    flag=false;
            }
        }
        for(int k=0;k<noOfVals;k++){
            valCount[k]=0;
        }

        for(int j=0;j<trainSize;j++){
            for(int k=0;k<noOfVals;k++){
                if((train[j][i]).equals(valValue[k])){
                    valCount[k]+=1;
                }

            }

        }
        
        total=0;
        for(int k=0;k<noOfVals;k++){
            total+=valCount[k];
        }
        

        int max=valCount[0];
        String maxValue="?";
        for(int f=0;f<noOfVals;f++){
            if((valCount[f])>max){
                maxValue=valValue[f];
            }
        }
        for(int u=0;u<trainSize;u++){
            if(train[u][i].equals("?")){
                train[u][i]=maxValue;
            }
        }
        return maxValue;
    }
    
    String preProcessX2(int k, int i){

        String label=train[k][columnSize-1];
        String[][] subset=new String[trainSize][columnSize];
        int f=0;

        
        for(int m=0;m<trainSize;m++){
            if(label.equals(train[m][columnSize-1])){
                for(int n=0;n<columnSize;n++){
                    subset[f][n]=train[m][n];
                }
                f++;
            }
        }

        int noOfVals=0;
        int total=0;
        boolean flag=false;
        String valValue[]=new String[f];
        int valCount[]=new int[f];
        noOfVals=0;
        for(int l=0;l<f;l++){
            if(!subset[l][i].equals("?")){
                valValue[0]=subset[l][i];
                break;
            }
        }

        for(int j=0;j<f;j++){
            if(!subset[j][i].equals("?")){
                for(int y=0;y<j;y++){
                    if(subset[y][i].equals(subset[j][i])){
                        flag=true;
                    }

                }
                if(!flag){
                    valValue[noOfVals++]=subset[j][i];
                }
                else
                    flag=false;
            }
        }

        for(int y=0;y<noOfVals;y++){
            valCount[y]=0;
        }

        for(int j=0;j<f;j++){
            for(int y=0;y<noOfVals;y++){
                if((subset[j][i]).equals(valValue[y])){
                    valCount[y]+=1;
                }

            }

        }
        
        total=0;
        for(int y=0;y<noOfVals;y++){
            total+=valCount[y];
        }
        

        int max=valCount[0];
        String maxValue=valValue[0];
        for(int t=0;t<noOfVals;t++){
            if((valCount[t])>max){
                maxValue=valValue[t];
            }
        }
        for(int u=0;u<f;u++){
            if(subset[u][i].equals("?")){
                subset[u][i]=maxValue;
            }
        }
        return maxValue;   
    }
    
    void preProcess11(){
        String max;
        for(int i=0;i<testRow;i++){
            for(int j=0;j<testCol;j++){
                if(test[i][j].equals("?"))
                    max=preProcessX11(j);
            }          
       } 
    }
    
    String preProcessX11(int i){
        int noOfVals=0;
        int total=0;
        boolean flag=false;
        String valValue[]=new String[testRow];
        int valCount[]=new int[testRow];
        noOfVals=0;
        for(int l=0;l<testRow;l++){
            if(!test[l][i].equals("?")){
                valValue[0]=test[l][i];
                break;
            }
        }

        for(int j=0;j<testRow;j++){
            if(!test[j][i].equals("?")){
                for(int k=0;k<j;k++){
                    if(test[k][i].equals(test[j][i])){
                        flag=true;
                    }

                }
                if(!flag){
                    valValue[noOfVals++]=test[j][i];
                }
                else
                    flag=false;
            }
        }
        for(int k=0;k<noOfVals;k++){
            valCount[k]=0;
        }

        for(int j=0;j<testRow;j++){
            for(int k=0;k<noOfVals;k++){
                if((test[j][i]).equals(valValue[k])){
                    valCount[k]+=1;
                }

            }

        }
        
        total=0;
        for(int k=0;k<noOfVals;k++){
            total+=valCount[k];
        }
        

        int max=valCount[0];
        String maxValue="?";
        for(int f=0;f<noOfVals;f++){
            if((valCount[f])>max){
                maxValue=valValue[f];
            }
        }
        for(int u=0;u<testRow;u++){
            if(test[u][i].equals("?")){
                test[u][i]=maxValue;
            }
        }
        return maxValue;
    }
    
    void preProcess21(){
        String max;
        for(int i=0;i<testRow;i++){
            for(int j=0;j<testCol;j++){
                    if(test[i][j].equals("?")){
                        max=preProcessX21(i,j);
                        test[i][j]=max;
                     //   System.out.println(max);
                }
            }          
       }  
    
    }
    
    String preProcessX21(int k, int i){

        String label=test[k][testCol-1];
        String[][] subset=new String[testRow][testCol];
        int f=0;

        
        for(int m=0;m<testRow;m++){
            if(label.equals(test[m][testCol-1])){
                for(int n=0;n<testCol;n++){
                    subset[f][n]=test[m][n];
                }
                f++;
            }
        }

        int noOfVals=0;
        int total=0;
        boolean flag=false;
        String valValue[]=new String[f];
        int valCount[]=new int[f];
        noOfVals=0;
        for(int l=0;l<f;l++){
            if(!subset[l][i].equals("?")){
                valValue[0]=subset[l][i];
                break;
            }
        }

        for(int j=0;j<f;j++){
            if(!subset[j][i].equals("?")){
                for(int y=0;y<j;y++){
                    if(subset[y][i].equals(subset[j][i])){
                        flag=true;
                    }

                }
                if(!flag){
                    valValue[noOfVals++]=subset[j][i];
                }
                else
                    flag=false;
            }
        }

        for(int y=0;y<noOfVals;y++){
            valCount[y]=0;
        }

        for(int j=0;j<f;j++){
            for(int y=0;y<noOfVals;y++){
                if((subset[j][i]).equals(valValue[y])){
                    valCount[y]+=1;
                }

            }

        }
        
        total=0;
        for(int y=0;y<noOfVals;y++){
            total+=valCount[y];
        }
        

        int max=valCount[0];
        String maxValue=valValue[0];
        for(int t=0;t<noOfVals;t++){
            if((valCount[t])>max){
                maxValue=valValue[t];
            }
        }
        for(int u=0;u<f;u++){
            if(subset[u][i].equals("?")){
                subset[u][i]=maxValue;
            }
        }
        return maxValue;   
    }
    

}


    
