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
   * @function common.timeService.dateAndTimeToUtcString
   *
   * @description Converts the date part of `date` and the time part in `time` to
   * a UTC time in a string.
   *
   * @param {Date} date - the date part comes from this parameter. If `time` is used, the time
   * values contained in this parameter are ignored.
   *
   * @param {date} [time] - the time part comes from this parameter. The date values contained in this
   * paramerter are ignored. If this parameter is omitted, then the time values in the `date`
   * parameter are used.
   *
   * @return {String} a string representation of the date and time, the date part comes from 'date' and the
   * time part comes from 'time'.
   *
   * @throws An error if date and time are undefined.
   */
  dateAndTimeToUtcString(date, time) {
    if (!date && !time) {
      throw new Error('date or time is invalid');
    }
    if (!time) {
      return dateToLocal(date).utc().format();
    }
    return dateAndTimeToLocal(date, time).utc().format();
  }

  /**
   * @function common.timeService.dateToDisplayString
   *
   * @description Converts the date to a string that can be displayed to the user.
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
    if (typeof date === 'string') {
      date = new Date(date);
    }
    return dateToLocal(date).format(this.AppConfig.dateTimeFormat);
  }

}

/*
 * Converts date to local time, with seconds and milliseconds set to zero, and returns it as
 * a moment.
 *
 * @param {Date} date - the date to convert to local time.
 *
 * @return {Moment} The date as local time in a string.
 *
 * @throws An error if date is undefined.
 */
function dateToLocal(date) {
  if (!date) {
    throw new Error('date is invalid');
  }

  if (! (date instanceof Date) ) {
    throw new Error('date is not a real date');
  }

  date.setUTCSeconds(0);
  date.setUTCMilliseconds(0);
  return moment(date).local();
}

/*
 * Converts the date part of `date` and the time part in `time` to
 * local time in a string.
 *
 * @param {Date} date - the date part comes from this parameter. If `time` is used, the time
 * values contained in this parameter are ignored.
 *
 * @param {date} [time] - the time part comes from this parameter. The date values contained in this
 * paramerter are ignored. If this parameter is omitted, then the time values in the `date`
 * parameter are used.
 *
 * @return {Moment} The combined date and time as local time.
 *
 * @throws An error if date or time are undefined.
 */
function dateAndTimeToLocal(date, time) {
  if (!date || !time) {
    throw new Error('date or time is invalid');
  }

  date.setUTCHours(time.getUTCHours());
  date.setUTCMinutes(time.getUTCMinutes());
  date.setUTCSeconds(0);
  date.setUTCMilliseconds(0);
  return moment(date).local();
}

export default ngModule => ngModule.service('timeService', TimeService)
