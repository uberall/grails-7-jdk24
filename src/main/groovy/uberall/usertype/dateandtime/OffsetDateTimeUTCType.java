package uberall.usertype.dateandtime;

import org.hibernate.type.OffsetDateTimeType;

/**
 * @author Jan Didschuneit
 * custom {@link OffsetDateTimeType} class to replicate the former joda date time behaviour by
 * writing values as UTC into the DB and converting them back to systemDefault zone when reading them
 */
public class OffsetDateTimeUTCType extends OffsetDateTimeType {

    public OffsetDateTimeUTCType() {
        setJavaTypeDescriptor(OffsetDateTimeUTCJavaDescriptor.INSTANCE);
    }
}
