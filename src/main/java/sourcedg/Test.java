package sourcedg;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdk.nashorn.internal.runtime.JSErrorType;
import sourcedg.builder.PDGBuilder;
import sourcedg.builder.PDGBuilderConfig;
import sourcedg.graph.PDG;
import sourcedg.graph.VertexType;
import sourcedg.util.GraphExporter;


public class Test {

//	private static Set<String> APIS = new HashSet<>();


	public static void main(final String[] args) throws Exception {
//		generateFiles();
		generateApiVocab();
//		testSingleFile();
//		ceshi();
//		writeCodeTxt("", "", "", 2, "");
	}

	public static void ceshi() {
		String text =
				"class GzipRequestInterceptor implements Interceptor {\n" +
				"    @Override public Response intercept(Chain chain) throws IOException {\n" +
				"      Request originalRequest = chain.request();\n" +
				"      if (originalRequest.body() == null || originalRequest.header(Constants.CONTENT_ENCODING) != null) {\n" +
				"        return chain.proceed(originalRequest);\n" +
				"      }\n" +
				"\n" +
				"      Request compressedRequest = originalRequest.newBuilder()\n" +
				"          .header(Constants.CONTENT_ENCODING, Constants.GZIP_VAL)\n" +
				"          .method(originalRequest.method(), gzip(originalRequest.body()))\n" +
				"          .build();\n" +
				"      return chain.proceed(compressedRequest);\n" +
				"    }\n" +
				"\n" +
				"    private RequestBody gzip(final RequestBody body) {\n" +
				"      return new RequestBody() {\n" +
				"        @Override public MediaType contentType() {\n" +
				"          return body.contentType();\n" +
				"        }\n" +
				"\n" +
				"        @Override public long contentLength() {\n" +
				"          return -1; // We don't know the compressed length in advance!\n" +
				"        }\n" +
				"\n" +
				"        @Override public void writeTo(BufferedSink sink) throws IOException {\n" +
				"          BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));\n" +
				"          body.writeTo(gzipSink);\n" +
				"          gzipSink.close();\n" +
				"        }\n" +
				"      };\n" +
				"    }\n" +
				"  }\n";
		System.out.println(text.length());

	}
	public static void generateFiles() throws Exception {
//		generateDot();
//		generateCsvGT();
//		String codePath = "hahaha";
		String codePath = "C:\\Users\\13416\\Downloads\\APIBench\\APIBench_C\\Java\\Java_Code\\general\\Training\\test";
		List<File> files = getAllFile(codePath, ".java");
		int idx = 0;
		int graphs_idx = 0;
		assert files != null;
		Path graphs = null;
		int file_num = 0;
		for (File file : files) {
			String fileNameWithoutEx = file.getName().substring(0, file.getName().length() - 5);
//			Path path = Paths.get("data/code");
			if (graphs_idx == 0) {
				graphs = Paths.get("C:\\Users\\13416\\PycharmProjects\\api_recommendation\\code\\raw\\graphs" + graphs_idx);
				File graphsFile = new File(graphs.toUri());
				graphsFile.mkdir();
				graphs_idx++;
			} else if (file_num % 1000 == 0) {
				if (Objects.requireNonNull(graphs.toFile().listFiles()).length==1000) {
					graphs = Paths.get("C:\\Users\\13416\\PycharmProjects\\api_recommendation\\code\\raw\\graphs" + graphs_idx);
					File graphsFile = new File(graphs.toUri());
					if (graphsFile.exists()) {
						delAllFileWithoutSelf(graphs.toFile());
					} else {
						graphsFile.mkdir();
						graphs_idx++;
					}
				}
			}
			Path path = Paths.get(graphs + "\\graph" + idx);
			File dir = new File(path.toUri());
			if (dir.exists()) {
				delAllFileWithoutSelf(path.toFile());
			} else {
				dir.mkdir();
			}
//			Path graphPath = Files.createDirectory(path);
//			delAllFileWithoutSelf(graphPath.toFile());
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println(file.getAbsoluteFile());
			boolean flag = false;
			try {
				generateDot(file.getAbsolutePath(), String.valueOf(path), fileNameWithoutEx);
			} catch (Exception e) {
				delAllFile(new File(path.toUri()));
				file_num--;
				flag = true;
			}

			try {
				if (!flag) {
					generateCsvGT(String.valueOf(path), fileNameWithoutEx, file.getAbsolutePath());
					idx++;
				}
			} catch (Exception e) {
				delAllFile(new File(path.toUri()));
				file_num--;
			}
			file_num++;
		}

		System.out.println(" dot files have all been parsed !!! ");

	}

	public static void testSingleFile() {
		String path = "src/main/java/sourcedg/validation/RGCN.java";
		boolean flag = false;
		try {
			generateDot(path, "data/code", "RGCN");
		} catch (Exception e) {
//			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
//			System.out.println("data");
//			File file1 = new File("data");
//			for (File file2 : Objects.requireNonNull(file1.listFiles())) {
//				file2.delete();
//			}
//			file1.delete();
			delAllFile(new File("data/code"));
			flag = true;
		}

		try {
			if (!flag) {
				generateCsvGT("data/code/RGCN.dot", "RGCN", path);
			} else {
				flag = !flag;
			}
		} catch (Exception e) {
			delAllFile(new File("data/code"));
		}
	}

	public static void delAllFile(File directory){
		if (!directory.isDirectory()){
			directory.delete();
		} else{
			File [] files = directory.listFiles();

			// 空文件夹
			if (files.length == 0){
				directory.delete();
				System.out.println("删除" + directory.getAbsolutePath());
				return;
			}

			// 删除子文件夹和子文件
			for (File file : files){
				if (file.isDirectory()){
//					delAllFile(file);  // only files in each directory, no recursion is required
					continue;
				} else {
					if (file.toString().endsWith(".dot")) {
						System.gc();
					}
					boolean delete = file.delete();
					System.out.println("删除" + file.getAbsolutePath());
				}
			}

			boolean b = directory.delete();
			System.out.println("删除" + directory.getAbsolutePath());
		}
	}

	public static void delAllFileWithoutSelf(File directory){
		if (!directory.isDirectory()){
			directory.delete();
		} else{
			File [] files = directory.listFiles();

			// 空文件夹
//			if (files.length == 0){
//				directory.delete();
//				System.out.println("删除" + directory.getAbsolutePath());
//				return;
//			}

			// 删除子文件夹和子文件
			for (File file : files){
				if (file.isDirectory()){
//					delAllFile(file);  // only files in each directory, no recursion is required
					continue;
				} else {
					if (file.toString().endsWith(".dot")) {
						System.gc();
					}
					boolean delete = file.delete();
					System.out.println("删除" + file.getAbsolutePath());
				}
			}

		}
	}


	public static void main3(final String[] args) throws Exception {
		generateDot("data/TestMain.java", "data/tmp", "random");
	}

	public static void generateApiVocab() throws IOException {
		String path = "C:\\Users\\13416\\PycharmProjects\\api_recommendation\\code\\raw\\";
		List<File> files = getAllFile(path, "groundtruth.txt");
		Set<String> apis = new HashSet<>();

		assert files != null;
		for (File file : files) {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();
			apis.add(line);
			br.close();
			isr.close();
			fis.close();
		}
		Map<String, Integer> apiIdxInText = new HashMap<>();
		int idx = 0;
		for (String str : apis) {
			apiIdxInText.put(str, idx);
			idx++;
		}

		for (String key : apiIdxInText.keySet()) {
			writeToTxtFile("C:\\Users\\13416\\PycharmProjects\\data\\apis.txt", apiIdxInText.get(key) + "   " + key, true);
		}

		System.out.println(apiIdxInText.size());
		// replace idx for gt
		for (File file : files) {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			String context = "";
			while ((line = br.readLine()) != null) {
				context = line;
			}
			br.close();
			isr.close();
			fis.close();
			FileOutputStream fos = new FileOutputStream(file, false);
			OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
			osw.write("" + apiIdxInText.get(context));
			osw.close();
		}
	}

	public static void generateDot(String filePath, String outputPath, String outFileName) throws Exception {
//		String path = "data/TestMain.java";

		final FileInputStream in = new FileInputStream(filePath);
		PDGBuilderConfig config = PDGBuilderConfig.create();
//		config.normalize();
//		config.keepLines();
		config.interproceduralCalls();
//		config.removeComments();
		final PDGBuilder builder = new PDGBuilder(config, Level.WARNING);
		builder.build(in);
		final PDG pdg = builder.getPDG();
		pdg.collapseNodes(VertexType.ACTUAL_OUT);
		pdg.collapseNodes(VertexType.ARRAY_IDX);
//		System.out.println(pdg);
//		final Iterator<CFG> it = builder.getCfgs().iterator();
//		final CFG cfg = it.next();
//		 System.out.println("*************" + cfg.cyclomaticComplexity());
		GraphExporter.exportAsDot(pdg, outputPath, outFileName);
	}

	public static void generateCsvGT(String inputFilePath,  String fileName,  String codePath) throws Exception {
//		String fileName = "data/random.dot";
		File file = new File(inputFilePath + "\\" + fileName + ".dot");
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);

		String line = "";
		String idx = "";
		int realIdx = -1;
		String label = "";
		String context = "";
		String pattStr;
		Pattern pattern;
		String src = "";
		String des = "";
		List<Integer> callList = new ArrayList<>();
		Map<Integer, List<String>> nodeCsv = new HashMap<>();
		Map<Integer, List<String>> edgeCsv = new HashMap<>();
//		List<List<String>> nodeCsv = new ArrayList<>();
//		List<List<String>> edgeCsv = new ArrayList<>();
		int i = 0;
		while ((line = br.readLine()) != null) {
			//process the line
			List<String> nodeLine = new ArrayList<>();
			List<String> edgeLine = new ArrayList<>();
			if (line.contains("->")) {
				// edge
				String[] splits = line.split(" ");
				src = String.valueOf(Integer.parseInt(splits[2]) - 1);
				des = String.valueOf(Integer.parseInt(splits[4]) - 1);
				pattStr = "(?<=\").*?(?=\")";
				pattern = Pattern.compile(pattStr);
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					label = matcher.group();
				}
				edgeLine.add(src);
				edgeLine.add(des);
				edgeLine.add(label);
				edgeCsv.put(i++, edgeLine);

			} else {
				// node
				String[] splits = line.split("=");
				if (splits.length >= 2) {
					String idx_label = splits[1];
					idx = idx_label.split("\\-")[0].substring(1);
					if (idx_label.length() >= idx.length() + 2) {
						label = idx_label.substring(idx.length() + 2);
					}
					Matcher m = Pattern.compile("\\d+").matcher(line);
					if (m.find()) {
						realIdx = Integer.parseInt(m.group(0));
					}
					if (label.equals("Call")) {
						callList.add(nodeCsv.size());
					}
					line = br.readLine();
					String tmpContext = "";
					while (!line.contains("shape=\"")) {
						tmpContext += line;
						line = br.readLine();
					}
					String range = "";
					String linePattern = "line=\\\"[\\d]+\\\"";
					Pattern pattern1 = Pattern.compile(linePattern);
					Matcher matcher = pattern1.matcher(line);
					if (matcher.find()) {
						String group = matcher.group(0);
						int groupSize = group.length();
						range = group.substring(6, groupSize - 1);
					}
					String[] tmps = line.split("\"");
					context = tmpContext + tmps[0];
//					if (tmps.length >= 4) {
//						range = tmps[4];
//					}
					nodeLine.add(idx);
					nodeLine.add(label);
					nodeLine.add(context);
					nodeLine.add(range);
					nodeCsv.put(realIdx, nodeLine);
				}
			}
		}
		br.close();
		isr.close();
		fis.close();

		String truth = "";
		int holeLine = -1;
		Random random = new Random();
		if (callList.size() > 0) {
//			Integer[] keys = nodeCsv.keySet().toArray(new Integer[0]);
//			Integer randomKey = keys[random.nextInt(keys.length)];
//			List<String> s = nodeCsv.get(randomKey);
//			int idxDot = Integer.parseInt(s.get(0));
//			holeLine = Integer.parseInt(nodeCsv.get(idxDot).get(3));
//			truth = s.get(2);
//			s.set(1, "Hole");
//			s.set(2, "");

			int ii = random.nextInt(callList.size());
			int callIdx = callList.get(ii);
			int minKey = Integer.MAX_VALUE;
			for (Integer key : nodeCsv.keySet()) {
				minKey = Math.min(minKey, key);
			}
			callIdx += minKey;
			List<String> call = nodeCsv.get(callIdx);
//			int idxDot = Integer.parseInt(call.get(0));
			holeLine = Integer.parseInt(call.get(3));
			truth = call.get(2);
			call.set(1, "Hole");
			call.set(2, "");
		} else {
			throw new Exception();
			// 没有api调用的代码就先跳过吧
//				if (nodeCsv.size() > 0) {
//					Integer[] keys = nodeCsv.keySet().toArray(new Integer[0]);
//					Integer randomKey = keys[random.nextInt(keys.length)];
//					List<String> s = nodeCsv.get(randomKey);
//					holeLine = Integer.parseInt(s.get(3));
//					truth = s.get(2);
//					s.set(1, "Hole");
//					s.set(2, "");
//				}
		}


		Map<Integer, List<String>> nodeMap = new HashMap<>();
		for (List<String> valueSet : nodeCsv.values()) {
//			System.out.println(valueSet);
			nodeMap.put(Integer.parseInt(valueSet.get(0)), Arrays.asList(valueSet.get(1), valueSet.get(2)));
		}
		int node_id = 0;
		StringBuffer sb = new StringBuffer();
		while (node_id < nodeMap.size()) {
			List<String> strings = nodeMap.get(node_id);
			String type = strings.get(0);
			if (type.equals("Call") || type.equals("Class") || type.equals("Formal-in") || type.equals("Actual-in")
					|| type.equals("Entry")) {
				sb.append(strings.get(1));
				sb.append(" ");
			} else if (type.equals("Assign")) {
				sb.append(strings.get(1).split("=")[0]);
				sb.append(" ");
			} else if (type.equals("Hole")) {
				sb.append("<mask>");
				sb.append(" ");
			}
			node_id++;
		}

		if (sb.length() > 1000) {
			System.out.println("===========================" + sb.length() + "================================");
			throw new Exception();
		}

//		writeCodeTxt(inputFilePath, codePath, fileName, holeLine, truth);
		writeTokenTxt(inputFilePath, String.valueOf(sb), fileName);

//			APIS.add(truth);
		// 去掉truth中的参数部分
		truth = truth.replaceAll("\\(([\\s\\S]*)\\)", "()");
		if (truth.contains(".")) {
			String[] strings = truth.split("\\.");
			if (strings.length > 1) {
				truth = strings[1];
			}
		}
		writeToTxtFile(inputFilePath + "//groundtruth.txt", truth, false);
//			System.out.println(line);

		List<String> nodeCsvHead = new ArrayList<>();
		nodeCsvHead.add("nodeId");
		nodeCsvHead.add("label");
		nodeCsvHead.add("context");
		nodeCsvHead.add("range");
//		nodeCsvHead.add("real_idx");

		createCSVFile(nodeCsvHead, nodeCsv, inputFilePath,fileName+"_node");

		List<String> edgeCsvHead = new ArrayList<>();
		edgeCsvHead.add("src");
		edgeCsvHead.add("des");
		edgeCsvHead.add("label");

		for (int j = 0; j < edgeCsv.size(); j++) {
			int srcIdx = Integer.parseInt(edgeCsv.get(j).get(0));
			int desIdx = Integer.parseInt(edgeCsv.get(j).get(1));
			String processedSrcIdx = nodeCsv.get(srcIdx + 1).get(0);
			String processedDesIdx = nodeCsv.get(desIdx + 1).get(0);
			edgeCsv.get(j).set(0, processedSrcIdx);
			edgeCsv.get(j).set(1, processedDesIdx);
		}

		createCSVFile(edgeCsvHead, edgeCsv, inputFilePath, fileName + "_edge");
	}

	private static void writeTokenTxt(String outPath, String sb, String fileName) throws IOException {
		try {
				sb = sb.replaceAll("[A-Z]", "_$0").toLowerCase();
				sb = sb.replaceAll("_", " ").replaceAll("\\.", " ").toLowerCase();
				sb = sb.replaceAll("[\\\\pP\\\\p{Punct}]", "");
				//  (.*)_(\w)(.*) 下划线字符串匹配
				String s = delRepStr(sb.toString());
				writeToTxtFile(outPath + "//" + fileName + ".txt", s, false);
			} catch (Exception e) {

		}
	}

	private static String delRepStr(String str) {
		StringBuffer sb = new StringBuffer();
		String[] strings = str.split(" ");
		for (String tmpStr : strings) {
			if (!sb.toString().contains(tmpStr)) {
				sb.append(tmpStr);
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private static void writeCodeTxt(String outPath, String codePath, String fileName, int holeLine, String truth) throws IOException {
		try {
			File file = new File(codePath);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			int idx = 1;
			while ((line = br.readLine()) != null) {
				if (idx == holeLine) {
					truth = truth.replace("'", "\"");
					line = line.replace(truth, "<mask>");
				}
				writeToTxtFile(outPath + "//" + fileName + ".txt", line, true);
				idx++;
			}
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {

		}

	}

	private static void writeToTxtFile(String path, String context, boolean flag) {

		try{
			File file = new File(path);
			FileOutputStream fos = null;
			if(!file.exists()){
				file.createNewFile();
				fos = new FileOutputStream(file, true);  // append
			}else{
				fos = new FileOutputStream(file,true);
			}

			OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

			osw.write(context);
			if (flag) {
				osw.write("\r\n");
			}
			osw.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createCSVFile(List<String> head, Map<Integer, List<String>> dataList, String outPutPath, String filename) {

		File csvFile = null;
		BufferedWriter csvWtriter = null;
		try {
			csvFile = new File(outPutPath + File.separator + filename + ".csv");
			File parent = csvFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			csvFile.createNewFile();

			csvWtriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(csvFile.toPath()), "utf-8"), 1024);
			writeRow(head, csvWtriter);

			for (List<String> row : dataList.values()) {
				writeRow(row, csvWtriter);
			}
			csvWtriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				assert csvWtriter != null;
				csvWtriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private static void writeRow(List<String> row, BufferedWriter csvWriter) throws IOException {
		for (Object data : row) {
			String rowStr = "\"" + data + "\",";
			csvWriter.write(rowStr);
		}
		csvWriter.newLine();
	}

	public static List<File> getAllFile(String path,  String ex) {

		File dir = new File(path);

		List<File> allFileList = new ArrayList<>();

		if (!dir.exists()) {
			System.out.println("dir is not exist");
			return null;
		}

		getAllFiles(dir, allFileList, ex);

		for (File file : allFileList) {
			System.out.println(file.getAbsoluteFile());
		}
		System.out.println("num of dir:  " + allFileList.size());
		return allFileList;
	}

	private static void getAllFiles(File fileInput, List<File> allFileList, String ex) {

		File[] fileList = fileInput.listFiles();
		assert fileList != null;
		if (fileList.length == 0) {
			return;
		}
		for (File file : fileList) {
			if (file.isDirectory()) {
				getAllFiles(file, allFileList, ex);
			} else {
				if (file.getName().endsWith(ex)) {
					allFileList.add(file);
				}
			}
		}
	}

}
