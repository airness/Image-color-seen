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
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.sun.image.codec.jpeg.ImageFormatException;

public class ImageTrueColorSeenTest {
	private static Logger log = Logger.getLogger(ImageTrueColorSeenTest.class.getSimpleName()); 
	static {
		Handler conHdlr = new ConsoleHandler();
		conHdlr.setFormatter(new Formatter() {
			public String format(LogRecord record) {
				return "[" + record.getLoggerName() + "] " + record.getLevel() + ": " + record.getMessage() + "\n";
			}
		});
		log.setUseParentHandlers(false);
		log.addHandler(conHdlr);
	}
	
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
					log.log(Level.INFO, "[" + s.toAbsolutePath() + "] = " + isBW);
//					copy(s.toFile(), isBW, "");					
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
				log.log(Level.INFO,"parallel - [" + s.toPath() + "] = " + isBW);
//				copy(s, isBW, "-parallel");
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
					log.log(Level.INFO,"async - [" + s.toPath() + "] = " + isBW);	
					if (ex != null) {
						System.out.println(ex.getMessage());
					} else {
//						copy(s, isBW, "-async");
					}
				});
			});
		
		writeOutService.shutdown();
		try {
			writeOutService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
//	private void copy(File f, boolean isBW, String prefix) {
//		try {
//			Files.copy(f.toPath(),
//				new File(((isBW)?bwWriteOutPath:colorWriteOutPath) 
//						+ f.getName() + prefix).toPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
