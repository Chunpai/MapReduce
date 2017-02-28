import java.util.*;
import java.io.*;
import java.util.Hashtable;
import java.lang.*;


//naming convention: name the parameters with "_", and name the methods and class with capitalized letter
public class ExtractReviews{
    public static void main(String[] args){
        String whole_dataset = "data_inputs/BookReviewRating.txt";
        String output_file = "data_inputs/" +args[0] + "reviews.txt";
        
        int count_threshold = Integer.parseInt( args[0] );
        String line;
        try{
            FileReader fr = new FileReader(whole_dataset);
            BufferedReader br = new BufferedReader(fr);
            File file = new File(output_file);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (int i=0; i < count_threshold; i++){
                line = br.readLine();
                bw.write(line +'\n');
            }
            br.close();
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }       
    }
}
