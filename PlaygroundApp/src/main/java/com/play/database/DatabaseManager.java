package com.play.database;

import javax.servlet.ServletContext;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class DatabaseManager {

    private ServletContext context;

    public void setContext(ServletContext context) {
        this.context = context;
    }

    public DatabaseManager(ServletContext context) {
        setContext(context);
    }

    // returns null on error
    private List<Object[]> makeDBCall(String query, List<Param> params, boolean expectResultSet) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBCPDataSource.getConnection();
            ps = conn.prepareStatement(query);

            addParams(ps, params);

            if (expectResultSet) {
                return getResultSetAsObjectList(rs, ps);
            } else {
                return getRowsAffectedByUpdate(ps);
            }
        } catch (SQLException e) {
            context.log("Error communicating with SQL DB", e);
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private List<Object[]> getResultSetAsObjectList(ResultSet rs, PreparedStatement ps) throws SQLException {
        rs = ps.executeQuery();

        List<Object[]> resultList = new ArrayList<>();
        while(rs.next()) {
            ResultSetMetaData meta = rs.getMetaData();

            int cols = meta.getColumnCount();
            Object[] result = new Object[cols];

            for (int i = 0; i < cols; i++) {
                result[i] = rs.getObject(i + 1);
            }

            resultList.add(result);
        }

        return resultList;
    }

    private List<Object[]> getRowsAffectedByUpdate(PreparedStatement ps) throws SQLException {
        int rowsAffected = ps.executeUpdate();

        List<Object[]> resultList = new ArrayList<>();
        Object[] result = new Object[1];
        result[0] = rowsAffected;
        resultList.add(result);

        return resultList;
    }

    private void addParams(PreparedStatement ps, List<Param> params) throws SQLException {
        int position = 1;
        for (Param param : params) {
            switch(param.type) {
                case Types.VARCHAR:
                case Types.CHAR:
                    String stringParam = (String) param.getValue();
                    ps.setString(position, stringParam);
                    break;
                case Types.TIMESTAMP:
                    Timestamp timestampParam = (Timestamp) param.getValue();
                    ps.setTimestamp(position, timestampParam);
                    break;
                default:
                    throw new SQLException("Error: Could not match SQL Type to Param Type.");
            }
            position++;
        }
    }

    // returns -1 on error
    private long getCount(String query, List<Param> params) {
        List<Object[]> results = makeDBCall(query, params, true);

        if (results == null || results.isEmpty()) {
            return -1;
        }

        Object result = results.get(0)[0];
        if (result == null) {
            return -1;
        } else {
            return (long) result;
        }
    }

    public long selectUsernameCount(String username) {
        List<Param> params = new ArrayList<Param>();
        params.add(new Param(Types.VARCHAR, username));

        return getCount(DatabaseQueries.SELECT_USERNAME_COUNT, params);
    }

    public long selectEmailCount(String email) {
        List<Param> params = new ArrayList<>();
        params.add(new Param(Types.VARCHAR, email));

        return getCount(DatabaseQueries.SELECT_EMAIL_COUNT, params);
    }

    public int insertNewUser(String username, String email, String hashedPassword) {
        List<Param> params = new ArrayList<>();
        params.add(new Param(Types.VARCHAR, username));
        params.add(new Param(Types.VARCHAR, email));
        params.add(new Param(Types.CHAR, hashedPassword));

        List<Object[]> result = makeDBCall(DatabaseQueries.INSERT_NEW_USER, params, false);

        if (result == null || result.isEmpty()) {
            return -1;
        }

        return (int) result.get(0)[0];
    }

    public String selectUserPassword(String usernameOrEmail) {
        List<Param> params = new ArrayList<>();
        params.add(new Param(Types.VARCHAR, usernameOrEmail));
        params.add(new Param(Types.VARCHAR, usernameOrEmail));

        List<Object[]> result = makeDBCall(DatabaseQueries.SELECT_USER_PASSWORD, params, true);

        if (result == null || result.isEmpty()) {
            return null;
        }

        return (String) result.get(0)[0];
    }

    private class Param {
        private int type;
        private Object value;

        public Param (int type, Object value) {
            setType(type);
            setValue(value);
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
