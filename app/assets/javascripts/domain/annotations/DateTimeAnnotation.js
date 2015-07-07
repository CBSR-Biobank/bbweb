/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['moment', 'underscore'], function(moment, _) {
  'use strict';

  DateTimeAnnotationFactory.$inject = ['Annotation', 'bbwebConfig'];

  function DateTimeAnnotationFactory(Annotation, bbwebConfig) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function DateTimeAnnotation(obj, annotationType, required) {
      var self = this,
          date,
          defaults = {
            annotationTypeId     : null
          };

      obj = obj || {};
      _.extend(self, defaults, _.pick(obj, _.keys(defaults)));
      Annotation.call(this, annotationType, required);

      if (obj.stringValue && (obj.stringValue !== '')) {
        date = moment(obj.stringValue, bbwebConfig.dateTimeFormat).toDate();
        this.date = date;
        this.time = date;
      } else {
        this.date = null;
        this.time = null;
      }
    }

    DateTimeAnnotation.prototype = Object.create(Annotation.prototype);

    /**
     * Must return a string.
     */
    DateTimeAnnotation.prototype.getValue = function () {
      var datetime;

      if ((this.date === null) || (this.time === null)) {
        return '';
      } else {
        if (this.time instanceof Date) {
          this.time = moment(this.time);
        }
        datetime = moment(this.date).set({
          'millisecond': 0,
          'second':      0,
          'minute':      this.time.minutes(),
          'hour':        this.time.hours()
        });
        return datetime.local().format(bbwebConfig.dateTimeFormat);
      }
    };

    DateTimeAnnotation.prototype.getServerAnnotation = function () {
      var datetime, stringValue;

      // date part is kept in this.date and time in this.time,
      //
      // they must be combined
      if (this.date && this.time) {
        if (this.time instanceof Date) {
          this.time = moment(this.time);
        }
        datetime = moment(this.date).set({
          'millisecond': 0,
          'second':      0,
          'minute':      this.time.minutes(),
          'hour':        this.time.hours()
        });
        stringValue = datetime.local().format(bbwebConfig.dateTimeFormat);
      } else {
        stringValue = '';
      }

      return {
        annotationTypeId: this.getAnnotationTypeId(),
        stringValue:      stringValue,
        selectedValues:   []
      };
    };

    return DateTimeAnnotation;
  }

  return DateTimeAnnotationFactory;
});

