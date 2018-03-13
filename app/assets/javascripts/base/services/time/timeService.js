/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import moment from 'moment'

/**
 * An AngularJS service that converts dates to strings.
 *
 * Dates are converted to local time and UTC strings.
 *
 * @memberof base.services
 *
 * @param {AngularJS_Provider} AppConfig - This application's configuration object.
 */
class TimeService {

  constructor(AppConfig) {
    'ngInject';
    Object.assign(this, { AppConfig });
  }

  /**
   * Converts date to local time, with seconds and milliseconds set to zero, and returns it as
   * a moment.
   *
   * @private
   *
   * @param {Date} date - the date to convert to local time.
   *
   * @return {Moment} The date as local time in a string.
   *
   * @throws An error if date is undefined.
   */
  dateToLocal(date) {
    var datetime;

    if (!date) {
      throw new Error('date is invalid');
    }

    datetime = moment(date).set({
      'millisecond': 0,
      'second':      0
    });
    return datetime.local();
  }

  /**
   * Converts the date part of <code>date</code> and the time part in <code>time</code> to
   * local time in a string.
   *
   * @private
   *
   * @param {Date} date - the date part comes from this parameter. If <code>time</code> is used, the time
   * values contained in this parameter are ignored.
   *
   * @param {date} [time] - the time part comes from this parameter. The date values contained in this
   * paramerter are ignored. If this parameter is omitted, then the time values in the <code>date</code>
   * parameter are used.
   *
   * @return {Moment} The combined date and time as local time.
   *
   * @throws {Error} An error if date or time are undefined.
   */
  dateAndTimeToLocal(date, time) {
    var datetime, momentTime;

    if (!date || !time) {
      throw new Error('date or time is invalid');
    }

    if (time instanceof Date) {
      momentTime = moment(time);
    }
    datetime = moment(date).set({
      'millisecond': 0,
      'second':      0,
      'minute':      momentTime.minutes(),
      'hour':        momentTime.hours()
    });
    return datetime.local();
  }

  /**
   * Converts the date part of <code>date</code> and the time part in `time` to a UTC time in a
   * string.
   *
   * @param {Date} date - the date part comes from this parameter. If `time` is used, the time values
   * contained in this parameter are ignored.
   *
   * @param {Date} [time] - the time part comes from this parameter. The date values contained in this
   * paramerter are ignored. If this parameter is omitted, then the time values in the `date` parameter are
   * used.
   *
   * @return {string} a string representation of the date and time, the date part comes from `date` and the
   * time part comes from `time`.
   *
   * @throws {Error} An error if date and time are undefined.
   */
  dateAndTimeToUtcString(date, time) {
    if (!date && !time) {
      throw new Error('date or time is invalid');
    }
    if (!time) {
      return this.dateToLocal(date).utc().format();
    }
    return this.dateAndTimeToLocal(date, time).utc().format();
  }

  /**
   * Converts the date to a string that can be displayed to the user.
   *
   * @param {Date} [date] - the date to be converted to a string. If it is undefined, then a blank string is
   * returned.
   *
   * @return {string} The date as a string, or if date is undefined a blank string.
   */
  dateToDisplayString(date) {
    if (!date) {
      return '';
    }
    return this.dateToLocal(date).format(this.AppConfig.dateTimeFormat);
  }

}

export default ngModule => ngModule.service('timeService', TimeService)
