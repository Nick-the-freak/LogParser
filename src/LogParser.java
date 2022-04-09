import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.text.DecimalFormat;

class LogParser {
    // input file
    BufferedReader infile;
    // output buffer
    PrintWriter outfile;
    // log pattern 
    Pattern pattern = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\"(\\S+)\"\\s+\"(.*)\"");
    // time format that use in raw log
    SimpleDateFormat time_format  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    // time format that use to output
    SimpleDateFormat output_format  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    // time diff format that use to output
    DecimalFormat df = new DecimalFormat("0.00000");
    // join map joins same logid to further use
    Map<String, ArrayList<String>> join_map = new HashMap<>();

    // constructor
    LogParser(BufferedReader in, PrintWriter out){
        infile = in;
        outfile = out;
    }

    // parse the file 
    public void parse() throws Exception {
        String line;
        // read line by line
        while((line = infile.readLine()) != null){
            // match the pattern of line
            Matcher m = pattern.matcher(line);
            if (!m.matches()){
                continue;
            }
            // logid is in position 5
            String logid = m.group(5);
            if (join_map.containsKey(logid)) {
                // found record used same logid 
                ArrayList<String> list = join_map.get(logid);
                // parse start time
                Date t1 = time_format.parse(list.get(0) + " " + list.get(1));
                // parse end time
                Date t2 = time_format.parse(m.group(1) + " " + m.group(2));
                // compute time cost
                double diff = (t2.getTime() - t1.getTime())/(double)(60*1000);
                diff = Double.parseDouble(df.format(diff));
                // concat log line
                String log = String.join(" - ", new String[]{list.get(5), m.group(6)});
                // compose log line
                String[] csv_line = new String[]{log, output_format.format(t1), output_format.format(t2),
                        Double.toString(diff)};
                // write to output file
                outfile.println(String.join(",", csv_line));
                // flush buffers
                outfile.flush();
                // remove used logid record
                join_map.remove(logid);
            } else {
                // if dont find matched pair of logid, store it
                ArrayList<String> list = new ArrayList<>();
                for (int i = 1; i <= m.groupCount();i++){
                    list.add(m.group(i));
                }
                // put the record to join map for further use
                join_map.put(logid, list);
            }
        }
    }

    public static void main(String[] args) {
        // assert params length > 2
        if (args.length < 2 ){
            System.out.println("Usage: LogParser <output file> [<input file>...]");
            System.exit(1);
        }
        // open output file 
        File outputFile = new File(args[0]);
        PrintWriter pw;
        try {
            pw = new PrintWriter(outputFile);
        } catch (Exception e) {
            System.out.println("error on open output file: " + e.getMessage());
            System.exit(1); 
            return;
        }

        // write csv line header
        String[] header = new String[]{"Log Message","Start Time","End Time","Time Diff"};
        pw.println(Stream.of(header).collect(Collectors.joining(",")));

        // for each files in remaining args, do parse 
        for(int i = 1; i < args.length; i ++ ) {
            try( BufferedReader br = new BufferedReader(new FileReader(args[i]))){ 
                LogParser l = new LogParser(br, pw);
                l.parse();
            } catch (Exception e) {
                System.out.println("error on parse input file: " + e.getMessage());
                System.exit(1); 
                return;    
            }
        }
        // flush buffer to file
        pw.flush();

        pw.close();
    }
}