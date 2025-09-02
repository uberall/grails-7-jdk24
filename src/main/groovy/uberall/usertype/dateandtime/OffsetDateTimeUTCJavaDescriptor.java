package uberall.usertype.dateandtime;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.OffsetDateTimeJavaDescriptor;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * @author Jan Didschuneit
 * custom {@link OffsetDateTimeJavaDescriptor} class to replicate the former
 * joda date time behaviour by writing values as UTC into the DB and converting them back to systemDefault zone when
 * reading them
 */
public class OffsetDateTimeUTCJavaDescriptor extends OffsetDateTimeJavaDescriptor {
    public static final OffsetDateTimeUTCJavaDescriptor INSTANCE = new OffsetDateTimeUTCJavaDescriptor();

    @Override
    public <X> OffsetDateTime wrap(X value, WrapperOptions options) {
        OffsetDateTime dbDateTime = super.wrap(value, options);
        // the returned values are being 1 or 2 hours off since the datetime is stored as UTC in the DB, so we
        // "manipulate" the OffsetDateTime to be correct
        ZonedDateTime utcDateTime = dbDateTime.toLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime systemDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault());
        return systemDateTime.toOffsetDateTime();
    }

    @Override
    public <X> X unwrap(OffsetDateTime offsetDateTime, Class<X> type, WrapperOptions options) {
        return super.unwrap(
                // for the datetime being stored as UTC in the DB, we simply remove the offset hours from it
                offsetDateTime.minusSeconds(offsetDateTime.getOffset().getTotalSeconds()),
                type,
                options
        );
    }
}
