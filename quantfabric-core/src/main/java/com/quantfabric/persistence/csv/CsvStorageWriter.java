/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quantfabric.persistence.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.supercsv.io.CsvListWriter;
//import org.supercsv.prefs.CsvPreference;

//import com.quantfabric.persistence.esper.EsperCsvStorageProvider.EsperCsvMetadata;
//import com.quantfabric.persistence.esper.EsperCsvStorageProvider.FlatCompositeObjectField;
import com.quantfabric.util.Converter;

public class CsvStorageWriter
{
	private static final Logger log = LoggerFactory.getLogger(CsvStorageWriter.class);
	
	//private CsvListWriter csvWriter;
	private CSVPrinter csvPrinter;
	@SuppressWarnings("unused")
	private final CsvMetadata metadata;
	private final CsvStorageProviderSettings settings;
	private final String storageLoaction;
	private Timer flusher;
	
	private int currentPartitionIndex = 0;
	private int countRecordsInCurrentPartition = 0;	
	
	public CsvStorageWriter(CsvStorageProviderSettings settings, CsvMetadata metadata) throws IOException
	{
		this.settings = settings;
		this.metadata = metadata;
		
		this.storageLoaction = (settings.getPathToStorage() + "/" + metadata.getSchemaName()).toLowerCase();
		
		prepareStorageLocation(storageLoaction, metadata.getSchemaName());	
		saveMetadata(storageLoaction, metadata);
		
		newWriter();
		
		setupFlusher(10000);
	}
	
	private void setupFlusher(int period)
	{
		if (flusher != null)
			flusher.cancel();
		
		TimerTask flusherTask = new TimerTask() 
		{
			@Override
			public void run()
			{
				synchronized (csvPrinter)
				{
					try
					{
						csvPrinter.flush();
					}
					catch (IOException e)
					{
						log.error("flush fail.", e);
					}
				}
			}
		};
		
		flusher = new Timer();
		flusher.schedule(flusherTask, period, period);
				
	}

	private void newWriter() throws IOException	
	{
		//CsvPreference csvPref = CsvPreference.STANDARD_PREFERENCE;
		Writer currentOutput = getOutputStream(storageLoaction, String.valueOf(currentPartitionIndex));		
		this.csvPrinter = new CSVPrinter(currentOutput, CSVFormat.DEFAULT);
		this.countRecordsInCurrentPartition = 0;
	}
	
	private static void saveMetadata(String storageLoaction, CsvMetadata metadata)
		throws IOException
	{
		File f = new File(storageLoaction + "/metadata.xml");
		try
		{
			f.createNewFile();
		}
		catch (IOException e)
		{
			try
			{
				log.info("Can't create metadata file (" + f.getAbsolutePath() + "). Retry...");
				Thread.sleep(2000);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			f = new File(storageLoaction + "/metadata.xml");
			f.createNewFile();
			log.info("Metadata file (" + f.getAbsolutePath() + ") was created successfully.");
		}
		
		try(FileWriter fw = new FileWriter(f)) {

			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fw.write(String.format("\t<object name=\"%s\">%n", metadata.getSchemaName()));

			for (Map.Entry<String, Class<? extends Object>> field : metadata.getFields().entrySet()) {
				String fieldName = field.getKey();
				Class<? extends Object> fieldType = field.getValue();

				if (fieldType.isPrimitive())
					fieldType = Converter.prmitiveTypeMappingToReferenceType.get(fieldType);

				fw.write(String.format("\t\t<field name=\"%s\" type=\"%s\"/>%n", fieldName, fieldType.getName()));
			}

			fw.write("\t</object>");

		}
	}

	private static void prepareStorageLocation(String pathToStorage, String schemaName)
	{
		deleteDir(new File(pathToStorage));
		try
		{
			//delay before creating folder which just was deleted - for updating of OS file system. 
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		new File(pathToStorage).mkdirs();
	}

	private static boolean deleteDir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) { return false; }
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
	
	public void saveBean(CsvBean bean) throws IOException	
	{
		synchronized (csvPrinter)
		{	
			try
			{
				List<Object> values = bean.getValues();

				Integer index = 0;
				for (Object value : bean.getValues())
				{
					if (value == null)
						values.set(index, "");
					else						
						if (value instanceof Boolean)
							values.set(index, ((Boolean)value).booleanValue() ? "1" : "0");
						else
							if (value instanceof Date)
								values.set(index, new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS").format((Date)value));
					index++;
				}

				csvPrinter.print(values);
			}
			catch(Exception e)
			{
				System.out.println("!!!DEBUG");
			}
			countRecordsInCurrentPartition++;
			
			if (settings.getPartitionSize() != CsvStorageProviderSettings.PARTITION_NOT_REQUIRED)
			{
				if (countRecordsInCurrentPartition == settings.getPartitionSize())
				{
					csvPrinter.close();
					currentPartitionIndex++;
					newWriter();
				}
			}
		}
	}
	
	protected static Writer getOutputStream(String pathToStorage, String postfix) throws IOException
	{
		String path = pathToStorage + "/"+ "data-" + postfix + ".csv";
		File f = new File(path);
		try
		{			
			f.createNewFile();
		}
		catch (IOException e)
		{
			try
			{
				log.info("Can't create data file (" + f.getAbsolutePath() + "). Retry...");
				Thread.sleep(2000);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			f = new File(path);
			f.createNewFile();
			log.info("Data file (" + f.getAbsolutePath() + ") was created successfully.");
		}
		
		lockFile(pathToStorage, f);
		
		return new FileWriter(f);
	}
	
	private static void lockFile(String pathToStorage, File f) throws IOException
	{
		FileWriter fw = new FileWriter(pathToStorage + "/.lockDataFile");
		try
		{
			fw.write(f.getName());
		}
		finally
		{
			fw.close();
		}
		
	}

	public void close() throws IOException
	{
		flusher.cancel();
		csvPrinter.close();
		new File(storageLoaction + "/.lockDataFile").delete();
	}
}
