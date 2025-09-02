/*
 *  Copyright 2010, 2011 Christopher Pheby
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uberall.usertype.dateandtime;

import org.hibernate.usertype.ParameterizedType;
import org.jadira.usertype.dateandtime.joda.columnmapper.TimestampColumnDateTimeMapper;
import org.jadira.usertype.spi.shared.AbstractParameterizedTemporalUserType;
import org.jadira.usertype.spi.shared.IntegratorConfiguredType;
import org.joda.time.DateTime;

import java.io.Serial;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Properties;

/**
 * @author Jan Didschuneit
 * Adjusted class of {@link org.jadira.usertype.dateandtime.joda.PersistentDateTime} to replicate the behaviour of the old
 * class which transformed the joda datetime into UTC for DB when persisting and back to systemDefault when reading
 */
public class PersistentDateTimeUTC extends AbstractParameterizedTemporalUserType<DateTime, Timestamp, TimestampColumnDateTimeMapper> implements ParameterizedType, IntegratorConfiguredType {

    @Serial
    private static final long serialVersionUID = -6656619988954550389L;

    @Override
    public void setParameterValues(Properties parameters) {
        parameters.setProperty("javaZone", ZoneId.systemDefault().getId());
        // let's replicate the Grails 3 monolith behaviour in which joda datetime was transformed to UTC
        parameters.setProperty("databaseZone", "UTC");
        super.setParameterValues(parameters);
    }
}
