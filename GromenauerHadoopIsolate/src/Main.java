import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Main extends Configured implements Tool{
	
	private String inputPath;
	private String outputPath;
	private String criteria;
	
	private static final String ALL = "ALL";
	private static final String LOCAL = "0";
	private static final String LOCALSTR = "LOCAL";
	private static final String HADOOP = "1";
	private static final String HADOOPSTR = "HADOOP";
	
	public static class MyMapper extends Mapper<LongWritable, Text, Text, LongWritable>{

		private List<String> STOP_WORDS = new ArrayList<String>();
		
		private static final String CRITERIA = "criteria";
		private static final String ALL = "ALL";
		
		
		private String criteriaParam;
		
		public MyMapper(){
			STOP_WORDS.add("OF");
			STOP_WORDS.add("A");
			STOP_WORDS.add("OR");
			STOP_WORDS.add("THE");
			STOP_WORDS.add("SHE");
			STOP_WORDS.add("HE");
			STOP_WORDS.add("IT");
			STOP_WORDS.add("AN");
			STOP_WORDS.add("THEY");
			STOP_WORDS.add("YOU");
			
			criteriaParam = "";
		}
		
		
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			String line = value.toString();
			
			Configuration conf = context.getConfiguration();
			criteriaParam = conf.get(CRITERIA);
			
			System.out.println("[MYMAPPER] CRITERIA " + criteriaParam);

			final List<String> words = extractWordsFromLine(line);

			for (String word : words) {
				System.out.println("[MYMAPPER] processed word " + word);
				context.write(new Text(word), new LongWritable(1));
			}

		}

		private List<String> extractWordsFromLine(String line) {
			if (line != null && !"".equals(line.trim())) {
				return cleanWords(line);
			}

			return Collections.emptyList();
		}


		private boolean isStopWord(String word) {

			return !STOP_WORDS.contains(word);

		}
		
		private boolean filterByWord(String word){
			
			if (criteriaParam.equalsIgnoreCase(ALL)){
				return true;
			}else{
				return criteriaParam.equalsIgnoreCase(word);
			}
			
		}
		

		private List<String> cleanWords(String line) {
			final String[] words = line.toUpperCase().split(" ");

			List<String> resultAfterCleaningWords = new ArrayList<String>(
					words.length);
			for (String word : words) {
				if (!"".equals(word)) {

					String finalWord = cleanWord(word);

					if (!!isStopWord(word) && filterByWord(word)) {
						resultAfterCleaningWords.add(finalWord);
					}
				}
			}
			return resultAfterCleaningWords;
		}

		private String cleanWord(String word) {
			String finalWord = word;
			if (word.endsWith(".") || word.endsWith(",") || word.endsWith(";")
					|| word.endsWith(":") || word.endsWith(")")
					|| word.endsWith("?") || word.endsWith("!")) {
				finalWord = cleanWord(word.substring(0, word.length() - 1));
			}

			if (word.startsWith("(")) {
				finalWord = cleanWord(word.substring(1, word.length()));
			}

			return finalWord;
		}
		
	}

	public static class MyReducer extends Reducer<Text, LongWritable, Text, LongWritable>{
		
		@Override
		protected void reduce(Text key, Iterable<LongWritable> values,
				Context context) throws IOException, InterruptedException {

			long timesThisWord = 0;
			
			for (LongWritable value : values) {
				timesThisWord++;
			}
			
			System.out.println("[MYREDUCER] <" + key + "> numAparitions <" + timesThisWord + ">");
			context.write(key, new LongWritable(timesThisWord));
		}
		
		
		
	}
	
	
	
	public Main(){
		inputPath = "";
		outputPath = "";
		criteria = ALL;
	}

	
	
	private String listFilesOfFolderToString(File folder) throws IOException{
		String files = "";
		
		File[] fil = folder.listFiles(
		new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".txt");
			}});
		
		if (fil.length > 0){
			int i=0;
			for(i=0; i<fil.length-1; i++){
				files += fil[i].getAbsolutePath() + ",";
			}
			
			if(i==fil.length-1){
				files += fil[i].getAbsolutePath();
			}
		}
		return files;
	}

	
	
	public int process(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		
		int return_int = 0;
		String listOfFiles = "";
		String flag = "";
		String flagStr = "";
		
		if (args.length < 3){
			System.out.println("numWordsInFiles <inputpath> <outputpath> <0=local/1=hadoop> Optional: <criteria> (default ALL)");
			System.out.println("example numWordsInFiles ./inputData ./outputData <0=local/1=hadoop> ==> (look for ALL ocurrences word in all files) ");
			System.out.println("example numWordsInFiles ./inputData ./outputData <0=local/1=hadoop> <criterium> ==> (look for 'criterium' ocurrences word in all files) ");
			System.out.println("example numWordsInFiles ./inputData ./outputData <0=local/1=hadoop> ALL ==> (looking for all word ocurrences in all input files)");
		}else if (args.length >= 3){
			
			//LOCAL/HADOOP
			if (!args[2].equalsIgnoreCase("")){
				if (this.HADOOP.equalsIgnoreCase(args[2])){
					flag = this.HADOOP;
					flagStr = this.HADOOPSTR;
				}else{
					flag = this.LOCAL;
					flagStr = this.LOCALSTR;
				}
			}else{
				flag = this.LOCAL;
				flagStr = this.LOCALSTR;
			}
			
			
			
			//1-InputData
			if (flag.equalsIgnoreCase(this.LOCAL)){
				if (!args[0].equalsIgnoreCase("")){
					File filInput = new File(args[0]);
					if (!filInput.exists()){
						throw new IOException("Input Folder (" + args[0] + ") doesn't exist");
					}else if (!filInput.isDirectory()){
						throw new IOException("Input Folder (" + args[0] + ") is not a file");
					}else{
						inputPath = args[0].trim();
						listOfFiles = listFilesOfFolderToString(filInput);
						System.out.println("processing input files [" + listOfFiles + "]");
						
						
					}
				}else{
					throw new IOException("Empty input Folder");
				}
				
			}else{
				if (!args[0].equalsIgnoreCase("")){
					listOfFiles = args[0].trim();
					inputPath = listOfFiles;
				}
			}
			
			
			//2-OutputData
			if (!args[1].equalsIgnoreCase("")){
				outputPath = args[1].trim();
			}else{
				throw new IOException("Empty output Folder");
			}
			
			
			//3-Criteria
			if (args.length >= 4){
				if (!args[3].equalsIgnoreCase("")){
					criteria = args[2].trim();
				}else{
					criteria = ALL;
				}
			}else{
				criteria = ALL;
			}
			
			
			
			System.out.println("==== NUMWORDSINFILES INI ====");
			System.out.println("numWordsInFiles " + inputPath + " " + outputPath + " " + flagStr+ " " + criteria + " ");
			
			
			
			Configuration conf = new Configuration();
			conf.set("criteria", criteria);
			
			Job job = new Job(conf, "numWordsInFiles");
			
			job.setJarByClass(this.getClass());
			
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
			
			job.setMapperClass(MyMapper.class);
			job.setReducerClass(MyReducer.class);
			
			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			
			//FileInputFormat.addInputPath(job, listOfFiles));
			
			if (flag.equalsIgnoreCase(this.LOCAL)){
				FileInputFormat.addInputPaths(job, listOfFiles);
			}else{
				FileInputFormat.addInputPath(job, new Path(listOfFiles));
			}
			
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			
			System.out.println("==== NUMWORDSINFILES END ====");
			
			//job.waitForCompletion(true);
			job.submit();
			return_int = 0;
		}
		
		return return_int;
		
	}
	
	public static void main(String[] args) throws Exception, IOException, ClassNotFoundException, InterruptedException{
		int res = ToolRunner.run(new Main(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] arg0) throws Exception {
		return this.process(arg0);
	}

}
