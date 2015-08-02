package funy.image;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.sun.image.codec.jpeg.ImageFormatException;

public class ImageTrueColorSeenTest {
	String fileExtension;
	ImageTrueColorSeen colorSeen;
	String dirAbsolutePath;
//	String bwWriteOutPath;
//	String colorWriteOutPath;

	@Before
	public void setUp() throws Exception {
		fileExtension = "(.*)(.jpg|.png)";
		colorSeen = ImageTrueColorSeen.getInstance();
		dirAbsolutePath = "/Volumes/ExtStorage/Downloads/";
//		bwWriteOutPath = "/Volumes/ExtStorage/Downloads/bw/";
//		colorWriteOutPath = "/Volumes/ExtStorage/Downloads/color/";
	}

	/**
	 * 一般循序性的處理流程
	 */
	@Test
	public void testIsLooksBlackWhite() {		
		Path dir = Paths.get(dirAbsolutePath);		
		try(DirectoryStream<Path> stream = 
				Files.newDirectoryStream(dir, x -> x.toString().matches(fileExtension))) {
			stream.forEach(s -> {
				try {
					boolean isBW = colorSeen.isLooksBlackWhite(s.toAbsolutePath().toString());
					System.out.println("[" + s.toAbsolutePath() + "] = " + isBW);
//					if (isBW) {
//						Files.copy(s.toAbsolutePath(), new File(bwWriteOutPath + s.getFileName()).toPath());
//					} else {
//						Files.copy(s.toAbsolutePath(), new File(colorWriteOutPath + s.getFileName()).toPath());
//					}
//				} catch (ImageFormatException | IOException e) {
				} catch (ImageFormatException e) {
					System.out.println(e.getMessage());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 採用平行處理的流程
	 */
	@Test
	public void testParallelIsLooksBlackWhite() {		
		File folder = new File(dirAbsolutePath);
		File[] files = folder.listFiles(x -> x.toString().matches(fileExtension));
		List<File> list = new ArrayList<File>(Arrays.asList(files));		
		list.parallelStream().forEach(s -> {
			try {
				boolean isBW = colorSeen.isLooksBlackWhite(s.getAbsolutePath());
				System.out.println("parallel - [" + s.toPath() + "] = " + isBW);
//				if (isBW) {
//					Files.copy(s.toPath(), new File(bwWriteOutPath + s.getName() + "-parallel").toPath());
//				} else {
//					Files.copy(s.toPath(), new File(colorWriteOutPath + s.getName() + "-parallel").toPath());
//				}
//			} catch (ImageFormatException | IOException e) { 
			} catch (ImageFormatException e) { 
				System.out.println(e.getMessage());
			}
		});
	}
	
	/**
	 * 採用非同步的平行處理流程
	 */
	@Test
	public void testAsyncParallelIsLooksBlackWhite() {		
		ExecutorService writeOutService = Executors.newFixedThreadPool(10);
		File folder = new File(dirAbsolutePath);
		File[] files = folder.listFiles(x -> x.toString().matches(fileExtension));
		List<File> list = new ArrayList<File>(Arrays.asList(files));		
		list.parallelStream().forEach(s -> {
				CompletableFuture.supplyAsync(() -> {
					return colorSeen.isLooksBlackWhite(s.getAbsolutePath());
				}, writeOutService).whenComplete((isBW, ex) -> {
					System.out.println("async - [" + s.toPath() + "] = " + isBW);	
					if (ex != null) {
						System.out.println(ex.getMessage());
//						return;
					}
//					try {
//						if (isBW) {
//							Files.copy(s.toPath(), new File(bwWriteOutPath + s.getName() + "-async").toPath());
//						} else {
//							Files.copy(s.toPath(), new File(colorWriteOutPath + s.getName() + "-async").toPath());
//						}
//					} catch (IOException ioe) {
//						ioe.printStackTrace();
//					}
				});
			});
		
		writeOutService.shutdown();
		try {
			writeOutService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
