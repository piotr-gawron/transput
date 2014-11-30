package put.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import put.Configuration;

public class FileNameMapper implements INameMapper {

	Map<String, String>	map	= new HashMap<String, String>();

	public FileNameMapper(String fileName, int fromColumn, int toColumn) throws IOException, InvalidFileException {
		Configuration configuration = Configuration.getConfiguration();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), configuration.getEncoding()));
			String line = br.readLine();

			while (line.length() > 0 && line.charAt(0) == 65279) {
				line = line.substring(1);
			}
			while (line != null) {
				String cols[] = line.split("\t");
				if (cols.length < Math.max(fromColumn, toColumn)) {
					throw new InvalidFileException("Too few columns: " + cols.length);
				}
				String key = cols[fromColumn];
				String value = cols[toColumn];
				if (getValue(key) != null)
					throw new InvalidFileException("Key duplicate: " + key);
				map.put(key, value);
				line = br.readLine();
			}
		} finally {
			if (br != null)
				br.close();
		}

	}

	@Override
	public String getValue(String key) {
		return map.get(key.trim().toLowerCase());
	}

}
