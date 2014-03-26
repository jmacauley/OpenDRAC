/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.common.genTl1Stubs;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Take a directory of class files built via the GenTl1Sub tool and attempt to
 * build a commands.txt file from it.
 * 
 * @author pitman
 */
public final class ReverseEngineerIt { // NO_UCD
  private static final Logger log = LoggerFactory.getLogger(ReverseEngineerIt.class);

	private ReverseEngineerIt(String dir, String packageName) throws Exception {

		/*
		 * Given a directory and a package name, we can scan the directory+package
		 * for class files, and load them via a class loader for inspection.
		 */
		String packageAsPath = packageName.replace(".", "/");
		File scanDir = new File(dir, packageAsPath);
		URLClassLoader cl = new URLClassLoader(new URL[] { new File(dir).toURI().toURL() });

		Map<String, CommandResponse> map = new TreeMap<String, CommandResponse>();
		List<CommandResponse> results = new ArrayList<CommandResponse>();
		for (String f : scanDir.list()) {
			log.debug("examining " + f);

			if (!f.endsWith(".class")) {
				continue;
			}

			if (f.endsWith("TL1Wrapper.class")) {
				continue;
			}

			CommandResponse c = loadIt(cl, packageName + "." + f);
			if (map.get(c.getVerb()) != null) {
				CommandResponse x = map.get(c.getVerb());
				if (!c.toString().equals(x.toString())) {
					log.error("Duplicated verb \n\t" + x.toString()
					    + "\n\t with different \n\t" + c.toString() + "\n\t for verb "
					    + c.getVerb());
					results.add(c);
				}
				// dont' add it if they are the same

			}
			else {
				results.add(c);
			}
			map.put(c.getVerb(), c);

		}
		StringBuilder sb = new StringBuilder();
		for (CommandResponse c : results) {
			sb.append(c.toString());
			sb.append("\n");
		}
		log.debug(sb.toString());

		FileWriter fw = new FileWriter("commandList.txt");
		fw.write("#TL1WRAPPER:#NETYPE=CPL,#VERSION=3.0,#TOTALCOMMANDSNUMBER=65,#LASTMODIFIEDDATA=2008.05.NOV,#DESCRIPTION=THIS TL1WRAPPER.JAR FILE IS FOR CPL updated by wp RTRVRTGINFO was wrong");
		fw.write('\n');
		fw.write(sb.toString());
		fw.write('\n');
		fw.close();

	}

	/**
	 * <pre>
	 * public class ACT_USER {
	 * 
	 * 	public static final boolean isMOD2Required = false;
	 * 	public static final boolean isMOD2Enumerated = false;
	 * 	public static final boolean isUnprintable = false;
	 * 	public static final boolean isTIDRequired = true;
	 * 	public static final String EmbeddedType = &quot;NONE&quot;;
	 * 	public static final String AID[] = { &quot;UID&quot; };
	 * 	public static final String CTAG = &quot;CTAG&quot;;
	 * 	public static final boolean isGBlockPresent = false;
	 * 	public static Parameter F_BLOCKS[][] = { { new Parameter(&quot;PID&quot;, false, &quot;PID&quot;) } };
	 * 	public static final boolean F_Block_Is_Pos[] = { true };
	 * 	public static Parameter R_BLOCKS[][] = new Parameter[0][];
	 * 	public static final boolean R_Block_Is_Pos[] = new boolean[0];
	 * }
	 * </pre>
	 * 
	 * Needs to produce
	 * 
	 * <pre>
	 *  #CMD=ACT-USER:[<TID>]:UID<UID>:<CTAG>::PID<PID>;
	 * </pre>
	 * 
	 * We can enumerate mod2 types such as
	 * 
	 * <pre>
	 * CMD=REPT-AD-{AIDTYPE=OC3,OC12,OC48,OC192,(ALL)}:[<TID>]::<CTAG>;
	 * </pre>
	 * 
	 * In a general form: #CMD=$COMMAND:[<TID>]:$AID:CTAG:$G_BLOCKS:$F_BLOCKS;
	 */
	public static void main(String[] args) {
		try {
			log.debug("Starting");
			// new ReverseEngineerIt("D:/Profiles/pitman/Desktop/ome",
			// "com.nortel.omea.ome6500.release2_0.mediation.tl1wrapper.stubs");

			new ReverseEngineerIt("D:/Profiles/pitman/Desktop/drac/cpl",
			    "com.nortel.omea.cpl.release3_0.mediation.tl1wrapper.stubs");
		}
		catch (Exception t) {
			log.error("Error: ", t);
		}
		log.debug("Done");
	}

	private void addBlock(StringBuilder sb, Object[] block, boolean isPos)
	    throws Exception {
		if (isPos) {
			boolean some = false;
			for (Object o : block) {
				some = true;
				String protocolLabel = (String) o.getClass().getMethod("getLabel")
				    .invoke(o);
				String normalizationLabel = (String) o.getClass()
				    .getMethod("getNormalizationLabel").invoke(o);
				sb.append(protocolLabel);
				sb.append('<');
				sb.append(normalizationLabel);
				sb.append('>');
				sb.append(',');
			}
			if (some) {
				// remove the trailing comma
				sb.setLength(sb.length() - 1);
			}
		}
		else {
			boolean some = false;
			for (Object o : block) {
				String protocolLabel = (String) o.getClass().getMethod("getLabel")
				    .invoke(o);
				String normalizationLabel = (String) o.getClass()
				    .getMethod("getNormalizationLabel").invoke(o);
				sb.append("[");
				if (some) {
					sb.append(',');
				}
				sb.append(protocolLabel);
				sb.append("=<");
				sb.append(normalizationLabel);
				sb.append(">]");
				some = true;
			}
		}

		sb.append(':');
	}

	private CommandResponse loadIt(URLClassLoader loader, String c)
	    throws Exception {
		String cName = c.replace(".class", "");
		Class<?> theClass = loader.loadClass(cName);

		Set<String> fieldSets = new HashSet<String>();
		Field[] fields = theClass.getFields();

		for (Field f : fields) {
			fieldSets.add(f.getName());
		}

		fieldSets.remove("isMOD2Required");
		fieldSets.remove("isMOD2Enumerated");
		fieldSets.remove("isUnprintable");
		fieldSets.remove("isTIDRequired");
		fieldSets.remove("EmbeddedType");
		fieldSets.remove("AID");
		fieldSets.remove("CTAG");
		fieldSets.remove("isGBlockPresent");
		fieldSets.remove("G_Block_Is_Pos");
		fieldSets.remove("G_BLOCK");

		fieldSets.remove("F_BLOCKS");
		fieldSets.remove("F_Block_Is_Pos");
		fieldSets.remove("R_BLOCKS");
		fieldSets.remove("R_Block_Is_Pos");

		fieldSets.remove("MOD2Name");
		fieldSets.remove("mod2Values");
		fieldSets.remove("MOD2Def");

		if (fieldSets.size() > 0) {
			log.debug("******** Extra fields " + fieldSets.toString());
			Thread.sleep(1000);
		}

		String MOD2Name = null;
		String[] mod2Values = null;
		String MOD2Def = null;

		boolean G_Block_Is_Pos = false;
		Object[] G_BLOCK = null;

		// boolean isMOD2Required =
		// theClass.getField("isMOD2Required").getBoolean(null);
		boolean isMOD2Enumerated = theClass.getField("isMOD2Enumerated")
		    .getBoolean(null);

		if (isMOD2Enumerated) {
			MOD2Name = theClass.getField("MOD2Name").get(null).toString();
			mod2Values = (String[]) theClass.getField("mod2Values").get(null);
			try {
				MOD2Def = theClass.getField("MOD2Def").get(null).toString();
			}
			catch (NoSuchFieldException nfe) {
				// ignore
				MOD2Def = null;
			}
		}
		// boolean isUnprintable =
		// theClass.getField("isUnprintable").getBoolean(null);
		// boolean isTIDRequired =
		// theClass.getField("isTIDRequired").getBoolean(null);

		// String EmbeddedType = theClass.getField("EmbeddedType").get(null)
		// .toString();
		String AID[] = (String[]) theClass.getField("AID").get(null);
		String CTAG = theClass.getField("CTAG").get(null).toString();

		boolean isGBlockPresent = theClass.getField("isGBlockPresent").getBoolean(
		    null);
		if (isGBlockPresent) {
			G_Block_Is_Pos = theClass.getField("G_Block_Is_Pos").getBoolean(null);
			G_BLOCK = (Object[]) theClass.getField("G_BLOCK").get(null);
		}

		Object[][] F_BLOCKS = (Object[][]) theClass.getField("F_BLOCKS").get(null);
		boolean F_Block_Is_Pos[] = (boolean[]) theClass.getField("F_Block_Is_Pos")
		    .get(null);
		Object R_BLOCKS[][] = (Object[][]) theClass.getField("R_BLOCKS").get(null);
		boolean R_Block_Is_Pos[] = (boolean[]) theClass.getField("R_Block_Is_Pos")
		    .get(null);

		// isMOD2Enumerated +
		// " isUnprintable " + isUnprintable
		// + " isTIDRequired " + isTIDRequired + " EmbeddedType " + EmbeddedType +
		// " AID[] " +
		// Arrays.toString(AID)
		// + " CTAG " + CTAG + " isGBlockPresent " + isGBlockPresent + " F_BLOCKS "
		// +
		// Arrays.toString(F_BLOCKS)
		// + " F_Block_Is_Pos " + Arrays.toString(F_Block_Is_Pos) + " R_BLOCKS " +
		// Arrays.toString(R_BLOCKS)
		// + " R_Block_Is_Pos " + Arrays.toString(R_Block_Is_Pos));

		/*
		 * In a general form: #CMD=$COMMAND:[<TID>]:$AID:CTAG:$G_BLOCKS:$F_BLOCKS;
		 */

		StringBuilder resp = null;
		StringBuilder cmd = new StringBuilder("#CMD=");
		String verb;

		String tl1Name = theClass.getSimpleName().replace('_', '-');

		verb = tl1Name;

		if (isMOD2Enumerated) {
			String[] b = tl1Name.split("-");
			if (b.length >= 2) {
				cmd.append(b[0]);
				cmd.append('-');
				cmd.append(b[1]);
				verb = b[0] + '-' + b[1];
			}

			cmd.append("-{");
			cmd.append(MOD2Name);
			cmd.append('=');
			boolean some = false;
			for (String s : mod2Values) {
				cmd.append(s);
				cmd.append(',');
				some = true;
			}
			if (some) {
				// remove trailing comma
				cmd.setLength(cmd.length() - 1);
			}
			if (MOD2Def != null && !"".equals(MOD2Def)) {
				cmd.append(",(");
				cmd.append(MOD2Def);
				cmd.append(')');
			}
			else {
				// TODO: This may be wrong
				cmd.append(",(ALL)");
			}
			cmd.append("}");
		}
		else {
			cmd.append(tl1Name);
		}
		cmd.append(":[<TID>]:");

		if (AID != null && AID.length > 0) {
			// AID [FROMAID,TOAID] becomes FROMAID<FROMAID>,TOAID<TOAID>
			boolean some = false;
			for (String a : AID) {
				cmd.append(a);
				cmd.append('<');
				cmd.append(a);
				cmd.append('>');
				cmd.append(',');
				some = true;
			}

			if (some) {
				// remove the trailing ,
				cmd.setLength(cmd.length() - 1);
			}

		}

		cmd.append(":<");
		cmd.append(CTAG);
		cmd.append(">:");
		if (isGBlockPresent) {
			addBlock(cmd, G_BLOCK, G_Block_Is_Pos);
		}
		else {
			if (F_BLOCKS.length > 0) {
				cmd.append(':');
			}
		}

		// F-blocks
		boolean some = false;
		for (int i = 0; i < F_BLOCKS.length; i++) {
			some = true;
			addBlock(cmd, F_BLOCKS[i], F_Block_Is_Pos[i]);
		}
		if (some) {
			cmd.setLength(cmd.length() - 1);
		}
		if (cmd.charAt(cmd.length() - 1) == ':') {
			// chop of the trailing colon
			cmd.setLength(cmd.length() - 1);
		}
		cmd.append(';');

		if (R_BLOCKS.length > 0) {
			resp = new StringBuilder("#RESP=");
			for (int i = 0; i < R_BLOCKS.length; i++) {
				addBlock(resp, R_BLOCKS[i], R_Block_Is_Pos[i]);
			}
			resp.setLength(resp.length() - 1);
		}

		return new CommandResponse(verb, cmd.toString(), resp == null ? null
		    : resp.toString());
	}
}

final class CommandResponse {
	private final String cmd;
	private final String rsp;
	private final String v;

	public CommandResponse(String verb, String command, String response) {
		v = verb;
		cmd = command;
		rsp = response;
	}

	public String getVerb() {
		return v;
	}

	@Override
	public String toString() {
		if (rsp == null) {
			return cmd;
		}

		return cmd + "\n" + rsp;
	}
}
