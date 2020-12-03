package entitygen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/** For a given Entity class, we generate some artifacts:
 * <ul>
 * <li>Home class - extends EntityHome<Entity,Long>
 * <li>List class - interface extends Repository
 * <li>List Page - xhtml
 * <li>Detail Page - xhtml
 * <li>Edit Page - xhtml
 * </ul>
 * @author ian
 *
 */
public class JenGen {
	
	// Things that will need to be parameterized
	final static String OUTPUT_DIR = "../entitygenoutput";
	final static String demoClassName = "model.MiniPerson";
	
	final static String SRC_DIR = "src/main/java";
	final static String WEB_DIR = "src/main/webapp";
	final static String PKG_NAME_ACTION = "action";
	final static String PKG_NAME_MODEL = "model";
	
	final static String homeClassFilePattern = "%s/%sHome.java",
		listClassFilePattern = "%s/%sList.java",
		listPageFilePattern = "%sList.xhtml",
		viewPageFilePattern = "%sView.xhtml",
		editPageFilePattern = "%sEdit.xhtml";

	public static void main(String[] args) throws Exception {
		if (args.length != 2 ) {
			System.err.println("Usage: JenGen classDir className");
			System.exit(1);
		}
		String dir = args[0];
		ClassLoader loader = new FileClassLoader(dir);
		String className = args[1];
		process(loader, className);
	}

	private static void process(ClassLoader loader, String className) throws Exception {
		System.out.println("Processing " + className);
		
		// Do this first - most likely to fail
		Class<?> clazz = loader.loadClass(className);
		
		VelocityEngine engine = new VelocityEngine();
		// See http://stackoverflow.com/questions/9051413/unable-to-find-velocity-template-resources
		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.init();

		VelocityContext context = new VelocityContext();

		String pkgDir = PKG_NAME_ACTION.replace('.', '/');
		final File x = new File(OUTPUT_DIR + "/" + SRC_DIR, pkgDir);
		ensureDirectoryExists(x);
		final File y = new File(OUTPUT_DIR + "/" + WEB_DIR);
		ensureDirectoryExists(y);
		
		
		context.put("entityClazz", clazz);
		final String simpleName = clazz.getSimpleName();
		context.put("EntityClassUC", simpleName);
		context.put("EntityClassLC", toBeanName(clazz.getSimpleName()));
		context.put("pkg_name", PKG_NAME_MODEL);
		// For @Generated annotation
		context.put("generate_value", JenGen.class.getName());
		context.put("generate_date", LocalDateTime.now().toString());

		// Java Code
		Template homeClassTemplate = engine.getTemplate("HomeClass.vm");
		Template listClassTemplate = engine.getTemplate("ListClass.vm");

		// Pages
		Template listPageTemplate = engine.getTemplate("ListPage.vm");
		Template viewPageTemplate = engine.getTemplate("ViewPage.vm");
		Template editPageTemplate = engine.getTemplate("EditPage.vm");
		
		renderFile(context, OUTPUT_DIR + "/" + SRC_DIR, String.format(homeClassFilePattern, pkgDir, simpleName), homeClassTemplate);
		renderFile(context, OUTPUT_DIR + "/" + SRC_DIR, String.format(listClassFilePattern, pkgDir, simpleName), listClassTemplate);
		
		renderFile(context, OUTPUT_DIR + "/" + WEB_DIR, String.format(listPageFilePattern, simpleName), listPageTemplate);
		renderFile(context, OUTPUT_DIR + "/" + WEB_DIR, String.format(viewPageFilePattern, simpleName), viewPageTemplate);
		renderFile(context, OUTPUT_DIR + "/" + WEB_DIR, String.format(editPageFilePattern, simpleName), editPageTemplate);

		System.out.println("Done.");
	}

	/**
	 * Velocity Merge of one file and write it to output.
	 * @param context The Velocity Context
	 * @param dirName The top-level directory
	 * @param pathName The subdirectory and filename
	 * @param template The Velocity template
	 * @throws IOException if there's a problem here.
	 */
	public static void renderFile(VelocityContext context, 
			String dirName, String pathName,
			Template template)
			throws IOException {
		File classFile = new File(dirName, pathName);
		PrintWriter classWriter = new PrintWriter(classFile);
		template.merge(context, classWriter);
		classWriter.close();
	}

	/**
	 * @param x
	 * @throws IOException
	 */
	public static void ensureDirectoryExists(final File x) throws IOException {
		x.mkdirs();
		if (!x.isDirectory()) {
			throw new IOException("Can't create " + x);
		}
	}

	static String toBeanName(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("toBeanName(empty string)");
		}
		char ch = Character.toLowerCase(name.charAt(0));
		if (name.length() == 1) {
			return new String(new char[] {ch});
		}
		return ch + name.substring(1);
	}
}
