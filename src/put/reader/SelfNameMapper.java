package put.reader;

public class SelfNameMapper implements INameMapper{

	@Override
	public String getValue(String key) {
		return key;
	}

}
