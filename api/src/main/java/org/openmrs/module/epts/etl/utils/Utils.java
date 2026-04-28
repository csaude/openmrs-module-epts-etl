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
		
		Pattern pattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
		Matcher matcher = pattern.matcher(sql);
		
		StringBuffer sb = new StringBuffer();
		
		while (matcher.find()) {
			
			String token = matcher.group();
			
			int start = matcher.start();
			int end = matcher.end();
			
			boolean hasDotBefore = start > 0 && sql.charAt(start - 1) == '.';
			boolean hasDotAfter = end < sql.length() && sql.charAt(end) == '.';
			boolean isKeyword = SQL_KEYWORDS.contains(token.toLowerCase());
			boolean isFunction = end < sql.length() && sql.charAt(end) == '(';
			boolean isParameter = start > 0 && sql.charAt(start - 1) == '@';
			
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
