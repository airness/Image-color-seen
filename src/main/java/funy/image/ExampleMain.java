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

import com.sun.image.codec.jpeg.ImageFormatException;

public class ExampleMain {
	public static void main(String[] args) throws IOException {
	
		ImageTrueColorSeen colorSeen = ImageTrueColorSeen.getInstance();
		//String dirAbsolutePath = "/Volumes/ExtStorage/Downloads/";
		String dirAbsolutePath = "/Library/Desktop Pictures/others/";
		String bwWriteOutPath = "/Volumes/ExtStorage/Downloads/bw/";
		String colorWriteOutPath = "/Volumes/ExtStorage/Downloads/color/";
		
		long time1 = System.currentTimeMillis();
		
		// way 1
		Path dir = Paths.get(dirAbsolutePath);		
		try(DirectoryStream<Path> stream = 
				Files.newDirectoryStream(dir, x -> x.toString().matches("(.*)(.jpg|.png)"))) {
			stream.forEach(s -> {
				try {
					boolean isBW = colorSeen.isLooksBlackWhite(s.toAbsolutePath().toString());
					if (isBW) {
						Files.copy(s.toAbsolutePath(), new File(bwWriteOutPath + s.getFileName()).toPath());
					} else {
						Files.copy(s.toAbsolutePath(), new File(colorWriteOutPath + s.getFileName()).toPath());
					}
				} catch (ImageFormatException | IOException e) {
					System.out.println(e.getMessage());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long time2 = System.currentTimeMillis();
		
		// way 2
		File folder = new File(dirAbsolutePath);
		File[] files = folder.listFiles(x -> x.toString().matches("(.*)(.jpg|.png)"));
		List<File> list = new ArrayList<File>(Arrays.asList(files));		
		list.parallelStream().forEach(s -> {
			try {
				boolean isBW = colorSeen.isLooksBlackWhite(s.getAbsolutePath());
				if (isBW) {
					Files.copy(s.toPath(), new File(bwWriteOutPath + s.getName() + "_").toPath());
				} else {
					Files.copy(s.toPath(), new File(colorWriteOutPath + s.getName() + "-").toPath());
				}
			} catch (ImageFormatException | IOException e) {
				System.out.println(e.getMessage());
			}
		});

		long time3 = System.currentTimeMillis();

		System.out.println("total " + files.length + " files");
		System.out.println("way 1：" + (time2-time1)/1000 + "秒");
		System.out.println("way 2：" + (time3-time2)/1000+"秒");
}}
