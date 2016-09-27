package com.jobaline.uiautomation.framework.casperjs;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.lang.MapUtils;
import com.jobaline.uiautomation.framework.util.FileUtils;
import com.jobaline.uiautomation.framework.EnvironmentUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by damian on 3/11/15.
 */
public class CasperJS
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CasperJS.class.getName().replace("com.jobaline.uiautomation.", ""));

	private static final String ENCODED_SPACE         = "-!-";
	private static final String ENCODED_DOUBLE_QUOTES = "-%-";

	private boolean takeScreenshots;


	public CasperJS()
	{
		this(false);
	}


	public CasperJS(boolean takeScreenshots)
	{
		this.takeScreenshots = takeScreenshots;
	}


	private String createCommand(String script, JSONObject args)
	{
		String casperJSPath = EnvironmentUtils.getCasperJSPath();
		String scriptPath = ResourceManager.getResourceAbsolutePath(script);

		String encodedArgs = args.toString().replaceAll(" ", ENCODED_SPACE).replaceAll("\"", ENCODED_DOUBLE_QUOTES);

		String command;

		if(encodedArgs.length() > 1024) // 1024 is arbitrary. We should do a stress testing to find the break point.
		{
			String argsFilePath = FileUtils.createTempFile(encodedArgs).replaceAll(" ", ENCODED_SPACE);

			String encodedFilePath = argsFilePath.replaceAll(" ", ENCODED_SPACE);

			command = String.format(
				casperJSPath + " test \"%s\" --ignore-ssl-errors=yes --myargsfile=\"%s\"",
				scriptPath,
				encodedFilePath
			);
		}
		else
		{
			command = String.format(
				casperJSPath + " test \"%s\" --myargs=\"%s\"",
				scriptPath,
				encodedArgs
			); // I had to use "myargs" becase "args" is used internally used by CasperJS
		}

		return command;
	}


	/**
	 * TODO implement timeout using a watch dog
	 * */
	public CasperJsResult executeScript(String script, JSONObject args, long timeout)
	{
		CasperJsResult result;

		if(takeScreenshots)
		{
			try
			{
				args = new JSONObject(args.toString());
				args.put("screenshotsDir", EnvironmentUtils.getScreenshotsDirectoryPath(true));
			}
			catch(JSONException e)
			{
				e.printStackTrace();

				throw new RuntimeException("There was an error trying to set the argument 'screenshotsDir' to the argument list of the CasperJS script.");
			}
		}

		String command = createCommand(script, args);

		ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
		PumpStreamHandler ps = new PumpStreamHandler(standardOutput);

		DefaultExecutor executor = new DefaultExecutor();

		executor.setStreamHandler(ps);

		System.out.println(command);

		String casperJsRawResult = null;

		try
		{
			// When there is a test failure, the casperjs script will exit with a value different from zero and the ExecuteException will be thrown. This is
			// perfectly expected and handled because we still have the result line. But if CasperJS crashes, abort, or there is a sintaxis error, I'm not sure yet what is the
			// exit status and if we can identify the failure. Right now it is being generally handled because the result line will not be present or will be invalid and
			// this will be handled by CasperJsResult.parse() but we need to be more granular and identify the root causes, which may be CasperJsResult.CODE_ERROR_PROCESS_CRASHED or
			// CasperJsResult.CODE_ERROR_OUT_OF_MEMORY or maybe someone else yet not identified.
			int exitStatus; // TODO considering the previous comment, use the exit status to handle better CasperJS errors (crash, out of memory, syntax error, etc)
			try
			{
				exitStatus = executor.execute(CommandLine.parse(command), MapUtils.createStringStringMap(
					"PHANTOMJS_EXECUTABLE", EnvironmentUtils.getPhantomJSPath()
				));
			}
			catch(ExecuteException e)
			{
				exitStatus = e.getExitValue();
			}

			Reader reader = new InputStreamReader(new ByteArrayInputStream(standardOutput.toByteArray()));
			BufferedReader r = new BufferedReader(reader);

			String tmp;
			while((tmp = r.readLine()) != null)
			{
				LOGGER.trace("CasperJS output: " + tmp);

				if(tmp.startsWith("<<<result>>>"))
				{
					casperJsRawResult = tmp.replace("<<<result>>>", "").replace("<<</result>>>", "");
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		result = CasperJsResult.parse(casperJsRawResult);

		return result;
	}

}
