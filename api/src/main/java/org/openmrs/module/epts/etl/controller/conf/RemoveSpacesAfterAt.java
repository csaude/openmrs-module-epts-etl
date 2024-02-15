package org.openmrs.module.epts.etl.controller.conf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveSpacesAfterAt {
    public static void main(String[] args) {
       String query = "select * from patient where patient_id = @\n  patientId and u=@kaka and J=@\n yusten";
       
       
       
       System.out.println(replaceParametersWithQuestionMarks(query));
       
    }

    private static String replaceParametersWithQuestionMarks(String sqlQuery) {
        // Regular expression to match parameters starting with @, considering optional spaces or newlines
        String parameterRegex = "@\\s*\\w+";
        Pattern pattern = Pattern.compile(parameterRegex);
        Matcher matcher = pattern.matcher(sqlQuery);

        // Replace each parameter with a question mark
        StringBuffer replacedQuery = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(replacedQuery, "?");
        }
        matcher.appendTail(replacedQuery);

        return replacedQuery.toString();
    }
}