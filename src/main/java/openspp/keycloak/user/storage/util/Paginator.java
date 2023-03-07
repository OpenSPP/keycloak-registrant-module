package openspp.keycloak.user.storage.util;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;

import openspp.keycloak.user.storage.JDBC;
import openspp.keycloak.user.storage.UserStorageException;

public class Paginator {

    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern SINGLE_QUESTION_MARK_REGEX = Pattern.compile("(^|[^\\?])(\\?)([^\\?]|$)");

    public static class Pageable {
        private final int firstResult;
        private final int maxResults;

        public Pageable(int firstResult, int maxResults) {
            this.firstResult = firstResult;
            this.maxResults = maxResults;
        }
    }

    public static String getPagableQuery(String query, Pageable pageable, JDBC JDBC) {

        final Dialect dialect = JDBC.getDialect();

        RowSelection rowSelection = new RowSelection();
        rowSelection.setFetchSize(pageable.maxResults);
        rowSelection.setFirstRow(pageable.firstResult);
        rowSelection.setMaxRows(pageable.maxResults);

        String escapedSQL = escapeQuestionMarks(query);

        StringBuilder processedSQL;
        try {
            LimitHandler limitHandler = dialect.getLimitHandler();
            processedSQL = new StringBuilder(limitHandler.processSql(escapedSQL, rowSelection));
            int col = 1;
            PreparedStatementParameterCollector collector = new PreparedStatementParameterCollector();
            col += limitHandler.bindLimitParametersAtStartOfQuery(rowSelection, collector, col);
            limitHandler.bindLimitParametersAtEndOfQuery(rowSelection, collector, col);

            Map<Integer, Object> parameters = collector.getParameters();
            for (int i = 1; i <= parameters.keySet().size(); i++) {
                Matcher matcher = SINGLE_QUESTION_MARK_REGEX.matcher(processedSQL);
                if (matcher.find()) {
                    String str = String.valueOf(parameters.get(i));
                    processedSQL.replace(matcher.start(2), matcher.end(2), str);
                }
            }
            return unescapeQuestionMarks(processedSQL.toString());

        } catch (SQLException e) {
            throw new UserStorageException(e.getMessage(), e);
        }
    }

    private static String unescapeQuestionMarks(String sql) {
        return sql.replaceAll("\\?\\?", "?");
    }

    private static String escapeQuestionMarks(String sql) {
        return sql.replaceAll("\\?", "??");
    }

}
