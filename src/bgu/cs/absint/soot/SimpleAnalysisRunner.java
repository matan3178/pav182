package bgu.cs.absint.soot;

import java.util.List;

import soot.PackManager;
import soot.Transform;
import bgu.cs.util.StringUtils;

/**
 * A convenience class for applying a given analysis to a class.
 * 
 * @author romanm
 */
public class SimpleAnalysisRunner {
	/**
	 * A list of arguments passed to Soot.
	 */
	public static final String DEFAULT_SOOT_ARGS = "-cp . -pp -f jimple -p jb use-original-names -p jb.ls enabled:false -p jb.ls enabled:false -keep-line-number -print-tags AEBenchmarks";

	/**
	 * Runs the given analysis on the class given by the first argument in the
	 * given string array.
	 * 
	 * @param analysis
	 *            An analysis.
	 * @param args
	 *            Expected to contain either a single class name or an
	 *            alternative array of arguments to Soot.
	 */
	public static void run(BaseAnalysis<?, ?> analysis, String[] args) {
		String[] sootArgs = getArgs(args);
		String phaseName = "jtp." + analysis.getClass().getSimpleName();
		PackManager.v().getPack("jtp").add(new Transform(phaseName, analysis));
		soot.Main.main(sootArgs);
	}

	protected static String[] getArgs(String[] args) {
		if (args.length == 1) {
			List<String> argsAsList = StringUtils
					.breakString(DEFAULT_SOOT_ARGS);
			argsAsList.add(args[0]);
			String[] sootArgs = argsAsList.toArray(new String[0]);
			return sootArgs;
		} else {
			return args;
		}
	}
}