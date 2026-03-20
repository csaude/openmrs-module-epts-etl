package org.openmrs.module.epts.etl.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	public static String qualifyUnqualifiedSqlFields(String sql, String tableName) {
		
		if (sql == null || sql.isBlank()) {
			return sql;
		}
		
		Set<String> SQL_KEYWORDS = Set.of("and", "or", "not", "in", "is", "null", "like", "between", "exists", "select",
		    "from", "where", "join", "left", "right", "inner", "outer", "on", "as", "case", "when", "then", "else", "end",
		    "distinct", "group", "by", "order", "having", "limit", "offset", "true", "false", "asc", "desc");
		
		Pattern tokenPattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
		
		StringBuilder result = new StringBuilder();
		
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		int lastIndex = 0;
		
		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
			
			// Quando entramos ou saímos de string, processamos o trecho anterior
			if ((c == '\'' && !inDoubleQuote) || (c == '"' && !inSingleQuote)) {
				
				if (!inSingleQuote && !inDoubleQuote) {
					// acabou string → copiar direto
					result.append(sql, lastIndex, i + 1);
					lastIndex = i + 1;
				} else {
					// começou string → processar antes dela
					String segment = sql.substring(lastIndex, i);
					result.append(processSegment(segment, tableName, SQL_KEYWORDS, tokenPattern));
					lastIndex = i;
				}
			}
		}
		
		// último trecho
		if (lastIndex < sql.length()) {
			if (inSingleQuote || inDoubleQuote) {
				result.append(sql.substring(lastIndex));
			} else {
				result.append(processSegment(sql.substring(lastIndex), tableName, SQL_KEYWORDS, tokenPattern));
			}
		}
		
		return result.toString();
	}
	
	private static String processSegment(String segment, String tableName, Set<String> SQL_KEYWORDS, Pattern pattern) {
		
		Matcher matcher = pattern.matcher(segment);
		StringBuffer sb = new StringBuffer();
		
		while (matcher.find()) {
			
			String token = matcher.group();
			
			int start = matcher.start();
			int end = matcher.end();
			
			boolean hasDotBefore = start > 0 && segment.charAt(start - 1) == '.';
			boolean hasDotAfter = end < segment.length() && segment.charAt(end) == '.';
			boolean isKeyword = SQL_KEYWORDS.contains(token.toLowerCase());
			boolean isFunction = end < segment.length() && segment.charAt(end) == '(';
			boolean isParameter = start > 0 && segment.charAt(start - 1) == '@';
			
			if (hasDotBefore || hasDotAfter || isKeyword || isFunction || isParameter) {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
			} else {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(tableName + "." + token));
			}
		}
		
		matcher.appendTail(sb);
		return sb.toString();
	}
}
