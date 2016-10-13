/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks  = require('angularMocks'),
      _      = require('lodash'),
      moment = require('moment');

  function SuiteMixinFactory(TestSuiteMixin, ServerReplyMixin) {

    function SuiteMixin() {
      TestSuiteMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    return SuiteMixin;
  }

  fdescribe('timeService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('timeService');
    }));

    describe('dateAndTimeToUtcString', function() {

      fit('converts a date and time to a string', function() {
        var date = new Date(Date.parse('Jan 1, 2000')),
            time = new Date(Date.parse('Wed, 31 Dec 1980 07:11:00 GMT')),
            datestr = this.timeService.dateAndTimeToUtcString(date, time);
        expect(datestr).toContain('2000-01-01');
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

});
