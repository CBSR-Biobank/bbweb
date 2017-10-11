/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import moment from 'moment';

describe('timeService', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('timeService');
    });
  });

  describe('dateAndTimeToUtcString', function() {

    it('converts a date and time to a string', function() {
      var date = new Date(Date.parse('Jan 1, 2000')),
          time = new Date(Date.parse('Wed, 31 Dec 1980 07:11:00 GMT')),
          datestr = this.timeService.dateAndTimeToUtcString(date, time);
      expect(datestr).toContain('2000-01-01');
      expect(datestr).toContain('T07:11:00Z');
    });

    it('calling with undefined parameters throws an error', function() {
      var self = this;
      expect(function () {
        self.timeService.dateAndTimeToUtcString(undefined, undefined);
      }).toThrowError('date or time is invalid');
    });

    it('calling with undefined time returns date as UTC string', function() {
      var date = new Date(Date.parse('Wed, 31 Dec 1980 07:11:00 GMT')),
          datestr = this.timeService.dateAndTimeToUtcString(date);
      expect(datestr).toContain('1980-12-31');
      expect(datestr).toContain('T07:11:00Z');
    });

  });

  describe('dateToDisplayString', function() {

    it('converts a date to local time with seconds and milliseconds set to zero', function() {
      var utcDate = new Date(Date.UTC(96, 11, 1, 0, 0, 0)),
          datestr = this.timeService.dateToDisplayString(utcDate);
      expect(datestr).not.toBeEmptyString();
      expect(datestr).not.toContain('T');
      expect(datestr).not.toContain('Z');
      expect(moment(utcDate).utc().format()).not.toEqual(datestr);
    });

    it('returns empty string if date is undefined', function() {
      expect(this.timeService.dateToDisplayString(undefined)).toBe('');
    });


  });

});
