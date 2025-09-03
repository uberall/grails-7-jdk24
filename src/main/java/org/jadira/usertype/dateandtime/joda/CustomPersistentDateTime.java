package org.jadira.usertype.dateandtime.joda;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor; // present in newer interfaces
import org.hibernate.engine.spi.SessionImplementor; // legacy signature required by current Hibernate in Grails 7
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserVersionType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Serializable;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Minimal self-contained replacement for the older Jadira AbstractVersionableUserType bridge.
 * Stores Joda DateTime as a TIMESTAMP (no timezone) in the configured databaseZone (default Europe/Berlin)
 * and returns application objects in the same zone (no shifting). Zones can be overridden via parameters
 * 'databaseZone' and 'javaZone' if needed.
 */
public class CustomPersistentDateTime implements UserVersionType, ParameterizedType {

    private static final int[] SQL_TYPES = new int[]{Types.TIMESTAMP};

    // Store and read using Europe/Berlin (DB shows local wall-clock time) and keep same in Java objects.
    private DateTimeZone databaseZone = DateTimeZone.forID("Europe/Berlin");
    private DateTimeZone javaZone = DateTimeZone.forID("Europe/Berlin");

    @Override
    public void setParameterValues(Properties parameters) {
        if (parameters == null) return;
        String dbz = parameters.getProperty("databaseZone");
        if (dbz != null && !dbz.isBlank() && !"jvm".equalsIgnoreCase(dbz)) {
            databaseZone = DateTimeZone.forID(dbz);
        }
        String jz = parameters.getProperty("javaZone");
        if (jz != null && !jz.isBlank() && !"jvm".equalsIgnoreCase(jz)) {
            javaZone = DateTimeZone.forID(jz);
        }
    }

    @Override
    public int[] sqlTypes() { return SQL_TYPES; }

    @Override
    public Class<?> returnedClass() { return DateTime.class; }

    @Override
    public boolean equals(Object x, Object y) { return Objects.equals(x, y); }

    @Override
    public int hashCode(Object x) { return Objects.hashCode(x); }

    // Hibernate (current in Grails 7) expects legacy SessionImplementor signatures.
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws SQLException {
        Timestamp ts = rs.getTimestamp(names[0]);
        if (rs.wasNull() || ts == null) return null;
        DateTime dtInDbZone = new DateTime(ts.getTime(), databaseZone);
        return dtInDbZone.withZone(javaZone);
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.TIMESTAMP);
            return;
        }
        DateTime dt = (DateTime) value;
        DateTime dtForDb = dt.withZone(databaseZone);
        st.setTimestamp(index, new Timestamp(dtForDb.getMillis()));
    }

    // Bridge methods for newer Hibernate APIs (do not use @Override to avoid mismatch if not present)
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        return nullSafeGet(rs, names, (SessionImplementor) session, owner);
    }
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws SQLException {
        nullSafeSet(st, value, index, (SessionImplementor) session);
    }

    @Override
    public Object deepCopy(Object value) { return value == null ? null : new DateTime(((DateTime) value).getMillis(), ((DateTime) value).getZone()); }

    @Override
    public boolean isMutable() { return true; }

    @Override
    public Serializable disassemble(Object value) throws HibernateException { return (Serializable) deepCopy(value); }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException { return deepCopy(cached); }

    @Override
    public Object replace(Object original, Object target, Object owner) { return deepCopy(original); }

    // Version related methods (for optimistic locking) - we just treat DateTime as increasing by millis
    // Legacy version methods
    public Object seed(SessionImplementor session) { return new DateTime(javaZone); }
    public Object next(Object current, SessionImplementor session) { return new DateTime(javaZone); }

    // Bridge
    public Object seed(SharedSessionContractImplementor session) { return seed((SessionImplementor) session); }
    public Object next(Object current, SharedSessionContractImplementor session) { return next(current, (SessionImplementor) session); }

    @Override
    public int compare(Object x, Object y) { return ((DateTime) x).compareTo((DateTime) y); }
}
