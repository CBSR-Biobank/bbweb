/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['moment'], function(moment) {
  'use strict';

  timeService.$inject = ['bbwebConfig'];

  /**
   * @class common.timeService
   * @memberof common
   *
   * @description An Anuglar service that converts dates to strings. Dates are converted to local time and UTC
   * strings.
   *
   */
  function timeService(bbwebConfig) {
    var service = {
      dateToLocal:                dateToLocal,
      dateToUtcString:            dateToUtcString,
      dateAndTimeToUtcString:     dateAndTimeToUtcString,
      dateToDisplayString:        dateToDisplayString
    };
    return service;

    //-------

    /**
     * @function common.timeService.dateToLocal
     *
     * @description Converts date to local time and returns it as a string.
     *
     * @param {Date} date - the date to convert to local time.
     *
     * @return {String} The date as local time in a string.
     *
     * @throws An error if date is undefined.
     */
    function dateToLocal(date) {
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
     * @function common.timeService.dateTimeToLocal
     *
     * @description Converts the date part of <code>date</code> and the time part in <code>time</code> to
     * local time in a string.
     *
     * @param {Date} date - the date part comes from this parameter. If <code>time</code> is used, the time
     * values contained in this parameter are ignored.
     *
     * @param {date} [time] - the time part comes from this parameter. The date values contained in this
     * paramerter are ignored. If this parameter is omitted, then the time values in the <code>date</code>
     * parameter are used.
     *
     * @return {String} The combined date and time as local time in a string.
     *
     * @throws An error if date or time are undefined.
     */
    function dateAndTimeToLocal(date, time) {
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
     * @function common.timeService.dateToUtcString
     *
     * @description Converts the date to a UTC time in a string.
     *
     * @param {Date} date - the date to be converted.
     *
     * @return {String} The date as UTC time in a string, or if the date is undefined an empty string.
     *
     * @throws An error if date is undefined.
     */
    function dateToUtcString(date) {
      if (!date) {
        return '';
      }
      return dateToLocal(date).format();
    }

    /**
     * @function common.timeService.dateAndTimeToUtcString
     *
     * @description Converts the date part of <code>date</code> and the time part in <code>time</code> to
     * a UTC time in a string.
     *
     * @param {Date} date - the date part comes from this parameter. If <code>time</code> is used, the time
     * values contained in this parameter are ignored.
     *
     * @param {date} [time] - the time part comes from this parameter. The date values contained in this
     * paramerter are ignored. If this parameter is omitted, then the time values in the <code>date</code>
     * parameter are used.
     *
     * @return {String} a string representation of the date and time, the date part comes from 'date' and the
     * time part comes from 'time'.
     *
     * @throws An error if date and time are undefined.
     */
    function dateAndTimeToUtcString(date, time) {
      if (!date && !time) {
        throw new Error('date or time is invalid');
      }
      if (!time) {
        return dateToLocal(date).format();
      }
      return dateAndTimeToLocal(date, time).format();
    }

    /**
     * @function common.timeService.dateToDisplayString
     *
     * @description Converts the date to a string that can be displayed to the user.
     *
     * @param {Date} [date] - the date to be converted to a string. If it is undefined, then a blank string is
     * returned.
     *
     * @return The date as a string, or if date is undefined a blank string.
     */
    function dateToDisplayString(date) {
      if (!date) {
        return '';
      }
      return dateToLocal(date).format(bbwebConfig.dateTimeFormat);
    }

  }

  return timeService;
});
