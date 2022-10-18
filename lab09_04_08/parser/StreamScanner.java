package lab09_04_08.parser;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class StreamScanner implements Scanner {
	private static final Pattern any = Pattern.compile(".*");
	private final Matcher matcher;
	private final BufferedReader buffReader;
	private MatchResult result = Pattern.compile("").matcher("").toMatchResult();

	private void reset(int start, int end, Pattern pat) {
		matcher.region(start, end);
		matcher.usePattern(pat);
	}

	private String skip() {
		String skipped;
		int regionEnd = matcher.regionEnd();
		Pattern pat = matcher.pattern();
		int end = matcher.find() ? matcher.start() : matcher.regionEnd();
		reset(matcher.regionStart(), end, any);
		matcher.lookingAt();
		skipped = matcher.group();
		reset(end, regionEnd, pat);
		return skipped;
	}

	public StreamScanner(String regex, Reader reader) {
		matcher = Pattern.compile(regex).matcher("");
		buffReader = new BufferedReader(reader);
	}

	@Override
	public void next() throws ScannerException {
		if (!hasNext())
			throw new ScannerException("Unexpected end of the stream");
		boolean matched = matcher.lookingAt();
		result = matcher.toMatchResult();
		if (!matched)
			throw new ScannerException("Unrecognized string " + skip());
		else
			matcher.region(matcher.end(), matcher.regionEnd());
	}

	@Override
	public boolean hasNext() throws ScannerException {
		String line;
		if (matcher.regionStart() == matcher.regionEnd()) {
			try {
				line = buffReader.readLine();
			} catch (IOException e) {
				throw new ScannerException(e);
			}
			if (line == null) {
				matcher.reset("");
				return false;
			}
			matcher.reset(line + " ");
		}
		return true;
	}

	@Override
	public String group() {
		return result.group();
	}

	@Override
	public String group(int group) {
		return result.group(group);
	}

	@Override
	public void close() throws ScannerException {
		try {
			buffReader.close();
		} catch (IOException e) {
			throw new ScannerException(e);
		}
	}

}
