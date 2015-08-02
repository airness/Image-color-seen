package funy.image;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExampleMain {
	public static void main(String[] args) throws IOException {
	
		ImageColorSeen probe = ImageColorSeen.getInstance();
		String dirAbsolutePath = "/Volumes/ExtStorage/Downloads/";
		
		long time1 = System.currentTimeMillis();
		
		// way 1
		Path dir = Paths.get(dirAbsolutePath);		
		try(DirectoryStream<Path> stream = 
				Files.newDirectoryStream(dir, x -> x.toString().matches("(.*)(.jpg|.png)"))) {
			
			stream.forEach(
				s -> System.out.println(
						s.getFileName().toString() + "==" +
								probe.isLooksBlackWhite(s.toAbsolutePath().toString()) ));
		} catch(IOException e) {
		    e.printStackTrace();
		}
		
		long time2 = System.currentTimeMillis();
		
		// way 2
		File folder = new File(dirAbsolutePath);
		File[] files = folder.listFiles(x -> x.toString().matches("(.*)(.jpg|.png)"));
		List<File> list = new ArrayList<File>(Arrays.asList(files));		
		list.parallelStream().forEach(
				s -> System.out.println(
						s.getName() + "==" + probe.isLooksBlackWhite(s.getAbsolutePath())));
				
		long time3 = System.currentTimeMillis();
		
		System.out.println("way 1：" + (time2-time1)/1000 + "秒");
		System.out.println("way 2：" + (time3-time2)/1000 + "秒");
	}
}
