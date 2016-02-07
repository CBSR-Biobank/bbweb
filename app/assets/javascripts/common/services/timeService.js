/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['moment'], function(moment) {
  'use strict';

  timeService.$inject = ['bbwebConfig'];

  /**
   *
   */
  function timeService(bbwebConfig) {
    var service = {
      stringToDateAndTime: stringToDateAndTime,
      dateAndTimeToUtcString: dateAndTimeToUtcString,
      dateAndTimeToDisplayString: dateAndTimeToDisplayString,
      timeToDisplayString: timeToDisplayString
    };
    return service;

    //-------

    function stringToDateAndTime(str) {
      var date;
      if (str && (str !== '')) {
        date = moment(str, bbwebConfig.dateTimeFormat).toDate();
        return { date: date, time: date };
      }
      return { date: null, time: null };
    }

    function dateAndTimeToLocal(date, time) {
      var datetime, momentTime;

      if (!date || !time) {
        throw new Error('date or time is invalid');
      } else {
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
    }

    /**
     * @return a string representation of the date and time, the date part comes from 'date' and the time part
     * comes from 'time'.
     */
    function dateAndTimeToUtcString(date, time) {
      if (!date || !time) {
        return '';
      }
      return dateAndTimeToLocal(date, time).format();
    }

    function dateAndTimeToDisplayString(date, time) {
      if (!date || !time) {
        return '';
      }
      return dateAndTimeToLocal(date, time).format(bbwebConfig.dateTimeFormat);
    }

    /**
     * Converts the string contained in time to a local time.
     *
     * The time string is a time in UTC format.
     */
    function timeToDisplayString(time) {
      return moment(time).local().format(bbwebConfig.dateTimeFormat);
    }

  }

  return timeService;
});
