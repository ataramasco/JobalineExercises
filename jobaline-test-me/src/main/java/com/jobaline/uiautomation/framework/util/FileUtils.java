package com.jobaline.uiautomation.framework.util;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.generators.IdGenerator;
import com.jobaline.uiautomation.framework.lang.Pause;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by damian on 5/27/15.
 */
public class FileUtils
{

	public static String createTempFile(String content)
	{
		String absolutePath = null;

		String fileName = IdGenerator.generateShort();

		boolean fileCreated = false;
		int tries = 3;
		int waitBetweenTries = 2000;
		for(int i = 0; i < tries; i++)
		{
			try
			{
				File file = File.createTempFile(fileName, ".tmp");

				org.apache.commons.io.FileUtils.writeStringToFile(file, content);

				absolutePath = file.getAbsolutePath();

				fileCreated = true;
				break;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			Pause.milliseconds(waitBetweenTries);
		}

		if(!fileCreated || absolutePath == null)
		{
			throw new RuntimeException("Could not create the temporary file");
		}

		return absolutePath;
	}


	public static String copyFileToATempFile(String sourceFilePath)
	{
		String absolutePath = null;

		String fileName = IdGenerator.generateShort();

		boolean fileCreated = false;
		int tries = 3;
		int waitBetweenTries = 2000;
		for(int i = 0; i < tries; i++)
		{
			try
			{
				String extension = FilenameUtils.getExtension(sourceFilePath);

				File source = new File(ResourceManager.getResourceURI(sourceFilePath));
				File dest = File.createTempFile(fileName, extension);

				Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

				absolutePath = dest.getAbsolutePath();

				fileCreated = true;
				break;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			Pause.milliseconds(waitBetweenTries);
		}

		if(!fileCreated || absolutePath == null)
		{
			throw new RuntimeException("Could not create the temporary file");
		}

		return absolutePath;
	}
}
